import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/Login.vue'),
    meta: { title: '登录', noAuth: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/auth/Register.vue'),
    meta: { title: '注册', noAuth: true },
  },
  {
    path: '/',
    component: () => import('@/components/layout/MainLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Home',
        component: () => import('@/views/Home.vue'),
        meta: { title: '首页' },
      },
      {
        path: 'skill',
        name: 'SkillList',
        component: () => import('@/views/skill/SkillList.vue'),
        meta: { title: '技能交换' },
      },
      {
        path: 'skill/publish',
        name: 'SkillPublish',
        component: () => import('@/views/skill/SkillPublish.vue'),
        meta: { title: '发布技能' },
      },
      {
        path: 'skill/:id',
        name: 'SkillDetail',
        component: () => import('@/views/skill/SkillDetail.vue'),
        meta: { title: '技能详情' },
      },
      {
        path: 'goods',
        name: 'GoodsList',
        component: () => import('@/views/goods/GoodsList.vue'),
        meta: { title: '闲置共享' },
      },
      {
        path: 'goods/publish',
        name: 'GoodsPublish',
        component: () => import('@/views/goods/GoodsPublish.vue'),
        meta: { title: '发布物品' },
      },
      {
        path: 'goods/:id',
        name: 'GoodsDetail',
        component: () => import('@/views/goods/GoodsDetail.vue'),
        meta: { title: '物品详情' },
      },
      {
        path: 'activity',
        name: 'ActivityList',
        component: () => import('@/views/activity/ActivityList.vue'),
        meta: { title: '临时搭伴' },
      },
      {
        path: 'activity/publish',
        name: 'ActivityPublish',
        component: () => import('@/views/activity/ActivityPublish.vue'),
        meta: { title: '发布活动' },
      },
      {
        path: 'activity/:id',
        name: 'ActivityDetail',
        component: () => import('@/views/activity/ActivityDetail.vue'),
        meta: { title: '活动详情' },
      },
      {
        path: 'message',
        name: 'Message',
        component: () => import('@/views/message/MessageCenter.vue'),
        meta: { title: '消息中心' },
      },
      {
        path: 'user/profile',
        name: 'UserProfile',
        component: () => import('@/views/user/UserProfile.vue'),
        meta: { title: '个人中心' },
      },
      {
        path: 'user/:id',
        name: 'UserDetail',
        component: () => import('@/views/user/UserDetail.vue'),
        meta: { title: '用户主页' },
      },
      {
        path: 'credit',
        name: 'CreditCenter',
        component: () => import('@/views/credit/CreditCenter.vue'),
        meta: { title: '信用中心' },
      },
      {
        path: 'mutual/:id',
        name: 'MutualDetail',
        component: () => import('@/views/mutual/MutualDetail.vue'),
        meta: { title: '互助详情' },
      },
    ],
  },
  {
    path: '/admin',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { title: '后台管理', requiresAdmin: true },
    children: [
      {
        path: '',
        name: 'AdminDashboard',
        component: () => import('@/views/admin/Dashboard.vue'),
        meta: { title: '管理看板' },
      },
      {
        path: 'users',
        name: 'AdminUsers',
        component: () => import('@/views/admin/UserManage.vue'),
        meta: { title: '用户管理' },
      },
      {
        path: 'audit',
        name: 'AdminAudit',
        component: () => import('@/views/admin/ContentAudit.vue'),
        meta: { title: '内容审核' },
      },
      {
        path: 'credit',
        name: 'AdminCredit',
        component: () => import('@/views/admin/CreditManage.vue'),
        meta: { title: '信用管理' },
      },
      {
        path: 'reports',
        name: 'AdminReports',
        component: () => import('@/views/admin/ReportManage.vue'),
        meta: { title: '举报管理' },
      },
      {
        path: 'logs',
        name: 'AdminLogs',
        component: () => import('@/views/admin/OperationLog.vue'),
        meta: { title: '操作日志' },
      },
      {
        path: 'config',
        name: 'AdminConfig',
        component: () => import('@/views/admin/SystemConfig.vue'),
        meta: { title: '系统配置' },
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFound.vue'),
    meta: { title: '404', noAuth: true },
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// 路由守卫
router.beforeEach(async (to, from, next) => {
  // 设置页面标题
  document.title = `${to.meta.title || '青年同城互助平台'} - 互助青年`

  const token = localStorage.getItem('access_token')

  if (!to.meta.noAuth && !token) {
    // 需要登录但未登录，跳转登录页
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 管理员路由校验
  if (to.matched.some(record => record.meta.requiresAdmin)) {
    const userStore = useUserStore()
    // 如果还没获取用户信息，先获取
    if (!userStore.userInfo && token) {
      try {
        await userStore.fetchUserInfo()
      } catch (e) {
        // 获取失败，跳转登录
        next({ name: 'Login', query: { redirect: to.fullPath } })
        return
      }
    }
    if (userStore.userInfo?.role !== 'ADMIN') {
      // 非管理员，跳转首页
      next({ name: 'Home' })
      return
    }
  }

  next()
})

export default router
