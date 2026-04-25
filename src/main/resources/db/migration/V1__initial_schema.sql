CREATE TABLE bank_stocks (
                             name     VARCHAR(128) PRIMARY KEY,
                             quantity BIGINT NOT NULL CHECK (quantity >= 0)
);

CREATE TABLE wallet_stocks (
                               wallet_id  VARCHAR(128) NOT NULL,
                               stock_name VARCHAR(128) NOT NULL,
                               quantity   BIGINT NOT NULL CHECK (quantity >= 0),
                               PRIMARY KEY (wallet_id, stock_name)
);

CREATE INDEX idx_wallet_stocks_wallet ON wallet_stocks (wallet_id);

CREATE TABLE audit_log (
                           id         BIGSERIAL PRIMARY KEY,
                           type       VARCHAR(8) NOT NULL CHECK (type IN ('buy', 'sell')),
                           wallet_id  VARCHAR(128) NOT NULL,
                           stock_name VARCHAR(128) NOT NULL,
                           created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_audit_log_created_at ON audit_log (created_at);