import Vue from 'vue'
import VueRouter from 'vue-router'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: () => import('@/views/HomeView.vue'),
    meta:{
      title: '文章管理'
    }
  },
  {
    path: '/posts/:id',
    name: 'edit-post',
    component: () => import('@/views/PostFormView.vue'),
    meta:{
      title: '編輯文章'
    }
  },
  {
    path: '/posts',
    name: 'add-post',
    component: () => import('@/views/PostFormView.vue'),
    meta:{
      title: '新增文章'
    }
  }
]

const router = new VueRouter({
  routes
})

router.beforeEach((to, from, next) => {
  if (to.meta.title) {
      document.title = `BLOG-${to.meta.title}`
  }
  next();
})

export default router
