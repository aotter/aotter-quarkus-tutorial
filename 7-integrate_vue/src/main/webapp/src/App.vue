<template>
  <div id="app" class="w-100">
    <Header isLogin="isLogin" @logout="logout"></Header>
    <router-view :isLogin="isLogin"></router-view>
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
            this.$router.push('/login');
        })
    },
  },
  mounted(){
    if(document.cookie.indexOf('quarkus-credential') !== -1){
      this.isLogin = true
    }
  }
}
</script>

<style>
#app {
  font-family: Avenir, Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  /*position: relative;*/
}
</style>
