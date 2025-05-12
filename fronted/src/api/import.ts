import request from '@/utils/request'

export interface ImportResult {
  successCount: number
  failCount: number
  totalCount: number
  errorMessages?: string[]
}

/**
 * 导入选择题
 * @param file 选择题文件（Excel或CSV）
 */
export function importChoiceProblems(file: File): Promise<{code: number, data: ImportResult, message: string}> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/admin/import/choice',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入判断题
 * @param file 判断题文件（Excel或CSV）
 */
export function importJudgeProblems(file: File): Promise<{code: number, data: ImportResult, message: string}> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/admin/import/judge',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * 导入编程题
 * @param file 编程题文件（Excel或CSV）
 */
export function importProgramProblems(file: File): Promise<{code: number, data: ImportResult, message: string}> {
  const formData = new FormData()
  formData.append('file', file)
  
  return request({
    url: '/admin/import/program',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
} 