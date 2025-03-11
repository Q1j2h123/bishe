import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'


// 基础问题类型
export interface BaseProblem {
  id?: number;
  title: string;
  content: string;
  difficulty: string;
  tags: string[];
  type: 'CHOICE' | 'JUDGE' | 'PROGRAM';
  createTime?: string;
  updateTime?: string;
  userId?: number;
  userName?: string;
}

// 选择题选项
export interface ChoiceOption {
  id?: number;
  content: string;
  isCorrect: boolean;
}

// 判断题
export interface JudgeProblem extends BaseProblem {
  answer: boolean;
}

// 选择题
export interface ChoiceProblem extends BaseProblem {
  options: ChoiceOption[];
}

// 编程题
export interface ProgramProblem extends BaseProblem {

  testCases: Array<{
    input: string;
    output: string;
  }>;
}

// 题目展示对象
export interface ProblemVO extends BaseProblem {
  // 根据题目类型可能包含不同字段
  answer?: boolean;
  options?: ChoiceOption[];
  testCases: Array<{
    input: string;
    output: string;
  }>;
  acceptCount?: number;
  submitCount?: number;
}

// 题目查询请求
export interface ProblemQueryRequest {
  current?: number;
  pageSize?: number;
  searchText?: string;
  id?: number;
  title?: string;
  type?: string;
  difficulty?: string;
  jobType?: string;
  userId?: number;
  tags?: string[];
  status?: string;
}

// 选择题添加请求
export interface ChoiceProblemAddRequest {
  title: string;
  content: string;
  difficulty: string;
  tags: string[];
  options: ChoiceOption[];
  jobType: string;
  analysis: string; // 题目解析
  answer: string; // 正确答案
  type: 'CHOICE'; // 题目类型
}

// 判断题添加请求
export interface JudgeProblemAddRequest {
  title: string;
  content: string;
  difficulty: string;
  tags: string[];
  answer: boolean;
  jobType: string;
  analysis: string; // 题目解析
  type: 'JUDGE'; // 题目类型
}

// 编程题添加请求
export interface ProgramProblemAddRequest {
  title: string;
  content: string;
  difficulty: string;
  tags: string[];
  testCases: Array<{ input: string; output: string; }>;
  jobType: string;
  analysis: string; // 题目解析
  type: 'PROGRAM'; // 题目类型
  functionName: string; // 函数名称
  paramTypes: string[]; // 参数类型列表
  returnType: string; // 返回值类型
  templates: Record<string, string>; // 代码模板
  standardSolution: Record<string, string>; // 标准答案
  timeLimit?: number; // 时间限制（毫秒）
  memoryLimit?: number; // 内存限制（MB）
}

// 更新请求
export interface ChoiceProblemUpdateRequest {
  id: number;
  title?: string;
  content?: string;
  difficulty?: string;
  tags?: string[];
  options?: ChoiceOption[];
  jobType?: string;
  analysis?: string; // 题目解析
  answer?: string; // 正确答案
  type?: 'CHOICE'; // 题目类型
}

export interface JudgeProblemUpdateRequest {
  id: number;
  title?: string;
  content?: string;
  difficulty?: string;
  tags?: string[];
  answer?: boolean;
  jobType?: string;
  analysis?: string; // 题目解析
  type?: 'JUDGE'; // 题目类型
}

export interface ProgramProblemUpdateRequest {
  id: number;
  title?: string;
  content?: string;
  difficulty?: string;
  tags?: string[];
  testCases?: Array<{ input: string; output: string; }>;
  jobType?: string;
  analysis?: string; // 题目解析
  type?: 'PROGRAM'; // 题目类型
  functionName?: string; // 函数名称
  paramTypes?: string[]; // 参数类型列表
  returnType?: string; // 返回值类型
  templates?: Record<string, string>; // 代码模板
  standardSolution?: Record<string, string>; // 标准答案
  timeLimit?: number; // 时间限制（毫秒）
  memoryLimit?: number; // 内存限制（MB）
}

