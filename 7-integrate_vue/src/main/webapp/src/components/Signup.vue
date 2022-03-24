<template>
  <div class="form-signin text-center">
    <img src="../assets/social-media.png" style="width: 80%; height: 80%">
    <h1 class="h3 mb-3 font-weight-normal">建立帳號</h1>

    <div class="input-group ml-4"
         :class="isAvailableUserName == false ?'' : 'mb-4'"
         :style="isAvailableUserName == null?'width: 85%':''">
      <label for="inputUserName" class="sr-only">使用者名稱</label>
      <input type="text" v-model="username" id="inputUserName"
             class="form-control" placeholder="使用者名稱" required autofocus>
      <div class="input-group-append"
           :style="isAvailableUserName? 'color:green' : 'color:red'">
        <i v-if="isAvailableUserName == true" class="far fa-check-circle icon"></i>
        <i v-if="isAvailableUserName == false" class="far fa-times-circle icon"></i>
      </div>
    </div>
    <div v-if="isAvailableUserName == false" style="color: red;"> {{ errMsg }}</div>
    <label for="inputPassword" class="sr-only">密碼</label>
    <input type="text" v-model="password" id="inputPassword" class="form-control mb-3 ml-4" style="width: 85%;" placeholder="密碼" required>

    <button class="btn btn-lg btn-primary btn-block ml-4" style="width: 85%;" @click="signUp">註冊</button>

    <button class="btn btn-lg btn-primary btn-block ml-4 mt-4" style="width: 85%;">
      <router-link to="/login" style="color: white; text-decoration: none; ">登入</router-link>
    </button>
  </div>
</template>
<script>
export default {
    data(){
        return {
          username: '',
          password: '',
          errMsg: '',
          isAvailableUserName: null,

        }
    },
    methods: {
        signUp() {
          if(this.username.trim().length === 0) {
            alert("名稱不能為空");
            return;
          }
          if(this.password.trim().length === 0) {
            alert("密碼不能為空");
            return
          }
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
                  this.isAvailableUserName = false;
                  this.errMsg = '此名稱已註冊過'
                }
            })
        }
    }
}
</script>
<style>
html,
body {
  height: 100%;
}

body {
  display: -ms-flexbox;
  display: flex;
  -ms-flex-align: center;
  align-items: center;
  padding-top: 40px;
  padding-bottom: 40px;
  background-color: #f5f5f5;
}

.form-signin {
  width: 100%;
  max-width: 330px;
  padding: 15px;
  margin: auto;
}
.form-signin .checkbox {
  font-weight: 400;
}
.form-signin .form-control {
  position: relative;
  box-sizing: border-box;
  height: auto;
  padding: 10px;
  font-size: 16px;
}
.form-signin .form-control:focus {
  z-index: 2;
}
.form-signin input[type="email"] {
  margin-bottom: -1px;
  border-bottom-right-radius: 0;
  border-bottom-left-radius: 0;
}
.form-signin input[type="password"] {
  margin-bottom: 10px;
  border-top-left-radius: 0;
  border-top-right-radius: 0;
}

.icon{
  font-size: 30px; margin: 7px 5px; padding-left: 5px
}
</style>
