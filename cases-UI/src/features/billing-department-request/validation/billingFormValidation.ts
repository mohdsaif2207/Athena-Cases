import { parseAppDate } from '@/features/cases/utils/dateFormat'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import {
  PRIORITY_OPTIONS,
  STATUS_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'

export type BillingFieldErrors = Partial<
  Record<keyof BillingFormValues | 'caseOwner', string>
>

const PRIORITY_RE = /^(High|Medium|Low)$/
const STATUS_RE = /^(Requested|In Progress|Killed|On Hold|Incomplete|Completed)$/
const INT_RE = /^-?\d+$/
/** Mirrors @Digits(integer=16, fraction=2) — optional sign, up to 16 integer digits, up to 2 fraction. */
const DECIMAL_RE = /^-?\d{1,16}(\.\d{1,2})?$/

export interface ValidateBillingFormOptions {
  /** Logged-in username shown as Case Owner (User Story mandatory). */
  caseOwner?: string
}

/**
 * Client-side checks that mirror BillingDepartmentRequestCreate/UpdateRequest Bean Validation
 * plus User Story mandatory fields (Case Owner, Priority, Status).
 * Request Description is optional (max 5000).
 */
export function validateBillingForm(
  values: BillingFormValues,
  mode: 'create' | 'edit',
  options: ValidateBillingFormOptions = {},
): BillingFieldErrors {
  const errors: BillingFieldErrors = {}

  if (!options.caseOwner?.trim()) {
    errors.caseOwner = 'Case Owner is required'
  }

  if (!values.priority?.trim()) {
    errors.priority = 'Priority is required'
  } else if (
    !PRIORITY_RE.test(values.priority) ||
    !(PRIORITY_OPTIONS as readonly string[]).includes(values.priority)
  ) {
    errors.priority = 'priority must be High, Medium, or Low'
  }

  if (!values.status?.trim()) {
    errors.status = 'Status is required'
  } else if (
    !STATUS_RE.test(values.status) ||
    !(STATUS_OPTIONS as readonly string[]).includes(values.status)
  ) {
    errors.status = 'status must be a defined case status value'
  }

  const description = values.requestDescription.trim()
  if (description.length > 5000) {
    errors.requestDescription = 'size must be between 0 and 5000'
  }

  if (values.reasonForImportance.length > 500) {
    errors.reasonForImportance = 'size must be between 0 and 500'
  }
  if (values.campaignId.length > 64) {
    errors.campaignId = 'size must be between 0 and 64'
  }
  if (values.assignedTo.length > 64) {
    errors.assignedTo = 'size must be between 0 and 64'
  }
  if (values.billingInstitution.length > 255) {
    errors.billingInstitution = 'size must be between 0 and 255'
  }
  if (values.billSet.length > 255) {
    errors.billSet = 'size must be between 0 and 255'
  }
  if (values.billingCycle.length > 255) {
    errors.billingCycle = 'size must be between 0 and 255'
  }
  if (values.hardDeclineCodes.length > 500) {
    errors.hardDeclineCodes = 'size must be between 0 and 500'
  }
  if (values.holdReason.length > 1000) {
    errors.holdReason = 'size must be between 0 and 1000'
  }

  if (values.approxNumberOfCoverages.trim()) {
    if (!INT_RE.test(values.approxNumberOfCoverages.trim())) {
      errors.approxNumberOfCoverages = 'must be a number'
    }
  }

  if (values.approxRevenueImpact.trim()) {
    if (!DECIMAL_RE.test(values.approxRevenueImpact.trim())) {
      errors.approxRevenueImpact =
        'numeric value out of bounds (<16 digits>.<2 digits> expected)'
    }
  }

  for (const key of [
    'requestedDueDate',
    'effectiveDate',
    'anticipatedReleaseDate',
    'targetPostDate',
  ] as const) {
    const raw = values[key].trim()
    if (raw && !parseAppDate(raw)) {
      errors[key] = 'Enter a valid date as mm/dd/yyyy.'
    }
  }

  if (mode === 'edit' && values.version == null) {
    errors.version = 'version is required'
  }

  return errors
}
