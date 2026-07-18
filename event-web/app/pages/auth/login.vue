<template>
  <section class="min-h-[calc(100vh-64px)] flex items-center justify-center">
    <div class="w-full max-w-md rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <div class="mb-6">
        <h1 class="text-3xl font-bold tracking-tight">로그인</h1>
        <p class="mt-2 text-sm text-gray-500">Event Web 서비스에 로그인합니다.</p>
      </div>
      <form class="space-y-6" @submit.prevent="handleLogin">
        <div class="space-y-2">
          <label class="text-sm font-medium" for="email"> 이메일 </label>
          <input
            id="email"
            v-model="form.email"
            class="h-10 w-full rounded-md border border-gray-300 px-3 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            placeholder="이메일을 입력해주세요"
            required
            type="email"
          />
        </div>
        <div class="space-y-2">
          <label class="text-sm font-medium" for="password"> 비밀번호 </label>
          <input
            id="password"
            v-model="form.password"
            class="h-10 w-full rounded-md border border-gray-300 px-3 text-sm outline-none focus:border-blue-500 focus:ring-2 focus:ring-blue-500/20"
            placeholder="비밀번호를 입력해주세요"
            required
            type="password"
          />
        </div>
        <button
          class="h-10 w-full rounded-md bg-black text-sm font-medium text-white transition hover:bg-gray-800"
          type="submit"
        >
          로그인
        </button>
      </form>
      <div class="mt-6 text-center text-sm text-gray-500">
        아직 회원이 아니신가요?
        <NuxtLink class="ml-1 font-medium text-black hover:underline" to="/auth/signup"> 회원가입 </NuxtLink>
      </div>
    </div>
  </section>
</template>

<script lang="ts" setup>
const form = reactive({
  email: "",
  password: "",
});

const handleLogin = () => {
  console.log({
    email: form.email,
    password: form.password,
  });
};
</script>
