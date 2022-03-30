<template>
  <div class="container-fluid w-100">
    <Header :isLogin="isLogin" @logout="logout"></Header>
    <div class="overflow-auto" style="height: calc(100vh - 56px); margin-top: 56px;">
      <router-view :isLogin="isLogin"></router-view>
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
      isLogin: false
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
  mounted(){
    if(document.cookie.indexOf('quarkus-credential') !== -1){
      this.isLogin = true
      // this.$router.push('/')
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
