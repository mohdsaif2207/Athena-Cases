import type { DbmCreateFormState, DbmMandatoryField } from '../types/form'

export type DbmCreateFieldErrors = Partial<Record<DbmMandatoryField, string>>

type MandatoryRule = {
  field: DbmMandatoryField
  label: string
}

/** Mandatory create fields from the DBM User Story / LLD §9.1 (no conditional rules). */
const MANDATORY_RULES: MandatoryRule[] = [
  { field: 'vendor', label: 'Vendor' },
  { field: 'requestedDueDate', label: 'Requested Due Date' },
  { field: 'priority', label: 'Priority' },
  { field: 'subject', label: 'Subject' },
  { field: 'status', label: 'Status' },
  { field: 'transferType', label: 'Transfer Type' },
  { field: 'returnFileExpected', label: 'Return File Expected' },
  { field: 'clientId', label: 'Client' },
]

/**
 * Validates only the story-mandated create fields.
 * Returns an empty object when valid.
 */
export function validateDbmCreateForm(
  form: DbmCreateFormState,
): DbmCreateFieldErrors {
  const errors: DbmCreateFieldErrors = {}

  for (const rule of MANDATORY_RULES) {
    const value = form[rule.field]
    if (typeof value !== 'string' || value.trim() === '') {
      errors[rule.field] = `${rule.label} is required.`
    }
  }

  return errors
}

export function hasDbmCreateFieldErrors(errors: DbmCreateFieldErrors): boolean {
  return Object.keys(errors).length > 0
}
