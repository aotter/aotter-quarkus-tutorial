<template>
    <div>
        <h1>Signup Page</h1>
        <form>
        <label>User Name : </label>
            <input type="text" name="username" required v-model="username">
            <br>
            <label>Password : </label>
            <input type="password" name="password" required v-model="password"> 
            <br>
            <button @click.prevent="signUp">Sign up</button>            
        </form>
        <div>{{ errMsg }}</div>
    </div>
</template>
<script>
export default {
    data(){
        return {
            username: '',
            password: '',
            errMsg: ''
        }
    },
    methods: {
        signUp() {
            const {username, password} = this
            fetch('/rest/signUp',{
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({username, password})
            }).then(res => {
                if(res.ok){
                    alert('註冊成功')
                    this.$router.push('/login');
                }else{
                    this.errMsg = '註冊失敗'
                }
            })
        }
    }
}
</script>
