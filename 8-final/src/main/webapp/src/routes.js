import Error from './components/Error.vue';
import BackStage from './components/BackStage.vue';
import ArticleEdit from './components/ArticleEdit.vue';

import {
  createRouter,
  createWebHashHistory
} from 'vue-router'

export const routes = [
  {
    path: '/error',
    component: Error
  },
  {
    path: '/back-stage',
    component: BackStage
  },
  {
    path: '/article-edit',
    component: ArticleEdit
  },
  {
    path: '/',
    component: BackStage
  }
];

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

export default router