-- 测试用数据库脚本 (H2兼容)

-- 1. 系统用户表
CREATE TABLE IF NOT EXISTS sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 2. 会员表
CREATE TABLE IF NOT EXISTS member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender INT DEFAULT 0,
    phone VARCHAR(20),
    avatar VARCHAR(255),
    level INT DEFAULT 1,
    balance DECIMAL(10,2) DEFAULT 0.00,
    expire_date DATE,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 3. 教练表
CREATE TABLE IF NOT EXISTS coach (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT NOT NULL,
    name VARCHAR(50) NOT NULL,
    gender INT DEFAULT 0,
    phone VARCHAR(20),
    avatar VARCHAR(255),
    specialty VARCHAR(100),
    certificate VARCHAR(255),
    entry_date DATE,
    salary DECIMAL(10,2) DEFAULT 0.00,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 4. 课程表
CREATE TABLE IF NOT EXISTS course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    coach_id BIGINT,
    max_capacity INT DEFAULT 20,
    current_count INT DEFAULT 0,
    price DECIMAL(10,2) DEFAULT 0.00,
    duration INT DEFAULT 60,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    location VARCHAR(100),
    description TEXT,
    status INT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 5. 预约表
CREATE TABLE IF NOT EXISTS reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    course_id BIGINT NOT NULL,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    checkin_time TIMESTAMP,
    cancel_time TIMESTAMP
);

-- 6. 器材表
CREATE TABLE IF NOT EXISTS equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    brand VARCHAR(50),
    location VARCHAR(100),
    purchase_date DATE,
    status INT DEFAULT 1,
    last_maintain_date DATE,
    next_maintain_date DATE,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 7. 消费记录表
CREATE TABLE IF NOT EXISTS consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    type INT NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    balance_after DECIMAL(10,2),
    description VARCHAR(255),
    operator_id BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 8. 健身记录表
CREATE TABLE IF NOT EXISTS fitness_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    record_date DATE NOT NULL,
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    duration INT,
    calories INT,
    content TEXT,
    remark TEXT,
    coach_advice TEXT,
    advice_coach_id BIGINT,
    advice_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 9. 意见反馈表
CREATE TABLE IF NOT EXISTS feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    member_id BIGINT NOT NULL,
    title VARCHAR(100),
    content TEXT NOT NULL,
    reply TEXT,
    reply_time TIMESTAMP,
    reply_user_id BIGINT,
    status INT DEFAULT 0,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 10. 教练请假调课申请表
CREATE TABLE IF NOT EXISTS coach_leave (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    coach_id BIGINT NOT NULL,
    course_id BIGINT,
    type INT NOT NULL,
    reason TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    status INT DEFAULT 0,
    audit_user_id BIGINT,
    audit_time TIMESTAMP,
    audit_remark VARCHAR(255),
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 11. 操作日志表
CREATE TABLE IF NOT EXISTS sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    user_id BIGINT,
    username VARCHAR(50),
    operation VARCHAR(100),
    method VARCHAR(200),
    params TEXT,
    ip VARCHAR(50),
    duration BIGINT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 12. 场地表
CREATE TABLE IF NOT EXISTS venue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50),
    capacity INT,
    area DECIMAL(10,2),
    location VARCHAR(200),
    facilities TEXT,
    status INT DEFAULT 1,
    remark TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
