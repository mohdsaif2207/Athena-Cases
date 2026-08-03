/** Static Billing enum / dropdown option lists — LLD §20.3 / user story. */

export const REQUEST_TYPE_OPTIONS = [
  'Compliance/Legal',
  'Extract',
  'Operations Alert',
  'Reject Review',
  'Research',
  'Schedule',
] as const

export const PRIORITY_OPTIONS = ['High', 'Medium', 'Low'] as const

export const STATUS_OPTIONS = [
  'Requested',
  'In Progress',
  'Killed',
  'On Hold',
  'Incomplete',
  'Completed',
] as const

export const BILLING_EXTRACT_TYPE_OPTIONS = ['Billing', 'Pre Note', 'Rebill'] as const

export const PRE_NOTE_REQUEST_TYPE_OPTIONS = ['All', 'Changes Only'] as const

export const PRIOR_HARD_DECLINES_OPTIONS = ['Yes', 'No'] as const

export const BILLING_HOLD_TYPE_OPTIONS = [
  'Client Level',
  'Coverage Level',
  'Product Level',
  'Segment Level',
] as const

/** Flat Available set until BA Hold Type matrix lands (backend interim). */
export const BILLING_HOLD_LEVEL_OPTIONS = [
  'All',
  'Auto Cancel',
  'Billing',
  'Pre Note',
  'Rebill',
  'Refund',
] as const

export type RequestTypeOption = (typeof REQUEST_TYPE_OPTIONS)[number]
export type PriorityOption = (typeof PRIORITY_OPTIONS)[number]
export type StatusOption = (typeof STATUS_OPTIONS)[number]
export type BillingExtractTypeOption = (typeof BILLING_EXTRACT_TYPE_OPTIONS)[number]
export type PreNoteRequestTypeOption = (typeof PRE_NOTE_REQUEST_TYPE_OPTIONS)[number]
export type PriorHardDeclinesOption = (typeof PRIOR_HARD_DECLINES_OPTIONS)[number]
export type BillingHoldTypeOption = (typeof BILLING_HOLD_TYPE_OPTIONS)[number]
export type BillingHoldLevelOption = (typeof BILLING_HOLD_LEVEL_OPTIONS)[number]
