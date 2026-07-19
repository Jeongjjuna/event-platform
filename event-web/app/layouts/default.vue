<template>
  <div class="min-h-screen flex flex-col">
    <AppHeader />
    <main class="flex-1 pt-16 border-4 border-blue-500">
      <slot />
    </main>
    <AppFooter />
  </div>
</template>

<script lang="ts" setup>
import AppHeader from "~/components/common/AppHeader.vue";
import AppFooter from "~/components/common/AppFooter.vue";
</script>
