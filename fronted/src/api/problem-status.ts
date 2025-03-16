import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'

// 用户题目状态接口
export interface UserProblemStatus {
  id?: number
  userId?: number
  problemId: number
  status: string
  lastSubmitTime?: string
}

// 用户题目状态VO接口
export interface UserProblemStatusVO {
  problemId: number
  problemTitle?: string
  type?: string
  status: string
  lastSubmitTime?: string
  difficulty?: string
  tags?: string[]
}

// 分页响应接口
export interface PaginationResponse<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}

// 用户题目状态API
export const problemStatusApi = {
  // 获取用户题目状态
  getUserProblemStatus(problemId: number): Promise<BaseResponse<UserProblemStatus | null>> {
    return request.get(`user/problem-status/get?problemId=${problemId}`)
  },
  
  // 获取用户已解决的题目列表
  getUserSolvedProblems(): Promise<BaseResponse<UserProblemStatusVO[]>> {
    return request.get('user/problem-status/solved')
  },
  
  // 获取用户尝试过的题目列表
  getUserAttemptedProblems(): Promise<BaseResponse<UserProblemStatusVO[]>> {
    return request.get('user/problem-status/attempted')
  },
  
  // 获取用户已解决的题目数量
  countUserSolvedProblems(): Promise<BaseResponse<number>> {
    return request.get('user/problem-status/count/solved')
  },
  
  // 获取用户尝试过的题目数量
  countUserAttemptedProblems(): Promise<BaseResponse<number>> {
    return request.get('user/problem-status/count/attempted')
  },
  
  // 分页获取用户题目状态列表
  getUserProblemStatusList(params: {
    status?: string
    current?: number
    size?: number
  }): Promise<BaseResponse<PaginationResponse<UserProblemStatusVO>>> {
    return request.get('user/problem-status/list/page', { params })
  },
  
  // 批量获取题目状态
  getBatchProblemStatus(problemIds: number[], forceRefresh: boolean = false): Promise<BaseResponse<Record<number, string>>> {
    // 添加详细日志
    console.log('批量获取题目状态请求开始, 题目IDs:', problemIds, '强制刷新:', forceRefresh);
    
    if (!problemIds || problemIds.length === 0) {
      console.warn('题目ID列表为空，无法获取状态');
      return Promise.resolve({
        code: 0,
        data: {} as Record<number, string>,
        message: '题目ID列表为空',
        success: true
      });
    }
    
    // 检查用户是否登录
    const token = localStorage.getItem('token');
    if (!token) {
      console.warn('用户未登录，不获取题目状态');
      return Promise.resolve({
        code: 0,
        data: {} as Record<number, string>,
        message: '用户未登录',
        success: true
      });
    }
    
    // 生成请求ID，用于跟踪日志
    const requestId = `status_batch_${Date.now()}`;
    console.log(`[${requestId}] 开始处理批量状态请求，题目数量: ${problemIds.length}，强制刷新:${forceRefresh}`);
    
    // 完全跳过缓存，直接使用fetch发送请求
    if (forceRefresh) {
      console.log(`[${requestId}] 强制刷新模式，清除所有缓存`);
      try {
        localStorage.removeItem('userProblemStatuses');
        localStorage.removeItem('statusCacheTime');
        localStorage.removeItem('problem_status_cache');
        localStorage.removeItem('currentProblemPage');
        console.log(`[${requestId}] 已清除所有状态相关缓存`);
      } catch (e) {
        console.error(`[${requestId}] 清除缓存失败:`, e);
      }
    } else {
      // 如果不强制刷新，先尝试从缓存获取
      try {
        const cacheKey = 'userProblemStatuses';
        const cachedData = localStorage.getItem(cacheKey);
        
        if (cachedData) {
          console.log(`[${requestId}] 找到缓存的状态数据`);
          const statusCache = JSON.parse(cachedData) as Record<number, string>;
          const now = Date.now();
          const cacheTime = parseInt(localStorage.getItem('statusCacheTime') || '0');
          
          // 如果缓存不超过5秒，使用缓存数据（减少缓存时间，防止数据过时）
          if (now - cacheTime < 5 * 1000) {
            console.log(`[${requestId}] 使用本地缓存的状态数据，上次更新时间:`, new Date(cacheTime).toISOString());
            
            // 过滤出已缓存的题目状态
            const result: Record<number, string> = {};
            let allCached = true;
            let cachedCount = 0;
            
            for (const id of problemIds) {
              if (statusCache[id]) {
                result[id] = statusCache[id];
                cachedCount++;
              } else {
                console.log(`[${requestId}] 题目 ${id} 在缓存中未找到状态`);
                allCached = false;
              }
            }
            
            console.log(`[${requestId}] 缓存命中率: ${cachedCount}/${problemIds.length}`);
            
            // 如果所有请求的ID都有缓存，直接返回缓存数据
            if (allCached) {
              console.log(`[${requestId}] 所有请求的题目状态都在缓存中，直接返回缓存数据:`, result);
              return Promise.resolve({
                code: 0,
                data: result,
                message: '从缓存获取题目状态成功',
                success: true
              });
            }
          } else {
            console.log(`[${requestId}] 缓存已过期，需要重新获取数据`);
          }
        }
      } catch (e) {
        console.error(`[${requestId}] 读取缓存时出错:`, e);
      }
    }
    
    return new Promise<BaseResponse<Record<number, string>>>((resolve) => {
      console.log(`[${requestId}] 开始使用fetch直接请求状态API`);
      
      // 构建请求URL
      const baseURL = import.meta.env.VITE_API_BASE_URL || '';
      
      // 确保API路径的一致性
      let apiPath = '/api/user/problem-status/batch';
      if (apiPath.startsWith('/') && baseURL.endsWith('/')) {
        apiPath = apiPath.substring(1);
      }
      
      const apiURL = `${baseURL}${apiPath}`;
      console.log(`[${requestId}] 请求URL:`, apiURL);
      
      // 请求连接超时检测
      let isRequestCompleted = false;
      let networkDiagnosticTimer = setTimeout(() => {
        if (!isRequestCompleted) {
          console.warn(`[${requestId}] 请求已经运行5秒钟但尚未收到响应，可能存在网络问题`);
          // 开始诊断网络
          try {
            const pingURL = `${baseURL}/api/ping`;
            fetch(pingURL, { 
              method: 'GET',
              headers: { 'Cache-Control': 'no-cache' },
              signal: AbortSignal.timeout(2000) // 2秒超时
            })
            .then(res => {
              if (res.ok) {
                console.log(`[${requestId}] 服务器ping测试成功，服务器可访问，可能是特定API问题`);
              }
            })
            .catch(() => {
              console.error(`[${requestId}] 服务器ping测试失败，可能是网络连接问题或服务器不可用`);
            });
          } catch (e) {
            console.error(`[${requestId}] 诊断网络时出错:`, e);
          }
        }
      }, 5000);
      
      // 准备POST请求
      const fetchPromise = fetch(apiURL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${token}`
        },
        body: JSON.stringify({ problemIds })
      })
      .then(response => {
        // 标记请求已完成
        isRequestCompleted = true;
        clearTimeout(networkDiagnosticTimer);
        
        // 记录原始响应
        console.log(`[${requestId}] 状态API响应状态:`, response.status, response.statusText);
        
        if (!response.ok) {
          throw new Error(`状态API请求失败: ${response.status} ${response.statusText}`);
        }
        
        return response.json();
      })
      .then(data => {
        // 处理响应数据
        console.log(`[${requestId}] 状态API返回的原始数据:`, data);
        
        let result: BaseResponse<Record<number, string>>;
        
        // 检查数据格式
        if ('code' in data && 'data' in data) {
          result = data as BaseResponse<Record<number, string>>;
        } else if (typeof data === 'object') {
          // 可能直接返回了状态映射
          result = {
            code: 0,
            data: data as Record<number, string>,
            message: '获取题目状态成功',
            success: true
          };
        } else {
          throw new Error('返回的数据格式不符合预期');
        }
        
        // 验证状态数据
        if (result.code === 0 && result.data) {
          console.log(`[${requestId}] 成功获取状态数据:`, result.data);
          
          // 更新本地缓存
          try {
            // 先读取现有缓存
            let statusCache: Record<number, string> = {};
            const existingCache = localStorage.getItem('userProblemStatuses');
            
            if (existingCache) {
              try {
                statusCache = JSON.parse(existingCache);
              } catch (e) {
                console.warn(`[${requestId}] 解析现有缓存失败，将创建新缓存`);
              }
            }
            
            // 合并新数据到缓存
            for (const [id, status] of Object.entries(result.data)) {
              const problemId = parseInt(id);
              if (!isNaN(problemId)) {
                statusCache[problemId] = status;
                console.log(`[${requestId}] 缓存题目 ${problemId} 的状态: ${status}`);
              }
            }
            
            // 保存新缓存
            if (Object.keys(statusCache).length > 0) {
              localStorage.setItem('userProblemStatuses', JSON.stringify(statusCache));
              localStorage.setItem('statusCacheTime', Date.now().toString());
              console.log(`[${requestId}] 状态缓存已更新, 题目数:`, Object.keys(statusCache).length);
            }
          } catch (e) {
            console.error(`[${requestId}] 更新缓存失败:`, e);
          }
        } else {
          console.warn(`[${requestId}] 状态API返回错误码或空数据:`, result);
        }
        
        resolve(result);
      })
      .catch(error => {
        // 标记请求已完成
        isRequestCompleted = true;
        clearTimeout(networkDiagnosticTimer);
        
        console.error(`[${requestId}] 状态API请求失败:`, error);
        
        // 尝试备用GET请求方式
        console.log(`[${requestId}] 尝试使用GET方式请求状态`);
        
        const queryParams = problemIds.map(id => `problemIds=${id}`).join('&');
        
        // 确保API路径的一致性
        let getApiPath = '/api/user/problem-status/batch';
        if (getApiPath.startsWith('/') && baseURL.endsWith('/')) {
          getApiPath = getApiPath.substring(1);
        }
        
        const getURL = `${baseURL}${getApiPath}?${queryParams}`;
        
        fetch(getURL, {
          method: 'GET',
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        .then(response => {
          console.log(`[${requestId}] GET状态API响应状态:`, response.status, response.statusText);
          
          if (!response.ok) {
            throw new Error(`GET状态API请求失败: ${response.status} ${response.statusText}`);
          }
          
          return response.json();
        })
        .then(data => {
          console.log(`[${requestId}] GET状态API返回的原始数据:`, data);
          
          let result: BaseResponse<Record<number, string>>;
          
          if ('code' in data && 'data' in data) {
            result = data as BaseResponse<Record<number, string>>;
          } else if (typeof data === 'object') {
            result = {
              code: 0,
              data: data as Record<number, string>,
              message: '通过GET获取题目状态成功',
              success: true
            };
          } else {
            throw new Error('GET返回的数据格式不符合预期');
          }
          
          // 更新缓存
          if (result.code === 0 && result.data) {
            try {
              // 先读取现有缓存
              let statusCache: Record<number, string> = {};
              const existingCache = localStorage.getItem('userProblemStatuses');
              
              if (existingCache) {
                try {
                  statusCache = JSON.parse(existingCache);
                } catch (e) {
                  console.warn(`[${requestId}] 解析现有缓存失败，将创建新缓存`);
                }
              }
              
              // 合并新数据
              for (const [id, status] of Object.entries(result.data)) {
                const problemId = parseInt(id);
                if (!isNaN(problemId)) {
                  statusCache[problemId] = status;
                }
              }
              
              if (Object.keys(statusCache).length > 0) {
                localStorage.setItem('userProblemStatuses', JSON.stringify(statusCache));
                localStorage.setItem('statusCacheTime', Date.now().toString());
                console.log(`[${requestId}] GET方法更新状态缓存, 题目数:`, Object.keys(statusCache).length);
              }
            } catch (e) {
              console.error(`[${requestId}] 更新缓存失败:`, e);
            }
          }
          
          resolve(result);
        })
        .catch(getError => {
          console.error(`[${requestId}] GET状态API请求也失败:`, getError);
          
          // 尝试从缓存恢复
          try {
            const cachedData = localStorage.getItem('userProblemStatuses');
            
            if (cachedData) {
              console.log(`[${requestId}] API请求全部失败，尝试从缓存恢复数据`);
              const statusCache = JSON.parse(cachedData) as Record<number, string>;
              
              const result: Record<number, string> = {};
              let recoveredCount = 0;
              
              for (const id of problemIds) {
                if (statusCache[id]) {
                  result[id] = statusCache[id];
                  recoveredCount++;
                }
              }
              
              if (recoveredCount > 0) {
                console.log(`[${requestId}] 已从缓存恢复 ${recoveredCount}/${problemIds.length} 个题目状态`);
                resolve({
                  code: 0,
                  data: result,
                  message: '从缓存恢复部分题目状态',
                  success: true
                });
                return;
              }
            }
          } catch (e) {
            console.error(`[${requestId}] 从缓存恢复失败:`, e);
          }
          
          // 所有方法都失败，返回空结果
          resolve({
            code: -1,
            data: {} as Record<number, string>,
            message: '获取题目状态完全失败',
            success: false
          });
        });
      });
      
      // 设置超时控制
      const timeoutPromise = new Promise<BaseResponse<Record<number, string>>>((resolve) => {
        setTimeout(() => {
          console.warn(`[${requestId}] 请求超时，将尝试从缓存恢复`);
          
          // 从缓存恢复数据
          try {
            const cachedData = localStorage.getItem('userProblemStatuses');
            if (cachedData) {
              const statusCache = JSON.parse(cachedData) as Record<number, string>;
              
              const result: Record<number, string> = {};
              let recoveredCount = 0;
              
              for (const id of problemIds) {
                if (statusCache[id]) {
                  result[id] = statusCache[id];
                  recoveredCount++;
                }
              }
              
              if (recoveredCount > 0) {
                console.log(`[${requestId}] 超时后从缓存恢复 ${recoveredCount}/${problemIds.length} 个题目状态`);
                resolve({
                  code: 0,
                  data: result,
                  message: '请求超时，从缓存恢复部分题目状态',
                  success: true
                });
                return;
              }
            }
          } catch (e) {
            console.error(`[${requestId}] 超时后从缓存恢复失败:`, e);
          }
          
          resolve({
            code: -1,
            data: {} as Record<number, string>,
            message: '请求超时，未能获取题目状态',
            success: false
          });
        }, 15000); // 15秒超时
      });
      
      // 使用Promise.race竞争解决
      Promise.race([fetchPromise, timeoutPromise])
        .catch(raceError => {
          console.error(`[${requestId}] 请求竞争过程中出错:`, raceError);
          // 这里不需要处理，因为各个Promise内部都有自己的错误处理
        });
    });
  }
} 