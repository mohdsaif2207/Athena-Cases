import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import { formatAppDate } from '@/features/cases/utils/dateFormat'
import type { WorkflowRecord } from '@/features/cases/types'

interface WorkflowQueueItemDto {
  id: number
  workflowId: string
  caseId: number
  messageKey: string
  messageId: string
  messageName: string
  messageObject: string
  status: string
  decision: string
  owner: string
  priority: string
  receivedAt: string
  action: string
  logs: string
}

export async function fetchWorkflows(): Promise<WorkflowRecord[]> {
  const { data } = await apiClient.get<ApiSuccess<WorkflowQueueItemDto[]>>('/api/v1/workflows')
  return (data.data ?? []).map((dto) => ({
    id: dto.id,
    workflowId: dto.workflowId,
    caseId: dto.caseId,
    messageKey: dto.messageKey,
    messageId: dto.messageId,
    messageName: dto.messageName,
    messageObject: dto.messageObject ?? '',
    status: dto.status,
    decision: dto.decision ?? '',
    owner: dto.owner ?? '',
    priority: dto.priority,
    receivedDate: formatReceived(dto.receivedAt),
    action: dto.action ?? '',
    logs: dto.logs ?? '',
  }))
}

function formatReceived(iso: string): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return formatAppDate(date)
}
