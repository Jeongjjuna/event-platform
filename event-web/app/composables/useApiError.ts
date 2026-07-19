import type { ErrorResponse } from "~/types/api";

export const useApiError = () => ({
  handleApiError(error: unknown) {
    const errorResponse = error as ErrorResponse;

    alert(`${errorResponse.code} : ${errorResponse.message}`);

    if (errorResponse.details != null) {
      errorResponse.details.forEach((detail) => {
        console.log(`${detail.field} : ${detail.reason}`);
      });
    }
  },
});
