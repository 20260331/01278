-- 健身俱乐部管理系统数据库脚本
-- 数据库: fitness_club
-- 字符集: utf8mb4

CREATE DATABASE IF NOT EXISTS fitness_club DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE fitness_club;

-- ================================
-- 删除表（按依赖关系倒序删除）
-- ================================
DROP TABLE IF EXISTS sys_log;
DROP TABLE IF EXISTS coach_leave;
DROP TABLE IF EXISTS feedback;
DROP TABLE IF EXISTS fitness_record;
DROP TABLE IF EXISTS consume_record;
DROP TABLE IF EXISTS reservation;
DROP TABLE IF EXISTS course;
DROP TABLE IF EXISTS equipment;
DROP TABLE IF EXISTS venue;
DROP TABLE IF EXISTS coach;
DROP TABLE IF EXISTS member;
DROP TABLE IF EXISTS sys_user;

-- ================================
-- 创建表（按依赖关系正序创建）
-- ================================

-- 1. 系统用户表（基础表，无外键依赖）
CREATE TABLE sys_user (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    username VARCHAR(50) NOT NULL COMMENT '用户名',
    password VARCHAR(100) NOT NULL COMMENT '密码(BCrypt加密)',
    role VARCHAR(20) NOT NULL COMMENT '角色: ROLE_ADMIN/ROLE_COACH/ROLE_MEMBER',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-禁用 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    UNIQUE KEY uk_username (username),
    INDEX idx_username (username),
    INDEX idx_role (role),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户表';

-- 2. 会员表（依赖 sys_user）
CREATE TABLE member (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    level TINYINT DEFAULT 1 COMMENT '会员等级: 1-普通 2-银卡 3-金卡 4-钻石',
    balance DECIMAL(10,2) DEFAULT 0.00 COMMENT '账户余额',
    expire_date DATE COMMENT '会员到期日',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-冻结 1-正常',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_member_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone),
    INDEX idx_name (name),
    INDEX idx_level (level),
    INDEX idx_status (status),
    INDEX idx_expire_date (expire_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='会员表';

-- 3. 教练表（依赖 sys_user）
CREATE TABLE coach (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT NOT NULL COMMENT '关联用户ID',
    name VARCHAR(50) NOT NULL COMMENT '姓名',
    gender TINYINT DEFAULT 0 COMMENT '性别: 0-未知 1-男 2-女',
    phone VARCHAR(20) COMMENT '手机号',
    avatar VARCHAR(255) COMMENT '头像URL',
    specialty VARCHAR(100) COMMENT '专长领域',
    certificate VARCHAR(255) COMMENT '资质证书',
    entry_date DATE COMMENT '入职日期',
    salary DECIMAL(10,2) DEFAULT 0.00 COMMENT '基本薪资',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-正常 2-离职',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_coach_user FOREIGN KEY (user_id) REFERENCES sys_user(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_user_id (user_id),
    INDEX idx_user_id (user_id),
    INDEX idx_phone (phone),
    INDEX idx_name (name),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练表';

-- 4. 场地表（无外键依赖）
CREATE TABLE venue (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '场地名称',
    type VARCHAR(50) COMMENT '场地类型',
    capacity INT COMMENT '容纳人数',
    area DECIMAL(10,2) COMMENT '面积(平方米)',
    location VARCHAR(200) COMMENT '位置描述',
    facilities TEXT COMMENT '设施设备',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-维护中 1-可用 2-已停用',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_status (status),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='场地表';

-- 5. 器材表（无外键依赖）
CREATE TABLE equipment (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '器材名称',
    type VARCHAR(50) COMMENT '器材类型',
    brand VARCHAR(50) COMMENT '品牌',
    location VARCHAR(100) COMMENT '存放位置',
    purchase_date DATE COMMENT '购买日期',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-维修中 1-正常 2-报废',
    last_maintain_date DATE COMMENT '上次维护日期',
    next_maintain_date DATE COMMENT '下次维护日期',
    remark TEXT COMMENT '备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='器材表';


-- 6. 课程表（依赖 coach）
CREATE TABLE course (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    name VARCHAR(100) NOT NULL COMMENT '课程名称',
    type VARCHAR(50) COMMENT '课程类型: 瑜伽/健身操/器械/游泳等',
    coach_id BIGINT COMMENT '授课教练ID',
    max_capacity INT DEFAULT 20 COMMENT '最大容量',
    current_count INT DEFAULT 0 COMMENT '当前预约人数',
    price DECIMAL(10,2) DEFAULT 0.00 COMMENT '课程价格',
    duration INT DEFAULT 60 COMMENT '课程时长(分钟)',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    location VARCHAR(100) COMMENT '上课地点',
    description TEXT COMMENT '课程描述',
    status TINYINT DEFAULT 1 COMMENT '状态: 0-取消 1-正常 2-已满 3-已结束',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    CONSTRAINT fk_course_coach FOREIGN KEY (coach_id) REFERENCES coach(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_name (name),
    INDEX idx_type (type),
    INDEX idx_coach_id (coach_id),
    INDEX idx_start_time (start_time),
    INDEX idx_end_time (end_time),
    INDEX idx_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='课程表';

-- 7. 预约表（依赖 member, course）
CREATE TABLE reservation (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    course_id BIGINT NOT NULL COMMENT '课程ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-已预约 1-已签到 2-已取消 3-缺席',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '预约时间',
    checkin_time DATETIME COMMENT '签到时间',
    cancel_time DATETIME COMMENT '取消时间',
    CONSTRAINT fk_reservation_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_reservation_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE CASCADE ON UPDATE CASCADE,
    UNIQUE KEY uk_member_course (member_id, course_id),
    INDEX idx_member_id (member_id),
    INDEX idx_course_id (course_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='预约表';

-- 8. 消费记录表（依赖 member, sys_user）
CREATE TABLE consume_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    type TINYINT NOT NULL COMMENT '类型: 1-充值 2-购课 3-购卡 4-其他消费',
    amount DECIMAL(10,2) NOT NULL COMMENT '金额(正为收入,负为支出)',
    balance_after DECIMAL(10,2) COMMENT '交易后余额',
    description VARCHAR(255) COMMENT '描述',
    operator_id BIGINT COMMENT '操作员ID',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_consume_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_consume_operator FOREIGN KEY (operator_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_member_id (member_id),
    INDEX idx_operator_id (operator_id),
    INDEX idx_type (type),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='消费记录表';

-- 9. 健身记录表（依赖 member, coach）
CREATE TABLE fitness_record (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    record_date DATE NOT NULL COMMENT '记录日期',
    weight DECIMAL(5,2) COMMENT '体重(kg)',
    height DECIMAL(5,2) COMMENT '身高(cm)',
    duration INT COMMENT '训练时长(分钟)',
    calories INT COMMENT '消耗卡路里',
    content TEXT COMMENT '训练内容',
    remark TEXT COMMENT '备注',
    coach_advice TEXT COMMENT '教练建议',
    advice_coach_id BIGINT COMMENT '建议教练ID',
    advice_time DATETIME COMMENT '建议时间',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_fitness_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_fitness_coach FOREIGN KEY (advice_coach_id) REFERENCES coach(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_member_id (member_id),
    INDEX idx_advice_coach_id (advice_coach_id),
    INDEX idx_record_date (record_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='健身记录表';

-- 10. 意见反馈表（依赖 member, sys_user）
CREATE TABLE feedback (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    member_id BIGINT NOT NULL COMMENT '会员ID',
    title VARCHAR(100) COMMENT '标题',
    content TEXT NOT NULL COMMENT '反馈内容',
    reply TEXT COMMENT '回复内容',
    reply_time DATETIME COMMENT '回复时间',
    reply_user_id BIGINT COMMENT '回复人ID',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待回复 1-已回复',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_feedback_member FOREIGN KEY (member_id) REFERENCES member(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_feedback_reply_user FOREIGN KEY (reply_user_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_member_id (member_id),
    INDEX idx_reply_user_id (reply_user_id),
    INDEX idx_status (status),
    INDEX idx_create_time (create_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='意见反馈表';

-- 11. 教练请假/调课申请表（依赖 coach, course, sys_user）
CREATE TABLE coach_leave (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    coach_id BIGINT NOT NULL COMMENT '教练ID',
    course_id BIGINT COMMENT '关联课程ID(调课时)',
    type TINYINT NOT NULL COMMENT '类型: 1-请假 2-调课',
    reason TEXT COMMENT '原因',
    start_time DATETIME COMMENT '开始时间',
    end_time DATETIME COMMENT '结束时间',
    status TINYINT DEFAULT 0 COMMENT '状态: 0-待审核 1-已通过 2-已拒绝',
    audit_user_id BIGINT COMMENT '审核人ID',
    audit_time DATETIME COMMENT '审核时间',
    audit_remark VARCHAR(255) COMMENT '审核备注',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    CONSTRAINT fk_leave_coach FOREIGN KEY (coach_id) REFERENCES coach(id) ON DELETE CASCADE ON UPDATE CASCADE,
    CONSTRAINT fk_leave_course FOREIGN KEY (course_id) REFERENCES course(id) ON DELETE SET NULL ON UPDATE CASCADE,
    CONSTRAINT fk_leave_audit_user FOREIGN KEY (audit_user_id) REFERENCES sys_user(id) ON DELETE SET NULL ON UPDATE CASCADE,
    INDEX idx_coach_id (coach_id),
    INDEX idx_course_id (course_id),
    INDEX idx_audit_user_id (audit_user_id),
    INDEX idx_status (status),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='教练请假调课申请表';

-- 12. 操作日志表（依赖 sys_user，但使用软关联）
CREATE TABLE sys_log (
    id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '主键ID',
    user_id BIGINT COMMENT '操作用户ID',
    username VARCHAR(50) COMMENT '操作用户名',
    operation VARCHAR(100) COMMENT '操作描述',
    method VARCHAR(200) COMMENT '请求方法',
    params TEXT COMMENT '请求参数',
    ip VARCHAR(50) COMMENT 'IP地址',
    duration BIGINT COMMENT '执行时长(ms)',
    create_time DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    INDEX idx_user_id (user_id),
    INDEX idx_username (username),
    INDEX idx_operation (operation),
    INDEX idx_create_time (create_time),
    INDEX idx_ip (ip)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='操作日志表';


-- ================================
-- 初始化数据
-- 默认账号密码均为 123456
-- ================================

-- 系统用户数据 (密码为BCrypt加密后的 123456)
INSERT INTO sys_user (id, username, password, role, status) VALUES 
(1, 'admin', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_ADMIN', 1),
(2, 'coach1', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_COACH', 1),
(3, 'coach2', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_COACH', 1),
(4, 'member1', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_MEMBER', 1),
(5, 'member2', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_MEMBER', 1);

-- 教练数据
INSERT INTO coach (id, user_id, name, gender, phone, specialty, salary, status) VALUES 
(1, 2, '张教练', 1, '13800000001', '健身塑形,力量训练', 8000.00, 1),
(2, 3, '李教练', 2, '13800000002', '瑜伽,普拉提', 7500.00, 1);

-- 会员数据
INSERT INTO member (id, user_id, name, gender, phone, level, balance, expire_date, status) VALUES 
(1, 4, '王小明', 1, '13900000001', 2, 1000.00, '2026-12-31', 1),
(2, 5, '刘小红', 2, '13900000002', 1, 500.00, '2026-06-30', 1);

-- 测试器材
INSERT INTO equipment (name, type, brand, location, status, last_maintain_date) VALUES 
('跑步机', '有氧器械', 'Life Fitness', '有氧区A01', 1, '2025-01-15'),
('哑铃组', '力量器械', 'Rogue', '力量区B01', 1, '2025-01-10'),
('划船机', '有氧器械', 'Concept2', '有氧区A02', 1, '2025-01-20');

-- 健身房场地数据
INSERT INTO venue (name, type, capacity, area, location, facilities, status, remark) VALUES 
('有氧训练区', '有氧区', 50, 300.00, '一楼东侧', '跑步机20台、椭圆机10台、动感单车15台、划船机5台', 1, '配备空调和新风系统，全天开放'),
('力量训练区', '力量区', 40, 250.00, '一楼西侧', '史密斯机4台、龙门架6台、哑铃区、杠铃区、固定器械20台', 1, '配备专业力量训练设备'),
('自由重量区', '力量区', 20, 150.00, '一楼西侧内部', '奥林匹克杠铃8根、哑铃架3组(2.5kg-50kg)、深蹲架4个', 1, '专业健身爱好者专区'),
('瑜伽室A', '团课教室', 25, 80.00, '二楼201', '瑜伽垫30张、瑜伽砖、瑜伽带、镜墙、音响系统', 1, '适合瑜伽、普拉提等静态课程'),
('瑜伽室B', '团课教室', 20, 60.00, '二楼202', '瑜伽垫25张、瑜伽辅助器材、空中瑜伽吊床10个', 1, '配备空中瑜伽设备'),
('动感单车房', '团课教室', 30, 100.00, '二楼203', '动感单车30台、投影仪、音响系统、灯光系统', 1, '沉浸式动感单车体验'),
('操课教室', '团课教室', 40, 120.00, '二楼204', '镜墙、音响系统、杠铃片、踏板、弹力带', 1, '适合有氧操、搏击操等团课'),
('拳击区', '搏击区', 15, 80.00, '三楼301', '拳击沙袋8个、拳击台1个、速度球4个、护具', 1, '配备专业搏击训练设备'),
('游泳池', '水上运动', 30, 500.00, '负一楼', '25米标准泳道6条、儿童池、按摩池', 1, '恒温泳池，水温26-28度'),
('私教室1', '私教区', 3, 30.00, '三楼302', 'TRX、壶铃、药球、弹力带、瑜伽垫', 1, '一对一私教专用'),
('私教室2', '私教区', 3, 30.00, '三楼303', '可调节哑铃、训练凳、小型器械', 1, '一对一私教专用'),
('私教室3', '私教区', 5, 40.00, '三楼304', '综合训练架、功能性训练设备', 1, '可容纳小团体私教'),
('拉伸放松区', '休息区', 15, 50.00, '一楼中央', '泡沫轴、筋膜枪、拉伸垫、按摩椅3台', 1, '训练前热身及训练后放松'),
('更衣室(男)', '配套设施', 50, 80.00, '一楼北侧', '储物柜100个、淋浴间10间、洗手台、吹风机', 1, '配备桑拿房'),
('更衣室(女)', '配套设施', 50, 80.00, '一楼南侧', '储物柜100个、淋浴间12间、洗手台、吹风机、化妆区', 1, '配备桑拿房'),
('休息大厅', '休息区', 30, 100.00, '一楼入口', '休息沙发、饮水机、自动售货机、前台服务', 1, '会员休息等候区域');

-- 课程数据
INSERT INTO course (name, type, coach_id, max_capacity, current_count, price, duration, start_time, end_time, location, description, status) VALUES 
('晨间瑜伽', '瑜伽', 2, 25, 0, 68.00, 60, '2026-02-03 07:00:00', '2026-02-03 08:00:00', '瑜伽室A', '适合初学者的基础瑜伽课程，帮助唤醒身体，提升一天的精神状态', 1),
('动感单车燃脂', '有氧操', 1, 30, 0, 58.00, 45, '2026-02-03 09:00:00', '2026-02-03 09:45:00', '动感单车房', '高强度间歇训练，配合动感音乐，快速燃烧脂肪', 1),
('力量训练入门', '器械', 1, 15, 0, 88.00, 60, '2026-02-03 10:00:00', '2026-02-03 11:00:00', '力量训练区', '学习正确的力量训练姿势，包括深蹲、硬拉、卧推等基础动作', 1),
('普拉提核心', '瑜伽', 2, 20, 0, 78.00, 50, '2026-02-03 14:00:00', '2026-02-03 14:50:00', '瑜伽室B', '专注核心肌群训练，改善体态，增强身体稳定性', 1),
('搏击操', '有氧操', 1, 25, 0, 68.00, 45, '2026-02-03 15:00:00', '2026-02-03 15:45:00', '操课教室', '结合拳击、跆拳道等动作的有氧课程，释放压力，塑造线条', 1),
('游泳技巧提升', '游泳', 1, 10, 0, 128.00, 60, '2026-02-03 16:00:00', '2026-02-03 17:00:00', '游泳池', '针对有一定基础的学员，提升游泳技巧和耐力', 1),
('晚间拉伸放松', '瑜伽', 2, 30, 0, 48.00, 40, '2026-02-03 20:00:00', '2026-02-03 20:40:00', '瑜伽室A', '一天训练后的放松课程，缓解肌肉疲劳，改善睡眠质量', 1),
('HIIT高强度间歇', '有氧操', 1, 20, 0, 78.00, 30, '2026-02-04 07:30:00', '2026-02-04 08:00:00', '操课教室', '短时高效的全身燃脂训练，适合时间紧张的上班族', 1),
('空中瑜伽', '瑜伽', 2, 10, 0, 98.00, 60, '2026-02-04 10:00:00', '2026-02-04 11:00:00', '瑜伽室B', '借助吊床完成瑜伽动作，增强柔韧性和核心力量', 1),
('拳击基础', '搏击', 1, 12, 0, 108.00, 60, '2026-02-04 14:00:00', '2026-02-04 15:00:00', '拳击区', '学习基本拳击技巧，包括站姿、出拳、防守等', 1),
('杠铃塑形', '器械', 1, 15, 0, 88.00, 50, '2026-02-04 16:00:00', '2026-02-04 16:50:00', '力量训练区', '使用杠铃进行全身塑形训练，打造完美身材曲线', 1),
('舞蹈健身', '有氧操', 2, 30, 0, 58.00, 45, '2026-02-04 19:00:00', '2026-02-04 19:45:00', '操课教室', '融合多种舞蹈风格的健身课程，边跳边瘦，快乐健身', 1);
