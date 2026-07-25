declare module 'vue-router' {
  interface RouteMeta {
    title?: string
    requiresAuth?: boolean
    icon?: string
    roles?: string[]
  }
}
