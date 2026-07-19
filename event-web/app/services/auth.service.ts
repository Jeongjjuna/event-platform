import type { SuccessResponse } from "~/types/api";

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
    body: request
  });
};

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

export const refreshTokenAPI = () => {
  const api = useApi();

  return api<SuccessResponse<RefreshResponse>>("/api/v1/users/refresh", {
    method: "POST"
  });
};

// 내 정보 조회
export interface MeResponse {
  userId: number;
  email: string;
  role: string;
  registeredAt: string;
}

export const getMeAPI = () => {
  const api = useApi();
  return api<SuccessResponse<MeResponse>>("/api/v1/users/me", {
    method: "GET"
  });
};

// 비밀번호 변경
export interface ChangePasswordRequest {
  currentPassword: string;
  newPassword: string;
}

export const changePasswordAPI = (request: ChangePasswordRequest) => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/me/password", {
    method: "PATCH",
    body: request,
  });
};

// 회원 탈퇴
export const withdrawAPI = () => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/me", {
    method: "DELETE"
  });
};

// 로그아웃
export const logoutAPI = () => {
  const api = useApi();
  return api<SuccessResponse<null>>("/api/v1/users/logout", {
    method: "POST"
  });
};
