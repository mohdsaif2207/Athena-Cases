import { Box } from '@mui/material'
import type { Control, FieldErrors } from 'react-hook-form'
import { Controller } from 'react-hook-form'
import { Checkbox, FormControlLabel, Stack, TextField } from '@mui/material'
import type { LookupItem } from '../types/exrt.types'
import type { ExrtCaseFormValues } from '../validation/exrtCaseCreateSchema'
import { ExrtSelectField, ExrtTextField } from './ExrtFormControls'

const FieldGrid = ({ children }: { children: React.ReactNode }) => (
  <Box
    sx={{
      display: 'grid',
      gap: 1.25,
      gridTemplateColumns: { xs: '1fr', md: 'repeat(3, 1fr)' },
      alignItems: 'start',
    }}
  >
    {children}
  </Box>
)

type CaseDetailsProps = {
  control: Control<ExrtCaseFormValues>
  errors: FieldErrors<ExrtCaseFormValues>
  clients: LookupItem[]
  products: LookupItem[]
  carriers: LookupItem[]
  tierIiAgents: LookupItem[]
  statuses: LookupItem[]
}

export function CaseDetailsSection({
  control,
  errors,
  clients,
  products,
  carriers,
  tierIiAgents,
  statuses,
}: CaseDetailsProps) {
  const statusOptions = statuses.map((s) => ({ ...s, id: s.label, code: s.label }))

  return (
    <FieldGrid>
      <Controller name="clientId" control={control} render={({ field }) => (
        <ExrtSelectField label="Client Name *" name={field.name} value={field.value} onChange={field.onChange}
          options={clients} required error={errors.clientId?.message} testId="exrt-client-id" />
      )} />
      <Controller name="status" control={control} render={({ field }) => (
        <ExrtSelectField label="Status *" name={field.name} value={field.value} onChange={field.onChange}
          options={statusOptions} required error={errors.status?.message} testId="exrt-status" />
      )} />
      <Controller name="caseOwner" control={control} render={({ field }) => (
        <TextField fullWidth size="small" label="Case Owner (Logged-in User)" value={field.value} disabled
          helperText="Auto-filled from logged-in user"
          sx={{
            '& .MuiInputBase-root': {
              backgroundColor: '#F2F4F7',
              borderRadius: '2px',
              minHeight: 32,
              fontSize: 12,
              fontFamily: "'Segoe UI', Roboto, sans-serif",
            },
            '& .MuiInputLabel-root': {
              fontWeight: 600,
              fontSize: 11,
              color: '#5F6B7A',
            },
            '& .MuiFormHelperText-root': { fontSize: 11, ml: 0 },
          }}
          slotProps={{ htmlInput: { 'data-testid': 'exrt-case-owner' } }} />
      )} />
      <Controller name="firstName" control={control} render={({ field }) => (
        <ExrtTextField label="First Name *" name={field.name} value={field.value} onChange={field.onChange}
          required error={errors.firstName?.message} testId="exrt-first-name" />
      )} />
      <Controller name="lastName" control={control} render={({ field }) => (
        <ExrtTextField label="Last Name *" name={field.name} value={field.value} onChange={field.onChange}
          required error={errors.lastName?.message} testId="exrt-last-name" />
      )} />
      <Controller name="mi" control={control} render={({ field }) => (
        <ExrtTextField label="MI" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          maxLength={1} error={errors.mi?.message} testId="exrt-mi" />
      )} />
      <Controller name="state" control={control} render={({ field }) => (
        <ExrtTextField label="State" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          error={errors.state?.message} testId="exrt-state" />
      )} />
      <Controller name="phoneNumber" control={control} render={({ field }) => (
        <ExrtTextField label="Phone Number" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          placeholder="10 digits" error={errors.phoneNumber?.message} testId="exrt-phone" />
      )} />
      <Controller name="productId" control={control} render={({ field }) => (
        <ExrtSelectField label="Product *" name={field.name} value={field.value} onChange={field.onChange}
          options={products} required error={errors.productId?.message} testId="exrt-product" />
      )} />
      <Controller name="coverageId" control={control} render={({ field }) => (
        <ExrtTextField label="Coverage ID" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          error={errors.coverageId?.message} testId="exrt-coverage-id" />
      )} />
      <Controller name="carrierId" control={control} render={({ field }) => (
        <ExrtSelectField label="Carrier for ExRT Case *" name={field.name} value={field.value} onChange={field.onChange}
          options={carriers} required error={errors.carrierId?.message} testId="exrt-carrier" />
      )} />
      <Controller name="customerContactEmail" control={control} render={({ field }) => (
        <ExrtTextField label="Customer Contact Email" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          error={errors.customerContactEmail?.message} testId="exrt-customer-email" />
      )} />
      <Controller name="tierIiAgentId" control={control} render={({ field }) => (
        <ExrtSelectField label="Tier II Agent Name" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          options={tierIiAgents} error={errors.tierIiAgentId?.message} testId="exrt-tier-ii-agent" />
      )} />
    </FieldGrid>
  )
}

type InfoProps = {
  control: Control<ExrtCaseFormValues>
  errors: FieldErrors<ExrtCaseFormValues>
  types: LookupItem[]
  dispositions: LookupItem[]
  inquirySources: LookupItem[]
  actionNeeded: LookupItem[]
  assignees: LookupItem[]
  escalationReasons: LookupItem[]
  reasonCodes: LookupItem[]
}

