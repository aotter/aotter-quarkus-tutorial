<template>
    <header>
        <b-navbar toggleable="md" type="dark" variant="dark">
        <b-navbar-brand href="/">首頁</b-navbar-brand>

        <b-navbar-toggle target="nav-collapse"></b-navbar-toggle>

        <b-collapse id="nav-collapse" is-nav>
            <b-navbar-nav>
            <b-nav-item to="/">文章管理</b-nav-item>
            </b-navbar-nav>

            <!-- Right aligned nav items -->
            <b-navbar-nav class="ml-auto">
            <b-nav-item href="/logout">登出</b-nav-item>
            </b-navbar-nav>
        </b-collapse>
        </b-navbar>
    </header>
</template>
<script>

import Vue from 'vue'
import { NavbarPlugin } from 'bootstrap-vue'
Vue.use(NavbarPlugin)

export default {
    name: 'NavbarHeader'
}
</script>