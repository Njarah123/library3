ALTER TABLE borrowings
ADD COLUMN renewal_count INT DEFAULT 0,
ADD COLUMN last_renewal_date DATETIME,
ADD COLUMN condition_before VARCHAR(50),
ADD COLUMN condition_after VARCHAR(50),
MODIFY COLUMN fine_amount DECIMAL(10,2) DEFAULT 0.00;