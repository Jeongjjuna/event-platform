import { defineStore } from "pinia";
import { refreshTokenAPI } from "~/services/auth.service";

export const useAuthStore = defineStore("auth", () => {
  const accessToken = ref<string | null>(null);
  const userId = ref<number | null>(null);
  const email = ref<string | null>(null);
  const roles = ref<string | null>(null);
  const registeredAt = ref<string | null>(null);

  // computed - 다른 상태를 기반으로 자동 계산되는 읽기 전용 값: accessToken 값이 변경되면 isLogin 값도 변경됨
  const isLogin = computed(() => {
    return accessToken.value !== null;
  });

  const login = (
    newAccessToken: string,
    newUserId: number,
    newEmail: string,
    newRoles: string,
    newRegisteredAt: string,
  ) => {
    accessToken.value = newAccessToken;
    userId.value = newUserId;
    email.value = newEmail;
    roles.value = newRoles;
    registeredAt.value = newRegisteredAt;
  };

  const refresh = async () => {
    const response = await refreshTokenAPI();

    login(
      response.data.accessToken,
      response.data.user.id,
      response.data.user.email,
      response.data.user.roles,
      response.data.user.registeredAt,
    );
  };

  const logout = () => {
    accessToken.value = null;
    userId.value = null;
    email.value = null;
    roles.value = null;
    registeredAt.value = null;
  };

  return {
    accessToken,

    userId,
    email,
    roles,
    registeredAt,

    isLogin,

    login,
    refresh,
    logout,
  };
});
