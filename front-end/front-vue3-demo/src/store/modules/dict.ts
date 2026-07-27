import { defineStore } from 'pinia'
import { ref } from 'vue'

interface DictionaryItem {
  key: string
  value: any
}

export const useDictStore = defineStore('dict', () => {
  // 存储字典项数组
  const dictList = ref<DictionaryItem[]>([])

  /**
   * 根据键获取字典值
   * @param key - 字典键
   * @returns 对应的值，未找到返回 null
   */
  function getDict(key: string): any | null {
    const item = dictList.value.find(item => item.key === key)
    return item ? item.value : null
  }

  /**
   * 设置或更新一个字典项
   * @param key - 键（非空字符串）
   * @param value - 值
   */
  function setDict(key: string, value: any): void {
    if (!key) return
    const existing = dictList.value.find(item => item.key === key)
    if (existing) {
      existing.value = value
    } else {
      dictList.value.push({ key, value })
    }
  }

  /**
   * 移除指定键的字典项
   * @param key - 键
   * @returns 是否成功移除
   */
  function removeDict(key: string): boolean {
    const index = dictList.value.findIndex(item => item.key === key)
    if (index !== -1) {
      dictList.value.splice(index, 1)
      return true
    }
    return false
  }

  /** 清空所有字典项 */
  function clearDict(): void {
    dictList.value = []
  }

  return { dictList, getDict, setDict, removeDict, clearDict }
})
