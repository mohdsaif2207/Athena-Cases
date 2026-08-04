import { BillingDateField } from '@/features/billing-department-request/components/BillingDateField'
import { BillingFieldError } from '@/features/billing-department-request/components/BillingFieldError'
import { BillingFieldLabel } from '@/features/billing-department-request/components/BillingFieldLabel'
import { BillingLookupSelect } from '@/features/billing-department-request/components/BillingLookupSelect'
import {
  PRIORITY_OPTIONS,
  REQUEST_TYPE_OPTIONS,
  STATUS_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'
import type { BillingAssigneeDto, LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import {
  filterDecimalInput,
  filterIntegerInput,
} from '@/features/billing-department-request/utils/numericInput'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'

interface GeneralSectionProps {
  values: BillingFormValues
  caseOwner: string
  readOnly: boolean
  errors: BillingFieldErrors
  clients: LookupItemDto[]
  campaigns: LookupItemDto[]
  assignees: BillingAssigneeDto[]
  parentCases: LookupItemDto[]
  onChange: <K extends keyof BillingFormValues>(key: K, value: BillingFormValues[K]) => void
  onClientChange: (clientId: string) => void
}

/**
 * General section — User Story field order ending at Effective Date.
 */
export function GeneralSection({
  values,
  caseOwner,
  readOnly,
  errors,
  clients,
  campaigns,
  assignees,
  parentCases,
  onChange,
  onClientChange,
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
              aria-invalid={Boolean(errors.requestType)}
            >
              <option value="">Select</option>
              {REQUEST_TYPE_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
            <BillingFieldError message={errors.requestType} testId="billing-request-type-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Client Name</BillingFieldLabel>
            <BillingLookupSelect
              value={values.clientId}
              options={clients}
              readOnly={readOnly}
              onChange={onClientChange}
              testId="billing-client-name"
              ariaInvalid={Boolean(errors.clientId)}
            />
            <BillingFieldError message={errors.clientId} testId="billing-client-name-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Campaign ID</BillingFieldLabel>
            <BillingLookupSelect
              value={values.campaignId}
              options={campaigns}
              readOnly={readOnly}
              onChange={(v) => onChange('campaignId', v)}
              testId="billing-campaign-id"
              ariaInvalid={Boolean(errors.campaignId)}
              valueKey="code"
            />
            <BillingFieldError message={errors.campaignId} testId="billing-campaign-id-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Billing Request Case Assigned To</BillingFieldLabel>
            <select
              value={assignees.length ? values.assignedTo : ''}
              disabled={readOnly || assignees.length === 0}
              onChange={(e) => onChange('assignedTo', e.target.value)}
              data-testid="billing-assigned-to"
              aria-invalid={Boolean(errors.assignedTo)}
            >
              <option value="">{assignees.length ? 'Select' : 'No records available'}</option>
              {assignees.map((a) => (
                <option key={a.username} value={a.username}>
                  {a.displayName || a.username}
                </option>
              ))}
            </select>
            <BillingFieldError message={errors.assignedTo} testId="billing-assigned-to-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel required>Case Owner</BillingFieldLabel>
            <input
              type="text"
              value={caseOwner}
              readOnly
              placeholder="Auto-filled from signed-in user"
              data-testid="billing-case-owner"
              aria-invalid={Boolean(errors.caseOwner)}
            />
            <BillingFieldError message={errors.caseOwner} testId="billing-case-owner-error" />
          </label>

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
            <BillingFieldError message={errors.priority} testId="billing-priority-error" />
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
            <BillingFieldError message={errors.status} testId="billing-status-error" />
          </label>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel>Reason for Importance</BillingFieldLabel>
            <textarea
              className="billing-textarea billing-textarea--medium"
              rows={3}
              value={values.reasonForImportance}
              maxLength={500}
              readOnly={readOnly}
              placeholder="Enter reason for importance (max 500 characters)"
              onChange={(e) => onChange('reasonForImportance', e.target.value)}
              data-testid="billing-reason-for-importance"
              aria-invalid={Boolean(errors.reasonForImportance)}
            />
            <p className="billing-field__hint">{values.reasonForImportance.length}/500</p>
            <BillingFieldError
              message={errors.reasonForImportance}
              testId="billing-reason-for-importance-error"
            />
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
              pattern="[0-9]*"
              value={values.approxNumberOfCoverages}
              readOnly={readOnly}
              placeholder="Numeric only"
              onChange={(e) => onChange('approxNumberOfCoverages', filterIntegerInput(e.target.value))}
              data-testid="billing-approx-coverages"
              aria-invalid={Boolean(errors.approxNumberOfCoverages)}
            />
            <BillingFieldError
              message={errors.approxNumberOfCoverages}
              testId="billing-approx-coverages-error"
            />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Approximate Revenue Impact</BillingFieldLabel>
            <input
              type="text"
              inputMode="decimal"
              value={values.approxRevenueImpact}
              readOnly={readOnly}
              placeholder="e.g. 1250.50"
              onChange={(e) => onChange('approxRevenueImpact', filterDecimalInput(e.target.value))}
              data-testid="billing-approx-revenue"
              aria-invalid={Boolean(errors.approxRevenueImpact)}
            />
            <BillingFieldError message={errors.approxRevenueImpact} testId="billing-approx-revenue-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Requested Due Date</BillingFieldLabel>
            <BillingDateField
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
            <BillingLookupSelect
              value={values.parentCaseId}
              options={parentCases}
              readOnly={readOnly}
              onChange={(v) => onChange('parentCaseId', v)}
              testId="billing-parent-case"
              ariaInvalid={Boolean(errors.parentCaseId)}
            />
            <BillingFieldError message={errors.parentCaseId} testId="billing-parent-case-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Effective Date</BillingFieldLabel>
            <BillingDateField
              value={values.effectiveDate}
              onChange={(v) => onChange('effectiveDate', v)}
              disabled={readOnly}
              error={errors.effectiveDate ?? null}
              aria-label="Effective Date"
              data-testid="billing-effective-date"
            />
          </div>
        </div>
      </div>
    </section>
  )
}
