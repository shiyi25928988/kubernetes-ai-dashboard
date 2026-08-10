import { createI18n } from 'vue-i18n'
import zhCN from './locales/zh-CN'
import enUS from './locales/en-US'

const STORAGE_KEY = 'app-lang'

export const supportedLocales = [
  { value: 'zh-CN', label: '中文' },
  { value: 'en-US', label: 'English' }
]

/** 探测初始语言：localStorage > 浏览器语言 > 默认中文 */
function detectLocale() {
  try {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) return saved
  } catch (e) {
    // localStorage 不可用时忽略
  }
  const browser = typeof navigator !== 'undefined' ? navigator.language : ''
  if (browser && browser.toLowerCase().startsWith('en')) return 'en-US'
  return 'zh-CN'
}

const i18n = createI18n({
  legacy: false,
  globalInjection: true,
  locale: detectLocale(),
  fallbackLocale: 'zh-CN',
  messages: {
    'zh-CN': zhCN,
    'en-US': enUS
  }
})

/** 设置语言并持久化 */
export function setLocale(locale) {
  i18n.global.locale.value = locale
  try {
    localStorage.setItem(STORAGE_KEY, locale)
  } catch (e) {
    // 忽略写入失败
  }
  if (typeof document !== 'undefined') {
    document.documentElement.setAttribute('lang', locale.startsWith('en') ? 'en' : 'zh-CN')
  }
}

/** 在两种语言之间切换 */
export function toggleLocale() {
  setLocale(i18n.global.locale.value === 'zh-CN' ? 'en-US' : 'zh-CN')
}

/** 当前语言用于 toLocaleString 等日期/数字格式化的 BCP47 tag */
export function dateLocale() {
  return i18n.global.locale.value === 'zh-CN' ? 'zh-CN' : 'en-US'
}

/** 当前语言是否为英文 */
export function isEnglish() {
  return i18n.global.locale.value === 'en-US'
}

export default i18n
