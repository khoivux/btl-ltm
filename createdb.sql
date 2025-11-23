
CREATE DATABASE IF NOT EXISTS btl_ltm
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;
    
USE btl_ltm;

-- 1. Bảng Người dùng
CREATE TABLE tblusers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(120) NOT NULL UNIQUE,
    password VARCHAR(120) NOT NULL,
    points INT DEFAULT 0
);

-- 2. Bảng tin nhắn 
CREATE TABLE tblchats (
    id INT AUTO_INCREMENT PRIMARY KEY,
    content VARCHAR(255) NOT NULL,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES tblusers(id)
        ON DELETE SET NULL
        ON UPDATE CASCADE
);

-- 3. Bảng thông tin trận đấu
CREATE TABLE tblmatch (
    match_id INT AUTO_INCREMENT PRIMARY KEY,
    start_time DATETIME NOT NULL,
    end_time DATETIME
);

-- 4. Bảng thông tin chi tiết trận đấu
CREATE TABLE tbldetail_match (
    id INT AUTO_INCREMENT PRIMARY KEY,
    match_id INT NOT NULL,
    player_id INT NOT NULL,
    score INT DEFAULT 0,
    is_winner TINYINT(1) DEFAULT 0,
    is_quit TINYINT(1) DEFAULT 0,

    FOREIGN KEY (match_id) REFERENCES tblmatch(match_id)
        ON DELETE CASCADE
        ON UPDATE CASCADE,

    FOREIGN KEY (player_id) REFERENCES tblusers(id)
        ON DELETE CASCADE
        ON UPDATE CASCADE
);
