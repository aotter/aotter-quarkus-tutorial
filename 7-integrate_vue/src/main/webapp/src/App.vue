<template>
  <div class="container-fluid w-100">
    <Header :isLogin="isLogin" :user="user" @logout="logout"></Header>
    <div class="overflow-auto" style="height: calc(100vh - 56px); margin-top: 56px;">
      <router-view :isLogin="isLogin" :user="user"></router-view>
    </div>
  </div>
</template>

<script>
import Header from './components/Header.vue'
export default {
  name: 'App',
  components: {
    Header
  },
  data: () => {
    return {
      isLogin: false,
      user: {
        username: '',
        role: ''
      }
    }
  },
  methods: {
    logout(){
        fetch('/rest/logout')
        .then(res => {
            if(res.ok) this.isLogin = false
            // this.$router.push('/login');
            location.href = "/rest/login"
        })
    },
  },
  created() {
    fetch('api/rest/user/me')
    .then(res =>{
      if (res.ok){
        return res.json()
      }else {
        location.href = "/rest/login"
      }
    }).then( user =>{
      this.user = user
    })
  },
  mounted(){
    if(document.cookie.indexOf('quarkus-credential') !== -1){
      this.isLogin = true
    } else {
      if(!["/","/index"].includes(this.$route.path)){
        location.href = "/rest/login"
      }
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  /*text-align: center;*/
  color: #2c3e50;
}
</style>
