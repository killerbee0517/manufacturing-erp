INSERT INTO roles (name, created_at, created_by, updated_at, updated_by)
SELECT 'HEAD', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'HEAD');

INSERT INTO roles (name, created_at, created_by, updated_at, updated_by)
SELECT 'PRODUCTION', CURRENT_TIMESTAMP, 'seed', CURRENT_TIMESTAMP, 'seed'
WHERE NOT EXISTS (SELECT 1 FROM roles WHERE name = 'PRODUCTION');
