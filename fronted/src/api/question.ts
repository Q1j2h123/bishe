import request from '../utils/request'

export interface Question {
  id: number
  title: string
  content: string
  tags: string[]
  answer: string
  submitNum: number
  questionType: 'programming' | 'choice' | 'judgement'
  acceptedNum: number
  judgeCase: string
  judgeConfig: string
  thumbNum: number
  favourNum: number
  userId: number
  createTime: string
}

export interface QuestionBank {
  id: number
  bankName: string
  description: string
  positionType: string
  difficultyLevel: number
  userId: number
  createTime: string
}

export interface QuestionQueryParams {
  current?: number
  pageSize?: number
  job?: string
  type?: string
  tags?: string[]
  bankId?: number
}

export interface QuestionSubmitParams {
  questionId: number
  language?: string
  code?: string
  userAnswer?: string
}

export const questionApi = {
  // 获取题目列表
  getQuestionList(params: QuestionQueryParams) {
    return request.get<{
      records: Question[]
      total: number
    }>('/question/list', { params })
  },

  // 获取题目详情
  getQuestionDetail(id: number) {
    return request.get<Question>(`/question/${id}`)
  },

  // 获取题库列表
  getQuestionBanks() {
    return request.get<QuestionBank[]>('/question/bank/list')
  },

  // 提交题目
  submitQuestion(data: QuestionSubmitParams) {
    return request.post<{
      id: number
      status: number
      judgeInfo: string
    }>('/question/submit', data)
  },

  // 获取提交记录
  getSubmitHistory(questionId: number) {
    return request.get<{
      id: number
      language: string
      code: string
      userAnswer: string
      judgeInfo: string
      status: number
      createTime: string
    }[]>(`/question/submit/list/${questionId}`)
  }
} 