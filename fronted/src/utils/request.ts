import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 不需要认证的接口
const noAuthUrls = ['/user/register', '/user/login']

// 请求拦截器
request.interceptors.request.use(
  config => {
    // 检查是否是无需认证的接口
    const isNoAuthUrl = noAuthUrls.some(url => config.url?.includes(url))
    
    // 获取token
    const token = localStorage.getItem('token')
    
    // 如果需要认证且有token，则添加认证头
    if (!isNoAuthUrl && token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    
    // 针对特定请求增加超时时间
    if (config.url?.includes('problem/program/add') || config.url?.includes('problem/program/update') || 
        config.url?.includes('problem/program/add') || config.url?.includes('problem/program/update')) {
      config.timeout = 30000; // 程序题提交增加到30秒
      console.log('编程题请求已设置超时时间为30秒');
    }
    
    // 打印请求信息
    console.log('发送请求:', {
      url: config.url,
      method: config.method,
      data: config.data,
      headers: config.headers,
      baseURL: config.baseURL,
      fullPath: `${config.baseURL}${config.url}`,
      needAuth: !isNoAuthUrl,
      hasToken: !!token,
      timeout: config.timeout
    })
    
    // 添加详细的请求数据日志
    if (config.data) {
      // 避免大数据日志输出
      if (typeof config.data === 'object' && !(config.data instanceof FormData)) {
        console.log('请求数据概要:', {
          type: typeof config.data,
          keys: Object.keys(config.data),
          size: JSON.stringify(config.data).length + ' bytes'
        });
      } else if (config.data instanceof FormData) {
        console.log('FormData请求:', {
          type: 'FormData',
          entries: Array.from((config.data as FormData).entries()).map(([key]) => key)
        });
      } else {
        console.log('请求数据:', config.data);
      }
    }
    
    return config
  },
  error => {
    console.error('请求配置错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    console.log('收到响应:', {
      status: response.status,
      url: response.config.url,
      dataType: typeof response.data
    });
    
    // 添加详细的响应数据日志
    if (response.data) {
      // 避免大数据日志输出
      const responseSize = JSON.stringify(response.data).length;
      if (responseSize > 1000) {
        console.log('响应数据概要:', {
          type: typeof response.data,
          size: responseSize + ' bytes',
          code: response.data.code,
          message: response.data.message,
          hasData: response.data.data !== undefined
        });
      } else {
        console.log('响应数据详情:', response.data);
      }
      
      // 检查响应格式
      if (response.data.code !== undefined) {
        const { code, message } = response.data;
        if (code !== 0) {
          // 处理业务错误
          console.error('业务错误:', message);
          
          // 不在这里弹出消息，而是让调用者处理
          // ElMessage.error(message || '请求失败');
        }
      }
    }
    
    return response.data; // 直接返回后端的数据结构
  },
  error => {
    console.error('请求错误:', error);
    
    // 错误处理
    if (error.response) {
      // 服务器返回错误
      const { status, data } = error.response;
      
      console.error('请求错误详情:', {
        status,
        data,
        url: error.config?.url,
        method: error.config?.method,
        headers: error.config?.headers
      });
      
      // 添加详细错误响应数据日志
      if (data) {
        console.error('错误响应详情:', JSON.stringify(data, null, 2))
      }
      
      if (status === 401) {
        // 未授权，清除token并跳转到登录页
        localStorage.removeItem('token');
        router.push('/login');
        ElMessage.error('登录已过期，请重新登录');
      } else if (status === 403) {
        ElMessage.error('权限不足');
      } else {
        // 其他错误
        ElMessage.error(data.message || '请求失败');
      }
    } else if (error.request) {
      // 请求已发送但没有收到响应
      ElMessage.error('服务器无响应，请稍后重试');
    } else {
      // 请求配置错误
      ElMessage.error('请求配置错误');
    }
    
    return Promise.reject(error);
  }
)

export default request 