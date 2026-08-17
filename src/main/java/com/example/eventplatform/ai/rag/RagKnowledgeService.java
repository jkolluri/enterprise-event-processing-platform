package com.example.eventplatform.ai.rag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class RagKnowledgeService {
    private final JdbcTemplate jdbcTemplate;
    private final OpenAiEmbeddingService embeddings;
    private final boolean enabled;
    private final int maxContentChars;

    public RagKnowledgeService(JdbcTemplate jdbcTemplate,
                               OpenAiEmbeddingService embeddings,
                               @Value("${app.ai.rag.enabled:true}") boolean enabled,
                               @Value("${app.ai.rag.max-content-chars:20000}") int maxContentChars) {
        this.jdbcTemplate = jdbcTemplate;
        this.embeddings = embeddings;
        this.enabled = enabled;
        this.maxContentChars = Math.max(1000, maxContentChars);
    }

    public KnowledgeDocument upsert(KnowledgeUpsertRequest request) {
        String content = truncate(request.content());
        Optional<List<Double>> vector = embeddings.embed(request.title() + "\n" + content);
        UUID id = UUID.randomUUID();
        String vectorLiteral = vector.map(this::toVectorLiteral).orElse(null);
        jdbcTemplate.update("""
                INSERT INTO ai_knowledge_document
                    (id, source_type, source_ref, title, content, embedding_model, embedding, created_at, updated_at)
                VALUES (?, ?, ?, ?, ?, ?, CAST(? AS vector), now(), now())
                ON CONFLICT (source_type, source_ref)
                DO UPDATE SET title = EXCLUDED.title,
                              content = EXCLUDED.content,
                              embedding_model = EXCLUDED.embedding_model,
                              embedding = EXCLUDED.embedding,
                              updated_at = now()
                """,
                id, request.sourceType(), request.sourceRef(), request.title(), content,
                vector.isPresent() ? embeddings.model() : null, vectorLiteral);
        return findBySource(request.sourceType(), request.sourceRef())
                .orElseThrow(() -> new IllegalStateException("Knowledge document was not persisted"));
    }

    public List<KnowledgeDocument> search(String query, int requestedLimit) {
        if (!enabled || query == null || query.isBlank()) return List.of();
        int limit = Math.max(1, Math.min(requestedLimit, 10));
        Optional<List<Double>> vector = embeddings.embed(truncate(query));
        if (vector.isPresent()) {
            List<KnowledgeDocument> vectorMatches = jdbcTemplate.query("""
                    SELECT id, source_type, source_ref, title, content,
                           1 - (embedding <=> CAST(? AS vector)) AS similarity,
                           created_at
                    FROM ai_knowledge_document
                    WHERE embedding IS NOT NULL
                    ORDER BY embedding <=> CAST(? AS vector)
                    LIMIT ?
                    """, this::mapDocument, toVectorLiteral(vector.get()), toVectorLiteral(vector.get()), limit);
            if (!vectorMatches.isEmpty()) return vectorMatches;
        }
        return keywordFallback(query, limit);
    }

    public List<String> retrieveContext(String query, int limit) {
        List<String> result = new ArrayList<>();
        for (KnowledgeDocument doc : search(query, limit)) {
            result.add("[" + doc.sourceType() + ":" + doc.sourceRef() + "] " + doc.title() + "\n" + truncate(doc.content()));
        }
        return result;
    }

    public int reindexMissingEmbeddings() {
        List<KnowledgeDocument> docs = jdbcTemplate.query("""
                SELECT id, source_type, source_ref, title, content, NULL::double precision AS similarity, created_at
                FROM ai_knowledge_document WHERE embedding IS NULL ORDER BY created_at
                """, this::mapDocument);
        int updated = 0;
        for (KnowledgeDocument doc : docs) {
            Optional<List<Double>> vector = embeddings.embed(doc.title() + "\n" + doc.content());
            if (vector.isPresent()) {
                updated += jdbcTemplate.update("""
                        UPDATE ai_knowledge_document
                        SET embedding_model = ?, embedding = CAST(? AS vector), updated_at = now()
                        WHERE id = ?
                        """, embeddings.model(), toVectorLiteral(vector.get()), doc.id());
            }
        }
        return updated;
    }

    public List<KnowledgeDocument> list(int requestedLimit) {
        int limit = Math.max(1, Math.min(requestedLimit, 100));
        return jdbcTemplate.query("""
                SELECT id, source_type, source_ref, title, content, NULL::double precision AS similarity, created_at
                FROM ai_knowledge_document ORDER BY updated_at DESC LIMIT ?
                """, this::mapDocument, limit);
    }

    private Optional<KnowledgeDocument> findBySource(String sourceType, String sourceRef) {
        List<KnowledgeDocument> docs = jdbcTemplate.query("""
                SELECT id, source_type, source_ref, title, content, NULL::double precision AS similarity, created_at
                FROM ai_knowledge_document WHERE source_type = ? AND source_ref = ?
                """, this::mapDocument, sourceType, sourceRef);
        return docs.stream().findFirst();
    }

    private List<KnowledgeDocument> keywordFallback(String query, int limit) {
        String[] terms = query.toLowerCase().replaceAll("[^a-z0-9 ]", " ").split("\\s+");
        String term = "";
        for (String candidate : terms) {
            if (candidate.length() >= 4) { term = candidate; break; }
        }
        if (term.isBlank()) return List.of();
        return jdbcTemplate.query("""
                SELECT id, source_type, source_ref, title, content, 0.25::double precision AS similarity, created_at
                FROM ai_knowledge_document
                WHERE lower(title) LIKE ? OR lower(content) LIKE ?
                ORDER BY updated_at DESC LIMIT ?
                """, this::mapDocument, "%" + term + "%", "%" + term + "%", limit);
    }

    private KnowledgeDocument mapDocument(java.sql.ResultSet rs, int rowNum) throws java.sql.SQLException {
        Timestamp created = rs.getTimestamp("created_at");
        Double similarity = rs.getObject("similarity") == null ? null : rs.getDouble("similarity");
        return new KnowledgeDocument(
                rs.getObject("id", UUID.class),
                rs.getString("source_type"),
                rs.getString("source_ref"),
                rs.getString("title"),
                rs.getString("content"),
                similarity,
                created == null ? Instant.EPOCH : created.toInstant()
        );
    }

    private String toVectorLiteral(List<Double> vector) {
        return vector.toString().replace(" ", "");
    }

    private String truncate(String value) {
        if (value == null) return "";
        return value.length() <= maxContentChars ? value : value.substring(0, maxContentChars) + "...[TRUNCATED]";
    }
}
