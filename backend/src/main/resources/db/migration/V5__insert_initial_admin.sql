-- Initial administrator
-- Email: admin@prematricula.uesb.br
-- Password: Admin@123

WITH new_admin AS (

    INSERT INTO users (
        id,
        full_name,
        email,
        password,
        first_access,
        role,
        created_at,
        updated_at,
        active
    )
    VALUES (
        gen_random_uuid(),
        'Administrador do Sistema',
        'admin@prematricula.uesb.br',
        '$2a$10$mWXPawON0QKxOIC05lkh9OlBH5ThS0/eAoNlwHtSndQwYu9POfgYe',
        FALSE,
        'ADMIN',
        CURRENT_TIMESTAMP,
        CURRENT_TIMESTAMP,
        TRUE
    )
    RETURNING id

)

INSERT INTO admins (
    id,
    user_id,
    created_at,
    updated_at,
    active
)
SELECT
    gen_random_uuid(),
    id,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    TRUE
FROM new_admin;