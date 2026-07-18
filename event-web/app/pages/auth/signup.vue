<template>
  <section class="min-h-[calc(100vh-64px)] flex items-center justify-center">
    <div class="w-full max-w-md rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <div class="mb-6">
        <h1 class="text-3xl font-bold tracking-tight">회원가입</h1>

        <p class="mt-2 text-sm text-gray-500">Event Web 서비스를 이용하기 위한 계정을 생성합니다.</p>
      </div>

      <form class="space-y-6" @submit.prevent="handleSignup">
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
          회원가입
        </button>
      </form>
    </div>
  </section>
</template>

<script lang="ts" setup>
const form = reactive({
  email: "",
  password: "",
});

const handleSignup = () => {
  console.log({
    email: form.email,
    password: form.password,
  });
};
</script>
