CREATE TABLE users
(
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    email         VARCHAR(100) NOT NULL UNIQUE,
    password      VARCHAR(100) NOT NULL,
    user_role     VARCHAR(20)  NOT NULL,
    last_login_at TIMESTAMP    NULL,

    created_at    TIMESTAMP    NOT NULL,
    updated_at    TIMESTAMP    NOT NULL,
    deleted_at    TIMESTAMP NULL
);

CREATE TABLE user_sessions
(
    user_id       BIGINT        NOT NULL PRIMARY KEY,
    refresh_token VARCHAR(1024) NOT NULL,
    updated_at    TIMESTAMP     NOT NULL
);