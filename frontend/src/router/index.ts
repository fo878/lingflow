import { createRouter, createWebHistory, RouteRecordRaw } from 'vue-router'

const routes: Array<RouteRecordRaw> = [
  {
    path: '/',
    redirect: '/designer'
  },
  {
    path: '/designer',
    name: 'Designer',
    component: () => import('@/views/Designer.vue')
  },
  {
    path: '/process',
    name: 'Process',
    component: () => import('@/views/Process.vue')
  },
  {
    path: '/task',
    name: 'Task',
    component: () => import('@/views/Task.vue')
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/views/Monitor.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
