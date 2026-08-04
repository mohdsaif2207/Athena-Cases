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
  updatedAt?: string
  action: string
  logs: string
  createdBy?: string
  receivingTeamCode?: string
}

export async function fetchWorkflows(): Promise<WorkflowRecord[]> {
  const { data } = await apiClient.get<ApiSuccess<WorkflowQueueItemDto[]>>('/api/v1/workflows')
  return (data.data ?? []).map(mapWorkflow)
}

export async function updateWorkflow(
  id: number,
  body: { status: string; decision: string; priority: string; owner: string },
): Promise<WorkflowRecord> {
  const { data } = await apiClient.put<ApiSuccess<WorkflowQueueItemDto>>(`/api/v1/workflows/${id}`, body)
  return mapWorkflow(data.data)
}

function mapWorkflow(dto: WorkflowQueueItemDto): WorkflowRecord {
  return {
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
    updatedDate: formatReceived(dto.updatedAt ?? dto.receivedAt),
    action: dto.action ?? '',
    logs: dto.logs ?? '',
    createdBy: dto.createdBy ?? '',
    receivingTeamCode: dto.receivingTeamCode ?? '',
  }
}

function formatReceived(iso: string): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  const d = formatAppDate(date)
  const hh = String(date.getHours()).padStart(2, '0')
  const mm = String(date.getMinutes()).padStart(2, '0')
  const ss = String(date.getSeconds()).padStart(2, '0')
  return `${d} ${hh}:${mm}:${ss}`
}
