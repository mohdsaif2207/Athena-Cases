/** Form state for the DBM Work Order Request Create screen. */
export type DbmCreateFormState = {
  vendor: string
  caseOwner: string
  requestedDueDate: string
  priority: string
  subject: string
  status: string
  description: string
  coreProcessorConversion: boolean
  transferType: string
  coverageLevels: string[]
  returnFileExpected: string
  pgpKeyAtAcxiom: string
  requestedAccountTypes: string[]
  expectedQuantity: string
  frequency: string
  specialInstructions: string
  clientId: string
  spokenKeys: string[]
  eventId: string
  mediaIds: string
  mailMonth: string
  mediaOutQuantity: string
  changesToMatchbackDb: boolean
  selectionCriteria: string
  field: string
  changeTo: string
  dbmWorkOrderNumber: string
  dbmCompletionNotes: string
  totalRecordsUpdated: string
}

export const DEFAULT_DBM_CREATE_FORM: DbmCreateFormState = {
  vendor: '',
  caseOwner: 'Logged-in user',
  requestedDueDate: '',
  priority: 'Medium',
  subject: '',
  status: 'Requested',
  description: '',
  coreProcessorConversion: false,
  transferType: '',
  coverageLevels: [],
  returnFileExpected: '',
  pgpKeyAtAcxiom: '',
  requestedAccountTypes: [],
  expectedQuantity: '',
  frequency: 'Once',
  specialInstructions: '',
  clientId: '',
  spokenKeys: [],
  eventId: '',
  mediaIds: '',
  mailMonth: '',
  mediaOutQuantity: '',
  changesToMatchbackDb: false,
  selectionCriteria: '',
  field: '',
  changeTo: '',
  dbmWorkOrderNumber: '',
  dbmCompletionNotes: '',
  totalRecordsUpdated: '',
}