export function InformationSection(props: InfoProps) {
  const { control, errors } = props
  return (
    <FieldGrid>
      <Controller name="type" control={control} render={({ field }) => (
        <ExrtSelectField label="Type *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.types} required error={errors.type?.message} testId="exrt-type" />
      )} />
      <Controller name="policyNumber" control={control} render={({ field }) => (
        <ExrtTextField label="Policy Number" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          maxLength={25} error={errors.policyNumber?.message} testId="exrt-policy-number" />
      )} />
      <Controller name="disposition" control={control} render={({ field }) => (
        <ExrtSelectField label="Disposition *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.dispositions} required error={errors.disposition?.message} testId="exrt-disposition" />
      )} />
      <Controller name="inquirySource" control={control} render={({ field }) => (
        <ExrtSelectField label="Inquiry Source *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.inquirySources} required error={errors.inquirySource?.message} testId="exrt-inquiry-source" />
      )} />
      <Controller name="actionNeeded" control={control} render={({ field }) => (
        <ExrtSelectField label="Action Needed *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.actionNeeded} required error={errors.actionNeeded?.message} testId="exrt-action-needed" />
      )} />
      <Controller name="requestAssignedTo" control={control} render={({ field }) => (
        <ExrtSelectField label="Request Assigned To" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          options={props.assignees} error={errors.requestAssignedTo?.message} testId="exrt-assigned-to" />
      )} />
      <Controller name="reasonForEscalation" control={control} render={({ field }) => (
        <ExrtSelectField label="Reason for Escalation *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.escalationReasons} required error={errors.reasonForEscalation?.message} testId="exrt-escalation-reason" />
      )} />
      <Controller name="reasonCode1" control={control} render={({ field }) => (
        <ExrtSelectField label="Reason Code 1 *" name={field.name} value={field.value} onChange={field.onChange}
          options={props.reasonCodes} required error={errors.reasonCode1?.message} testId="exrt-reason-code-1" />
      )} />
    </FieldGrid>
  )
}

type NotesProps = {
  control: Control<ExrtCaseFormValues>
  errors: FieldErrors<ExrtCaseFormValues>
}

export function NotesSection({ control, errors }: NotesProps) {
  return (
    <Stack spacing={1.5}>
      <Controller name="requestorNotes" control={control} render={({ field }) => (
        <ExrtTextField label="Requestor Notes" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          multiline rows={3} maxLength={1000} error={errors.requestorNotes?.message} testId="exrt-requestor-notes" />
      )} />
      <Controller name="notesIssues" control={control} render={({ field }) => (
        <ExrtTextField label="Notes/Issues" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          multiline rows={3} maxLength={1000} error={errors.notesIssues?.message} testId="exrt-notes-issues" />
      )} />
      <Controller name="coachingFeedback" control={control} render={({ field }) => (
        <ExrtTextField label="Coaching Feedback" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          multiline rows={3} maxLength={1000} error={errors.coachingFeedback?.message} testId="exrt-coaching-feedback" />
      )} />
      <Controller name="callCenterEducation" control={control} render={({ field }) => (
        <FormControlLabel
          control={<Checkbox checked={Boolean(field.value)} onChange={(e) => field.onChange(e.target.checked)} data-testid="exrt-call-center-education" />}
          label="Call Center Education"
        />
      )} />
    </Stack>
  )
}

type SystemProps = {
  control: Control<ExrtCaseFormValues>
  errors: FieldErrors<ExrtCaseFormValues>
  caseOrigins: LookupItem[]
  contacts: LookupItem[]
  priorities: LookupItem[]
}

export function SystemInformationSection({ control, errors, caseOrigins, contacts, priorities }: SystemProps) {
  return (
    <FieldGrid>
      <Controller name="caseOrigin" control={control} render={({ field }) => (
        <ExrtSelectField label="Case Origin *" name={field.name} value={field.value} onChange={field.onChange}
          options={caseOrigins} required error={errors.caseOrigin?.message} testId="exrt-case-origin" />
      )} />
      <Controller name="webMail" control={control} render={({ field }) => (
        <ExrtTextField label="Web Mail" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          error={errors.webMail?.message} testId="exrt-web-mail" />
      )} />
      <Controller name="priority" control={control} render={({ field }) => (
        <ExrtSelectField label="Priority *" name={field.name} value={field.value} onChange={field.onChange}
          options={priorities} required error={errors.priority?.message} testId="exrt-priority" />
      )} />
      <Controller name="subject" control={control} render={({ field }) => (
        <ExrtTextField label="Subject *" name={field.name} value={field.value} onChange={field.onChange}
          required maxLength={200} error={errors.subject?.message} testId="exrt-subject" />
      )} />
      <Controller name="contactName" control={control} render={({ field }) => (
        <ExrtSelectField label="Contact Name" name={field.name} value={field.value ?? ''} onChange={field.onChange}
          options={contacts} error={errors.contactName?.message} testId="exrt-contact-name" />
      )} />
      <Box sx={{ gridColumn: { md: '1 / -1' } }}>
        <Controller name="description" control={control} render={({ field }) => (
          <ExrtTextField label="Description" name={field.name} value={field.value ?? ''} onChange={field.onChange}
            multiline rows={4} maxLength={1000} error={errors.description?.message} testId="exrt-description" />
        )} />
      </Box>
    </FieldGrid>
  )
}
