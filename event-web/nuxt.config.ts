// https://nuxt.com/docs/api/configuration/nuxt-config
export default defineNuxtConfig({
  compatibilityDate: '2025-07-15',
  devtools: {enabled: true},
  app: {
    head: {
      charset: 'utf-8',
      viewport: 'width=device-width, initial-scale=1, maximum-scale=1',
    }
  },
  modules: ['@nuxtjs/tailwindcss', 'nuxt-lucide-icons'],

  tailwindcss: {
    // Options
  },

  lucide: {
    namePrefix: 'Icon'
  }
})