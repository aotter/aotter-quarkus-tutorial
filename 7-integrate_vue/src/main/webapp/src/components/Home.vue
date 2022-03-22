<template>
    <div>
        <h1>Home Page</h1>
        {{ isLogin ? '已登入' : '未登入'}}
    </div>
    
</template>
<script>
export default {
    props: ['isLogin']
}
</script>
