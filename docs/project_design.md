# 健身俱乐部综合管理系统设计文档

## 一、系统概述

基于Spring Boot的健身俱乐部综合管理系统，采用前后端分离架构，支持管理员、教练、会员三类角色的全流程信息化管理。

### 技术栈
- **后端**: Spring Boot 2.7.x + MyBatis-Plus + Spring Security + JWT
- **前端**: Vue3 + Element Plus + Tailwind CSS + Axios + Pinia + Vue Router
- **数据库**: MySQL 8.0

## 二、系统架构

```
┌─────────────────────────────────────────────────────────────┐
│                        前端 (Vue3)                           │
│  ┌─────────┐ ┌─────────┐ ┌─────────┐ ┌─────────┐           │
│  │ 管理员  │ │  教练   │ │  会员   │ │  登录   │           │
│  └────┬────┘ └────┬────┘ └────┬────┘ └────┬────┘           │
└───────┼───────────┼───────────┼───────────┼─────────────────┘
        │           │           │           │
        └───────────┴───────────┴───────────┘
                          │ Axios HTTP
┌─────────────────────────┼───────────────────────────────────┐
│                    后端 (Spring Boot)                        │
│  ┌──────────────────────┴──────────────────────┐            │
│  │              Controller 层                   │            │
│  │   (AuthController, MemberController, ...)   │            │
│  └──────────────────────┬──────────────────────┘            │
│  ┌──────────────────────┴──────────────────────┐            │
│  │              Service 层                      │            │
│  │   (业务逻辑处理, 事务管理)                   │            │
│  └──────────────────────┬──────────────────────┘            │
│  ┌──────────────────────┴──────────────────────┐            │
│  │              Mapper 层 (MyBatis-Plus)        │            │
│  └──────────────────────┬──────────────────────┘            │
└─────────────────────────┼───────────────────────────────────┘
                          │
┌─────────────────────────┼───────────────────────────────────┐
│                    MySQL 8.0                                 │
│   sys_user, member, coach, course, reservation, ...         │
└─────────────────────────────────────────────────────────────┘
```

## 三、数据库设计 (ER图)

```
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│   sys_user   │       │    member    │       │    coach     │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │──┐    │ id (PK)      │       │ id (PK)      │
│ username     │  │    │ user_id (FK) │◄──┐   │ user_id (FK) │◄──┐
│ password     │  │    │ name         │   │   │ name         │   │
│ role         │  └───►│ phone        │   │   │ specialty    │   │
│ status       │       │ level        │   │   │ status       │   │
│ create_time  │       │ balance      │   │   │ salary       │   │
└──────────────┘       │ expire_date  │   │   └──────────────┘   │
                       └──────────────┘   │                      │
                              │           │                      │
                              │           └──────────────────────┘
        ┌─────────────────────┘
        │
        ▼
┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│ reservation  │       │    course    │       │  equipment   │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)      │
│ member_id(FK)│◄─────►│ coach_id(FK) │       │ name         │
│ course_id(FK)│       │ name         │       │ status       │
│ status       │       │ type         │       │ location     │
│ create_time  │       │ max_capacity │       │ last_maintain│
└──────────────┘       │ price        │       └──────────────┘
                       │ start_time   │
                       │ status       │
                       └──────────────┘

┌──────────────┐       ┌──────────────┐       ┌──────────────┐
│consume_record│       │fitness_record│       │   feedback   │
├──────────────┤       ├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │       │ id (PK)      │
│ member_id(FK)│       │ member_id(FK)│       │ member_id(FK)│
│ type         │       │ weight       │       │ content      │
│ amount       │       │ duration     │       │ reply        │
│ description  │       │ record_date  │       │ status       │
│ create_time  │       │ create_time  │       │ create_time  │
└──────────────┘       └──────────────┘       └──────────────┘

┌──────────────┐       ┌──────────────┐
│  sys_log     │       │ coach_leave  │
├──────────────┤       ├──────────────┤
│ id (PK)      │       │ id (PK)      │
│ user_id      │       │ coach_id(FK) │
│ operation    │       │ course_id(FK)│
│ method       │       │ reason       │
│ params       │       │ status       │
│ ip           │       │ create_time  │
│ create_time  │       └──────────────┘
└──────────────┘
```

## 四、接口设计

### 4.1 认证模块 (AuthController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| POST | /api/auth/login | 用户登录 | 公开 |
| POST | /api/auth/logout | 退出登录 | 已认证 |
| GET | /api/auth/info | 获取当前用户信息 | 已认证 |
| PUT | /api/auth/password | 修改密码 | 已认证 |

