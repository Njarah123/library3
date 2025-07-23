-- Alter the type column in the notifications table to increase its size
ALTER TABLE notifications MODIFY COLUMN type VARCHAR(50) NOT NULL;