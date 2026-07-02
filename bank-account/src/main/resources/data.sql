INSERT INTO users (username, created_at, updated_at, version)
VALUES ('demo_user', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

INSERT INTO accounts (user_id, currency_id, balance, created_at, updated_at, version)
VALUES
    (1, 1, 2500.0000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- EUR, id=1
    (1, 2, 1200.0000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- USD, id=2
    (1, 3, 8000.0000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0), -- SEK, id=3
    (1, 4,  650.0000, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0); -- GBP, id=4

INSERT INTO transactions (account_id, transaction_type_id, amount, currency_id, balance_after, description, created_at, updated_at, version)
VALUES
    (1, 1, 3000.0000, 1, 3000.0000, 'Initial deposit',          '2026-01-01 09:00:00', '2026-01-01 09:00:00', 0),
    (1, 2,  200.0000, 1, 2800.0000, 'Withdrawal for expenses',  '2026-01-15 11:30:00', '2026-01-15 11:30:00', 0),
    (1, 1,  500.0000, 1, 3300.0000, 'Salary deposit',           '2026-02-01 09:00:00', '2026-02-01 09:00:00', 0),
    (1, 2,  100.0000, 1, 3200.0000, 'Grocery shopping',         '2026-02-10 14:20:00', '2026-02-10 14:20:00', 0),
    (1, 4,  700.0000, 1, 2500.0000, 'Exchange to USD account',  '2026-03-05 10:00:00', '2026-03-05 10:00:00', 0);

INSERT INTO transactions (account_id, transaction_type_id, amount, currency_id, balance_after, description, created_at, updated_at, version)
VALUES
    (2, 1,  500.0000, 2,  500.0000, 'Initial deposit',          '2026-01-05 10:00:00', '2026-01-05 10:00:00', 0),
    (2, 1,  700.0000, 2, 1200.0000, 'Exchange from EUR account','2026-03-05 10:01:00', '2026-03-05 10:01:00', 0),
    (2, 2,  150.0000, 2, 1050.0000, 'Online purchase',          '2026-03-10 16:45:00', '2026-03-10 16:45:00', 0),
    (2, 2,  300.0000, 2,  750.0000, 'Hotel booking',            '2026-04-01 12:00:00', '2026-04-01 12:00:00', 0),
    (2, 1,  450.0000, 2, 1200.0000, 'Freelance payment',        '2026-04-15 09:30:00', '2026-04-15 09:30:00', 0);

INSERT INTO transactions (account_id, transaction_type_id, amount, currency_id, balance_after, description, created_at, updated_at, version)
VALUES
    (3, 1, 5000.0000, 3, 5000.0000, 'Initial deposit',          '2026-01-10 08:00:00', '2026-01-10 08:00:00', 0),
    (3, 1, 3000.0000, 3, 8000.0000, 'Salary deposit',           '2026-02-01 09:00:00', '2026-02-01 09:00:00', 0),
    (3, 2,  500.0000, 3, 7500.0000, 'Rent payment',             '2026-02-05 10:00:00', '2026-02-05 10:00:00', 0),
    (3, 2, 1200.0000, 3, 6300.0000, 'Car insurance',            '2026-03-01 11:00:00', '2026-03-01 11:00:00', 0),
    (3, 1, 1700.0000, 3, 8000.0000, 'Bonus payment',            '2026-03-15 14:00:00', '2026-03-15 14:00:00', 0);

INSERT INTO transactions (account_id, transaction_type_id, amount, currency_id, balance_after, description, created_at, updated_at, version)
VALUES
    (4, 1, 1000.0000, 4, 1000.0000, 'Initial deposit',          '2026-01-20 09:00:00', '2026-01-20 09:00:00', 0),
    (4, 2,  200.0000, 4,  800.0000, 'Subscription fee',         '2026-02-01 10:00:00', '2026-02-01 10:00:00', 0),
    (4, 1,  300.0000, 4, 1100.0000, 'Freelance payment',        '2026-02-20 15:00:00', '2026-02-20 15:00:00', 0),
    (4, 2,  250.0000, 4,  850.0000, 'Utility bills',            '2026-03-01 09:00:00', '2026-03-01 09:00:00', 0),
    (4, 2,  200.0000, 4,  650.0000, 'Online shopping',          '2026-04-10 13:30:00', '2026-04-10 13:30:00', 0);

INSERT INTO exchange_rates (from_currency, to_currency, rate, created_at, updated_at, version)
VALUES
    ('EUR', 'USD', 1.08, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('USD', 'EUR', 0.93, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('EUR', 'GBP', 0.85, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('GBP', 'EUR', 1.18, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('EUR', 'SEK', 11.30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('SEK', 'EUR', 0.09, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('EUR', 'VND', 27500.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('VND', 'EUR', 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('USD', 'GBP', 0.79, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('GBP', 'USD', 1.27, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('USD', 'SEK', 10.45, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('SEK', 'USD', 0.10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('GBP', 'SEK', 13.30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('SEK', 'GBP', 0.08, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('USD', 'VND', 25400.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('VND', 'USD', 0.00, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);