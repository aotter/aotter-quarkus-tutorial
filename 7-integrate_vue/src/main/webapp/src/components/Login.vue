<template>
  <div class="form-signin text-center">
    <img src="../assets/social-media.png" style="width: 80%; height: 80%">
    <h1 class="h3 mb-3 font-weight-normal">登入</h1>

    <form action="j_security_check" method="POST">
      <div class="input-group ml-4 mb-4" style="width: 85%">
        <label for="inputUserName" class="sr-only">使用者名稱</label>
        <input type="text" id="inputUserName" name="j_username"
               class="form-control" placeholder="帳號" required autofocus>
      </div>
      <label for="inputPassword" class="sr-only">密碼</label>
      <input type="text" id="inputPassword" name="j_password" class="form-control mb-3 ml-4" style="width: 85%;" placeholder="密碼" required>
      <button class="btn btn-lg btn-primary btn-block ml-4" style="width: 85%;">登入</button>
    </form>

    <button class="btn btn-lg btn-primary btn-block ml-4 mt-4" style="width: 85%;">
      <router-link to="/signup" style="color: white; text-decoration: none; ">註冊帳號</router-link>
    </button>
  </div>
</template>