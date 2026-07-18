import type { ErrorResponse } from "~/types/api";

const systemError = (message: string): ErrorResponse => ({
  code: 500,
  message,
  details: null,
  timestamp: new Date().toISOString(),
});

export const useApi = () => {
  return $fetch.create({
    // baseURL: useRuntimeConfig().public.apiBase,
    baseURL: "http://localhost:8080",

    onRequestError({ error }) {
      throw systemError("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    },

    onResponseError({ response }) {
      if (response.headers.get("X-Service-Name")) {
        throw response._data as ErrorResponse;
      }
      throw systemError("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
    },
  });
};
