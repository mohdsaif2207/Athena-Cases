import * as XLSX from 'xlsx'
import type { NotificationRecord, WorkflowRecord } from '@/features/cases/types'

export function exportWorkflowsToExcel(rows: WorkflowRecord[]): void {
  const data = rows.map((r) => ({
    'Workflow ID': r.workflowId,
    'Message Key': r.messageKey,
    'Message ID': r.messageId,
    'Message Name': r.messageName,
    'Message Object': r.messageObject,
    Status: r.status,
    Decision: r.decision,
    Owner: r.owner,
    Priority: r.priority,
    'Received Date': r.receivedDate,
    Action: r.action,
    Logs: r.logs,
  }))
  writeSheet(data, 'Workflows', 'Workflow_Export')
}

export function exportNotificationsToExcel(rows: NotificationRecord[]): void {
  const data = rows.map((r) => ({
    'Notification ID': r.notificationId,
    'Message Key': r.messageKey,
    'Message ID': r.messageId,
    'Message Name': r.messageName,
    'Message Object': r.messageObject,
    Message: r.message,
    'Received Date': r.receivedDate,
    Details: r.details,
  }))
  writeSheet(data, 'Notifications', 'Notification_Export')
}

function writeSheet(data: Record<string, string>[], sheetName: string, filePrefix: string): void {
  const worksheet = XLSX.utils.json_to_sheet(data)
  const workbook = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(workbook, worksheet, sheetName)
  XLSX.writeFile(workbook, `${filePrefix}_${formatTimestamp(new Date())}.xlsx`)
}

function formatTimestamp(d: Date): string {
  const p = (n: number) => String(n).padStart(2, '0')
  return `${d.getFullYear()}${p(d.getMonth() + 1)}${p(d.getDate())}_${p(d.getHours())}${p(d.getMinutes())}${p(d.getSeconds())}`
}
