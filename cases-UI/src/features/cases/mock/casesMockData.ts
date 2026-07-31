import type { CaseColumnDef, CaseColumnKey, CaseRecord } from '@/features/cases/types'

export const DEFAULT_COLUMNS: CaseColumnDef[] = [
  { key: 'caseId', label: 'Case ID', filterable: true, hideable: false, defaultVisible: true },
  { key: 'caseType', label: 'Case Type', filterable: true, hideable: true, defaultVisible: true },
  { key: 'clientId', label: 'Client ID', filterable: true, hideable: true, defaultVisible: true },
  { key: 'subject', label: 'Subject', filterable: true, hideable: true, defaultVisible: true },
  { key: 'caseOwner', label: 'Case Owner', filterable: true, hideable: true, defaultVisible: true },
  { key: 'caseStatus', label: 'Case Status', filterable: true, hideable: true, defaultVisible: true },
  { key: 'createdDate', label: 'Created Date', filterable: true, hideable: true, defaultVisible: true },
  {
    key: 'requestedDueDate',
    label: 'Requested Due Date',
    filterable: true,
    hideable: true,
    defaultVisible: true,
  },
  { key: 'action', label: 'Action', filterable: false, hideable: false, defaultVisible: true },
]

export const EMPTY_COLUMN_FILTERS = {
  caseId: '',
  caseType: '',
  clientId: '',
  subject: '',
  caseOwner: '',
  caseStatus: '',
  createdDate: '',
  requestedDueDate: '',
}

export const EMPTY_ADVANCED_FILTERS = {
  carrier: '',
  priority: '',
  assignedTo: '',
  segmentId: '',
  frequency: '',
  spokenKey: '',
  eventId: '',
  mailMonth: '',
}

const CASE_TYPES = [
  'Billing Department Request',
  'DBM Work Order Request',
  'ExRT Request',
  'Coverage Amount Request',
  'Report Request',
  'Project Tracker Request',
]

const ASSIGNEES = ['Billing Ops', 'DBM Team', 'ExRT Queue', 'Client Services', 'Unassigned']
const CARRIERS = ['Acme Life', 'Northstar', 'Allied Mutual', 'Pioneer Cover']
const FREQUENCIES = ['Once', 'Daily', 'Weekly', 'Monthly', 'Quarterly', 'Yearly']
const STATUSES = ['Requested', 'In Progress', 'On Hold', 'Completed', 'Canceled'] as const
const PRIORITIES = ['High', 'Medium', 'Low'] as const

/**
 * Case list payload source for the Search grid.
 * Empty until Case Search API returns records — keep structure for integration.
 */
export const MOCK_CASES: CaseRecord[] = []

export const LOOKUP_OPTIONS = {
  carriers: CARRIERS,
  priorities: [...PRIORITIES],
  assignees: ASSIGNEES,
  frequencies: FREQUENCIES,
  statuses: [...STATUSES],
  caseTypes: CASE_TYPES,
}

export function defaultColumnPreferences(): Array<{ key: CaseColumnKey; visible: boolean }> {
  return DEFAULT_COLUMNS.map((c) => ({ key: c.key, visible: c.defaultVisible }))
}
