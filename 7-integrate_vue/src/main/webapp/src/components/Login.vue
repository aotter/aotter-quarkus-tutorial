<template>
  <div>
    <h1>Login Page</h1>
    <form action="j_security_check" method="POST">
      <label>User Name : </label>
      <input type="text" name="j_username" required>
      <br>
      <label>Password : </label>
      <input type="password" name="j_password" required>
      <br>
      <button>Log In</button>
    </form>
  </div>
</template>