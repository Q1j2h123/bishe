// src/types/response.ts
export interface BaseResponse<T = any> {
    code: number;
    data: T;
    message: string;
    success: boolean;
  }// 后端响应类型
