CREATE DATABASE IF NOT EXISTS classes_db;
USE classes_db;

DROP TABLE IF EXISTS classe;

CREATE TABLE classe (
    id INT AUTO_INCREMENT PRIMARY KEY,
    nom_etudiant VARCHAR(100) NOT NULL,
    nom_classe VARCHAR(50) NOT NULL
);

INSERT INTO classe (nom_etudiant, nom_classe) VALUES
    ('Rakoto', '6eA'),
    ('Rabe', '6eA'),
    ('Faniry', '6eA'),
    ('Lova', '6eB'),
    ('Mamy', '6eB'),
    ('Tiana', '6eB'),
    ('Voahangy', '5eA'),
    ('Hery', '5eA');

CREATE USER IF NOT EXISTS 'classes_user'@'localhost' IDENTIFIED BY 'classes_pass';
GRANT ALL PRIVILEGES ON classes_db.* TO 'classes_user'@'localhost';
FLUSH PRIVILEGES;
