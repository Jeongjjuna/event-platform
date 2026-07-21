export interface ApiResponse {
  code: number;
  message: string;
}

export interface SuccessResponse<T> extends ApiResponse {
  data: T;
}

export interface ErrorResponse extends ApiResponse {
  details: ErrorDetail[] | null;
  timestamp: string;
}

export interface ErrorDetail {
  field: string;
  reason: string;
}

export const SystemError = (message: string): ErrorResponse => ({
  code: 500,
  message,
  details: null,
  timestamp: new Date().toISOString(),
});
