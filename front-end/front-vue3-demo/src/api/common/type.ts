/** 分页请求参数 */
export interface PageParam {
  page: number
  pageSize: number
}

/** 分页结果 */
export interface PageResult<T = any> {
  total: number
  list: T[]
}

/** 基础查询参数 */
export interface BaseQuery extends PageParam {
  keyword?: string
  startTime?: string
  endTime?: string
}
