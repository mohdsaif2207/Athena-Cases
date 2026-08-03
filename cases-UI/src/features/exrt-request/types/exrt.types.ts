export type LookupItem = {
  id: string
  code: string
  label: string
}

export type ExrtCaseCreatePayload = {
  clientId: string
  status: string
  lastName: string
  firstName: string
  tierIiAgentId?: string
  mi?: string
  state?: string
  phoneNumber?: string
  productId: string
  coverageId?: string
  carrierId: string
  customerContactEmail?: string
  type: string
  policyNumber?: string
  disposition: string
  inquirySource: string
  actionNeeded: string
  requestAssignedTo?: string
  reasonForEscalation: string
  reasonCode1: string
  requestorNotes?: string
  notesIssues?: string
  coachingFeedback?: string
  callCenterEducation?: boolean
  caseOrigin: string
  webMail?: string
  subject: string
  contactName?: string
  description?: string
  priority: string
}

export type ExrtCaseCreateResult = {
  caseId: number
  caseNumber: string
  caseType: string
  status: string
  caseOwner: string
  message: string
}

export type ApiEnvelope<T> = {
  data: T
  requestId: string
  timestamp: string
}