// 问题API接口
export const problemApi = {
  // 获取题目列表
  getProblemList(params: ProblemQueryRequest): Promise<BaseResponse<{records: ProblemVO[], total: number}>> {
    // 复制参数，避免修改原始对象
    const queryParams: Record<string, any> = { ...params };
    
    // 特殊处理标签参数，将数组转为字符串
    if (queryParams.tags && Array.isArray(queryParams.tags) && queryParams.tags.length > 0) {
      queryParams.tagList = queryParams.tags.join(',');
      delete queryParams.tags; // 删除原始tags参数
    }
    
    // 处理难度参数 - 将中文难度转换为后端期望的英文大写格式
    if (queryParams.difficulty) {
      const difficultyMap: Record<string, string> = {
        '简单': 'EASY',
        '中等': 'MEDIUM',
        '困难': 'HARD'
      };
      
      if (difficultyMap[queryParams.difficulty]) {
        queryParams.difficulty = difficultyMap[queryParams.difficulty];
      }
    }
    
    return request.get('problem/list/page', { params: queryParams });
  },
  
  // 获取我的题目
  getMyProblemList(params: ProblemQueryRequest): Promise<BaseResponse<{records: ProblemVO[], total: number}>> {
    return request.post('problem/list/my', params);
  },
  
  // 获取题目基本信息
  getProblemById(id: number): Promise<BaseResponse<ProblemVO>> {
    return request.get(`problem/get/${id}`);
  },
  
  // 获取题目详情
  getProblemDetail(id: number): Promise<BaseResponse<ProblemVO>> {
    return request.get(`problem/detail/${id}`);
  },
  
  // 添加选择题
  addChoiceProblem(data: ChoiceProblemAddRequest): Promise<BaseResponse<number>> {
    // 直接使用JSON格式
    return request.post('problem/choice/add', data);
  },
  
  // 添加判断题
  addJudgeProblem(data: JudgeProblemAddRequest): Promise<BaseResponse<number>> {
    return request.post('problem/judge/add', data);
  },
  
  // 添加编程题
  addProgramProblem(data: ProgramProblemAddRequest): Promise<BaseResponse<number>> {
    console.log('开始处理编程题数据...')
    
    try {
      // 创建简化的请求对象，避免使用Proxy对象
      const requestData = {
        title: data.title,
        content: data.content,
        difficulty: data.difficulty,
        tags: Array.isArray(data.tags) ? [...data.tags] : [],
        jobType: data.jobType || '',
        analysis: data.analysis || '',
        functionName: data.functionName,
        paramTypes: Array.isArray(data.paramTypes) ? [...data.paramTypes] : [],
        returnType: data.returnType,
        testCases: Array.isArray(data.testCases) ? data.testCases.map(tc => ({
          input: tc.input || '',
          output: tc.output || ''
        })) : [],
        templates: data.templates ? {...data.templates} : {},
        standardSolution: data.standardSolution ? {...data.standardSolution} : {},
        timeLimit: data.timeLimit || 1000,
        memoryLimit: data.memoryLimit || 256,
        type: 'PROGRAM' as const
      };
      
      // 打印JSON格式的数据大小
      const jsonString = JSON.stringify(requestData);
      const sizeInKB = Math.round(jsonString.length / 1024);
      console.log('请求数据大小:', sizeInKB, 'KB');
      
      // 检查请求数据大小是否合理
      if (sizeInKB > 5000) {
        return Promise.reject(new Error(`请求数据过大(${sizeInKB}KB)，超过服务器限制，请减少代码模板或测试用例的大小`));
      }
      
      // 显示警告如果数据较大
      if (sizeInKB > 1000) {
        console.warn(`请求数据较大(${sizeInKB}KB)，可能需要较长处理时间`);
      }
      
      // 使用简单直接的请求方式，不使用复杂的Promise.race
      return request.post('problem/program/add', requestData, {
        headers: {
          'Content-Type': 'application/json'
        },
        timeout: 60000 // 60秒超时
      });
    } catch (error) {
      console.error('处理请求数据时出错:', error);
      return Promise.reject(error);
    }
  },
  
  // 更新选择题
  updateChoiceProblem(data: ChoiceProblemUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('problem/choice/update', data);
  },
  
  // 更新判断题
  updateJudgeProblem(data: JudgeProblemUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('problem/judge/update', data);
  },
  
  // 更新编程题
  updateProgramProblem(data: ProgramProblemUpdateRequest): Promise<BaseResponse<boolean>> {
    return request.post('problem/program/update', data);
  },
  
  // 删除选择题
  deleteChoiceProblem(id: number): Promise<BaseResponse<boolean>> {
    return request.post('problem/choice/delete', null, { params: { id } });
  },
  
  // 删除判断题
  deleteJudgeProblem(id: number): Promise<BaseResponse<boolean>> {
    return request.post('problem/judge/delete', null, { params: { id } });
  },
  
  // 删除编程题
  deleteProgramProblem(id: number): Promise<BaseResponse<boolean>> {
    return request.post('problem/program/delete', null, { params: { id } });
  },
  
  // 批量获取题目
  getProblemsByIds(problemIds: number[]): Promise<BaseResponse<ProblemVO[]>> {
    return request.post('problem/batch', problemIds);
  },
  
  // 获取用户的题目
  getProblemsByUserId(userId: number): Promise<BaseResponse<ProblemVO[]>> {
    return request.get(`problem/user/${userId}`);
  },
  
  // 搜索题目
  searchProblems(keyword: string): Promise<BaseResponse<ProblemVO[]>> {
    return request.get('problem/search', { params: { keyword } });
  },
  
  // 获取随机题目
  getRandomProblem(): Promise<BaseResponse<ProblemVO>> {
    return request.get('problem/random');
  },
  
  // 获取每日一题
  getDailyProblem(): Promise<BaseResponse<ProblemVO>> {
    return request.get('problem/daily');
  },
  
  // 更新题目状态
  updateProblemStatus(problemId: number, status: string): Promise<BaseResponse<boolean>> {
    return request.post('problem/status/update', null, { 
      params: { problemId, status } 
    });
  }
};
