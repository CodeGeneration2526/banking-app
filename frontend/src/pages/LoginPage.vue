<template>
    <form>
      <fieldset>
        <label>
          Email
          <input name="first_name" placeholder="Email Address" autocomplete="off" />
        </label>
        <label>
          Password
          <input type="password" name="password" placeholder="Your super secret password" autocomplete="off" />
        </label>
      </fieldset>

      <input type="submit" value="Login" />
    </form>
</template>

<style scoped>
form {
    max-width: 32rem;
    margin: 0 auto;
}
</style>

