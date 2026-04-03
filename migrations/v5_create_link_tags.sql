--liquibase formatted sql
--changeset artur:5

CREATE TABLE IF NOT EXISTS link_tags (
    link_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,
    PRIMARY KEY (link_id, tag_id),
    FOREIGN KEY (link_id) REFERENCES links(id) ON DELETE CASCADE,
    FOREIGN KEY (tag_id) REFERENCES tags(id) ON DELETE CASCADE
);

--rollback DROP TABLE link_tags;
