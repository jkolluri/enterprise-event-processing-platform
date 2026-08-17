CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS ai_knowledge_document (
    id UUID PRIMARY KEY,
    source_type VARCHAR(80) NOT NULL,
    source_ref VARCHAR(200) NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    embedding_model VARCHAR(120),
    embedding vector(1536),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_ai_knowledge_source UNIQUE (source_type, source_ref)
);

CREATE INDEX IF NOT EXISTS idx_ai_knowledge_source_type ON ai_knowledge_document(source_type);
CREATE INDEX IF NOT EXISTS idx_ai_knowledge_embedding_hnsw
    ON ai_knowledge_document USING hnsw (embedding vector_cosine_ops)
    WHERE embedding IS NOT NULL;
