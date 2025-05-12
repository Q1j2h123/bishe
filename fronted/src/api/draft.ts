// import request from '@/utils/request'
// import type { BaseResponse } from '@/types/response'

// // 草稿请求接口
// export interface DraftSolutionRequest {
//   problemId: number
//   type: string
//   content: string
//   language?: string
// }

// // 草稿响应接口
// export interface DraftSolution {
//   id?: number
//   userId?: number
//   problemId: number
//   type: string
//   content: string
//   language?: string
//   lastSaveTime?: string
//   createTime?: string
// }

// // 草稿API
// export const draftApi = {
//   // 保存草稿
//   saveDraft(data: DraftSolutionRequest): Promise<BaseResponse<boolean>> {
//     return request.post('draft/save', data)
//   },
  
//   // 获取草稿
//   getDraft(problemId: number): Promise<BaseResponse<DraftSolution | null>> {
//     return request.get(`draft/get?problemId=${problemId}`)
//   },
  
//   // 删除草稿
//   deleteDraft(problemId: number): Promise<BaseResponse<boolean>> {
//     return request.delete(`draft/delete?problemId=${problemId}`)
//   }
// } 