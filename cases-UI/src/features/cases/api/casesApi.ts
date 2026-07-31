import { apiClient } from '@/api/client'
import type { ApiSuccess } from '@/api/types'
import { formatAppDate } from '@/features/cases/utils/dateFormat'
import type { CasePriority, CaseRecord, CaseStatus } from '@/features/cases/types'

interface CaseListItemDto {
  id: number
  caseId: string
  caseType: string
  caseTypeCode: string
  clientId: string
  subject: string
  description: string
  caseOwner: string
  caseStatus: string
  createdAt: string
  updatedAt: string
  requestedDueDate: string | null
  carrier: string
  priority: string
  assignedTo: string
  segmentId: string
  frequency: string
  spokenKey: string
  eventId: string
  mailMonth: string
}

/**
 * Loads cases for the Search grid from PostgreSQL via JWT-protected API.
 */
export async function fetchCases(): Promise<CaseRecord[]> {
  const { data } = await apiClient.get<ApiSuccess<CaseListItemDto[]>>('/api/v1/cases')
  const rows = data.data ?? []
  return rows
    .map(mapCase)
    .sort((a, b) => {
      const byUpdated = b.updatedDate.localeCompare(a.updatedDate)
      if (byUpdated !== 0) return byUpdated
      return b.createdDate.localeCompare(a.createdDate)
    })
}

function mapCase(dto: CaseListItemDto): CaseRecord {
  return {
    id: dto.id,
    caseId: dto.caseId,
    caseType: dto.caseType,
    clientId: dto.clientId ?? '',
    subject: dto.subject,
    description: dto.description ?? '',
    caseOwner: dto.caseOwner,
    caseStatus: dto.caseStatus as CaseStatus,
    createdDate: formatInstantDate(dto.createdAt),
    updatedDate: formatInstantDate(dto.updatedAt),
    requestedDueDate: dto.requestedDueDate ? formatIsoDate(dto.requestedDueDate) : '',
    carrier: dto.carrier ?? '',
    priority: (dto.priority as CasePriority) || 'Medium',
    assignedTo: dto.assignedTo ?? '',
    segmentId: dto.segmentId ?? '',
    frequency: dto.frequency ?? '',
    spokenKey: dto.spokenKey ?? '',
    eventId: dto.eventId ?? '',
    mailMonth: dto.mailMonth ?? '',
  }
}

function formatInstantDate(iso: string): string {
  if (!iso) return ''
  const date = new Date(iso)
  if (Number.isNaN(date.getTime())) return ''
  return formatAppDate(date)
}

function formatIsoDate(isoDate: string): string {
  // yyyy-MM-dd → mm/dd/yyyy
  const [y, m, d] = isoDate.split('-').map(Number)
  if (!y || !m || !d) return ''
  return formatAppDate(new Date(y, m - 1, d))
}
