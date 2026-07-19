export default defineNuxtPlugin(async () => {
  const authStore = useAuthStore();

  try {
    await authStore.refresh();
  } catch {
    authStore.logout();
  }
});
