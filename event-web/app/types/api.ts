export interface ApiResponse {
  code: number;
  message: string;
}

export interface SuccessResponse<T> extends ApiResponse {
  data: T | null;
}

export interface ErrorResponse extends ApiResponse {
  details: ErrorDetail[] | null;
  timestamp: string;
}

export interface ErrorDetail {
  field: string | null;
  reason: string;
}
