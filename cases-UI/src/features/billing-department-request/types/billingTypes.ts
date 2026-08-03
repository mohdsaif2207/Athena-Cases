import type {
  BillingExtractTypeOption,
  BillingHoldLevelOption,
  BillingHoldTypeOption,
  PreNoteRequestTypeOption,
  PriorHardDeclinesOption,
  PriorityOption,
  RequestTypeOption,
  StatusOption,
} from '@/features/billing-department-request/constants/billingEnums'

/**
 * Create-form UI model — mirrors LLD field catalogue (no API mapping yet).
 * Lookup-backed ids stay strings until Phase 4 wiring.
 */
export interface BillingCreateFormValues {
  requestType: RequestTypeOption | ''
  clientId: string
  campaignId: string
  assignedTo: string
  priority: PriorityOption
  status: StatusOption
  reasonForImportance: string
  dailyIssueReport: boolean
  approxNumberOfCoverages: string
  approxRevenueImpact: string
  requestedDueDate: string
  parentCaseId: string
  effectiveDate: string
  segmentId: string
  productId: string
  anticipatedReleaseDate: string
  requestDescription: string
  billingExtractType: BillingExtractTypeOption | ''
  preNoteRequestType: PreNoteRequestTypeOption | ''
  billingInstitution: string
  targetPostDate: string
  billSet: string
  billingCycle: string
  priorHardDeclines: PriorHardDeclinesOption | ''
  hardDeclineCodes: string
  billingHoldType: BillingHoldTypeOption | ''
  holdLevelCodes: BillingHoldLevelOption[]
  holdReason: string
  billingHoldByProductId: string
}

export function createDefaultBillingFormValues(): BillingCreateFormValues {
  return {
    requestType: '',
    clientId: '',
    campaignId: '',
    assignedTo: '',
    priority: 'Medium',
    status: 'Requested',
    reasonForImportance: '',
    dailyIssueReport: false,
    approxNumberOfCoverages: '',
    approxRevenueImpact: '',
    requestedDueDate: '',
    parentCaseId: '',
    effectiveDate: '',
    segmentId: '',
    productId: '',
    anticipatedReleaseDate: '',
    requestDescription: '',
    billingExtractType: '',
    preNoteRequestType: '',
    billingInstitution: '',
    targetPostDate: '',
    billSet: '',
    billingCycle: '',
    priorHardDeclines: '',
    hardDeclineCodes: '',
    billingHoldType: '',
    holdLevelCodes: [],
    holdReason: '',
    billingHoldByProductId: '',
  }
}
