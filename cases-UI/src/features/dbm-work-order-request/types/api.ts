/**
 * Mirrors backend `CreateDbmWorkOrderRequest`.
 * JSON property names must stay aligned with the Java record components.
 */
export type CreateDbmWorkOrderRequest = {
  vendor: string
  requestedDueDate: string
  priority: string
  subject: string
  status: string
  description: string | null
  coreProcessorConversion: boolean
  transferType: string
  coverageLevels: string[]
  returnFileExpected: string
  pgpKeyAtAcxiom: string | null
  requestedAccountTypes: string[]
  expectedQuantity: number | null
  frequency: string | null
  specialInstructions: string | null
  clientId: string
  spokenKeys: string[]
  eventId: string | null
  mediaIds: string | null
  mailMonth: string | null
  mediaOutQuantity: number | null
  changesToMatchbackDb: boolean
  selectionCriteria: string | null
  matchbackField: string | null
  changeTo: string | null
  dbmWorkOrderNumber: string | null
  dbmCompletionNotes: string | null
  totalRecordsUpdated: number | null
}

/** Mirrors backend `DbmWorkOrderResponse` (fields used by the create flow). */
export type DbmWorkOrderResponse = {
  caseId: number
  caseNumber: string
  caseType: string
  caseOwner: string
  requestedDueDate: string
  priority: string
  subject: string
  status: string
  description: string | null
  clientId: string
  pendingDbmApproval: boolean
  vendor: string
  coreProcessorConversion: boolean
  transferType: string
  coverageLevels: string[]
  returnFileExpected: string
  pgpKeyAtAcxiom: string | null
  requestedAccountTypes: string[]
  expectedQuantity: number | null
  frequency: string | null
  specialInstructions: string | null
  spokenKeys: string[]
  eventId: string | null
  mediaIds: string | null
  mailMonth: string | null
  mediaOutQuantity: number | null
  changesToMatchbackDb: boolean
  selectionCriteria: string | null
  matchbackField: string | null
  changeTo: string | null
  dbmWorkOrderNumber: string | null
  dbmCompletionNotes: string | null
  totalRecordsUpdated: number | null
  createdAt: string
  updatedAt: string
}