### 4.2 用户管理 (UserController) - 管理员
| 方法 | 路径 | 描述 |
|------|------|------|
| GET | /api/user/page | 分页查询用户 |
| POST | /api/user | 新增用户 |
| PUT | /api/user | 修改用户 |
| DELETE | /api/user/{id} | 删除用户 |

### 4.3 会员管理 (MemberController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/member/page | 分页查询会员 | ADMIN |
| GET | /api/member/{id} | 获取会员详情 | ADMIN,COACH |
| POST | /api/member | 新增会员 | ADMIN |
| PUT | /api/member | 修改会员信息 | ADMIN,MEMBER |
| PUT | /api/member/status/{id} | 冻结/激活会员 | ADMIN |
| DELETE | /api/member/{id} | 删除会员 | ADMIN |

### 4.4 教练管理 (CoachController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/coach/page | 分页查询教练 | ADMIN |
| GET | /api/coach/{id} | 获取教练详情 | ADMIN |
| POST | /api/coach | 新增教练 | ADMIN |
| PUT | /api/coach | 修改教练信息 | ADMIN,COACH |
| PUT | /api/coach/audit/{id} | 资质审核 | ADMIN |
| GET | /api/coach/schedule/{id} | 获取教练排班 | ADMIN,COACH |

### 4.5 课程管理 (CourseController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/course/page | 分页查询课程 | ALL |
| GET | /api/course/available | 可预约课程列表 | MEMBER |
| POST | /api/course | 新增课程 | ADMIN |
| PUT | /api/course | 修改课程 | ADMIN |
| DELETE | /api/course/{id} | 删除课程 | ADMIN |
| POST | /api/course/leave | 教练请假申请 | COACH |
| PUT | /api/course/audit/{id} | 请假审核 | ADMIN |

### 4.6 预约管理 (ReservationController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/reservation/page | 预约记录分页 | ALL |
| POST | /api/reservation | 创建预约 | MEMBER |
| PUT | /api/reservation/cancel/{id} | 取消预约 | MEMBER |
| PUT | /api/reservation/checkin/{id} | 课程签到 | COACH |

### 4.7 器材管理 (EquipmentController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/equipment/page | 分页查询器材 | ADMIN |
| POST | /api/equipment | 新增器材 | ADMIN |
| PUT | /api/equipment | 修改器材 | ADMIN |
| DELETE | /api/equipment/{id} | 删除器材 | ADMIN |
| PUT | /api/equipment/maintain/{id} | 记录维护 | ADMIN |

### 4.8 消费记录 (ConsumeController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/consume/page | 消费记录分页 | ADMIN,MEMBER |
| POST | /api/consume/recharge | 会员充值 | ADMIN |
| GET | /api/consume/statistics | 收支统计 | ADMIN |

### 4.9 健身记录 (FitnessController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/fitness/page | 健身记录分页 | MEMBER,COACH |
| POST | /api/fitness | 录入健身数据 | MEMBER |
| GET | /api/fitness/report | 生成健身周报 | MEMBER |

### 4.10 意见反馈 (FeedbackController)
| 方法 | 路径 | 描述 | 权限 |
|------|------|------|------|
| GET | /api/feedback/page | 反馈列表 | ADMIN,MEMBER |
| POST | /api/feedback | 提交反馈 | MEMBER |
| PUT | /api/feedback/reply/{id} | 回复反馈 | ADMIN |

## 五、角色权限设计

| 角色 | 权限范围 |
|------|----------|
| ROLE_ADMIN | 全部功能：用户管理、会员管理、教练管理、课程管理、器材管理、财务管理、系统日志 |
| ROLE_COACH | 个人课程管理、会员跟进、业绩查询、个人信息修改 |
| ROLE_MEMBER | 课程预约、消费记录查看、健身数据录入、意见反馈、个人信息修改 |

## 六、UI/UX设计规范

### 色彩体系
- 主色调: #4F46E5 (Indigo-600)
- 成功色: #10B981 (Emerald-500)
- 警告色: #F59E0B (Amber-500)
- 危险色: #F43F5E (Rose-500)
- 背景色: #F8FAFC (Slate-50)

### 组件规范
- 圆角: 卡片16px, 按钮8px
- 阴影: 卡片shadow-sm, 悬浮shadow-lg
- 间距: 8px/16px/24px
