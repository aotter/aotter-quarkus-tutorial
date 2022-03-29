import Home from './components/Home.vue';
// import Login from './components/Login.vue';
import Signup from './components/Signup.vue';
import Error from './components/Error.vue';
import BackStage from './components/BackStage.vue';
import ArticleEdit from './components/ArticleEdit.vue';

import {
  createRouter,
  // createWebHistory,
  createWebHashHistory
} from 'vue-router'

export const routes = [
  // {
  //   path: '/login',
  //   component: Login
  // },
  {
    path: '/signup',
    component: Signup
  },
  {
    path: '/error',
    component: Error
  },
  {
    path: '/back-stage',
    component: BackStage
  },
  {
    path: '/index/back-stage',
    component: BackStage
  },
  {
    path: '/article-edit',
    component: ArticleEdit
  },
  {
    path: '/',
    component: Home
  },
  {
    path: '/index',
    component: Home
  }
];

const router = createRouter({
  // history: createWebHistory(process.env.BASE_URL),
  history: createWebHashHistory(),
  routes
})

export default router