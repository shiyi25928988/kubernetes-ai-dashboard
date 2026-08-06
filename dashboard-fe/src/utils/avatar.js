import { createAvatar } from '@dicebear/core'
import { bottts } from '@dicebear/collection'

/**
 * 生成随机 dicebear 头像（bottts 机器人风格），返回 data URI。
 * 头像在前端本地生成，不再依赖后端数据库。
 * @param {string} [seed] - 可选种子；不传则随机生成
 * @returns {string} data:image/svg+xml 形式的 URI，可直接用于 <img :src>
 */
export function generateRandomAvatar(seed) {
  const s = seed || Math.random().toString(36).slice(2, 10)
  const svg = createAvatar(bottts, { seed: s }).toString()
  return `data:image/svg+xml;utf8,${encodeURIComponent(svg)}`
}
