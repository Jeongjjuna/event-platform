<template>
  <header
    class="fixed top-0 left-0 z-50 flex h-16 w-full items-center justify-between border-b border-gray-200 bg-white px-6"
  >
    <div class="flex items-center">
      <img src="/assets/icons/header-image.svg" alt="" class="h-10" />
      <NuxtLink to="/" class="ml-2 text-2xl font-semibold tracking-tight"> Event Web Header </NuxtLink>
    </div>

    <!-- 로그인 상태 -->
    <div v-if="authStore.isLogin" class="flex items-center gap-3">
      <span class="text-sm text-gray-700"> {{ authStore.email }}님 로그인 중 </span>

      <button
        type="button"
        class="rounded-md border px-3 py-1 text-sm transition hover:bg-gray-100"
        @click="handleLogout"
      >
        로그아웃
      </button>
    </div>

    <!-- 비로그인 상태 -->
    <button v-else type="button" class="overflow-hidden rounded-full transition hover:opacity-80">
      <img :src="profileImage" alt="프로필" class="h-10 w-10 rounded-full object-cover" />
    </button>
  </header>
</template>

<script setup lang="ts">
import profileImage from "~/assets/icons/profile-image.png";
import { logoutAPI } from "~/services/auth.service";

const { handleApiError } = useApiError();

const authStore = useAuthStore();

const router = useRouter();

const handleLogout = async () => {
  try {
    await logoutAPI();
    authStore.logout();
    alert("로그아웃에 성공했습니다.");
    await router.push("/");
  } catch (error) {
    handleApiError(error);
  }
};
</script>
