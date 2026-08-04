/** Billing API DTO shapes — mirror backend records / @JsonValue enums. */

export interface LookupItemDto {
  id: string
  code: string
  label: string
}

export interface BillingAssigneeDto {
  username: string
  displayName: string
}

export interface HoldLevelsLookupDto {
  available: string[]
}

/** Response from create / get / update — LLD §15.3 */
export interface BillingDepartmentRequestResponseDto {
  caseId: number
  caseNumber: string
  businessCaseId: string | null
  caseType: string | null
  caseOwner: string | null
  assignedTo: string | null
  priority: string | null
  status: string | null
  parentCaseId: number | null
  parentCaseNumber: string | null
  requestType: string | null
  clientId: string | null
  clientName: string | null
  campaignId: string | null
  reasonForImportance: string | null
  dailyIssueReport: boolean
  approxNumberOfCoverages: number | null
  approxRevenueImpact: number | string | null
  requestedDueDate: string | null
  effectiveDate: string | null
  segmentId: number | null
  segmentName: string | null
  productId: number | null
  productName: string | null
  anticipatedReleaseDate: string | null
  requestDescription: string
  billingExtractType: string | null
  preNoteRequestType: string | null
  billingInstitution: string | null
  targetPostDate: string | null
  billSet: string | null
  billingCycle: string | null
  priorHardDeclines: string | null
  hardDeclineCodes: string | null
  billingHoldType: string | null
  holdLevelCodes: string[] | null
  holdReason: string | null
  billingHoldByProductId: number | null
  version: number | null
  createdAt: string | null
  updatedAt: string | null
  workflowId: number | null
  workflowStatus: string | null
}

/** POST body — BillingDepartmentRequestCreateRequest */
export interface BillingDepartmentRequestCreatePayload {
  requestType: string | null
  clientId: string | null
  campaignId: string | null
  assignedTo: string | null
  priority: string
  status: string
  reasonForImportance: string | null
  dailyIssueReport: boolean | null
  approxNumberOfCoverages: number | null
  approxRevenueImpact: number | null
  requestedDueDate: string | null
  parentCaseId: number | null
  effectiveDate: string | null
  segmentId: number | null
  productId: number | null
  anticipatedReleaseDate: string | null
  requestDescription: string
  billingExtractType: string | null
  preNoteRequestType: string | null
  billingInstitution: string | null
  targetPostDate: string | null
  billSet: string | null
  billingCycle: string | null
  priorHardDeclines: string | null
  hardDeclineCodes: string | null
  billingHoldType: string | null
  holdLevelCodes: string[] | null
  holdReason: string | null
  billingHoldByProductId: number | null
}

/** PUT body — BillingDepartmentRequestUpdateRequest */
export type BillingDepartmentRequestUpdatePayload = BillingDepartmentRequestCreatePayload & {
  version: number
}
