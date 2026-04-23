CREATE TABLE payments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          user_id BIGINT NOT NULL,
                          plan_id BIGINT NOT NULL,
                          amount DOUBLE NOT NULL,
                          txn_ref VARCHAR(255) NOT NULL UNIQUE,
                          status VARCHAR(20) NOT NULL,
                          bank_code VARCHAR(20),
                          created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
                          CONSTRAINT fk_payment_user FOREIGN KEY (user_id) REFERENCES users(id),
                          CONSTRAINT fk_payment_plan FOREIGN KEY (plan_id) REFERENCES pricing_plans(id)
);