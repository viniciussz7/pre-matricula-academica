CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(255) NOT NULL UNIQUE,
    token_type VARCHAR(50) NOT NULL,
    expires_at TIMESTAMP NOT NULL,
    used BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL,
    user_id UUID NOT NULL,

    CONSTRAINT fk_verification_token_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)

);