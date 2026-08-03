import { isAxiosError } from 'axios'
import type { ApiErrorBody } from '@/api/types'
import { getErrorMessage } from '@/lib/errors'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'

/** Map backend ErrorResponse.details[] onto form field keys when names match. */
export function mapApiErrorsToFields(error: unknown): BillingFieldErrors {
  if (!isAxiosError(error)) return {}
  const body = error.response?.data as ApiErrorBody | undefined
  const details = body?.error?.details
  if (!details?.length) {
    const field = body?.error?.field
    if (field && isFormField(field)) {
      return { [field]: body?.error?.message ?? 'Invalid value' }
    }
    return {}
  }

  const mapped: BillingFieldErrors = {}
  for (const detail of details) {
    if (detail.field && isFormField(detail.field)) {
      mapped[detail.field] = detail.message
    }
  }
  return mapped
}

export function getBillingErrorMessage(error: unknown): string {
  return getErrorMessage(error, 'Unable to save Billing Department Request. Please try again.')
}

function isFormField(field: string): field is keyof BillingFormValues {
  return field in {
    requestType: true,
    clientId: true,
    campaignId: true,
    assignedTo: true,
    priority: true,
    status: true,
    reasonForImportance: true,
    dailyIssueReport: true,
    approxNumberOfCoverages: true,
    approxRevenueImpact: true,
    requestedDueDate: true,
    parentCaseId: true,
    effectiveDate: true,
    segmentId: true,
    productId: true,
    anticipatedReleaseDate: true,
    requestDescription: true,
    billingExtractType: true,
    preNoteRequestType: true,
    billingInstitution: true,
    targetPostDate: true,
    billSet: true,
    billingCycle: true,
    priorHardDeclines: true,
    hardDeclineCodes: true,
    billingHoldType: true,
    holdLevelCodes: true,
    holdReason: true,
    billingHoldByProductId: true,
    version: true,
  }
}
