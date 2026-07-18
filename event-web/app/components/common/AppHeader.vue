<template>
  <header
    class="fixed top-0 left-0 z-50 flex h-16 w-full items-center justify-between border-b border-gray-200 bg-white px-6"
  >
    <div class="flex items-center">
      <img src="/assets/icons/header-image.svg" alt="" class="h-10" />
      <NuxtLink to="/" class="ml-2 text-2xl font-semibold tracking-tight"> Event Web Header </NuxtLink>
    </div>
    <button type="button" class="overflow-hidden rounded-full transition hover:opacity-80">
      <img :src="profileImage" alt="프로필" class="h-10 w-10 rounded-full object-cover" />
    </button>
  </header>
</template>

<script setup lang="ts">
import profileImage from "~/assets/icons/profile-image.png";
</script>
