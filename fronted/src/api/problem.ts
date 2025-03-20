import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'
import { problemStatusApi } from './problem-status'


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
  isMultiple?: boolean; // 是否为多选题，用于选择题区分单选和多选
}

// 扩展接口以包含状态
export interface ProblemWithStatusVO extends ProblemVO {
  userStatus?: string // 用户题目状态: UNSOLVED, ATTEMPTED, SOLVED
  displayId?: number  // 用于在列表中显示的序号
}

// 扩展ProblemQueryRequest类型，添加强制刷新参数
export interface ProblemQueryRequestExt extends ProblemQueryRequest {
  forceRefresh?: boolean;
  userStatus?: string; // 添加userStatus字段用于题目状态查询
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
  tag?: string; // 单标签查询参数，兼容后端接口
  userStatus?: string; // 添加userStatus字段，用于扩展查询
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
    console.log('原始查询参数:', params);
    
    // 创建一个新的参数对象
    const queryParams: Record<string, any> = {};
    
    // 添加基本分页参数
    if (params.current) queryParams.current = params.current;
    if (params.pageSize) queryParams.pageSize = params.pageSize;
    
    // 添加其他有效查询参数
    if (params.searchText) queryParams.searchText = params.searchText;
    if (params.type) queryParams.type = params.type;
    if (params.jobType) queryParams.jobType = params.jobType;
    
    // 注意：不传递status参数给后端API
    // 我们会获取所有题目，然后在前端根据userStatus进行筛选
    // 这样可以避免后端查询返回空结果的问题
    // if (params.status) queryParams.status = params.status;
    
    // 如果请求中包含forceRefresh参数，将其设置为true
    if ('forceRefresh' in params) {
      queryParams.forceRefresh = true;
    }
    
    // 处理难度参数
    if (params.difficulty) {
      const difficultyMap: Record<string, string> = {
        '简单': 'EASY',
        '中等': 'MEDIUM',
        '困难': 'HARD'
      };
      
      queryParams.difficulty = difficultyMap[params.difficulty] || params.difficulty;
    }
    
