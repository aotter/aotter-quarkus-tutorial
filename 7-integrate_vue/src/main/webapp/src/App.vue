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
        fetch('/logout')
        .then(res => {
            if(res.ok) this.isLogin = false
            // this.$router.push('/login');
            location.href = "/login"
        })
    },
  },
  created() {
    fetch('/api/user/me')
    .then(res =>{
      if (res.ok){
        return res.json()
      }else {
        location.href = "/login"
      }
    }).then( user =>{
      this.user = user
    })
  },
  mounted(){
    if(document.cookie.indexOf('quarkus-credential') !== -1){
      this.isLogin = true
    } else {
      if(!["/","/back-stage"].includes(this.$route.path)){
        location.href = "/login"
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
