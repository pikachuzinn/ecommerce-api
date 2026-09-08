-- =====================================================================
-- V1: schema inicial do e-commerce
-- =====================================================================

CREATE TABLE users (
    id            BIGSERIAL PRIMARY KEY,
    name          VARCHAR(120) NOT NULL,
    email         VARCHAR(180) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    CONSTRAINT uk_users_email UNIQUE (email)
);

CREATE TABLE user_roles (
    user_id BIGINT      NOT NULL,
    role    VARCHAR(40) NOT NULL,
    CONSTRAINT pk_user_roles PRIMARY KEY (user_id, role),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE
);

CREATE TABLE categories (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL,
    slug VARCHAR(90) NOT NULL,
    CONSTRAINT uk_categories_name UNIQUE (name),
    CONSTRAINT uk_categories_slug UNIQUE (slug)
);

CREATE TABLE products (
    id             BIGSERIAL PRIMARY KEY,
    sku            VARCHAR(40)    NOT NULL,
    name           VARCHAR(160)   NOT NULL,
    description    VARCHAR(2000),
    price          NUMERIC(12, 2) NOT NULL,
    stock_quantity INTEGER        NOT NULL,
    active         BOOLEAN        NOT NULL DEFAULT TRUE,
    category_id    BIGINT         NOT NULL,
    version        BIGINT         NOT NULL DEFAULT 0,
    created_at     TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT fk_products_category FOREIGN KEY (category_id) REFERENCES categories (id),
    CONSTRAINT ck_products_price_positive CHECK (price > 0),
    CONSTRAINT ck_products_stock_non_negative CHECK (stock_quantity >= 0)
);

CREATE INDEX idx_products_category ON products (category_id);
CREATE INDEX idx_products_name_lower ON products (LOWER(name));

CREATE TABLE orders (
    id                  BIGSERIAL PRIMARY KEY,
    code                VARCHAR(20)    NOT NULL,
    user_id             BIGINT         NOT NULL,
    status              VARCHAR(30)    NOT NULL,
    shipping_street     VARCHAR(160),
    shipping_number     VARCHAR(20),
    shipping_complement VARCHAR(80),
    shipping_district   VARCHAR(80),
    shipping_city       VARCHAR(80),
    shipping_state      VARCHAR(2),
    shipping_zip_code   VARCHAR(9),
    shipping_fee        NUMERIC(12, 2) NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    updated_at          TIMESTAMPTZ    NOT NULL DEFAULT now(),
    CONSTRAINT uk_orders_code UNIQUE (code),
    CONSTRAINT fk_orders_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT ck_orders_shipping_fee CHECK (shipping_fee >= 0)
);

-- Consultas mais frequentes: "meus pedidos" e o painel do admin por status.
CREATE INDEX idx_orders_user_created ON orders (user_id, created_at DESC);
CREATE INDEX idx_orders_status ON orders (status);

CREATE TABLE order_items (
    id           BIGSERIAL PRIMARY KEY,
    order_id     BIGINT         NOT NULL,
    product_id   BIGINT         NOT NULL,
    product_name VARCHAR(160)   NOT NULL,
    product_sku  VARCHAR(40)    NOT NULL,
    quantity     INTEGER        NOT NULL,
    unit_price   NUMERIC(12, 2) NOT NULL,
    CONSTRAINT fk_order_items_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT fk_order_items_product FOREIGN KEY (product_id) REFERENCES products (id),
    CONSTRAINT ck_order_items_quantity CHECK (quantity > 0),
    CONSTRAINT ck_order_items_unit_price CHECK (unit_price > 0)
);

CREATE INDEX idx_order_items_order ON order_items (order_id);
CREATE INDEX idx_order_items_product ON order_items (product_id);

-- Heranca SINGLE_TABLE: uma tabela para os tres meios de pagamento,
-- discriminados por payment_type.
CREATE TABLE payments (
    id               BIGSERIAL PRIMARY KEY,
    payment_type     VARCHAR(20) NOT NULL,
    order_id         BIGINT      NOT NULL,
    paid_at          TIMESTAMPTZ NOT NULL DEFAULT now(),
    card_brand       VARCHAR(30),
    card_last4       VARCHAR(4),
    installments     INTEGER,
    pix_txid         VARCHAR(40),
    boleto_barcode   VARCHAR(60),
    boleto_due_date  DATE,
    CONSTRAINT uk_payments_order UNIQUE (order_id),
    CONSTRAINT fk_payments_order FOREIGN KEY (order_id) REFERENCES orders (id) ON DELETE CASCADE,
    CONSTRAINT ck_payments_type CHECK (payment_type IN ('CREDIT_CARD', 'PIX', 'BOLETO'))
);
