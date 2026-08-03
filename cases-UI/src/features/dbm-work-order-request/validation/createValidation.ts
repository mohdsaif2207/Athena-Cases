import type { DbmCreateFormState } from '../types/form'

export const TRANSFER_TYPE_OTHER = 'Other (Requires Description)'
export const TRANSFER_TYPE_CUSTOM = 'Custom (Requires Approval)'

const NUMERIC_VALUE_MESSAGE = 'Please enter a numeric value.'

/** Story §9.1 mandatory create fields. */
export type DbmMandatoryField =
  | 'vendor'
  | 'requestedDueDate'
  | 'priority'
  | 'subject'
  | 'status'
  | 'transferType'
  | 'returnFileExpected'
  | 'clientId'

/** Fields that can show create-form validation messages. */
export type DbmCreateValidatedField =
  | DbmMandatoryField
  | 'specialInstructions'
  | 'expectedQuantity'
  | 'mediaOutQuantity'
  | 'totalRecordsUpdated'

export type DbmCreateFieldErrors = Partial<
  Record<DbmCreateValidatedField, string>
>

type MandatoryRule = {
  field: DbmMandatoryField
  label: string
}

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

const NUMERIC_OPTIONAL_FIELDS = [
  'expectedQuantity',
  'mediaOutQuantity',
  'totalRecordsUpdated',
] as const satisfies ReadonlyArray<DbmCreateValidatedField>

/** True when Special Instructions is required by Transfer Type. */
export function isSpecialInstructionsRequired(transferType: string): boolean {
  return transferType === TRANSFER_TYPE_OTHER
}

/** True when the Custom approval UI hint should show. */
export function showsCustomApprovalHint(transferType: string): boolean {
  return transferType === TRANSFER_TYPE_CUSTOM
}

/** Empty is allowed; otherwise must be digits only (non-negative integer text). */
export function isOptionalNumericValue(value: string): boolean {
  const trimmed = value.trim()
  if (trimmed === '') {
    return true
  }
  return /^\d+$/.test(trimmed)
}

/**
 * Validates story-mandated create fields, Transfer Type → Special Instructions,
 * and optional numeric quantity fields.
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

  if (
    isSpecialInstructionsRequired(form.transferType) &&
    form.specialInstructions.trim() === ''
  ) {
    errors.specialInstructions = 'Special Instructions is required.'
  }

  for (const field of NUMERIC_OPTIONAL_FIELDS) {
    if (!isOptionalNumericValue(form[field])) {
      errors[field] = NUMERIC_VALUE_MESSAGE
    }
  }

  return errors
}

export function hasDbmCreateFieldErrors(errors: DbmCreateFieldErrors): boolean {
  return Object.keys(errors).length > 0
}
