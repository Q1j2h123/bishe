import { ref, watch } from 'vue'
import { draftApi, type DraftSolutionRequest } from '@/api/draft'

/**
 * 自动保存选项
 */
interface AutoSaveOptions {
  // 自动保存延迟（毫秒），默认2000ms
  delay?: number
  // 保存成功回调
  onSaved?: () => void
  // 保存失败回调
  onError?: (error: any) => void
  // 是否在初始化时尝试加载保存的草稿
  loadOnInit?: boolean
  // 加载成功回调
  onLoaded?: (content: string) => void
}

/**
 * 创建自动保存功能
 * @param problemId 题目ID
 * @param type 题目类型: CHOICE, JUDGE, PROGRAM
 * @param initialContent 初始内容
 * @param options 自动保存选项
 * @returns 自动保存状态和方法
 */
export function useAutoSave(
  problemId: number,
  type: string,
  initialContent: string = '',
  options: AutoSaveOptions = {}
) {
  // 默认选项
  const defaultOptions: AutoSaveOptions = {
    delay: 2000,
    loadOnInit: true,
    onSaved: () => {},
    onError: (error) => console.error('自动保存失败:', error),
    onLoaded: () => {}
  }
  
  // 合并选项
  const mergedOptions = { ...defaultOptions, ...options }
  
  // 草稿内容
  const content = ref(initialContent)
  // 是否有未保存的更改
  const isDirty = ref(false)
  // 上次保存时间
  const lastSaved = ref<Date | null>(null)
  // 是否正在保存
  const isSaving = ref(false)
  // 编程语言(仅编程题适用)
  const language = ref<string | undefined>(undefined)
  
  // 定时器ID
  let saveTimerId: number | null = null
  
  // 监听内容变化
  watch(content, () => {
    isDirty.value = true
    
    // 清除之前的定时器
    if (saveTimerId !== null) {
      window.clearTimeout(saveTimerId)
    }
    
    // 设置新的定时器
    saveTimerId = window.setTimeout(() => {
      save()
    }, mergedOptions.delay)
  })
  
  // 保存草稿
  const save = async () => {
    if (!isDirty.value || isSaving.value) return
    
    isSaving.value = true
    
    try {
      const data: DraftSolutionRequest = {
        problemId,
        type,
        content: content.value
      }
      
      // 如果设置了编程语言，则添加到请求中
      if (language.value) {
        data.language = language.value
      }
      
      const res = await draftApi.saveDraft(data)
      
      if (res.code === 0) {
        isDirty.value = false
        lastSaved.value = new Date()
        mergedOptions.onSaved?.()
      } else {
        mergedOptions.onError?.(new Error(res.message || '保存失败'))
      }
    } catch (error) {
      mergedOptions.onError?.(error)
    } finally {
      isSaving.value = false
    }
  }
  
  // 加载草稿
  const load = async () => {
    try {
      const res = await draftApi.getDraft(problemId)
      
      if (res.code === 0 && res.data) {
        content.value = res.data.content
        if (res.data.language) {
          language.value = res.data.language
        }
        isDirty.value = false
        lastSaved.value = res.data.lastSaveTime ? new Date(res.data.lastSaveTime) : new Date()
        mergedOptions.onLoaded?.(res.data.content)
        return true
      }
    } catch (error) {
      console.error('加载草稿失败:', error)
    }
    return false
  }
  
  // 删除草稿
  const remove = async () => {
    try {
      await draftApi.deleteDraft(problemId)
      isDirty.value = false
      lastSaved.value = null
      return true
    } catch (error) {
      console.error('删除草稿失败:', error)
      return false
    }
  }
  
  // 强制保存
  const forceSave = () => {
    if (saveTimerId !== null) {
      window.clearTimeout(saveTimerId)
      saveTimerId = null
    }
    return save()
  }
  
  // 设置内容
  const setContent = (newContent: string) => {
    content.value = newContent
  }
  
  // 设置编程语言
  const setLanguage = (newLanguage: string) => {
    language.value = newLanguage
  }
  
  // 初始化时加载草稿
  if (mergedOptions.loadOnInit) {
    load()
  }
  
  return {
    content,
    isDirty,
    lastSaved,
    isSaving,
    language,
    save: forceSave,
    load,
    remove,
    setContent,
    setLanguage
  }
} 