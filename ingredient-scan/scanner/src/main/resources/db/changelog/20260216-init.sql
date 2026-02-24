CREATE TABLE IF NOT EXISTS compositions (
    id TEXT PRIMARY KEY,
    text TEXT NOT NULL UNIQUE,
    create_date TIMESTAMP NOT NULL
    );

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_compositions_id_text_hash
    ON compositions (id, md5(text));

CREATE TABLE IF NOT EXISTS analysis (
    id TEXT PRIMARY KEY,
    composition_id TEXT NOT NULL UNIQUE REFERENCES compositions(id) ON DELETE CASCADE,
    create_date TIMESTAMP NOT NULL,
    description TEXT,
    rating DOUBLE PRECISION,
    color TEXT,
    components JSONB
    );

CREATE INDEX CONCURRENTLY IF NOT EXISTS idx_analysis_composition_id ON analysis(composition_id);
