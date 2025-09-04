import { createRouter, createWebHistory } from 'vue-router'
import HelloView from '../views/HelloView.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'hello',
      component: HelloView,
    },
    {
      path: '/home',
      name: 'home',
      component: () => import('../views/HomeView.vue'),
    },
    {
      path: '/about',
      name: 'about',
      component: () => import('../views/AboutView.vue'),
    },
    // 项目管理路由
    {
      path: '/projects',
      name: 'project-list',
      component: () => import('../views/ProjectListView.vue'),
    },
    {
      path: '/projects/create',
      name: 'project-create',
      component: () => import('../views/ProjectFormView.vue'),
    },
    {
      path: '/projects/:id',
      name: 'project-detail',
      component: () => import('../views/ProjectDetailView.vue'),
    },
    {
      path: '/projects/:id/edit',
      name: 'project-edit',
      component: () => import('../views/ProjectFormView.vue'),
    },
    {
      path: '/projects/:id/calculate',
      name: 'project-calculate',
      component: () => import('../views/NesmaCalculateView.vue'),
    },
  ],
})

export default router
