import type { CreateDbmWorkOrderRequest, DbmWorkOrderResponse } from '../types/api'
import type { DbmCreateFormState } from '../types/form'
import { DEFAULT_DBM_CREATE_FORM } from '../types/form'

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
 * When {@code includeDbmOnly} is false, Section 4 fields are omitted (non-DBM users).
 */
export function toCreateDbmWorkOrderRequest(
  form: DbmCreateFormState,
  includeDbmOnly = true,
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
    dbmWorkOrderNumber: includeDbmOnly
      ? blankToNull(form.dbmWorkOrderNumber)
      : null,
    dbmCompletionNotes: includeDbmOnly
      ? blankToNull(form.dbmCompletionNotes)
      : null,
    totalRecordsUpdated: includeDbmOnly
      ? toOptionalNonNegativeInt(form.totalRecordsUpdated)
      : null,
  }
}

/** Maps API response into form state for view mode (Billing-style details). */
export function toFormFromDbmResponse(
  response: DbmWorkOrderResponse,
): DbmCreateFormState {
  return {
    ...DEFAULT_DBM_CREATE_FORM,
    vendor: response.vendor ?? '',
    caseOwner: response.caseOwner ?? '',
    requestedDueDate: response.requestedDueDate ?? '',
    priority: response.priority ?? 'Medium',
    subject: response.subject ?? '',
    status: response.status ?? 'Requested',
    description: response.description ?? '',
    coreProcessorConversion: response.coreProcessorConversion ?? false,
    transferType: response.transferType ?? '',
    coverageLevels: [...(response.coverageLevels ?? [])],
    returnFileExpected: response.returnFileExpected ?? '',
    pgpKeyAtAcxiom: response.pgpKeyAtAcxiom ?? '',
    requestedAccountTypes: [...(response.requestedAccountTypes ?? [])],
    expectedQuantity:
      response.expectedQuantity == null ? '' : String(response.expectedQuantity),
    frequency: response.frequency ?? 'Once',
    specialInstructions: response.specialInstructions ?? '',
    clientId: response.clientId ?? '',
    spokenKeys: [...(response.spokenKeys ?? [])],
    eventId: response.eventId ?? '',
    mediaIds: response.mediaIds ?? '',
    mailMonth: response.mailMonth ?? '',
    mediaOutQuantity:
      response.mediaOutQuantity == null ? '' : String(response.mediaOutQuantity),
    changesToMatchbackDb: response.changesToMatchbackDb ?? false,
    selectionCriteria: response.selectionCriteria ?? '',
    field: response.matchbackField ?? '',
    changeTo: response.changeTo ?? '',
    dbmWorkOrderNumber: response.dbmWorkOrderNumber ?? '',
    dbmCompletionNotes: response.dbmCompletionNotes ?? '',
    totalRecordsUpdated:
      response.totalRecordsUpdated == null
        ? ''
        : String(response.totalRecordsUpdated),
  }
}

/** Path used when opening an existing DBM case (optional). Post-save goes to /cases. */
export function dbmCaseViewPath(caseId: number): string {
  return `/cases/new/dbm?caseId=${caseId}&mode=view`
}

/** True when the grid row is a DBM Work Order Request. */
export function isDbmCaseRecord(row: {
  caseId: string
  caseType: string
}): boolean {
  return (
    row.caseId.toUpperCase().startsWith('DBM') ||
    /DBM Work Order/i.test(row.caseType)
  )
}

/**
 * Builds an update payload from the current DBM record + header fields edited in the Cases grid modal.
 * Converts display dates (mm/dd/yyyy) to API ISO dates (yyyy-MM-dd).
 */
export function toUpdateRequestFromGridEdit(
  current: DbmWorkOrderResponse,
  grid: {
    clientId: string
    subject: string
    caseStatus: string
    priority: string
    requestedDueDate: string
    description: string
  },
): CreateDbmWorkOrderRequest {
  const dueIso = appDateToIso(grid.requestedDueDate) ?? current.requestedDueDate
  return {
    vendor: current.vendor,
    requestedDueDate: dueIso,
    priority: grid.priority.trim() || current.priority,
    subject: grid.subject.trim() || current.subject,
    status: grid.caseStatus.trim() || current.status,
    description: blankToNull(grid.description) ?? current.description,
    coreProcessorConversion: current.coreProcessorConversion,
    transferType: current.transferType,
    coverageLevels: [...(current.coverageLevels ?? [])],
    returnFileExpected: current.returnFileExpected,
    pgpKeyAtAcxiom: current.pgpKeyAtAcxiom,
    requestedAccountTypes: [...(current.requestedAccountTypes ?? [])],
    expectedQuantity: current.expectedQuantity,
    frequency: current.frequency,
    specialInstructions: current.specialInstructions,
    clientId: grid.clientId.trim() || current.clientId,
    spokenKeys: [...(current.spokenKeys ?? [])],
    eventId: current.eventId,
    mediaIds: current.mediaIds,
    mailMonth: current.mailMonth,
    mediaOutQuantity: current.mediaOutQuantity,
    changesToMatchbackDb: current.changesToMatchbackDb,
    selectionCriteria: current.selectionCriteria,
    matchbackField: current.matchbackField,
    changeTo: current.changeTo,
    dbmWorkOrderNumber: current.dbmWorkOrderNumber,
    dbmCompletionNotes: current.dbmCompletionNotes,
    totalRecordsUpdated: current.totalRecordsUpdated,
  }
}

function appDateToIso(value: string): string | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  // Already ISO
  if (/^\d{4}-\d{2}-\d{2}$/.test(trimmed)) return trimmed
  const match = /^(0[1-9]|1[0-2])\/(0[1-9]|[12]\d|3[01])\/(\d{4})$/.exec(trimmed)
  if (!match) return null
  const [, mm, dd, yyyy] = match
  return `${yyyy}-${mm}-${dd}`
}
