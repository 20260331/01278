# 健身俱乐部管理系统 API 接口文档

## 目录

- [概述](#概述)
- [通用说明](#通用说明)
- [认证管理](#认证管理)
- [会员管理](#会员管理)
- [教练管理](#教练管理)
- [课程管理](#课程管理)
- [预约管理](#预约管理)
- [消费记录](#消费记录)
- [健身记录](#健身记录)
- [意见反馈](#意见反馈)
- [器材管理](#器材管理)
- [场地管理](#场地管理)
- [用户管理](#用户管理)
- [薪资结算](#薪资结算)
- [统计查询](#统计查询)
- [数据备份](#数据备份)

---

## 概述

### 基础信息

| 项目 | 说明 |
|------|------|
| 基础URL | `http://localhost:8080/api/v1` |
| 协议 | HTTP/HTTPS |
| 数据格式 | JSON |
| 字符编码 | UTF-8 |
| 认证方式 | JWT Bearer Token |

### 在线文档

系统集成了 Swagger/OpenAPI 在线文档，启动后可访问：
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI JSON: `http://localhost:8080/api-docs`

---

## 通用说明

### 请求头

| Header | 说明 | 必填 |
|--------|------|------|
| Content-Type | `application/json` | 是 |
| Authorization | `Bearer {token}` | 需认证接口必填 |

### 统一响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {}
}
```

### 状态码说明

| code | 说明 |
|------|------|
| 200 | 成功 |
| 400 | 请求参数错误 |
| 401 | 未认证/Token过期 |
| 403 | 无权限 |
| 500 | 服务器错误 |

### 分页响应格式

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [],
    "total": 100,
    "size": 10,
    "current": 1,
    "pages": 10
  }
}
```

### 角色说明

| 角色 | 标识 | 说明 |
|------|------|------|
| 管理员 | ROLE_ADMIN | 系统管理员，拥有所有权限 |
| 教练 | ROLE_COACH | 健身教练 |
| 会员 | ROLE_MEMBER | 健身会员 |

---

## 认证管理

### 用户登录

**POST** `/auth/login`

**权限：** 公开

**请求参数：**

```json
{
  "username": "admin",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| username | String | 是 | 用户名 |
| password | String | 是 | 密码 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "userId": 1,
    "username": "admin",
    "role": "ROLE_ADMIN",
    "nickname": "系统管理员"
  }
}
```

---

### 获取当前用户信息

**GET** `/auth/info`

**权限：** 需认证

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "userId": 1,
    "username": "admin",
    "role": "ROLE_ADMIN",
    "nickname": "系统管理员"
  }
}
```

---

### 修改密码

**PUT** `/auth/password`

**权限：** 需认证

**请求参数：**

```json
{
  "oldPassword": "123456",
  "newPassword": "654321"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| oldPassword | String | 是 | 原密码 |
| newPassword | String | 是 | 新密码 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

### 退出登录

**POST** `/auth/logout`

**权限：** 需认证

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": null
}
```

---

## 会员管理

### 分页查询会员

**GET** `/member/page`

**权限：** ADMIN, COACH

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页条数 |
| name | String | 否 | - | 会员姓名（模糊查询） |
| phone | String | 否 | - | 手机号（模糊查询） |
| status | Integer | 否 | - | 状态：0-冻结 1-正常 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "张三",
        "gender": 1,
        "phone": "13800138001",
        "level": 1,
        "balance": 1000.00,
        "status": 1,
        "createTime": "2024-01-01 10:00:00"
      }
    ],
    "total": 50,
    "size": 10,
    "current": 1
  }
}
```

---

### 获取会员详情

**GET** `/member/{id}`

**权限：** ADMIN, COACH

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 会员ID |

---

### 获取当前会员信息

**GET** `/member/current`

**权限：** MEMBER

**说明：** 获取当前登录会员的详细信息

---

### 新增会员

**POST** `/member`

**权限：** ADMIN

**请求参数：**

```json
{
  "name": "张三",
  "gender": 1,
  "phone": "13800138001",
  "level": 1,
  "username": "zhangsan",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名 |
| gender | Integer | 是 | 性别：0-女 1-男 |
| phone | String | 是 | 手机号 |
| level | Integer | 是 | 会员等级：1-普通 2-银卡 3-金卡 4-钻石 |
| username | String | 是 | 登录用户名 |
| password | String | 是 | 登录密码 |

---

### 修改会员信息

**PUT** `/member`

**权限：** ADMIN, MEMBER

**说明：** 会员可修改自己的基本信息（姓名、性别、手机号），管理员可修改任意会员的所有信息（包括等级）

**请求参数：**

```json
{
  "id": 1,
  "name": "张三",
  "gender": 1,
  "phone": "13800138001",
  "level": 2
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| id | Long | 是 | 会员ID |
| name | String | 否 | 姓名 |
| gender | Integer | 否 | 性别：0-女 1-男 |
| phone | String | 否 | 手机号 |
| level | Integer | 否 | 会员等级（仅管理员可修改） |

**会员自己修改信息示例：**

```json
{
  "id": 1,
  "name": "张三",
  "gender": 1,
  "phone": "13900139001"
}
```

---

### 修改会员状态

**PUT** `/member/status/{id}`

**权限：** ADMIN

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 会员ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 状态：0-冻结 1-正常 |

---

### 删除会员

**DELETE** `/member/{id}`

**权限：** ADMIN

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 会员ID |

---

## 教练管理

### 分页查询教练

**GET** `/coach/page`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页条数 |
| name | String | 否 | - | 教练姓名 |
| status | Integer | 否 | - | 状态 |

---

### 获取所有教练（下拉选择）

**GET** `/coach/list`

**权限：** 公开

**说明：** 用于下拉选择框获取所有教练列表

---

### 获取教练详情

**GET** `/coach/{id}`

**权限：** ADMIN

---

### 获取当前教练信息

**GET** `/coach/current`

**权限：** COACH

---

### 新增教练

**POST** `/coach`

**权限：** ADMIN

**请求参数：**

```json
{
  "name": "李教练",
  "gender": 1,
  "phone": "13900139001",
  "specialty": "健身塑形",
  "username": "coach_li",
  "password": "123456"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| name | String | 是 | 姓名 |
| gender | Integer | 是 | 性别 |
| phone | String | 是 | 手机号 |
| specialty | String | 否 | 专长 |
| username | String | 是 | 登录用户名 |
| password | String | 是 | 登录密码 |

---

### 修改教练信息

**PUT** `/coach`

**权限：** ADMIN, COACH

---

### 审核教练

**PUT** `/coach/audit/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 审核状态 |

---

### 获取教练排班

**GET** `/coach/schedule/{id}`

**权限：** ADMIN, COACH

**说明：** 获取指定教练的课程排班表

---

### 删除教练

**DELETE** `/coach/{id}`

**权限：** ADMIN

---

## 课程管理

### 分页查询课程

**GET** `/course/page`

**权限：** 公开

**请求参数：**

| 参数 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| current | Integer | 否 | 1 | 当前页码 |
| size | Integer | 否 | 10 | 每页条数 |
| name | String | 否 | - | 课程名称 |
| type | String | 否 | - | 课程类型 |
| coachId | Long | 否 | - | 教练ID |
| status | Integer | 否 | - | 课程状态 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "records": [
      {
        "id": 1,
        "name": "瑜伽初级班",
        "type": "团课",
        "coachId": 1,
        "coachName": "李教练",
        "price": 100.00,
        "maxCount": 20,
        "currentCount": 15,
        "startTime": "2024-01-15 09:00:00",
        "duration": 60,
        "status": 1
      }
    ],
    "total": 30
  }
}
```

---

### 获取可预约课程

**GET** `/course/available`

**权限：** MEMBER

**说明：** 获取当前可预约的课程列表

---

### 获取课程详情

**GET** `/course/{id}`

**权限：** 公开

---

### 新增课程

**POST** `/course`

**权限：** ADMIN

**请求参数：**

```json
{
  "name": "瑜伽初级班",
  "type": "团课",
  "coachId": 1,
  "price": 100.00,
  "maxCount": 20,
  "startTime": "2024-01-15 09:00:00",
  "duration": 60,
  "description": "适合初学者的瑜伽课程"
}
```

---

### 修改课程

**PUT** `/course`

**权限：** ADMIN

---

### 删除课程

**DELETE** `/course/{id}`

**权限：** ADMIN

---

### 提交请假/调课申请

**POST** `/course/leave`

**权限：** COACH

**请求参数：**

```json
{
  "courseId": 1,
  "type": 1,
  "reason": "身体不适",
  "startTime": "2024-01-15 00:00:00",
  "endTime": "2024-01-16 00:00:00"
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 否 | 关联课程ID |
| type | Integer | 是 | 类型：1-请假 2-调课 |
| reason | String | 是 | 原因 |
| startTime | DateTime | 是 | 开始时间 |
| endTime | DateTime | 是 | 结束时间 |

---

### 审核请假/调课申请

**PUT** `/course/leave/audit/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 审核状态：1-通过 2-拒绝 |
| remark | String | 否 | 审核备注 |

---

### 获取请假申请列表

**GET** `/course/leave/page`

**权限：** ADMIN, COACH

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| coachId | Long | 否 | 教练ID |
| status | Integer | 否 | 审核状态 |

---

### 检查教练请假状态

**GET** `/course/leave/check`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| coachId | Long | 是 | 教练ID |
| startTime | String | 是 | 开始时间（yyyy-MM-dd HH:mm:ss） |
| duration | Integer | 是 | 时长（分钟） |

---

## 预约管理

### 分页查询预约记录

**GET** `/reservation/page`

**权限：** 公开

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| memberId | Long | 否 | 会员ID |
| courseId | Long | 否 | 课程ID |
| status | Integer | 否 | 状态 |

---

### 获取我的预约

**GET** `/reservation/my`

**权限：** MEMBER

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| status | Integer | 否 | 状态 |

---

### 创建预约

**POST** `/reservation`

**权限：** MEMBER

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| courseId | Long | 是 | 课程ID |

---

### 取消预约

**PUT** `/reservation/cancel/{id}`

**权限：** MEMBER

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 预约ID |

---

### 课程签到

**PUT** `/reservation/checkin/{id}`

**权限：** COACH

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 预约ID |

---

## 消费记录

### 分页查询消费记录

**GET** `/consume/page`

**权限：** ADMIN, MEMBER

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| memberId | Long | 否 | 会员ID |
| type | Integer | 否 | 类型：1-充值 2-消费 3-退款 |

---

### 获取我的消费记录

**GET** `/consume/my`

**权限：** MEMBER

---

### 会员充值

**POST** `/consume/recharge`

**权限：** ADMIN

**请求参数：**

```json
{
  "memberId": 1,
  "amount": 500.00,
  "description": "充值500元"
}
```

---

### 收支统计

**GET** `/consume/statistics`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期（yyyy-MM-dd） |
| endDate | String | 是 | 结束日期（yyyy-MM-dd） |

---

### 导出我的消费记录

**GET** `/consume/export`

**权限：** MEMBER

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| type | Integer | 否 | 消费类型 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

**响应：** Excel文件下载

---

### 导出消费记录（管理员）

**GET** `/consume/export/admin`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| memberId | Long | 否 | 会员ID |
| type | Integer | 否 | 消费类型 |
| startDate | String | 否 | 开始日期 |
| endDate | String | 否 | 结束日期 |

---

## 健身记录

### 分页查询健身记录

**GET** `/fitness/page`

**权限：** MEMBER, COACH

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| memberId | Long | 否 | 会员ID |

---

### 获取我的健身记录

**GET** `/fitness/my`

**权限：** MEMBER

---

### 录入健身数据

**POST** `/fitness`

**权限：** MEMBER

**请求参数：**

```json
{
  "weight": 70.5,
  "height": 175,
  "exerciseType": "跑步",
  "duration": 30,
  "calorie": 300,
  "remark": "今日感觉良好"
}
```

---

### 生成健身周报

**GET** `/fitness/report`

**权限：** MEMBER

**说明：** 获取当前会员的健身周报统计

---

### 教练发送健身建议

**PUT** `/fitness/advice/{id}`

**权限：** COACH

**路径参数：**

| 参数 | 类型 | 说明 |
|------|------|------|
| id | Long | 健身记录ID |

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| advice | String | 是 | 健身建议内容 |

---

## 意见反馈

### 分页查询反馈

**GET** `/feedback/page`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| memberId | Long | 否 | 会员ID |
| status | Integer | 否 | 状态：0-未处理 1-已处理 |

---

### 获取我的反馈

**GET** `/feedback/my`

**权限：** MEMBER

---

### 提交反馈

**POST** `/feedback`

**权限：** MEMBER

**请求参数：**

```json
{
  "title": "建议增加早间课程",
  "content": "希望能在早上7点开设瑜伽课程",
  "type": 1
}
```

| 字段 | 类型 | 必填 | 说明 |
|------|------|------|------|
| title | String | 是 | 标题 |
| content | String | 是 | 内容 |
| type | Integer | 否 | 类型：1-建议 2-投诉 3-其他 |

---

### 回复反馈

**PUT** `/feedback/reply/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| reply | String | 是 | 回复内容 |

---

## 器材管理

### 分页查询器材

**GET** `/equipment/page`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| name | String | 否 | 器材名称 |
| type | String | 否 | 器材类型 |
| status | Integer | 否 | 状态：0-维护中 1-正常 |

---

### 获取器材详情

**GET** `/equipment/{id}`

**权限：** ADMIN

---

### 新增器材

**POST** `/equipment`

**权限：** ADMIN

**请求参数：**

```json
{
  "name": "跑步机",
  "type": "有氧器材",
  "brand": "舒华",
  "count": 10,
  "location": "有氧区",
  "purchaseDate": "2024-01-01",
  "status": 1
}
```

---

### 修改器材

**PUT** `/equipment`

**权限：** ADMIN

---

### 删除器材

**DELETE** `/equipment/{id}`

**权限：** ADMIN

---

### 记录维护

**PUT** `/equipment/maintain/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| remark | String | 否 | 维护备注 |

---

## 场地管理

### 分页查询场地

**GET** `/venue/page`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| name | String | 否 | 场地名称 |
| type | String | 否 | 场地类型 |
| status | Integer | 否 | 状态 |

---

### 获取场地详情

**GET** `/venue/{id}`

**权限：** ADMIN

---

### 新增场地

**POST** `/venue`

**权限：** ADMIN

**请求参数：**

```json
{
  "name": "瑜伽室A",
  "type": "团操室",
  "area": 100,
  "capacity": 30,
  "status": 1
}
```

---

### 修改场地

**PUT** `/venue`

**权限：** ADMIN

---

### 删除场地

**DELETE** `/venue/{id}`

**权限：** ADMIN

---

### 修改场地状态

**PUT** `/venue/status/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | Integer | 是 | 状态：0-关闭 1-开放 |

---

## 用户管理

### 分页查询用户

**GET** `/user/page`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| username | String | 否 | 用户名 |
| role | String | 否 | 角色 |

---

### 获取用户详情

**GET** `/user/{id}`

**权限：** ADMIN

---

### 新增用户

**POST** `/user`

**权限：** ADMIN

**请求参数：**

```json
{
  "username": "newuser",
  "password": "123456",
  "role": "ROLE_ADMIN",
  "nickname": "新用户"
}
```

---

### 修改用户

**PUT** `/user`

**权限：** ADMIN

---

### 删除用户

**DELETE** `/user/{id}`

**权限：** ADMIN

---

### 重置用户密码

**PUT** `/user/reset-password/{id}`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| newPassword | String | 否 | 新密码（不传则重置为默认密码） |

---

## 薪资结算

### 获取教练薪资列表

**GET** `/salary/list`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | String | 是 | 月份（yyyy-MM格式） |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": [
    {
      "coachId": 1,
      "coachName": "李教练",
      "baseSalary": 5000.00,
      "courseCount": 20,
      "studentCount": 150,
      "courseIncome": 15000.00,
      "commissionRate": "10%",
      "commission": 1500.00,
      "totalSalary": 6500.00
    }
  ]
}
```

---

### 获取薪资汇总

**GET** `/salary/summary`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| month | String | 是 | 月份（yyyy-MM格式） |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "coachCount": 5,
    "totalBaseSalary": 25000.00,
    "totalCommission": 8000.00,
    "totalSalary": 33000.00,
    "totalCourses": 100,
    "totalStudents": 800,
    "month": "2024-01"
  }
}
```

---

## 统计查询

### 获取管理员首页统计

**GET** `/statistics/admin`

**权限：** ADMIN

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "memberCount": 500,
    "coachCount": 10,
    "courseCount": 50,
    "todayIncome": 5000.00,
    "weeklyIncome": 30000.00,
    "monthlyIncome": 120000.00,
    "recentReservations": []
  }
}
```

---

### 获取教练业绩统计

**GET** `/statistics/coach/performance`

**权限：** COACH

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| startDate | String | 是 | 开始日期 |
| endDate | String | 是 | 结束日期 |

---

### 获取会员统计

**GET** `/statistics/member`

**权限：** MEMBER

**说明：** 获取当前会员的统计数据

---

### 获取系统日志

**GET** `/statistics/logs`

**权限：** ADMIN

**请求参数：**

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| current | Integer | 否 | 当前页码 |
| size | Integer | 否 | 每页条数 |
| username | String | 否 | 操作用户名 |

---

## 数据备份

### 导出数据备份

**GET** `/backup/export`

**权限：** ADMIN

**响应：** JSON文件下载

**说明：** 导出系统所有数据为JSON格式备份文件

---

### 获取备份统计信息

**GET** `/backup/stats`

**权限：** ADMIN

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "memberCount": 500,
    "coachCount": 10,
    "courseCount": 50,
    "equipmentCount": 100,
    "feedbackCount": 200,
    "consumeRecordCount": 5000
  }
}
```

---

### 验证备份文件

**POST** `/backup/validate`

**权限：** ADMIN

**请求：** multipart/form-data

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 备份JSON文件 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "membersInBackup": 10,
    "coachesInBackup": 3,
    "coursesInBackup": 15,
    "equipmentsInBackup": 20,
    "feedbacksInBackup": 5,
    "consumeRecordsInBackup": 50,
    "totalRecords": 103,
    "backupTime": "2024-01-15 10:30:00",
    "version": "1.0",
    "valid": true,
    "message": "备份文件验证成功，包含 103 条记录"
  }
}
```

**说明：** 验证备份文件格式和完整性，返回统计信息

---

### 导入数据恢复

**POST** `/backup/import`

**权限：** ADMIN

**请求：** multipart/form-data

| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| file | File | 是 | 备份JSON文件 |
| mode | String | 否 | 恢复模式：increment（增量，默认）或 overwrite（覆盖） |

**恢复模式说明：**

| 模式 | 说明 |
|------|------|
| increment | 增量导入：仅导入ID不存在的新记录，跳过已有数据（安全模式） |
| overwrite | 覆盖导入：更新已存在的记录，同时导入新记录 |

**响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mode": "增量导入",
    "imported": {
      "members": 5,
      "coaches": 2,
      "courses": 8,
      "equipments": 10,
      "feedbacks": 3,
      "consumeRecords": 20
    },
    "skipped": {
      "members": 5,
      "coaches": 1,
      "courses": 7,
      "equipments": 10,
      "feedbacks": 2,
      "consumeRecords": 30
    },
    "totalImported": 48,
    "totalSkipped": 55,
    "success": true,
    "message": "数据恢复完成，共导入 48 条记录，跳过 55 条记录"
  }
}
```

**错误响应示例：**

```json
{
  "code": 200,
  "message": "success",
  "data": {
    "mode": "增量导入",
    "imported": { "members": 3 },
    "skipped": { "members": 2 },
    "totalImported": 3,
    "totalSkipped": 2,
    "success": false,
    "errors": {
      "coaches": ["教练导入失败: 外键约束错误"]
    },
    "message": "数据恢复完成，共导入 3 条记录，跳过 2 条记录"
  }
}
```

**说明：** 从备份文件恢复数据，支持增量和覆盖两种模式。恢复操作使用事务管理，失败时自动回滚。

---

## 错误码说明

| 错误码 | 说明 |
|--------|------|
| 200 | 操作成功 |
| 400 | 请求参数错误 |
| 401 | 未登录或Token过期 |
| 403 | 无访问权限 |
| 404 | 资源不存在 |
| 500 | 服务器内部错误 |

---

## 附录

### 会员等级

| 值 | 说明 |
|------|------|
| 1 | 普通会员 |
| 2 | 银卡会员 |
| 3 | 金卡会员 |
| 4 | 钻石会员 |

### 消费类型

| 值 | 说明 |
|------|------|
| 1 | 充值 |
| 2 | 消费 |
| 3 | 退款 |

### 预约状态

| 值 | 说明 |
|------|------|
| 0 | 已取消 |
| 1 | 待签到 |
| 2 | 已签到 |
| 3 | 已完成 |

### 请假/调课审核状态

| 值 | 说明 |
|------|------|
| 0 | 待审核 |
| 1 | 已通过 |
| 2 | 已拒绝 |
