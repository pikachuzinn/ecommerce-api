-- =====================================================================
-- V2: dados de partida para o ambiente de desenvolvimento e demonstracao
--
-- Contas criadas (senha em texto plano apenas para uso local):
--   admin@ecommerce.dev  / Admin@123  -> ROLE_ADMIN + ROLE_CUSTOMER
--   cliente@ecommerce.dev / Admin@123 -> ROLE_CUSTOMER
-- Trocar antes de qualquer ambiente exposto.
-- =====================================================================

INSERT INTO users (name, email, password_hash) VALUES
    ('Administrador', 'admin@ecommerce.dev',   '$2a$10$V8lIKXdEWf9pSKrnAUTUlurZQkJKBKvgr10NX91yUrsp.LxOAst8S'),
    ('Cliente Demo',  'cliente@ecommerce.dev', '$2a$10$V8lIKXdEWf9pSKrnAUTUlurZQkJKBKvgr10NX91yUrsp.LxOAst8S');

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_ADMIN' FROM users WHERE email = 'admin@ecommerce.dev';

INSERT INTO user_roles (user_id, role)
SELECT id, 'ROLE_CUSTOMER' FROM users WHERE email IN ('admin@ecommerce.dev', 'cliente@ecommerce.dev');

INSERT INTO categories (name, slug) VALUES
    ('Eletronicos',   'eletronicos'),
    ('Perifericos',   'perifericos'),
    ('Livros',        'livros'),
    ('Games',         'games');

-- A ordem explicita (v.ord) torna os ids gerados deterministicos, o que permite
-- que os testes de integracao referenciem produtos por id sem depender de sorte.
INSERT INTO products (sku, name, description, price, stock_quantity, active, category_id)
SELECT v.sku, v.name, v.description, v.price, v.stock, TRUE, c.id
FROM (VALUES
    ( 1, 'ELE-001', 'Notebook Ultra 14"',     'Notebook 14 polegadas, 16GB RAM, SSD 512GB',     4899.00, 12, 'eletronicos'),
    ( 2, 'ELE-002', 'Monitor 27" QHD',        'Monitor IPS 27 polegadas, 2560x1440, 144Hz',     1799.90, 25, 'eletronicos'),
    ( 3, 'ELE-003', 'Smartphone Neo 5G',      'Tela 6.5 polegadas, 256GB, camera tripla',       2499.00, 40, 'eletronicos'),
    ( 4, 'PER-001', 'Teclado Mecanico ABNT2', 'Switch marrom, hot swap, layout brasileiro',      449.90, 60, 'perifericos'),
    ( 5, 'PER-002', 'Mouse Optico 16000 DPI', 'Sensor optico, 7 botoes programaveis',            229.90, 80, 'perifericos'),
    ( 6, 'PER-003', 'Headset com Microfone',  'Headset over-ear com cancelamento de ruido',      399.00, 35, 'perifericos'),
    ( 7, 'LIV-001', 'Clean Code',             'Robert C. Martin - manual de bom codigo',         139.90, 50, 'livros'),
    ( 8, 'LIV-002', 'Refatoracao',            'Martin Fowler - melhorando o projeto do codigo',  189.90, 30, 'livros'),
    ( 9, 'LIV-003', 'Padroes de Projeto',     'GoF - solucoes reutilizaveis de software',        219.90, 20, 'livros'),
    (10, 'GAM-001', 'Controle sem fio',       'Controle bluetooth compativel com PC e console',  349.00, 45, 'games'),
    (11, 'GAM-002', 'Cadeira Gamer Pro',      'Cadeira reclinavel com apoio lombar',            1299.00,  8, 'games'),
    (12, 'GAM-003', 'Volante com Pedais',     'Volante force feedback 900 graus',               1899.00,  5, 'games')
) AS v(ord, sku, name, description, price, stock, category_slug)
JOIN categories c ON c.slug = v.category_slug
ORDER BY v.ord;
