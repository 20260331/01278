-- 测试数据

-- 系统用户数据 (密码为BCrypt加密后的 123456)
INSERT INTO sys_user (id, username, password, role, status) VALUES 
(1, 'admin', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_ADMIN', 1),
(2, 'coach1', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_COACH', 1),
(3, 'member1', '$2a$10$9WAuX9i4iizVsM8EIQ3xeOZIMFudAAom7Rb/m9OTVSdK1O46ZAHOe', 'ROLE_MEMBER', 1);

-- 教练数据
INSERT INTO coach (id, user_id, name, gender, phone, specialty, salary, status) VALUES 
(1, 2, '张教练', 1, '13800000001', '健身塑形,力量训练', 8000.00, 1);

-- 会员数据
INSERT INTO member (id, user_id, name, gender, phone, level, balance, expire_date, status) VALUES 
(1, 3, '王小明', 1, '13900000001', 2, 1000.00, '2026-12-31', 1);

-- 课程数据
INSERT INTO course (id, name, type, coach_id, max_capacity, current_count, price, duration, start_time, end_time, location, description, status) VALUES 
(1, '晨间瑜伽', '瑜伽', 1, 25, 0, 68.00, 60, '2026-02-03 07:00:00', '2026-02-03 08:00:00', '瑜伽室A', '适合初学者的基础瑜伽课程', 1);

-- 器材数据
INSERT INTO equipment (id, name, type, brand, location, status) VALUES 
(1, '跑步机', '有氧器械', 'Life Fitness', '有氧区A01', 1);

-- 场地数据
INSERT INTO venue (id, name, type, capacity, area, location, facilities, status) VALUES 
(1, '有氧训练区', '有氧区', 50, 300.00, '一楼东侧', '跑步机20台、椭圆机10台', 1);
