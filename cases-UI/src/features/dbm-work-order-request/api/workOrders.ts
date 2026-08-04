import { apiClient } from '../../../api/client'
import type {
  CreateDbmWorkOrderRequest,
  DbmWorkOrderResponse,
} from '../types/api'

/** POST /api/dbm/work-orders — create a DBM Work Order Request. */
export async function createDbmWorkOrder(
  payload: CreateDbmWorkOrderRequest,
): Promise<DbmWorkOrderResponse> {
  const response = await apiClient.post<DbmWorkOrderResponse>(
    '/api/dbm/work-orders',
    payload,
  )
  return response.data
}

/** GET /api/dbm/work-orders/{caseId} — load for view/edit. */
export async function getDbmWorkOrder(
  caseId: number,
): Promise<DbmWorkOrderResponse> {
  const response = await apiClient.get<DbmWorkOrderResponse>(
    `/api/dbm/work-orders/${caseId}`,
  )
  return response.data
}

/** PUT /api/dbm/work-orders/{caseId} — update (triggers DBM team notification). */
export async function updateDbmWorkOrder(
  caseId: number,
  payload: CreateDbmWorkOrderRequest,
): Promise<DbmWorkOrderResponse> {
  const response = await apiClient.put<DbmWorkOrderResponse>(
    `/api/dbm/work-orders/${caseId}`,
    payload,
  )
  return response.data
}
