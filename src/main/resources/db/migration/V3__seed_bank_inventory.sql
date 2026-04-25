INSERT INTO bank_stocks (name, quantity) VALUES
    ('AAPL', 100),
    ('MSFT', 100),
    ('GOOG', 100),
    ('AMZN', 100),
    ('NVDA', 100)
ON CONFLICT (name) DO NOTHING;
