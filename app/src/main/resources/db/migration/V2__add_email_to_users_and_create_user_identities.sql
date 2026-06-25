ALTER TABLE users ADD COLUMN email VARCHAR(255);
ALTER TABLE users ADD COLUMN avatar_url VARCHAR(512);
ALTER TABLE users ADD CONSTRAINT uk_users_email UNIQUE (email);

CREATE TABLE user_identities (
    id               BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id          BIGINT       NOT NULL,
    provider         VARCHAR(50)  NOT NULL,
    provider_user_id VARCHAR(255) NOT NULL,
    email            VARCHAR(255) NOT NULL,
    name             VARCHAR(255),
    avatar_url       VARCHAR(512),
    created_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at       TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6),
    UNIQUE (provider, provider_user_id),
    FOREIGN KEY (user_id) REFERENCES users(id)
);
