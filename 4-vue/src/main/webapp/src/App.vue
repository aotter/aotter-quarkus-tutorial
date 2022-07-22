<template>
  <div id="app">
    <navbar-header />
    <router-view/>
  </div>
</template>
<script>
import NavbarHeader from '@/components/NavbarHeader.vue'
export default {
  name: 'App',
  components: {
    NavbarHeader
  }
}
</script>