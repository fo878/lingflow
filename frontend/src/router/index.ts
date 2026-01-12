import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/process'
  },
  {
    path: '/process',
    name: 'Process',
    component: () => import('@/views/process/Index.vue')
  },
  {
    path: '/process/designer',
    name: 'Designer',
    component: () => import('@/views/process/Designer.vue')
  },

  {
    path: '/task',
    name: 'Task',
    component: () => import('@/views/task/Task.vue')
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/views/monitor/Index.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
