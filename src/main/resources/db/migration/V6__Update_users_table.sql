ALTER TABLE users
    ADD COLUMN reset_otp VARCHAR(6),
    ADD COLUMN otp_expiry DATETIME;