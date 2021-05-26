import Home from './components/Home.vue';
import Login from './components/Login.vue';
import Signup from './components/Signup.vue';
import Error from './components/Error.vue';

export const routes = [
  {
    path: '/login',
    component: Login
  },
  {
    path: '/signup',
    component: Signup
  },
  {
    path: '/error',
    component: Error
  },
  {
    path: '/',
    component: Home
  }
];