--liquibase formatted sql
--changeset artur:6

CREATE INDEX IF NOT EXISTS idx_chats_id ON chats(id);
CREATE INDEX IF NOT EXISTS idx_links_url ON links(url);
CREATE INDEX IF NOT EXISTS idx_links_last_check_time ON links(last_check_time);
CREATE INDEX IF NOT EXISTS idx_subscriptions_chat_id ON subscriptions(chat_id);
CREATE INDEX IF NOT EXISTS idx_subscriptions_link_id ON subscriptions(link_id);
CREATE INDEX IF NOT EXISTS idx_tags_name ON tags(name);
CREATE INDEX IF NOT EXISTS idx_link_tags_link_id ON link_tags(link_id);
CREATE INDEX IF NOT EXISTS idx_link_tags_tag_id ON link_tags(tag_id);

--rollback DROP INDEX IF EXISTS idx_chats_id, idx_links_url, idx_links_last_check_time, idx_subscriptions_chat_id, idx_subscriptions_link_id, idx_tags_name, idx_link_tags_link_id, idx_link_tags_tag_id;
