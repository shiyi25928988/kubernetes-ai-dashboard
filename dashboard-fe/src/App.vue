<template>
  <el-config-provider :locale="elementLocale">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { ElConfigProvider } from 'element-plus'
import zhCn from 'element-plus/es/locale/lang/zh-cn'
import en from 'element-plus/es/locale/lang/en'
import i18n from '@/i18n'

// Element Plus 内置文案随当前语言切换
const elementLocale = computed(() =>
  i18n.global.locale.value === 'zh-CN' ? zhCn : en
)

const route = useRoute()
// 页面标题随路由与语言变化统一设置
watch(
  () => [i18n.global.locale.value, route.meta.title],
  () => {
    const key = route.meta.title || 'route.default'
    document.title = i18n.global.t(key)
  },
  { immediate: true }
)
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

html, body, #app {
  width: 100%;
  height: 100%;
  overflow: hidden;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>
