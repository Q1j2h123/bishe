import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'

// 提交记录接口
export interface SubmissionVO {
  id?: number
  problemId: number
  userId?: number
  userName?: string
  problemTitle?: string
  type: 'CHOICE' | 'JUDGE' | 'PROGRAM'
  language?: string
  code?: string
  answer?: string | boolean
  status: 'PENDING' | 'ACCEPTED' | 'WRONG_ANSWER' | 'COMPILE_ERROR' | 'RUNTIME_ERROR' | 'TIME_LIMIT_EXCEEDED' | 'MEMORY_LIMIT_EXCEEDED'
  executeTime?: number // 毫秒
  executeMemory?: number // MB
  errorMessage?: string
  createTime?: string
  score?: number
}

// 选择题提交请求
export interface ChoiceSubmitRequest {
  problemId: number
  answer: string[] // 选项的标识符列表，如 ['A', 'C']
}

// 判断题提交请求
export interface JudgeSubmitRequest {
  problemId: number
  answer: boolean
}

// 编程题提交请求
export interface ProgramSubmitRequest {
  problemId: number
  language: string
  code: string
}

// 提交记录查询请求
export interface SubmissionQueryRequest {
  current?: number
  pageSize?: number
  problemId?: number
  userId?: number
  type?: string
  status?: string
  startTime?: string
  endTime?: string
}

// 提交API
export const submissionApi = {
  // 提交选择题答案
  submitChoice(data: ChoiceSubmitRequest): Promise<BaseResponse<SubmissionVO>> {
    return request.post('submission/choice', data)
  },
  
  // 提交判断题答案
  submitJudge(data: JudgeSubmitRequest): Promise<BaseResponse<SubmissionVO>> {
    return request.post('submission/judge', data)
  },
  
  // 提交编程题代码
  submitProgram(data: ProgramSubmitRequest): Promise<BaseResponse<SubmissionVO>> {
    return request.post('submission/program', data)
  },
  
  // 获取提交结果
  getSubmission(id: number): Promise<BaseResponse<SubmissionVO>> {
    return request.get(`submission/${id}`)
  },
  
  // 获取提交列表
  getSubmissionList(params: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {
    return request.get('submission/list/page', { params })
  },
  
  // 获取我的提交列表
  getMySubmissionList(params: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {
    return request.get('submission/list/my', { params })
  },

  // 获取指定题目的提交列表
  getProblemSubmissionList(problemId: number, params?: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {
    return request.get(`submission/problem/${problemId}`, { 
      params: { ...params, problemId } 
    })
  },
  
  // 获取用户最近的提交记录
  getUserRecentSubmissions(userId: number, limit: number = 10): Promise<BaseResponse<SubmissionVO[]>> {
    return request.get(`submission/user/${userId}/recent`, { 
      params: { limit } 
    })
  }
} 