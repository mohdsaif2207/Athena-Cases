import { isValidAppDateString } from '@/features/cases/utils/dateFormat'
import type {
  AdvancedFilterState,
  CaseRecord,
  ColumnFilterState,
} from '@/features/cases/types'

export function applyCaseFilters(
  rows: CaseRecord[],
  columnFilters: ColumnFilterState,
  advanced: AdvancedFilterState,
): CaseRecord[] {
  return rows.filter((row) => {
    if (!matchesText(row.caseId, columnFilters.caseId)) return false
    if (!matchesText(row.caseType, columnFilters.caseType)) return false
    if (!matchesText(row.clientId, columnFilters.clientId)) return false
    if (!matchesText(row.subject, columnFilters.subject)) return false
    if (!matchesText(row.caseOwner, columnFilters.caseOwner)) return false
    if (!matchesText(row.caseStatus, columnFilters.caseStatus)) return false
    if (!matchesDateFilter(row.createdDate, columnFilters.createdDate)) return false
    if (!matchesDateFilter(row.requestedDueDate, columnFilters.requestedDueDate)) return false

    if (advanced.carrier && row.carrier !== advanced.carrier) return false
    if (advanced.priority && row.priority !== advanced.priority) return false
    if (advanced.assignedTo && row.assignedTo !== advanced.assignedTo) return false
    if (!matchesText(row.segmentId, advanced.segmentId)) return false
    if (advanced.frequency && row.frequency !== advanced.frequency) return false
    if (!matchesText(row.spokenKey, advanced.spokenKey)) return false
    if (!matchesText(row.eventId, advanced.eventId)) return false
    if (!matchesText(row.mailMonth, advanced.mailMonth)) return false

    return true
  })
}

function matchesText(value: string, query: string): boolean {
  if (!query.trim()) return true
  return value.toLowerCase().includes(query.trim().toLowerCase())
}

/** Applies date filters only when the query is a valid mm/dd/yyyy value. */
function matchesDateFilter(value: string, query: string): boolean {
  const trimmed = query.trim()
  if (!trimmed) return true
  if (!isValidAppDateString(trimmed)) return true
  return value === trimmed || value.toLowerCase().includes(trimmed.toLowerCase())
}

export function paginate<T>(rows: T[], page: number, pageSize: number): T[] {
  const start = page * pageSize
  return rows.slice(start, start + pageSize)
}
