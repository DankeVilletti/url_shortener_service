DROP TABLE IF EXISTS registered_urls CASCADE;
DROP TABLE IF EXISTS free_urls CASCADE;
DROP TABLE IF EXISTS unique_incrementer_urls CASCADE;
DROP TABLE IF EXISTS archive_urls CASCADE;

CREATE TABLE registered_urls
(
    project_id BIGINT,
    creator_id BIGINT      NOT NULL,
    short_url  VARCHAR(16) NOT NULL,
    full_url   TEXT        NOT NULL,
    counter    BIGINT      NOT NULL DEFAULT 0,
    expires_at TIMESTAMPTZ            DEFAULT 'infinity',
    created_at timestamptz          DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamptz          DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (short_url)
);

CREATE INDEX idx_expires_at ON registered_urls (expires_at);


CREATE TABLE free_urls
(
    short_url VARCHAR(16) NOT NULL,
    PRIMARY KEY (short_url)
);

CREATE TABLE unique_incrementer_urls
(
    counter          BIGINT PRIMARY KEY,
    generation_state BOOLEAN      DEFAULT FALSE,
    thread_id        VARCHAR(128) DEFAULT NULL,
    version          BIGINT       DEFAULT 0
);

insert into unique_incrementer_urls (counter)
values (5680023560);

CREATE TABLE archive_urls
(
    project_id             BIGINT,
    creator_id             BIGINT      NOT NULL,
    short_url              VARCHAR(16) NOT NULL,
    full_url               TEXT        NOT NULL,
    counter                BIGINT      NOT NULL DEFAULT 0,
    archived_at            TIMESTAMPTZ          DEFAULT CURRENT_TIMESTAMP,
    reuse_short_url_status BOOLEAN              DEFAULT TRUE,
    PRIMARY KEY (short_url, archived_at)
);

CREATE INDEX idx_archived_at ON archive_urls (archived_at, reuse_short_url_status);


