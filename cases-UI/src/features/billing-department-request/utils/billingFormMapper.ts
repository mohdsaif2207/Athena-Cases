import {
  formatAppDate,
  parseAppDate,
} from '@/features/cases/utils/dateFormat'
import type {
  BillingDepartmentRequestCreatePayload,
  BillingDepartmentRequestResponseDto,
  BillingDepartmentRequestUpdatePayload,
} from '@/features/billing-department-request/types/billingTypes'
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
import {
  BILLING_EXTRACT_TYPE_OPTIONS,
  BILLING_HOLD_LEVEL_OPTIONS,
  BILLING_HOLD_TYPE_OPTIONS,
  PRE_NOTE_REQUEST_TYPE_OPTIONS,
  PRIOR_HARD_DECLINES_OPTIONS,
  PRIORITY_OPTIONS,
  REQUEST_TYPE_OPTIONS,
  STATUS_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'

/** UI form model (string fields for controlled inputs). */
export interface BillingFormValues {
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
  /** Optimistic lock token from GET/create response — required on PUT. */
  version: number | null
}

export function createDefaultBillingFormValues(): BillingFormValues {
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
    version: null,
  }
}

/** API LocalDate (yyyy-MM-dd) → UI mm/dd/yyyy */
export function apiDateToApp(value: string | null | undefined): string {
  if (!value) return ''
  const match = /^(\d{4})-(\d{2})-(\d{2})/.exec(value)
  if (!match) return ''
  const [, y, m, d] = match
  const date = new Date(Number(y), Number(m) - 1, Number(d))
  return formatAppDate(date)
}

/** UI mm/dd/yyyy → API LocalDate (yyyy-MM-dd) or null */
export function appDateToApi(value: string): string | null {
  const parsed = parseAppDate(value)
  if (!parsed) return null
  const y = parsed.getFullYear()
  const m = String(parsed.getMonth() + 1).padStart(2, '0')
  const d = String(parsed.getDate()).padStart(2, '0')
  return `${y}-${m}-${d}`
}

function emptyToNull(value: string): string | null {
  const trimmed = value.trim()
  return trimmed === '' ? null : trimmed
}

function parseOptionalLong(value: string): number | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  const n = Number(trimmed)
  return Number.isFinite(n) ? n : null
}

function parseOptionalInt(value: string): number | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  if (!/^-?\d+$/.test(trimmed)) return null
  return Number(trimmed)
}

function parseOptionalDecimal(value: string): number | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  const n = Number(trimmed)
  return Number.isFinite(n) ? n : null
}

function asOption<T extends string>(value: string | null | undefined, allowed: readonly T[]): T | '' {
  if (!value) return ''
  return (allowed as readonly string[]).includes(value) ? (value as T) : ''
}

function asHoldLevels(codes: string[] | null | undefined): BillingHoldLevelOption[] {
  if (!codes?.length) return []
  return codes.filter((c): c is BillingHoldLevelOption =>
    (BILLING_HOLD_LEVEL_OPTIONS as readonly string[]).includes(c),
  )
}