    // 特殊处理标签参数
    if (params.tags && Array.isArray(params.tags) && params.tags.length > 0) {
      console.log('处理标签参数:', params.tags);
      
      // 对每个标签进行清理
      const cleanTags = params.tags.map(tag => String(tag).replace(/["'\[\]{}]/g, '').trim());
      console.log('清理后的标签:', cleanTags);
      
      // 如果只有一个标签，使用tag参数
      if (cleanTags.length === 1) {
        queryParams.tag = cleanTags[0];
        console.log('单个标签查询参数:', queryParams.tag);
      } else {
        // 多个标签直接传递tags数组
        queryParams.tags = cleanTags;
        console.log('多标签查询参数:', queryParams.tags);
      }
    }
    
    // 单独处理tag参数
    if (params.tag) {
      queryParams.tag = String(params.tag).replace(/["'\[\]{}]/g, '').trim();
    }
    
    console.log('最终请求参数:', queryParams);
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
    console.log('请求每日推荐题目')
    return request.get('problem/daily');
  },
  
  // 更新题目状态
  updateProblemStatus(problemId: number, status: string): Promise<BaseResponse<boolean>> {
    return request.post('problem/status/update', null, { 
      params: { problemId, status } 
    });
  },
  
  // 获取所有标签
  getAllTags(): Promise<BaseResponse<string[]>> {
    return request.get('problem/tags/all');
  },
  
  // 获取所有岗位类型
  getAllJobTypes(): Promise<BaseResponse<string[]>> {
    return request.get('problem/jobTypes/all');
  }
};

// 缓存完整的筛选后数据，用于分页
let cachedFilteredResults: ProblemWithStatusVO[] = [];
let cachedFilterStatus: string = '';
let cachedTotalCount: number = 0;

// 添加获取带状态的题目列表方法
export async function getProblemsWithStatus(params: ProblemQueryRequestExt): Promise<BaseResponse<{records: ProblemWithStatusVO[], total: number}>> {
  console.log('开始获取带状态的题目列表:', params);
  
  // 创建请求ID，带页码信息便于调试
  const requestId = `problem_req_${Date.now()}_page${params.current || 1}`;
  console.log(`[${requestId}] 开始处理请求`);
  
  // 保存状态筛选条件供本地过滤使用
  const statusFilter = params.userStatus || params.status || '';
  console.log(`[${requestId}] 状态筛选条件:`, statusFilter);
  
  // 获取当前分页参数
  const currentPage = params.current || 1;
  const pageSize = params.pageSize || 10;
  
  // 检查是否可以使用缓存的筛选结果进行分页
  if (cachedFilteredResults.length > 0 && 
      statusFilter === cachedFilterStatus && 
      !params.forceRefresh &&
      statusFilter) {
    console.log(`[${requestId}] 使用缓存的筛选结果进行分页, 总数据量: ${cachedFilteredResults.length}`);
    
    // 计算当前页的起始和结束索引
    const startIndex = (currentPage - 1) * pageSize;
    const endIndex = Math.min(startIndex + pageSize, cachedFilteredResults.length);
    
    // 获取当前页的数据
    const pageRecords = cachedFilteredResults.slice(startIndex, endIndex);
    console.log(`[${requestId}] 当前页数据 (${startIndex}-${endIndex}): ${pageRecords.length}条`);
    
    return {
      code: 0,
      data: {
        records: pageRecords,
        total: cachedFilteredResults.length
      },
      message: '成功',
      success: true
    };
  }
  
  // 强制刷新的情况下清除缓存
  if (params.forceRefresh) {
    console.log(`[${requestId}] 检测到强制刷新参数，清除所有缓存`);
    try {
      localStorage.removeItem('userProblemStatuses');
      localStorage.removeItem('statusCacheTime');
      localStorage.removeItem('problem_status_cache');
      localStorage.removeItem('currentProblemPage');
      
      // 清除筛选结果缓存
      cachedFilteredResults = [];
      cachedFilterStatus = '';
      cachedTotalCount = 0;
      
      console.log(`[${requestId}] 所有状态相关缓存已清除`);
    } catch (e) {
      console.error(`[${requestId}] 清除缓存失败:`, e);
    }
    
    // 记录当前请求的页码
    try {
      localStorage.setItem('lastRequestedPage', String(params.current || 1));
    } catch (e) {
      console.error(`[${requestId}] 保存页码信息失败:`, e);
    }
    
    // 移除forceRefresh参数，防止传递到后端API
    const { forceRefresh, ...cleanParams } = params;
    params = cleanParams as ProblemQueryRequestExt;
  }
  
  // 打印最终传递给API的参数
  console.log(`[${requestId}] 实际传递给API的查询参数:`, JSON.stringify(params));
  
  // 先获取题目列表
  console.log(`[${requestId}] 请求题目列表，页码: ${params.current}，每页数量: ${params.pageSize || 10}`);
  let problemsResponse: BaseResponse<{records: ProblemVO[], total: number}> = {
    code: -1,
    data: { records: [], total: 0 },
    message: '获取题目列表失败',
    success: false
  };
  
  try {
    problemsResponse = await problemApi.getProblemList(params);
    console.log(`[${requestId}] 题目列表请求结果:`, problemsResponse);
    console.log(`[${requestId}] 获取到题目数量:`, problemsResponse.data?.records?.length || 0);
  } catch (error) {
    console.error(`[${requestId}] 题目列表请求异常:`, error);
    return {
      code: -1,
      data: { records: [], total: 0 },
      message: '获取题目列表失败: ' + (error instanceof Error ? error.message : String(error)),
      success: false
    };
  }
  
  if (problemsResponse.code !== 0 || !problemsResponse.data || !problemsResponse.data.records?.length) {
    console.warn(`[${requestId}] 题目列表请求失败或为空，返回原始数据`);
    return problemsResponse;
  }
  
  // 提取题目ID列表
  const problemIds = problemsResponse.data.records
    .map(problem => problem.id)
    .filter(id => id !== undefined) as number[];
  
  console.log(`[${requestId}] 提取的题目ID列表:`, problemIds);
  
  if (problemIds.length === 0) {
    console.warn(`[${requestId}] 题目ID列表为空，返回原始数据`);
    return problemsResponse;
  }
  
  // 检查用户是否登录，通过检查localStorage中是否有token
  const token = localStorage.getItem('token');
  if (!token) {
    console.warn(`[${requestId}] 用户未登录，不获取题目状态，设置所有题目为UNSOLVED`);
    
    // 为所有题目设置默认状态为UNSOLVED
    const recordsWithDefaultStatus = problemsResponse.data.records.map(problem => ({
      ...problem,
      userStatus: 'UNSOLVED' // 未登录用户默认显示未解决
    }));
    
    // 基于状态进行本地过滤（如果有状态筛选条件）
    let filteredRecords = recordsWithDefaultStatus;
    if (statusFilter) {
      console.log(`[${requestId}] 未登录状态下，根据状态 "${statusFilter}" 进行本地筛选，筛选前数量: ${recordsWithDefaultStatus.length}`);
      
      // 确保状态名称的大小写匹配
      const normalizedStatusFilter = statusFilter.toUpperCase();
      
      filteredRecords = recordsWithDefaultStatus.filter(problem => {
        const problemStatus = (problem.userStatus || 'UNSOLVED').toUpperCase();
        return problemStatus === normalizedStatusFilter;
      });
      
      console.log(`[${requestId}] 未登录状态下，筛选后的题目数量: ${filteredRecords.length}, 状态: ${normalizedStatusFilter}`);
    }
    
    return {
      ...problemsResponse,
      data: {
        ...problemsResponse.data,
        records: filteredRecords,
        total: statusFilter ? filteredRecords.length : problemsResponse.data.total // 如果有筛选，更新总数
      }
    };
  }
  
  try {
    console.log(`[${requestId}] 开始请求批量题目状态, 题目IDs:`, problemIds);
    
    // 在这里添加页面信息到日志，帮助调试
    console.log(`[${requestId}] 当前页面信息:`, {
      current: params.current || 1,
      pageSize: params.pageSize || 10, 
      problemCount: problemIds.length
    });

    // 我们总是尝试获取新的状态数据，而不完全依赖缓存
    // 强制刷新参数传递给getBatchProblemStatus
    const statusResponse = await problemStatusApi.getBatchProblemStatus(problemIds, !!params.forceRefresh);
    console.log(`[${requestId}] 获取状态响应:`, statusResponse);
    
    // 检查响应
    if (statusResponse.code !== 0 || !statusResponse.data) {
      console.warn(`[${requestId}] 状态请求失败或返回空数据`);
      // 使用默认状态
      const recordsWithDefaultStatus = problemsResponse.data.records.map(problem => ({
        ...problem,
        userStatus: 'UNSOLVED'
      }));
      
      // 基于状态进行本地过滤（如果有状态筛选条件）
      let filteredRecords = recordsWithDefaultStatus;
      if (statusFilter) {
        console.log(`[${requestId}] 状态请求失败情况下，根据状态 "${statusFilter}" 进行本地筛选，筛选前数量: ${recordsWithDefaultStatus.length}`);
        
        // 确保状态名称的大小写匹配
        const normalizedStatusFilter = statusFilter.toUpperCase();
        
        filteredRecords = recordsWithDefaultStatus.filter(problem => {
          const problemStatus = (problem.userStatus || 'UNSOLVED').toUpperCase();
          return problemStatus === normalizedStatusFilter;
        });
        
        console.log(`[${requestId}] 状态请求失败情况下，筛选后的题目数量: ${filteredRecords.length}, 状态: ${normalizedStatusFilter}`);
      }
      
      return {
        ...problemsResponse,
        data: {
          ...problemsResponse.data,
          records: filteredRecords,
          total: statusFilter ? filteredRecords.length : problemsResponse.data.total // 如果有筛选，更新总数
        }
      };
    }
    
    // 获取到状态数据
    const statusData = statusResponse.data;
    console.log(`[${requestId}] 成功获取状态数据:`, statusData);
    
    // 将状态映射到题目上
    const recordsWithStatus = problemsResponse.data.records.map(problem => {
      const problemId = problem.id;
      
      // 确保problemId是数字并映射状态
      let status = 'UNSOLVED'; // 默认状态
      if (problemId && statusData[problemId]) {
        status = statusData[problemId];
        console.log(`[${requestId}] 题目 ${problemId} 的状态: ${status}`);
      } else {
        console.log(`[${requestId}] 题目 ${problemId} 没有找到对应状态，使用默认UNSOLVED`);
      }
      
      return {
        ...problem,
        userStatus: status
      };
    });
    
    console.log(`[${requestId}] 最终的带状态题目列表准备完成，页码: ${params.current}，题目数: ${recordsWithStatus.length}`);
    
    // 基于状态进行本地过滤（如果有状态筛选条件）
    let filteredRecords = recordsWithStatus;
    if (statusFilter) {
      console.log(`[${requestId}] 根据状态 "${statusFilter}" 进行本地筛选，筛选前数量: ${recordsWithStatus.length}`);
      
      // 确保状态名称的大小写匹配
      const normalizedStatusFilter = statusFilter.toUpperCase();
      
      filteredRecords = recordsWithStatus.filter(problem => {
        const problemStatus = (problem.userStatus || 'UNSOLVED').toUpperCase();
        const isMatch = problemStatus === normalizedStatusFilter;
        
        if (isMatch) {
          console.log(`[${requestId}] 题目 ${problem.id} 符合状态条件: ${problemStatus}`);
        }
        
        return isMatch;
      });
      
      console.log(`[${requestId}] 筛选后的题目数量: ${filteredRecords.length}, 状态: ${normalizedStatusFilter}`);
      
      // 当有状态筛选时，修改请求策略
      if (statusFilter) {
        // 当是第一页或缓存无效时，获取全部数据
        if (currentPage === 1 || cachedFilteredResults.length === 0 || cachedFilterStatus !== statusFilter) {
          console.log(`[${requestId}] 需要重新获取所有数据用于状态筛选`);
          
          // 临时将页大小设置为较大值，尝试一次获取全部数据
          const largeRequest = {
            ...params,
            current: 1,
            pageSize: 1000, // 使用一个较大的值
            forceRefresh: params.forceRefresh || false
          };
          
          // 删除状态相关参数，确保获取所有数据
          delete largeRequest.status;
          delete largeRequest.userStatus;
          
          console.log(`[${requestId}] 发起大数据量请求:`, largeRequest);
          
          try {
            // 发起请求获取所有数据
            const allDataResponse = await problemApi.getProblemList(largeRequest);
            
            if (allDataResponse.code === 0 && allDataResponse.data) {
              // 获取所有题目记录
              const allRecords = allDataResponse.data.records || [];
              console.log(`[${requestId}] 获取到所有题目: ${allRecords.length}条`);
              
              // 获取所有题目的状态
              const allProblemIds = allRecords.map(p => p.id).filter(id => id !== undefined) as number[];
              const statusResponse = await problemStatusApi.getBatchProblemStatus(allProblemIds, !!params.forceRefresh);
              
              if (statusResponse.code === 0 && statusResponse.data) {
                // 将状态映射到所有题目
                const allRecordsWithStatus = allRecords.map(problem => {
                  const problemId = problem.id;
                  let status = 'UNSOLVED'; // 默认状态
                  
                  if (problemId && statusResponse.data[problemId]) {
                    status = statusResponse.data[problemId];
                  }
                  
                  return {
                    ...problem,
                    userStatus: status
                  };
                });
                
                // 基于状态筛选所有记录
                const normalizedStatusFilter = statusFilter.toUpperCase();
                const allFilteredRecords = allRecordsWithStatus.filter(problem => {
                  const problemStatus = (problem.userStatus || 'UNSOLVED').toUpperCase();
                  return problemStatus === normalizedStatusFilter;
                });
                
                console.log(`[${requestId}] 状态筛选后的记录总数: ${allFilteredRecords.length}条`);
                
                // 更新缓存
                cachedFilteredResults = [...allFilteredRecords];
                cachedFilterStatus = statusFilter;
                cachedTotalCount = allFilteredRecords.length;
                
                // 计算当前页的数据
                const startIndex = (currentPage - 1) * pageSize;
                const endIndex = Math.min(startIndex + pageSize, allFilteredRecords.length);
                const currentPageRecords = allFilteredRecords.slice(startIndex, endIndex);
                
                console.log(`[${requestId}] 返回第${currentPage}页数据: ${currentPageRecords.length}条, 范围${startIndex}-${endIndex}`);
                
                return {
                  code: 0,
                  data: {
                    records: currentPageRecords,
                    total: allFilteredRecords.length
                  },
                  message: '成功',
                  success: true
                };
              }
            }
          } catch (error) {
            console.error(`[${requestId}] 获取全部数据异常:`, error);
          }
        } else {
          // 使用已有缓存进行分页
          console.log(`[${requestId}] 使用已有缓存(${cachedFilteredResults.length}条)进行分页`);
          const startIndex = (currentPage - 1) * pageSize;
          const endIndex = Math.min(startIndex + pageSize, cachedFilteredResults.length);
          const pageRecords = cachedFilteredResults.slice(startIndex, endIndex);
          
          console.log(`[${requestId}] 从缓存返回第${currentPage}页数据: ${pageRecords.length}条, 范围${startIndex}-${endIndex}`);
          
          return {
            code: 0,
            data: {
              records: pageRecords,
              total: cachedFilteredResults.length
            },
            message: '成功',
            success: true
          };
        }
      }
    }
    
    // 记录结果到localStorage以供调试使用
    try {
      localStorage.setItem('lastProblemDataPage', String(params.current));
      localStorage.setItem('lastProblemDataTime', new Date().toISOString());
    } catch (e) {
      console.error(`[${requestId}] 保存调试信息失败:`, e);
    }
    
    // 返回筛选后的结果
    return {
      ...problemsResponse,
      data: {
        ...problemsResponse.data,
        records: filteredRecords,
        total: statusFilter ? filteredRecords.length : problemsResponse.data.total // 如果有筛选，更新总数
      }
    };
  } catch (error) {
    console.error(`[${requestId}] 获取状态过程中发生异常:`, error);
    
    // 出现异常时也为所有题目设置默认状态
    const recordsWithDefaultStatus = problemsResponse.data.records.map(problem => ({
      ...problem,
      userStatus: 'UNSOLVED' // 设置默认状态
    }));
    
    // 基于状态进行本地过滤（如果有状态筛选条件）
    let filteredRecords = recordsWithDefaultStatus;
    if (statusFilter) {
      console.log(`[${requestId}] 异常情况下，根据状态 "${statusFilter}" 进行本地筛选，筛选前数量: ${recordsWithDefaultStatus.length}`);
      
      // 确保状态名称的大小写匹配
      const normalizedStatusFilter = statusFilter.toUpperCase();
      
      filteredRecords = recordsWithDefaultStatus.filter(problem => {
        const problemStatus = (problem.userStatus || 'UNSOLVED').toUpperCase();
        return problemStatus === normalizedStatusFilter;
      });
      
      console.log(`[${requestId}] 异常情况下，筛选后的题目数量: ${filteredRecords.length}, 状态: ${normalizedStatusFilter}`);
    }
    
    return {
      ...problemsResponse,
      data: {
        ...problemsResponse.data,
        records: filteredRecords,
        total: statusFilter ? filteredRecords.length : problemsResponse.data.total // 如果有筛选，更新总数
      }
    };
  }
}
