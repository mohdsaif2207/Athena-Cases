import { z } from 'zod'

const optionalEmail = z
  .string()
  .trim()
  .optional()
  .or(z.literal(''))
  .refine((v) => !v || /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v), {
    message: 'Please enter a valid email address.',
  })

export const exrtCaseCreateSchema = z.object({
  clientId: z.string().min(1, 'Client Name is required'),
  status: z.string().min(1, 'Status is required'),
  lastName: z
    .string()
    .min(1, 'Last Name is required')
    .regex(/^[A-Za-z][A-Za-z '\-]*$/, 'Last name must be text only'),
  firstName: z
    .string()
    .min(1, 'First Name is required')
    .regex(/^[A-Za-z][A-Za-z '\-]*$/, 'First name must be text only'),
  tierIiAgentId: z.string().optional(),
  mi: z.string().max(1, 'MI max 1 character').optional(),
  state: z.string().optional(),
  phoneNumber: z
    .string()
    .optional()
    .refine((v) => !v || /^\d{10}$/.test(v), { message: 'Phone number must be exactly 10 digits' }),
  productId: z.string().min(1, 'Product is required'),
  coverageId: z.string().optional(),
  carrierId: z.string().min(1, 'Carrier is required'),
  customerContactEmail: optionalEmail,
  type: z.string().min(1, 'Type is required'),
  policyNumber: z.string().max(25, 'Max 25 characters').optional(),
  disposition: z.string().min(1, 'Disposition is required'),
  inquirySource: z.string().min(1, 'Inquiry Source is required'),
  actionNeeded: z.string().min(1, 'Action Needed is required'),
  requestAssignedTo: z.string().optional(),
  reasonForEscalation: z.string().min(1, 'Reason for Escalation is required'),
  reasonCode1: z.string().min(1, 'Reason Code 1 is required'),
  requestorNotes: z.string().max(1000).optional(),
  notesIssues: z.string().max(1000).optional(),
  coachingFeedback: z.string().max(1000).optional(),
  callCenterEducation: z.boolean().optional(),
  caseOrigin: z.string().min(1, 'Case Origin is required'),
  webMail: optionalEmail,
  subject: z.string().min(1, 'Subject is required').max(200, 'Max 200 characters'),
  contactName: z.string().optional(),
  description: z.string().max(1000).optional(),
  priority: z.string().min(1, 'Priority is required'),
  caseOwner: z.string().min(1),
})

export type ExrtCaseFormValues = z.infer<typeof exrtCaseCreateSchema>

export const exrtFormDefaults: ExrtCaseFormValues = {
  clientId: '',
  status: 'Requested - ExRT',
  lastName: '',
  firstName: '',
  tierIiAgentId: '',
  mi: '',
  state: '',
  phoneNumber: '',
  productId: '',
  coverageId: '',
  carrierId: '',
  customerContactEmail: '',
  type: '',
  policyNumber: '',
  disposition: '',
  inquirySource: '',
  actionNeeded: '',
  requestAssignedTo: '',
  reasonForEscalation: '',
  reasonCode1: '',
  requestorNotes: '',
  notesIssues: '',
  coachingFeedback: '',
  callCenterEducation: false,
  caseOrigin: '',
  webMail: '',
  subject: '',
  contactName: '',
  description: '',
  priority: 'MEDIUM',
  caseOwner: '',
}
