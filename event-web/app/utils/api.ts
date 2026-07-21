import { type ErrorResponse, type SuccessResponse, SystemError } from "~/types/api";

// refresh 중복 요청을 방지하기 위한 Lock 객체(Race Condition 방지)
let refreshPromise: Promise<void> | null = null;

export const $api = async <T>(
  request: Parameters<typeof $fetch>[0],
  options?: Parameters<typeof $fetch>[1],
): Promise<T> => {

  try {
    return await fetchInstance<T>(request, options);
  } catch (error: any) {
    if (error.response?.status !== 401) {
      if (error.response.headers.get("X-Service-Name")) {
        throw error.response._data as ErrorResponse;
      }
      throw SystemError("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    }

    // Refresh를 시도하면 안 되는 API
    const path = request.toString();
    if (
      path.includes("/api/v1/users/login") ||
      path.includes("/api/v1/users/signup") ||
      path.includes("/api/v1/users/refresh")
    ) {
      throw error.response._data as ErrorResponse;
    }

    // 1. 401 에 대해서는, refresh 재시도.
    if (!refreshPromise) {
      refreshPromise = (async () => {
        const res = await $fetch<SuccessResponse<RefreshResponse>>("/api/v1/users/refresh", {
          baseURL: "http://localhost:8080",
          method: "POST",
          credentials: "include",
        });

        const authStore = useAuthStore();
        authStore.updateAccessToken(res.data.accessToken);
      })().finally(() => {
        refreshPromise = null;
      });
    }

    // 2. 재시도 결과에 따른, 최초 API 재호출
    try {
      await refreshPromise; // 다른 요청 끝날때까지 Blocking
      console.log("success refresh token reissue");
      return await fetchInstance<T>(request, options);
    } catch (error: any) {
      console.log("fail refresh token reissue");

      const { handleApiError } = useApiError();
      handleApiError(error);

      const authStore = useAuthStore();
      authStore.logout();
      navigateTo("/auth/login");
      throw error;
    }
  }
};

const fetchInstance = $fetch.create({
  baseURL: "http://localhost:8080",
  onRequest({ options }) {
    const headers = new Headers(options.headers);
    const authStore = useAuthStore();
    if (authStore.accessToken) {
      headers.set("Authorization", `Bearer ${authStore.accessToken}`);
    }
    options.headers = headers;
  },
});

// 토큰 재발급
export interface RefreshResponse {
  accessToken: string;
  user: RefreshUserResponse;
}

export interface RefreshUserResponse {
  id: number;
  email: string;
  roles: string;
  registeredAt: string;
}

// 토큰 갱신 함수
async function doRefreshToken() {
  const authStore = useAuthStore();

  console.log("try refresh token API");
  const res = await $fetch<SuccessResponse<RefreshResponse>>("/api/v1/users/refresh", {
    baseURL: "http://localhost:8080",
    method: "POST",
    credentials: "include", // HttpOnly Cookie 전송
  });

  const data = res.data;
  authStore.updateAccessToken(data.accessToken);
}