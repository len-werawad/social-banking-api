ALTER TABLE accounts
    ADD INDEX idx_accounts_user (user_id),
    ADD INDEX idx_accounts_type_user (user_id, type);

ALTER TABLE account_details
    ADD INDEX idx_account_details_user (user_id),
  ADD UNIQUE KEY uk_account_details_account (account_id);

ALTER TABLE account_balances
    ADD INDEX idx_account_balances_user (user_id),
  ADD UNIQUE KEY uk_account_balances_account (account_id);

ALTER TABLE debit_cards
    ADD INDEX idx_debit_cards_user (user_id);

ALTER TABLE debit_card_status
    ADD INDEX idx_debit_card_status_user (user_id);

ALTER TABLE debit_card_details
    ADD INDEX idx_debit_card_details_user (user_id);

ALTER TABLE debit_card_design
    ADD INDEX idx_debit_card_design_user (user_id);

ALTER TABLE banners
    ADD INDEX idx_banners_user (user_id);

ALTER TABLE transactions
    ADD INDEX idx_transactions_user (user_id);

ALTER TABLE account_flags
    ADD INDEX idx_flags_user_account (user_id, account_id),
    ADD INDEX idx_flags_account_type (account_id, flag_type);

ALTER TABLE user_credentials
    ADD UNIQUE KEY uk_user_credentials_user (user_id);

ALTER TABLE transactions
    ADD COLUMN created_at DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
  ADD INDEX idx_tx_user_created (user_id, created_at DESC);

ALTER TABLE app_config
  ADD INDEX idx_app_config_env_platform_version (environment, platform, app_version);
