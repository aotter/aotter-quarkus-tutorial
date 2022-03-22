<template>
<!--  <div>-->
<!--    <router-link to="/signup" v-if="!isLogin">| Signup |</router-link>-->
<!--    <router-link to="/login" v-if="!isLogin"> Login |</router-link>-->
<!--    <a href="" @click.prevent="$emit('logout')" v-if="isLogin"> Logout |</a>-->
<!--  </div>-->
  <div id="header"  class="d-flex flex-column flex-md-row align-items-center p-3 px-md-4 mb-3 bg-white border-bottom shadow-sm">
    <h5 class="my-0 mr-md-auto font-weight-normal"><a class="text-dark" style="text-decoration: none" href="/article-list">POST</a></h5>
    <a class="btn btn-outline-primary mr-2" href="/index.html#/login">Sign in</a>
    <a class="btn btn-outline-primary ml-1" href="/index.html#/signup">Sign up</a>
  </div>
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