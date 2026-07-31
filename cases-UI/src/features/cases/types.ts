export type CaseStatus =
  | 'Requested'
  | 'In Progress'
  | 'On Hold'
  | 'Completed'
  | 'Canceled'

export type CasePriority = 'High' | 'Medium' | 'Low'

export interface CaseRecord {
  id: number
  caseId: string
  caseType: string
  clientId: string
  subject: string
  description: string
  caseOwner: string
  caseStatus: CaseStatus
  createdDate: string
  updatedDate: string
  requestedDueDate: string
  carrier: string
  priority: CasePriority
  assignedTo: string
  segmentId: string
  frequency: string
  spokenKey: string
  eventId: string
  mailMonth: string
}

export type CaseColumnKey =
  | 'caseId'
  | 'caseType'
  | 'clientId'
  | 'subject'
  | 'caseOwner'
  | 'caseStatus'
  | 'createdDate'
  | 'requestedDueDate'
  | 'action'

export interface CaseColumnDef {
  key: CaseColumnKey
  label: string
  filterable: boolean
  hideable: boolean
  defaultVisible: boolean
}

export interface ColumnFilterState {
  caseId: string
  caseType: string
  clientId: string
  subject: string
  caseOwner: string
  caseStatus: string
  createdDate: string
  requestedDueDate: string
}

export interface AdvancedFilterState {
  carrier: string
  priority: string
  assignedTo: string
  segmentId: string
  frequency: string
  spokenKey: string
  eventId: string
  mailMonth: string
}

export interface ColumnPreference {
  key: CaseColumnKey
  visible: boolean
}

export type CaseDetailMode = 'view' | 'edit' | null

export interface WorkflowRecord {
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
  receivedDate: string
  action: string
  logs: string
}

export interface NotificationRecord {
  id: number
  notificationId: string
  caseId: number
  messageKey: string
  messageId: string
  messageName: string
  messageObject: string
  message: string
  receivedDate: string
  details: string
}
