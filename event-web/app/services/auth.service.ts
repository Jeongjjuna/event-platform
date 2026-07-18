import type { SuccessResponse } from "~/types/api";
import { useApi } from "~/composables/useApi";

// 회원가입
export interface SignUpRequest {
  email: string;
  password: string;
}

export const signUpAPI = (request: SignUpRequest) => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/signup", {
    method: "POST",
    body: request,
  });
};

// 로그인
export interface LoginRequest {
  email: string;
  password: string;
}

export interface LoginResponse {
  accessToken: string;
  refreshToken: string;
  user: LoginUserResponse;
}

export interface LoginUserResponse {
  id: number;
  email: string;
  roles: string;
  registeredAt: string;
}

export const loginAPI = (request: LoginRequest) => {
  const api = useApi();
  return api<SuccessResponse<LoginResponse>>("/api/v1/users/login", {
    method: "POST",
    body: request,
  });
};

// 토큰 재발급
export interface RefreshTokenRequest {
  refreshToken: string;
}

export interface RefreshResponse {
  accessToken: string;
  refreshToken: string;
}

export const refreshTokenAPI = (request: RefreshTokenRequest) => {
  const api = useApi();
  return api<SuccessResponse<RefreshResponse>>("/api/v1/users/refresh", {
    method: "POST",
    body: request,
  });
};

// 내 정보 조회
export interface MeResponse {
  userId: number;
  email: string;
  role: string;
  registeredAt: string;
}

export const getMeAPI = (accessToken: string) => {
  const api = useApi();
  return api<SuccessResponse<MeResponse>>("/api/v1/users/me", {
    method: "GET",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
};

// 비밀번호 변경
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const changePasswordAPI = (accessToken: string, request: ChangePasswordRequest) => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/me/password", {
    method: "PATCH",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
    body: request,
  });
};

// 회원 탈퇴
export const withdrawAPI = (accessToken: string) => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/me", {
    method: "DELETE",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
};

// 로그아웃
export const logoutAPI = (accessToken: string) => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/logout", {
    method: "POST",
    headers: {
      Authorization: `Bearer ${accessToken}`,
    },
  });
};
