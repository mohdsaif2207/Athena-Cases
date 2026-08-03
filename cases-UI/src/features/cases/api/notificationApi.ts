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
  details: string
}

export async function fetchNotifications(): Promise<NotificationRecord[]> {
  const { data } = await apiClient.get<ApiSuccess<NotificationQueueItemDto[]>>('/api/v1/notifications')
  return (data.data ?? []).map((dto) => ({
    id: dto.id,
    notificationId: dto.notificationId,
    caseId: dto.caseId,
    messageKey: dto.messageKey,
    messageId: dto.messageId,
    messageName: dto.messageName,
    messageObject: dto.messageObject ?? '',
    message: dto.message,
    receivedDate: formatReceived(dto.receivedAt),
    details: dto.details ?? '',
  }))
}

function formatReceived(iso: string): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return formatAppDate(date)
}
