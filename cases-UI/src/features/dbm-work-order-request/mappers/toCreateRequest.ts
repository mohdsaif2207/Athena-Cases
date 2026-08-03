import type { CreateDbmWorkOrderRequest } from '../types/api'
import type { DbmCreateFormState } from '../types/form'

function blankToNull(value: string): string | null {
  const trimmed = value.trim()
  return trimmed === '' ? null : trimmed
}

function toOptionalNonNegativeInt(value: string): number | null {
  const trimmed = value.trim()
  if (trimmed === '') {
    return null
  }
  const parsed = Number(trimmed)
  if (!Number.isFinite(parsed) || parsed < 0) {
    return null
  }
  return Math.trunc(parsed)
}

/**
 * Maps create-form UI state to `CreateDbmWorkOrderRequest`.
 * Form field `field` maps to API `matchbackField`.
 */
export function toCreateDbmWorkOrderRequest(
  form: DbmCreateFormState,
): CreateDbmWorkOrderRequest {
  return {
    vendor: form.vendor.trim(),
    requestedDueDate: form.requestedDueDate,
    priority: form.priority.trim(),
    subject: form.subject.trim(),
    status: form.status.trim(),
    description: blankToNull(form.description),
    coreProcessorConversion: form.coreProcessorConversion,
    transferType: form.transferType.trim(),
    coverageLevels: [...form.coverageLevels],
    returnFileExpected: form.returnFileExpected.trim(),
    pgpKeyAtAcxiom: blankToNull(form.pgpKeyAtAcxiom),
    requestedAccountTypes: [...form.requestedAccountTypes],
    expectedQuantity: toOptionalNonNegativeInt(form.expectedQuantity),
    frequency: blankToNull(form.frequency),
    specialInstructions: blankToNull(form.specialInstructions),
    clientId: form.clientId.trim(),
    spokenKeys: [...form.spokenKeys],
    eventId: blankToNull(form.eventId),
    mediaIds: blankToNull(form.mediaIds),
    mailMonth: blankToNull(form.mailMonth),
    mediaOutQuantity: toOptionalNonNegativeInt(form.mediaOutQuantity),
    changesToMatchbackDb: form.changesToMatchbackDb,
    selectionCriteria: blankToNull(form.selectionCriteria),
    matchbackField: blankToNull(form.field),
    changeTo: blankToNull(form.changeTo),
    dbmWorkOrderNumber: blankToNull(form.dbmWorkOrderNumber),
    dbmCompletionNotes: blankToNull(form.dbmCompletionNotes),
    totalRecordsUpdated: toOptionalNonNegativeInt(form.totalRecordsUpdated),
  }
}
