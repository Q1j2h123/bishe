import request from '@/utils/request'
import type { BaseResponse } from '@/types/response'

// 统计数据接口
export interface DashboardStats {
  problemCount: number;       // 题目总数
  userCount: number;          // 用户总数
  submissionCount: number;    // 提交总数
  passRate: number;           // 通过率
  todayActiveUsers: number;   // 今日活跃用户
  weeklyNewUsers: number;     // 本周新增用户
  problemDistribution: {      // 题目分布
    choice: number;           // 选择题数量
    judge: number;            // 判断题数量
    program: number;          // 编程题数量
  };
  difficultyDistribution: {   // 难度分布
    easy: number;             // 简单题目数量
    medium: number;           // 中等题目数量
    hard: number;             // 困难题目数量
  };
}

// 活动记录接口
export interface ActivityRecord {
  id: number;
  userId: number;
  username: string;
  action: string;
  targetType: string;
  targetId: number;
  targetName: string;
  time: string;
  ip: string;
}

// 提交记录统计接口
export interface SubmissionStats {
  totalSubmissions: number;   // 总提交数
  acceptedSubmissions: number; // 通过的提交数
  timeDistribution: {        // 最近7天每天的提交数
    date: string;
    count: number;
  }[];
  languageDistribution: {    // 语言使用分布
    language: string;
    count: number;
  }[];
}

// 首页统计数据
export interface HomeStats {
  totalProblems: number;      // 题目总数
  totalUsers: number;         // 用户总数
  totalSubmissions: number;   // 提交总数
}

// 仪表盘API
export const dashboardApi = {
  // 获取管理员统计数据
  getStats(): Promise<BaseResponse<DashboardStats>> {
    return request.get('/admin/dashboard/stats');
  },
  
  // 获取最近活动
  getRecentActivities(limit: number = 10): Promise<BaseResponse<ActivityRecord[]>> {
    return request.get('/admin/dashboard/activities', { params: { limit } });
  },
  
  // 获取提交统计
  getSubmissionStats(): Promise<BaseResponse<SubmissionStats>> {
    return request.get('/admin/dashboard/submissions');
  },

  // 获取首页统计数据
  getHomeStats(): Promise<BaseResponse<HomeStats>> {
    return request.get('/dashboard/home/stats');
  }
}; 