export function responseToFormValues(dto: BillingDepartmentRequestResponseDto): BillingFormValues {
  return {
    requestType: asOption(dto.requestType, REQUEST_TYPE_OPTIONS),
    clientId: dto.clientId != null ? String(dto.clientId) : '',
    campaignId: dto.campaignId ?? '',
    assignedTo: dto.assignedTo ?? '',
    priority: asOption(dto.priority, PRIORITY_OPTIONS) || 'Medium',
    status: asOption(dto.status, STATUS_OPTIONS) || 'Requested',
    reasonForImportance: dto.reasonForImportance ?? '',
    dailyIssueReport: Boolean(dto.dailyIssueReport),
    approxNumberOfCoverages:
      dto.approxNumberOfCoverages != null ? String(dto.approxNumberOfCoverages) : '',
    approxRevenueImpact:
      dto.approxRevenueImpact != null ? String(dto.approxRevenueImpact) : '',
    requestedDueDate: apiDateToApp(dto.requestedDueDate),
    parentCaseId: dto.parentCaseId != null ? String(dto.parentCaseId) : '',
    effectiveDate: apiDateToApp(dto.effectiveDate),
    segmentId: dto.segmentId != null ? String(dto.segmentId) : '',
    productId: dto.productId != null ? String(dto.productId) : '',
    anticipatedReleaseDate: apiDateToApp(dto.anticipatedReleaseDate),
    requestDescription: dto.requestDescription ?? '',
    billingExtractType: asOption(dto.billingExtractType, BILLING_EXTRACT_TYPE_OPTIONS),
    preNoteRequestType: asOption(dto.preNoteRequestType, PRE_NOTE_REQUEST_TYPE_OPTIONS),
    billingInstitution: dto.billingInstitution ?? '',
    targetPostDate: apiDateToApp(dto.targetPostDate),
    billSet: dto.billSet ?? '',
    billingCycle: dto.billingCycle ?? '',
    priorHardDeclines: asOption(dto.priorHardDeclines, PRIOR_HARD_DECLINES_OPTIONS),
    hardDeclineCodes: dto.hardDeclineCodes ?? '',
    billingHoldType: asOption(dto.billingHoldType, BILLING_HOLD_TYPE_OPTIONS),
    holdLevelCodes: asHoldLevels(dto.holdLevelCodes),
    holdReason: dto.holdReason ?? '',
    billingHoldByProductId:
      dto.billingHoldByProductId != null ? String(dto.billingHoldByProductId) : '',
    version: dto.version,
  }
}

export function formValuesToCreatePayload(values: BillingFormValues): BillingDepartmentRequestCreatePayload {
  return {
    requestType: emptyToNull(values.requestType),
    clientId: parseOptionalLong(values.clientId),
    campaignId: emptyToNull(values.campaignId),
    assignedTo: emptyToNull(values.assignedTo),
    priority: values.priority,
    status: values.status,
    reasonForImportance: emptyToNull(values.reasonForImportance),
    dailyIssueReport: values.dailyIssueReport,
    approxNumberOfCoverages: parseOptionalInt(values.approxNumberOfCoverages),
    approxRevenueImpact: parseOptionalDecimal(values.approxRevenueImpact),
    requestedDueDate: appDateToApi(values.requestedDueDate),
    parentCaseId: parseOptionalLong(values.parentCaseId),
    effectiveDate: appDateToApi(values.effectiveDate),
    segmentId: parseOptionalLong(values.segmentId),
    productId: parseOptionalLong(values.productId),
    anticipatedReleaseDate: appDateToApi(values.anticipatedReleaseDate),
    requestDescription: values.requestDescription.trim(),
    billingExtractType: emptyToNull(values.billingExtractType),
    preNoteRequestType: emptyToNull(values.preNoteRequestType),
    billingInstitution: emptyToNull(values.billingInstitution),
    targetPostDate: appDateToApi(values.targetPostDate),
    billSet: emptyToNull(values.billSet),
    billingCycle: emptyToNull(values.billingCycle),
    priorHardDeclines: emptyToNull(values.priorHardDeclines),
    hardDeclineCodes: emptyToNull(values.hardDeclineCodes),
    billingHoldType: emptyToNull(values.billingHoldType),
    holdLevelCodes: values.holdLevelCodes.length ? [...values.holdLevelCodes] : null,
    holdReason: emptyToNull(values.holdReason),
    billingHoldByProductId: parseOptionalLong(values.billingHoldByProductId),
  }
}

export function formValuesToUpdatePayload(values: BillingFormValues): BillingDepartmentRequestUpdatePayload {
  if (values.version == null) {
    throw new Error('version is required for update')
  }
  return {
    ...formValuesToCreatePayload(values),
    version: values.version,
  }
}
