// import Vue from 'vue';
// import VueRouter from 'vue-router';
// import App from './App.vue';
// import { routes } from './routes' //路由規則
//
// Vue.use(VueRouter);
//
// const router = new VueRouter({
//   routes
// });
//
// new Vue({
//   el: '#app',
//   router,
//   render: h => h(App)
// });

import { createApp } from 'vue'
import App from '@/App.vue'
import router from '@/routes'

import 'bootstrap'
import 'bootstrap/dist/css/bootstrap.min.css'
import 'blueimp-file-upload/css/jquery.fileupload-ui.css'
import 'blueimp-file-upload/css/jquery.fileupload.css'
import 'blueimp-file-upload/js/jquery.fileupload.js'
import 'jquery-ui/ui/widgets/datepicker.js'
import 'jquery-ui/ui/widget.js'

createApp(App)
    .use(router)
    .mount('#app')