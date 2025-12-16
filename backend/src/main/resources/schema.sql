-- Chat Messages Table
CREATE TABLE IF NOT EXISTS chat_messages (
  id BIGSERIAL PRIMARY KEY,
  room_id VARCHAR(100) NOT NULL,
  user_name VARCHAR(100) NOT NULL,
  message_text TEXT NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_room_created ON chat_messages(room_id, created_at DESC);

-- Users Table
CREATE TABLE IF NOT EXISTS users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(100) UNIQUE NOT NULL,
  display_name VARCHAR(200),
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Stock Transactions Table
CREATE TABLE IF NOT EXISTS stock_transactions (
  id BIGSERIAL PRIMARY KEY,
  user_id VARCHAR(100) NOT NULL,
  symbol VARCHAR(10) NOT NULL,
  quantity INTEGER NOT NULL,
  price_per_share DECIMAL(10, 2) NOT NULL,
  transaction_type VARCHAR(10) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_created ON stock_transactions(user_id, created_at DESC);
