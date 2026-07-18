export interface SignUpRequest {
  email: string;
  password: string;
}

export const signUpAPI = (request: SignUpRequest) => {
  return $fetch("http://localhost:8080/api/v1/users/signup", {
    method: "POST",
    body: request,
  });
};