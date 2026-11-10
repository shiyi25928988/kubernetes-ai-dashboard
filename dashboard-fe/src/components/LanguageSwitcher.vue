<template>
  <el-dropdown trigger="click" @command="handleChange">
    <span class="lang-switch" :title="currentLabel">
      <img class="lang-icon" :src="langIcon" alt="" />
      <span class="lang-label">{{ currentLabel }}</span>
    </span>
    <template #dropdown>
      <el-dropdown-menu>
        <el-dropdown-item
          v-for="l in supportedLocales"
          :key="l.value"
          :command="l.value"
          :disabled="l.value === current"
        >
          {{ l.label }}
        </el-dropdown-item>
      </el-dropdown-menu>
    </template>
  </el-dropdown>
</template>

<script setup>
import { computed } from 'vue'
import i18n, { setLocale, supportedLocales } from '@/i18n'
import langIcon from '@/assets/language.png'

const current = computed(() => i18n.global.locale.value)
const currentLabel = computed(
  () => supportedLocales.find((l) => l.value === current.value)?.label || current.value
)

const handleChange = (locale) => {
  if (locale && locale !== current.value) setLocale(locale)
}
</script>

<style scoped>
.lang-switch {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  cursor: pointer;
  color: inherit;
  outline: none;
  user-select: none;
  font-size: 14px;
}
.lang-icon {
  width: 16px;
  height: 16px;
  display: block;
  line-height: 1;
}
</style>
