CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE knowledge_base (
    id BIGSERIAL PRIMARY KEY,
    scenario TEXT NOT NULL,
    user_queries JSONB NOT NULL,
    solution TEXT NOT NULL,
    tags JSONB NOT NULL,
    content_hash VARCHAR(64) NOT NULL UNIQUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE knowledge_embeddings (
    id BIGSERIAL PRIMARY KEY,
    knowledge_id BIGINT NOT NULL UNIQUE REFERENCES knowledge_base(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    embedding VECTOR NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX knowledge_embeddings_embedding_hnsw_idx
    ON knowledge_embeddings USING hnsw (embedding vector_cosine_ops);

CREATE TABLE chat_sessions (
    id UUID PRIMARY KEY,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    session_id UUID NOT NULL REFERENCES chat_sessions(id) ON DELETE CASCADE,
    role VARCHAR(20) NOT NULL CHECK (role IN ('user', 'assistant')),
    content TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX chat_messages_session_created_idx ON chat_messages (session_id, created_at, id);