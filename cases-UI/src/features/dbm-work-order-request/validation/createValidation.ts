import type { DbmCreateFormState } from '../types/form'

export const TRANSFER_TYPE_OTHER = 'Other (Requires Description)'
export const TRANSFER_TYPE_CUSTOM = 'Custom (Requires Approval)'

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

/** Fields that can show create-form validation messages (includes conditional). */
export type DbmCreateValidatedField = DbmMandatoryField | 'specialInstructions'

export type DbmCreateFieldErrors = Partial<Record<DbmCreateValidatedField, string>>

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

/** True when Special Instructions is required by Transfer Type. */
export function isSpecialInstructionsRequired(transferType: string): boolean {
  return transferType === TRANSFER_TYPE_OTHER
}

/** True when the Custom approval UI hint should show. */
export function showsCustomApprovalHint(transferType: string): boolean {
  return transferType === TRANSFER_TYPE_CUSTOM
}

/**
 * Validates story-mandated create fields plus Transfer Type → Special Instructions.
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

  if (
    isSpecialInstructionsRequired(form.transferType) &&
    form.specialInstructions.trim() === ''
  ) {
    errors.specialInstructions = 'Special Instructions is required.'
  }

  return errors
}

export function hasDbmCreateFieldErrors(errors: DbmCreateFieldErrors): boolean {
  return Object.keys(errors).length > 0
}
