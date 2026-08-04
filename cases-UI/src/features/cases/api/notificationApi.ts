import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import { formatAppDate } from '@/features/cases/utils/dateFormat'
import type { NotificationRecord } from '@/features/cases/types'

interface NotificationQueueItemDto {
  id: number
  notificationId: string
  caseId: number
  messageKey: string
  messageId: string
  messageName: string
  messageObject: string
  message: string
  receivedAt: string
  updatedAt?: string
  details: string
  createdBy?: string
  priority?: string
  owner?: string
  receivingTeamCode?: string
}

export async function fetchNotifications(): Promise<NotificationRecord[]> {
  const { data } = await apiClient.get<ApiSuccess<NotificationQueueItemDto[]>>('/api/v1/notifications')
  return (data.data ?? []).map(mapNotification)
}

export async function updateNotification(
  id: number,
  body: { messageName: string; message: string; details: string },
): Promise<NotificationRecord> {
  const { data } = await apiClient.put<ApiSuccess<NotificationQueueItemDto>>(
    `/api/v1/notifications/${id}`,
    body,
  )
  return mapNotification(data.data)
}

function mapNotification(dto: NotificationQueueItemDto): NotificationRecord {
  return {
    id: dto.id,
    notificationId: dto.notificationId,
    caseId: dto.caseId,
    messageKey: dto.messageKey,
    messageId: dto.messageId,
    messageName: dto.messageName,
    messageObject: dto.messageObject ?? '',
    message: dto.message,
    receivedDate: formatReceived(dto.receivedAt),
    updatedDate: formatReceived(dto.updatedAt ?? dto.receivedAt),
    details: dto.details ?? '',
    createdBy: dto.createdBy ?? '',
    priority: dto.priority ?? '',
    owner: dto.owner ?? '',
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
