import * as XLSX from 'xlsx'
import type { CaseRecord } from '@/features/cases/types'

const EXPORT_HEADERS = [
  'Case ID',
  'Case Type',
  'Case Description',
  'Case Owner',
  'Case Status',
  'Created Date',
] as const

/** Builds Cases_Export_<yyyyMMdd_HHmmss>.xlsx from the filtered dataset. */
export function exportCasesToExcel(rows: CaseRecord[]): void {
  const data = rows.map((r) => ({
    'Case ID': r.caseId,
    'Case Type': r.caseType,
    'Case Description': r.description || r.subject,
    'Case Owner': r.caseOwner,
    'Case Status': r.caseStatus,
    'Created Date': r.createdDate,
  }))

  const worksheet = XLSX.utils.json_to_sheet(data, { header: [...EXPORT_HEADERS] })
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, 'Cases')

  const stamp = formatTimestamp(new Date())
  XLSX.writeFile(workbook, `Cases_Export_${stamp}.xlsx`)
}

function formatTimestamp(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}_${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
}
