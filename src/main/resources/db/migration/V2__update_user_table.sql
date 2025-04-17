ALTER TABLE users
ADD COLUMN activation_token VARCHAR(255) UNIQUE ,
ADD COLUMN password_reset_token VARCHAR(255) UNIQUE ,
ADD COLUMN password_reset_deadline DATETIME;
