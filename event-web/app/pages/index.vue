<template>
  <div class="flex min-h-full flex-col items-center justify-center gap-6">
    <!-- 로그인 상태 -->
    <template v-if="authStore.isLogin">
      <h1 class="text-3xl font-bold text-gray-800">👋 환영합니다!</h1>
      <p class="text-gray-600">
        <span class="font-semibold">{{ authStore.email }}</span> 님이 로그인 중입니다.
      </p>
      <div class="rounded-lg border border-gray-200 bg-gray-50 px-6 py-4 text-center">
        <p class="text-sm text-gray-500">이벤트 관리 서비스를 이용해 보세요.</p>
      </div>
    </template>

    <!-- 비로그인 상태 -->
    <template v-else>
      <h1 class="text-3xl font-bold text-gray-800">Event Platform</h1>
      <p class="text-gray-500">이벤트 관리 서비스입니다.</p>
      <div class="mt-2 flex gap-3">
        <NuxtLink
          to="/auth/login"
          class="rounded-md bg-gray-900 px-5 py-2 text-sm font-medium text-white transition hover:bg-gray-800"
        >
          로그인
        </NuxtLink>
        <NuxtLink
          to="/auth/signup"
          class="rounded-md border border-gray-300 px-5 py-2 text-sm font-medium transition hover:bg-gray-100"
        >
          회원가입
        </NuxtLink>
      </div>
    </template>
  </div>
</template>

<script setup lang="ts">
const authStore = useAuthStore();
</script>
