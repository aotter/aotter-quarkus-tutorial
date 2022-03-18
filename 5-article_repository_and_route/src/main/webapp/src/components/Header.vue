<template>
  <div>
    <router-link to="/signup" v-if="!isLogin">| Signup |</router-link>
    <router-link to="/login" v-if="!isLogin"> Login |</router-link>
    <a href="" @click.prevent="$emit('logout')" v-if="isLogin"> Logout |</a>
  </div>
</template>
<script>
  export default {
    props:['isLogin']
  }
</script>