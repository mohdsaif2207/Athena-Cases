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
