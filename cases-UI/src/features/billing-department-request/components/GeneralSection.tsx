import { DatePickerField } from '@/features/cases/components/DatePickerField'
import { BillingFieldLabel } from '@/features/billing-department-request/components/BillingFieldLabel'
import {
  PRIORITY_OPTIONS,
  REQUEST_TYPE_OPTIONS,
  STATUS_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'
import type { BillingAssigneeDto, LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'

interface GeneralSectionProps {
  values: BillingFormValues
  caseOwner: string
  readOnly: boolean
  errors: BillingFieldErrors
  clients: LookupItemDto[]
  assignees: BillingAssigneeDto[]
  onChange: <K extends keyof BillingFormValues>(key: K, value: BillingFormValues[K]) => void
}

function FieldError({ message, testId }: { message?: string; testId: string }) {
  if (!message) return null
  return (
    <p className="billing-field__error" role="alert" data-testid={testId}>
      {message}
    </p>
  )
}

export function GeneralSection({
  values,
  caseOwner,
  readOnly,
  errors,
  clients,
  assignees,
  onChange,
}: GeneralSectionProps) {
  return (
    <section className="billing-section" aria-label="General" data-testid="billing-general-section">
      <h2 className="billing-section__header">General</h2>
      <div className="billing-section__body">
        <div className="billing-grid">
          <label className="billing-field">
            <BillingFieldLabel>Request Type</BillingFieldLabel>
            <select
              value={values.requestType}
              disabled={readOnly}
              onChange={(e) => onChange('requestType', e.target.value as BillingFormValues['requestType'])}
              data-testid="billing-request-type"
            >
              <option value="">Select</option>
              {REQUEST_TYPE_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            <FieldError message={errors.requestType} testId="billing-request-type-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Client Name</BillingFieldLabel>
            <select
              value={values.clientId}
              disabled={readOnly}
              onChange={(e) => onChange('clientId', e.target.value)}
              data-testid="billing-client-name"
            >
              <option value="">Select</option>
              {clients.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.label || c.code || c.id}
                </option>
              ))}
            </select>
            <FieldError message={errors.clientId} testId="billing-client-name-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Campaign ID</BillingFieldLabel>
            <select
              value={values.campaignId}
              disabled={readOnly}
              onChange={(e) => onChange('campaignId', e.target.value)}
              data-testid="billing-campaign-id"
            >
              <option value="">Select</option>
            </select>
            <FieldError message={errors.campaignId} testId="billing-campaign-id-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Billing Request Case Assigned To</BillingFieldLabel>
            <select
              value={values.assignedTo}
              disabled={readOnly}
              onChange={(e) => onChange('assignedTo', e.target.value)}
              data-testid="billing-assigned-to"
            >
              <option value="">Select</option>
              {assignees.map((a) => (
                <option key={a.username} value={a.username}>
                  {a.displayName || a.username}
                </option>
              ))}
            </select>
            <FieldError message={errors.assignedTo} testId="billing-assigned-to-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel required>Case Owner</BillingFieldLabel>
            <input
              type="text"
              value={caseOwner}
              readOnly
              data-testid="billing-case-owner"
              aria-readonly="true"
            />
            <p className="billing-field__hint">Auto-filled from logged-in user</p>
          </div>

          <label className="billing-field">
            <BillingFieldLabel required>Priority</BillingFieldLabel>
            <select
              value={values.priority}
              disabled={readOnly}
              onChange={(e) => onChange('priority', e.target.value as BillingFormValues['priority'])}
              data-testid="billing-priority"
              aria-invalid={Boolean(errors.priority)}
            >
              {PRIORITY_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            <FieldError message={errors.priority} testId="billing-priority-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel required>Status</BillingFieldLabel>
            <select
              value={values.status}
              disabled={readOnly}
              onChange={(e) => onChange('status', e.target.value as BillingFormValues['status'])}
              data-testid="billing-status"
              aria-invalid={Boolean(errors.status)}
            >
              {STATUS_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            <FieldError message={errors.status} testId="billing-status-error" />
          </label>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel>Reason for Importance</BillingFieldLabel>
            <input
              type="text"
              value={values.reasonForImportance}
              maxLength={500}
              readOnly={readOnly}
              onChange={(e) => onChange('reasonForImportance', e.target.value)}
              data-testid="billing-reason-for-importance"
              aria-invalid={Boolean(errors.reasonForImportance)}
            />
            <FieldError message={errors.reasonForImportance} testId="billing-reason-for-importance-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Daily Issue Report</BillingFieldLabel>
            <label className="billing-field__checkbox">
              <input
                type="checkbox"
                checked={values.dailyIssueReport}
                disabled={readOnly}
                onChange={(e) => onChange('dailyIssueReport', e.target.checked)}
                data-testid="billing-daily-issue-report"
              />
              Yes
            </label>
          </div>

          <label className="billing-field">
            <BillingFieldLabel>Approximate Number of Coverages</BillingFieldLabel>
            <input
              type="text"
              inputMode="numeric"
              value={values.approxNumberOfCoverages}
              readOnly={readOnly}
              onChange={(e) => onChange('approxNumberOfCoverages', e.target.value)}
              data-testid="billing-approx-coverages"
              aria-invalid={Boolean(errors.approxNumberOfCoverages)}
            />
            <FieldError message={errors.approxNumberOfCoverages} testId="billing-approx-coverages-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Approximate Revenue Impact</BillingFieldLabel>
            <input
              type="text"
              inputMode="decimal"
              value={values.approxRevenueImpact}
              readOnly={readOnly}
              onChange={(e) => onChange('approxRevenueImpact', e.target.value)}
              data-testid="billing-approx-revenue"
              aria-invalid={Boolean(errors.approxRevenueImpact)}
            />
            <FieldError message={errors.approxRevenueImpact} testId="billing-approx-revenue-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Requested Due Date</BillingFieldLabel>
            <DatePickerField
              value={values.requestedDueDate}
              onChange={(v) => onChange('requestedDueDate', v)}
              disabled={readOnly}
              error={errors.requestedDueDate ?? null}
              aria-label="Requested Due Date"
              data-testid="billing-requested-due-date"
            />
          </div>

          <label className="billing-field">
            <BillingFieldLabel>Parent Case</BillingFieldLabel>
            <select
              value={values.parentCaseId}
              disabled={readOnly}
              onChange={(e) => onChange('parentCaseId', e.target.value)}
              data-testid="billing-parent-case"
            >
              <option value="">Select</option>
            </select>
            <FieldError message={errors.parentCaseId} testId="billing-parent-case-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Effective Date</BillingFieldLabel>
            <DatePickerField
              value={values.effectiveDate}
              onChange={(v) => onChange('effectiveDate', v)}
              disabled={readOnly}
              error={errors.effectiveDate ?? null}
              aria-label="Effective Date"
              data-testid="billing-effective-date"
            />
          </div>

          <label className="billing-field">
            <BillingFieldLabel>Segment ID</BillingFieldLabel>
            <select
              value={values.segmentId}
              disabled={readOnly}
              onChange={(e) => onChange('segmentId', e.target.value)}
              data-testid="billing-segment-id"
            >
              <option value="">Select</option>
            </select>
            <FieldError message={errors.segmentId} testId="billing-segment-id-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Product Name</BillingFieldLabel>
            <select
              value={values.productId}
              disabled={readOnly}
              onChange={(e) => onChange('productId', e.target.value)}
              data-testid="billing-product-name"
            >
              <option value="">Select</option>
            </select>
            <FieldError message={errors.productId} testId="billing-product-name-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Anticipated Release Date</BillingFieldLabel>
            <DatePickerField
              value={values.anticipatedReleaseDate}
              onChange={(v) => onChange('anticipatedReleaseDate', v)}
              disabled={readOnly}
              error={errors.anticipatedReleaseDate ?? null}
              aria-label="Anticipated Release Date"
              data-testid="billing-anticipated-release-date"
            />
          </div>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel required>Request Description</BillingFieldLabel>
            <textarea
              rows={4}
              value={values.requestDescription}
              maxLength={5000}
              readOnly={readOnly}
              onChange={(e) => onChange('requestDescription', e.target.value)}
              data-testid="billing-request-description"
              aria-invalid={Boolean(errors.requestDescription)}
            />
            <FieldError message={errors.requestDescription} testId="billing-request-description-error" />
          </label>
        </div>
      </div>
    </section>
  )
}
