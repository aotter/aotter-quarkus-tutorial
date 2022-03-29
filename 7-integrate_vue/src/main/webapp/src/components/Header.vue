<template>
  <nav class="navbar navbar-expand-md fixed-top navbar-dark bg-dark">
    <div class="container-fluid">
      <router-link to="/" class="navbar-brand" style="color: whitesmoke; text-decoration: none">BLOG</router-link>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarCollapse">
        <ul class="navbar-nav me-auto mb-2 mb-md-0">
          <li v-if="isLogin === true" class="nav-item">
            <div class="nav-link active" aria-current="page">
              <router-link to="/back-stage" style="color: whitesmoke; text-decoration: none">個人頁</router-link>
            </div>
          </li>
          <li class="nav-item">
            <a class="nav-link active" aria-current="page" href="/article-list">文章列</a>
          </li>
        </ul>
      </div>
      <a class="btn btn-outline-primary mr-2" v-if="isLogin === false" href="/rest/login">登入</a>
      <router-link to="/signup" class="btn btn-outline-primary mr-2" style="text-decoration: none">註冊</router-link>
      <a class="btn btn-outline-primary mr-2" v-if="isLogin === true" @click="logout">登出</a>
    </div>
  </nav>
</template>
<script>
  export default {
    props:['isLogin'],
    methods:{
      logout(){
        fetch('/api/rest/user/logout')
            .then(res => {
              if(res.ok)
                // this.isLogin = false
              // this.$router.push('/rest/login');
              window.location.href = '/rest/login'
            })
      },
    }
  }
</script>
<style>
/*#header{*/
/*  position: absolute;*/
/*  top:0px*/
/*}*/

</style>