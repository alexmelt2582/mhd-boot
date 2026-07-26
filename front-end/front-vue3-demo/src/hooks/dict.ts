import {useDictStore} from '@/store/modules/dict'
import {ref, type Ref} from 'vue'
import {getDictByType} from "@/api/admin/system/dict/api.ts";

interface DictItem {
  label: string
  value: string
  elTagType: string
  elTagClass: string
}

// -------------- 工具函数：加载单个字典，返回响应式数组 --------------
/**
 * 获取单个字典类型的选项列表
 * @param dictType - 字典类型标识
 * @returns 响应式数组 Ref<DictItem[]>
 * @example
 * const logTypeOptions = useDict('log_type')
 * // 模板中直接 v-for="item in logTypeOptions"
 */
export function useDict(dictType: string): Ref<DictItem[]> {
  const dictStore = useDictStore()
  const dictOptions = ref<DictItem[]>([])

  const cached = dictStore.getDict(dictType)
  if (cached) {
    dictOptions.value = cached as DictItem[]
  } else {
    getDictByType(dictType)
      .then((res) => {
        const items: DictItem[] = res.data.map((item: any) => ({
          label: item.label,
          value: item.value,
          elTagType: item.listClass ?? item.elTagType,
          elTagClass: item.cssClass ?? item.elTagClass,
        }))
        dictOptions.value = items
        dictStore.setDict(dictType, items)
      })
      .catch((err) => {
        console.error(`加载字典“${dictType}”失败:`, err)
      })
  }

  return dictOptions
}

// -------------- 批量加载多个字典，返回一个包含所有字段的对象 --------------
/**
 * 批量获取多个字典类型的选项列表
 * @param dictTypes - 字典类型标识列表
 * @returns 对象，属性名为字典类型，值为对应的响应式数组 Ref<DictItem[]>
 * @example
 * const { log_type, user_status } = useDicts('log_type', 'user_status')
 * // 模板中分别使用 log_type 和 user_status
 */
export function useDicts(...dictTypes: string[]): Record<string, Ref<DictItem[]>> {
  const result: Record<string, Ref<DictItem[]>> = {}
  for (const type of dictTypes) {
    result[type] = useDict(type)  // 直接复用单个加载逻辑
  }
  return result
}
