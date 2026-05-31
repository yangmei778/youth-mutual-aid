import request from '@/utils/request'

/**
 * 认证相关 API
 */
export const authApi = {
  /** 用户名密码登录 */
  login(data) {
    return request.post('/v1/auth/login', data)
  },

  /** 注册 */
  register(data) {
    return request.post('/v1/auth/register', data)
  },

  /** 获取当前用户信息 */
  getUserInfo() {
    return request.get('/v1/auth/user-info')
  },

  /** 刷新 Token */
  refreshToken(refreshToken) {
    return request.post('/v1/auth/refresh-token', { refreshToken })
  },
}

/**
 * 技能交换 API
 */
export const skillApi = {
  /** 获取技能列表 */
  getList(params) {
    return request.get('/v1/skill/posts', { params })
  },

  /** 获取技能详情 */
  getDetail(id) {
    return request.get(`/v1/skill/posts/${id}`)
  },

  /** 发布技能 */
  publish(data) {
    return request.post('/v1/skill/posts', data)
  },

  /** 获取技能匹配推荐 */
  getRecommendations(params) {
    return request.get('/v1/skill/recommendations', { params })
  },
}

/**
 * 闲置共享 API
 */
export const goodsApi = {
  /** 获取物品列表 */
  getList(params) {
    return request.get('/v1/goods/posts', { params })
  },

  /** 获取物品详情 */
  getDetail(id) {
    return request.get(`/v1/goods/posts/${id}`)
  },

  /** 发布物品 */
  publish(data) {
    return request.post('/v1/goods/posts', data)
  },
}

/**
 * 临时搭伴 API
 */
export const activityApi = {
  /** 获取活动列表 */
  getList(params) {
    return request.get('/v1/activity/posts', { params })
  },

  /** 获取活动详情 */
  getDetail(id) {
    return request.get(`/v1/activity/posts/${id}`)
  },

  /** 发布活动 */
  publish(data) {
    return request.post('/v1/activity/posts', data)
  },

  /** 报名活动 */
  join(id, data) {
    return request.post(`/v1/activity/posts/${id}/join`, data)
  },
}

/**
 * 信用 API
 */
export const creditApi = {
  /** 获取信用分信息 */
  getCreditInfo() {
    return request.get('/v1/credit/info')
  },

  /** 获取信用变动记录 */
  getCreditLogs(params) {
    return request.get('/v1/credit/logs', { params })
  },
}

/**
 * 消息 API
 */
export const messageApi = {
  /** 获取消息列表 */
  getMessages(params) {
    return request.get('/v1/messages', { params })
  },

  /** 发送消息 */
  sendMessage(data) {
    return request.post('/v1/messages', data)
  },

  /** 获取系统通知 */
  getNotifications(params) {
    return request.get('/v1/notifications', { params })
  },

  /** 标记通知已读 */
  markAsRead(id) {
    return request.put(`/v1/notifications/${id}/read`)
  },

  /** 标记所有通知已读 */
  markAllRead() {
    return request.put('/v1/notifications/read-all')
  },

  /** 获取未读通知数 */
  getUnreadCount() {
    return request.get('/v1/notifications/unread-count')
  },
}

/**
 * 用户 API
 */
export const userApi = {
  /** 获取用户详情 */
  getUserDetail(id) {
    return request.get(`/v1/users/${id}`)
  },

  /** 更新个人资料 */
  updateProfile(data) {
    return request.put('/v1/users/profile', data)
  },

  /** 更新技能标签 */
  updateSkillTags(data) {
    return request.put('/v1/users/skill-tags', data)
  },

  /** 获取用户的技能标签 */
  getUserSkillTags(id) {
    return request.get(`/v1/users/${id}/skill-tags`)
  },

  /** 获取用户的发布 */
  getUserPosts(id, pageNum = 1, pageSize = 10) {
    return request.get(`/v1/users/${id}/posts`, { params: { pageNum, pageSize } })
  },

  /** 获取用户收到的评价 */
  getUserReviews(id, pageNum = 1, pageSize = 10) {
    return request.get(`/v1/users/${id}/reviews`, { params: { pageNum, pageSize } })
  },
}

/**
 * 文件上传 API
 */
export const uploadApi = {
  /** 上传图片 */
  uploadImage(file) {
    const formData = new FormData()
    formData.append('file', file)
    return request.post('/v1/files/upload', formData, {
      headers: { 'Content-Type': 'multipart/form-data' },
    })
  },
}

/**
 * 互助记录 API
 */
export const mutualApi = {
  /** 发起互助请求 */
  createRequest(data) {
    return request.post('/v1/mutual/requests', data)
  },

  /** 接受互助请求 */
  acceptRequest(id) {
    return request.put(`/v1/mutual/records/${id}/accept`)
  },

  /** 拒绝互助请求 */
  rejectRequest(id) {
    return request.put(`/v1/mutual/records/${id}/reject`)
  },

  /** 确认互助完成 */
  confirmComplete(id) {
    return request.put(`/v1/mutual/records/${id}/confirm`)
  },

  /** 取消互助 */
  cancelRecord(id) {
    return request.put(`/v1/mutual/records/${id}/cancel`)
  },

  /** 获取我的互助记录 */
  getMyRecords(params) {
    return request.get('/v1/mutual/records', { params })
  },

  /** 获取互助详情 */
  getRecordDetail(id) {
    return request.get(`/v1/mutual/records/${id}`)
  },
}

/**
 * 评价 API
 */
export const reviewApi = {
  /** 提交评价 */
  submitReview(data) {
    return request.post('/v1/reviews', data)
  },

  /** 查看收到的评价 */
  getReceivedReviews(params) {
    return request.get('/v1/reviews/received', { params })
  },

  /** 查看发出的评价 */
  getSentReviews(params) {
    return request.get('/v1/reviews/sent', { params })
  },
}

/**
 * 管理员 API
 */
export const adminApi = {
  /** 获取看板统计 */
  getStats() {
    return request.get('/v1/admin/dashboard/stats')
  },

  /** 获取用户列表 */
  getUsers(params) {
    return request.get('/v1/admin/users', { params })
  },

  /** 更新用户状态 */
  updateUserStatus(id, status) {
    return request.put(`/v1/admin/users/${id}/status`, null, { params: { status } })
  },

  /** 获取技能列表（管理） */
  getSkills(params) {
    return request.get('/v1/admin/content/skills', { params })
  },

  /** 下架技能 */
  offlineSkill(id) {
    return request.put(`/v1/admin/content/skills/${id}/offline`)
  },

  /** 获取物品列表（管理） */
  getGoods(params) {
    return request.get('/v1/admin/content/goods', { params })
  },

  /** 下架物品 */
  offlineGoods(id) {
    return request.put(`/v1/admin/content/goods/${id}/offline`)
  },

  /** 获取活动列表（管理） */
  getActivities(params) {
    return request.get('/v1/admin/content/activities', { params })
  },

  /** 下架活动 */
  offlineActivity(id) {
    return request.put(`/v1/admin/content/activities/${id}/offline`)
  },

  /** 调整信用分 */
  adjustCredit(userId, changeValue, reason) {
    return request.put('/v1/admin/credit/adjust', null, { params: { userId, changeValue, reason } })
  },
}
