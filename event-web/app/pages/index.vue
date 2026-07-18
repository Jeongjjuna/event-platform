<script lang="ts" setup></script>
<template>
  <main class="min-h-full flex flex-col items-center justify-center gap-6">
    <p class="m-16 text-gray-500">이벤트 관리 서비스입니다.</p>

    <div class="flex gap-3">
      <NuxtLink class="rounded-md border px-4 py-2 text-sm font-medium hover:bg-gray-100 transition" to="/auth/login">
        로그인
      </NuxtLink>

      <NuxtLink class="rounded-md border px-4 py-2 text-sm font-medium hover:bg-gray-100 transition" to="/auth/signup">
        회원가입
      </NuxtLink>
    </div>
  </main>
</template>
