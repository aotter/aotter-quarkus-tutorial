<template>
  <nav class="navbar navbar-expand-md fixed-top navbar-dark bg-dark">
    <div class="container-fluid">
      <a class="navbar-brand" href="/article-list">POST</a>
      <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarCollapse" aria-controls="navbarCollapse" aria-expanded="false" aria-label="Toggle navigation">
        <span class="navbar-toggler-icon"></span>
      </button>
      <div class="collapse navbar-collapse" id="navbarCollapse">
        <ul class="navbar-nav me-auto mb-2 mb-md-0">
          <li class="nav-item">
            <div class="nav-link active" aria-current="page">
              <router-link to="/back-stage" style="color: whitesmoke; text-decoration: none">個人頁</router-link>
            </div>
          </li>
        </ul>
      </div>
      <a class="btn btn-outline-primary mr-2" v-if="isLogin === false" href="/index.html#/login">登入</a>
      <a class="btn btn-outline-primary mr-2" href="/index.html#/signup">註冊</a>
      <a class="btn btn-outline-primary mr-2" v-if="isLogin === true" href="#">登出</a>
    </div>
  </nav>
</template>
<script>
  export default {
    props:['isLogin']
  }
</script>
<style>
/*#header{*/
/*  position: absolute;*/
/*  top:0px*/
/*}*/

</style>