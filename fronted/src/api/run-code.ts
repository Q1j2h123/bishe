import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'

// 运行代码请求
export interface CodeRunRequest {
  problemId: number
  language: string
  code: string
}

// 运行结果
export interface CodeRunResult {
  success: boolean
  executionTime?: number  // 执行时间(ms)
  memoryUsage?: number    // 内存使用(MB)
  output?: string         // 输出结果
  errorMessage?: string   // 错误信息
  passedTestCases?: number // 通过测试用例数
  totalTestCases?: number  // 总测试用例数
}

// 代码运行API
export const codeRunApi = {
  // 运行代码
  runCode(data: CodeRunRequest): Promise<BaseResponse<CodeRunResult>> {
    return request.post('execution/run', data)
  }
} 