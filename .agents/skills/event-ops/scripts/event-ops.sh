#!/usr/bin/env bash
set -euo pipefail

BASE_URL="${EVENT_PLATFORM_URL:-http://localhost:8080}"
USER_NAME="${EVENT_PLATFORM_USER:-admin}"
PASSWORD="${EVENT_PLATFORM_PASSWORD:-admin123}"

usage() {
  cat <<'USAGE'
Usage:
  event-ops.sh failures [minutes]
  event-ops.sh event <event-uuid>
  event-ops.sh history <event-uuid>
  event-ops.sh analyze <event-uuid> [--force]
  event-ops.sh analyses <event-uuid>
  event-ops.sh incident [minutes]
  event-ops.sh ask "<question>" [minutes]
  event-ops.sh knowledge "<query>" [limit]

Environment:
  EVENT_PLATFORM_URL       default: http://localhost:8080
  EVENT_PLATFORM_USER      default: admin
  EVENT_PLATFORM_PASSWORD  default: admin123
USAGE
}

need_cmd() {
  command -v "$1" >/dev/null 2>&1 || { echo "Required command not found: $1" >&2; exit 2; }
}
need_cmd curl

json_get_token() {
  if command -v jq >/dev/null 2>&1; then
    jq -r '.token // .accessToken // empty'
  elif command -v python3 >/dev/null 2>&1; then
    python3 -c 'import json,sys; d=json.load(sys.stdin); print(d.get("token") or d.get("accessToken") or "")'
  else
    echo "Install jq or python3 to parse the login response." >&2
    exit 2
  fi
}

pretty() {
  if command -v jq >/dev/null 2>&1; then jq .; else cat; fi
}

login_payload=$(printf '{"username":"%s","password":"%s"}' "$USER_NAME" "$PASSWORD")
login_response=$(curl -fsS -H 'Content-Type: application/json' -d "$login_payload" "$BASE_URL/api/auth/login") || {
  echo "Unable to authenticate to $BASE_URL. Is the application running and are credentials correct?" >&2
  exit 1
}
TOKEN=$(printf '%s' "$login_response" | json_get_token)
if [[ -z "$TOKEN" ]]; then
  echo "Login succeeded but no token was found in the response." >&2
  exit 1
fi
AUTH=( -H "Authorization: Bearer $TOKEN" )

cmd="${1:-}"
case "$cmd" in
  failures)
    minutes="${2:-60}"
    curl -fsS "${AUTH[@]}" "$BASE_URL/api/ai/ops/failures?minutes=$minutes" | pretty
    ;;
  event)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    curl -fsS "${AUTH[@]}" "$BASE_URL/api/ai/ops/events/$2" | pretty
    ;;
  history)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    curl -fsS "${AUTH[@]}" "$BASE_URL/api/ai/ops/events/$2/history" | pretty
    ;;
  analyze)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    force=false
    [[ "${3:-}" == "--force" ]] && force=true
    curl -fsS -X POST "${AUTH[@]}" "$BASE_URL/api/ai/events/$2/analyze?force=$force" | pretty
    ;;
  analyses)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    curl -fsS "${AUTH[@]}" "$BASE_URL/api/ai/events/$2/analyses" | pretty
    ;;
  incident)
    minutes="${2:-60}"
    curl -fsS "${AUTH[@]}" "$BASE_URL/api/ai/incidents/summary?minutes=$minutes" | pretty
    ;;
  ask)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    question="$2"
    minutes="${3:-60}"
    if command -v python3 >/dev/null 2>&1; then
      payload=$(QUESTION="$question" MINUTES="$minutes" python3 - <<'PY'
import json, os
print(json.dumps({"question": os.environ["QUESTION"], "minutes": int(os.environ["MINUTES"])}))
PY
)
    else
      escaped=${question//\\/\\\\}; escaped=${escaped//\"/\\\"}
      payload=$(printf '{"question":"%s","minutes":%s}' "$escaped" "$minutes")
    fi
    curl -fsS -X POST "${AUTH[@]}" -H 'Content-Type: application/json' -d "$payload" "$BASE_URL/api/ai/ops/ask" | pretty
    ;;
  knowledge)
    [[ $# -ge 2 ]] || { usage; exit 2; }
    query="$2"
    limit="${3:-5}"
    curl -fsS -G "${AUTH[@]}" --data-urlencode "query=$query" --data-urlencode "limit=$limit" "$BASE_URL/api/ai/knowledge/search" | pretty
    ;;
  -h|--help|help|"")
    usage
    ;;
  *)
    echo "Unknown command: $cmd" >&2
    usage
    exit 2
    ;;
esac
