<template>
  <section class="min-h-[calc(100vh-64px)] flex items-center justify-center">
    <div class="w-full max-w-md rounded-lg border border-gray-200 bg-white p-6 shadow-sm">
      <div class="mb-6">
        <h1 class="text-3xl font-bold tracking-tight">로그인</h1>
        <p class="mt-2 text-sm text-gray-500">Event Web 서비스에 로그인합니다.</p>
      </div>
      <form class="space-y-6">
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
          @click="handleLogin"
          type="button"
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
import { loginAPI } from "~/services/auth.service";

// Nuxt composable: 공통 에러 처리 로직
const { handleApiError } = useApiError();

// Nuxt composable: 페이지 이동 제어
const router = useRouter();

// Pinia store: 로그인 상태 관리
const authStore = useAuthStore();

// Vue reactive: 객체 상태를 반응형으로 관리
const form = reactive({
  email: "",
  password: "",
});

const handleLogin = async () => {
  try {
    const response = await loginAPI({
      email: form.email,
      password: form.password,
    });

    // pinia 에 로그인 상태 저장.
    authStore.login(
      response.data.accessToken,
      response.data.user.id,
      response.data.user.email,
      response.data.user.roles,
      response.data.user.registeredAt,
    );

    alert("로그인에 성공했습니다.");
    await router.push("/");
  } catch (error) {
    handleApiError(error);
  }
};
</script>
