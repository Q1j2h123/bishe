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

// 提交详情基础接口
export interface SubmissionDetailVO {
  id: number
  problemId: number
  problemTitle?: string
  problemContent?: string
  problemTags?: string[]
  userId?: number
  userName?: string
  type: string
  status: string
  submissionTime: string
  jobType?: string
  difficulty?: string
}

// 选择/判断题提交详情接口
export interface ChoiceJudgeSubmissionDetailVO extends SubmissionDetailVO {
  answer: string
  correctAnswer?: string
  analysis?: string
  canViewAnalysis?: boolean
}

// 编程题提交详情接口
export interface ProgramSubmissionDetailVO extends SubmissionDetailVO {
  language: string
  code: string
  functionName?: string
  paramTypes?: string[]
  returnType?: string
  timeLimit?: number
  memoryLimit?: number
  executeTime?: number
  memoryUsage?: number
  errorMessage?: string
  testcaseResults?: string
  passedTestCases?: number
  totalTestCases?: number
  standardSolution?: Record<string, string>
}

// 选择题提交请求
export interface ChoiceSubmitRequest {
  problemId: number
  type: 'CHOICE'
  answer: string
}

// 判断题提交请求
export interface JudgeSubmitRequest {
  problemId: number
  type: 'JUDGE'
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
  difficulty?: string
  jobType?: string
  tag?: string
  keyword?: string
  startTime?: string
  endTime?: string
}

// 提交API
export const submissionApi = {
  // 提交选择题答案
  submitChoice(data: ChoiceSubmitRequest): Promise<BaseResponse<SubmissionVO>> {
    return request.post('submission/choice-judge', data)
  },
  
  // 提交判断题答案
  submitJudge(data: JudgeSubmitRequest): Promise<BaseResponse<SubmissionVO>> {
    return request.post('submission/choice-judge', data)
  },
  
  // 提交编程题代码
  submitProgram(data: ProgramSubmitRequest): Promise<BaseResponse<number>> {
    return request.post('submission/program', data)
  },
  
  // 获取提交详情（通用）
  getSubmission(id: number): Promise<BaseResponse<SubmissionDetailVO>> {
    return request.get(`submission/detail?submissionId=${id}`)
  },
  
  // 获取选择/判断题提交详情
  getChoiceJudgeSubmissionDetail(id: number): Promise<BaseResponse<ChoiceJudgeSubmissionDetailVO>> {
    return request.get(`submission/detail/choice-judge?submissionId=${id}`)
  },
  
  // 获取编程题提交详情
  getProgramSubmissionDetail(id: number): Promise<BaseResponse<ProgramSubmissionDetailVO>> {
    return request.get(`submission/detail/program?submissionId=${id}`)
  },
  
  // 获取提交列表
  getSubmissionList(params: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {
    return request.get('submission/list/page', { params })
  },
  
  // 获取我的提交列表
  getMySubmissionList(params: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {
    // 过滤掉undefined, null和空字符串参数
    const cleanParams: Record<string, any> = { ...params };
    Object.keys(cleanParams).forEach(key => {
      const value = cleanParams[key];
      if (value === undefined || value === null || value === '') {
        delete cleanParams[key];
      }
    });
    
    console.log('发送筛选参数:', cleanParams);
    return request.get('submission/user/list', { params: cleanParams });
  },

  // 获取指定题目的提交列表
  getProblemSubmissionList(problemId: number, params?: SubmissionQueryRequest): Promise<BaseResponse<{records: SubmissionVO[], total: number}>> {

    // 构建请求参数，确保包含userId
    const queryParams = { 
      ...params, 
      problemId,
    };
      // 添加调试日志
  console.log('获取题目提交记录，参数:', queryParams);
    return request.get('submission/problem/list', { 
      params: queryParams
    });
  },
  
  // 获取用户最近的提交记录
  getUserRecentSubmissions(userId: number, limit: number = 10): Promise<BaseResponse<SubmissionVO[]>> {
    return request.get(`submission/user/${userId}/recent`, { 
      params: { limit } 
    })
  }
} 