import request from '../utils/request'

export interface Problem {
  id: number
  title: string
  content: string
  type: 'CHOICE' | 'JUDGE' | 'PROGRAM'
  jobType: 'FRONTEND' | 'BACKEND' | 'ALGORITHM'
  tags: string
  difficulty: 'EASY' | 'MEDIUM' | 'HARD'
  acceptRate: string
  submissionCount: number
  status: 'SOLVED' | 'ATTEMPTED' | 'UNSOLVED'
  createTime: string
  updateTime: string
  // 选择题选项
  options?: Array<{
    key: string
    content: string
  }>
  // 编程题示例
  samples?: Array<{
    input: string
    output: string
  }>
}

export interface ProblemListResponse {
  records: Problem[]
  total: number
}

export interface ProblemQueryParams {
  page: number
  pageSize: number
  category?: string
  search?: string
  sortBy?: string
  difficulty?: string
  type?: string
  jobType?: string
}

export interface Submission {
  id: number
  userId: number
  problemId: number
  problemTitle: string
  language: string
  code: string
  status: 'ACCEPTED' | 'WRONG_ANSWER' | 'TIME_LIMIT_EXCEEDED' | 'MEMORY_LIMIT_EXCEEDED' | 'RUNTIME_ERROR' | 'COMPILE_ERROR'
  submitTime: string
  executeTime?: number
  memoryUsage?: number
}

export interface UserStats {
  solvedCount: number
  submissionCount: number
  acceptRate: number
}

export interface Category {
  id: number
  name: string
  icon?: string
  description?: string
  problemCount?: number
  isHot?: boolean
  isRecommended?: boolean
  children?: Category[]
}

export interface CodeRunResult {
  status: string
  success: boolean
  message: string
  output?: string
  error?: string
  executeTime?: number
  memoryUsage?: number
}

// 示例题目数据
const mockProblems: Problem[] = [
  {
    id: 1,
    title: '选择正确的 JavaScript 变量声明',
    content: `
在 JavaScript 中，以下哪个变量声明是正确的？

请选择正确的选项。
    `,
    type: 'CHOICE',
    jobType: 'FRONTEND',
    tags: 'javascript,基础',
    difficulty: 'EASY',
    acceptRate: '85',
    submissionCount: 1000,
    status: 'UNSOLVED',
    createTime: '2024-03-05T10:00:00Z',
    updateTime: '2024-03-05T10:00:00Z',
    options: [
      { key: 'A', content: 'var 1name = "John"' },
      { key: 'B', content: 'let @name = "John"' },
      { key: 'C', content: 'const userName = "John"' },
      { key: 'D', content: 'variable name = "John"' }
    ]
  },
  {
    id: 2,
    title: '判断 HTTP 状态码说法是否正确',
    content: `
HTTP 状态码 404 表示"服务器内部错误"。

请判断这个说法是否正确。
    `,
    type: 'JUDGE',
    jobType: 'FRONTEND',
    tags: 'http,网络',
    difficulty: 'EASY',
    acceptRate: '75',
    submissionCount: 800,
    status: 'UNSOLVED',
    createTime: '2024-03-05T11:00:00Z',
    updateTime: '2024-03-05T11:00:00Z'
  },
  {
    id: 3,
    title: '实现数组去重函数',
    content: `
请实现一个函数 \`removeDuplicates\`，该函数接收一个数组作为参数，返回一个新数组，其中包含原数组中的所有不重复元素。

要求：
1. 保持元素原有的顺序
2. 不修改原数组
3. 考虑各种数据类型（数字、字符串、布尔值等）

示例：
\`\`\`javascript
输入：[1, 2, 2, 3, '3', true, true, { a: 1 }, { a: 1 }]
输出：[1, 2, 3, '3', true, { a: 1 }, { a: 1 }]
\`\`\`

注意：对于对象类型，需要考虑引用相等。
    `,
    type: 'PROGRAM',
    jobType: 'FRONTEND',
    tags: 'javascript,数组,算法',
    difficulty: 'MEDIUM',
    acceptRate: '45',
    submissionCount: 500,
    status: 'UNSOLVED',
    createTime: '2024-03-05T12:00:00Z',
    updateTime: '2024-03-05T12:00:00Z',
    samples: [
      {
        input: '[1, 2, 2, 3, 3, 4]',
        output: '[1, 2, 3, 4]'
      },
      {
        input: '["a", "b", "a", "c"]',
        output: '["a", "b", "c"]'
      }
    ]
  }
]

// 修改 API 实现，使用模拟数据
export const problemApi = {
  // 获取题目列表
  async getProblems(params: ProblemQueryParams) {
    // 模拟分页
    const start = (params.page - 1) * params.pageSize
    const end = start + params.pageSize
    const filteredProblems = mockProblems
      .filter(p => {
        if (params.difficulty && p.difficulty !== params.difficulty) return false
        if (params.type && p.type !== params.type) return false
        if (params.jobType && p.jobType !== params.jobType) return false
        if (params.search && !p.title.toLowerCase().includes(params.search.toLowerCase())) return false
        return true
      })
    
    return {
      records: filteredProblems.slice(start, end),
      total: filteredProblems.length
    }
  },

  // 获取题目详情
  async getProblemDetail(id: number) {
    const problem = mockProblems.find(p => p.id === id)
    if (!problem) {
      throw new Error('题目不存在')
    }
    return problem
  },

  // 获取用户统计信息
  async getUserStats() {
    return {
      solvedCount: 10,
      submissionCount: 20,
      acceptRate: 50
    }
  },

  // 获取最近提交记录
  async getRecentSubmissions() {
    return [
      {
        id: 1,
        userId: 1,
        problemId: 1,
        problemTitle: '选择正确的 JavaScript 变量声明',
        language: 'javascript',
        code: '',
        status: 'ACCEPTED' as const,
        submitTime: '2024-03-05T14:00:00Z',
        executeTime: 100,
        memoryUsage: 1024
      }
    ]
  },

  // 运行代码
  async runCode(problemId: number, language: string, code: string) {
    // 模拟运行结果
    return {
      status: 'SUCCESS',
      success: true,
      message: '代码运行成功',
      output: '测试用例通过',
      executeTime: 100,
      memoryUsage: 1024
    }
  },

  // 提交代码
  async submitCode(problemId: number, language: string, code: string) {
    // 模拟提交结果
    return {
      id: Date.now(),
      userId: 1,
      problemId,
      problemTitle: mockProblems.find(p => p.id === problemId)?.title || '',
      language,
      code,
      status: 'ACCEPTED' as const,
      submitTime: new Date().toISOString(),
      executeTime: 100,
      memoryUsage: 1024
    }
  },

  // 获取题目的提交记录
  async getProblemSubmissions(problemId: number) {
    return [
      {
        id: 1,
        userId: 1,
        problemId,
        problemTitle: mockProblems.find(p => p.id === problemId)?.title || '',
        language: 'javascript',
        code: '',
        status: 'ACCEPTED' as const,
        submitTime: '2024-03-05T14:00:00Z',
        executeTime: 100,
        memoryUsage: 1024
      }
    ]
  },

  // 获取随机题目
  async getRandomProblem(): Promise<Problem> {
    const randomIndex = Math.floor(Math.random() * mockProblems.length)
    return mockProblems[randomIndex]
  },

  // 获取题库列表
  async getCategories(): Promise<Category[]> {
    return [
      {
        id: 1,
        name: '前端开发',
        icon: '🌐',
        description: '前端开发相关题目',
        problemCount: 100,
        isHot: true
      }
    ]
  }
} 