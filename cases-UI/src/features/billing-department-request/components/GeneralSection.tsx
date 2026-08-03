import { DatePickerField } from '@/features/cases/components/DatePickerField'
import {
  PRIORITY_OPTIONS,
  REQUEST_TYPE_OPTIONS,
  STATUS_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'
import type { BillingCreateFormValues } from '@/features/billing-department-request/types/billingTypes'

interface GeneralSectionProps {
  values: BillingCreateFormValues
  caseOwner: string
  onChange: <K extends keyof BillingCreateFormValues>(
    key: K,
    value: BillingCreateFormValues[K],
  ) => void
}

/**
 * General section fields — LLD FR-010 / user story.
 * Lookup dropdowns render empty options until Phase 4 API integration.
 */
export function GeneralSection({ values, caseOwner, onChange }: GeneralSectionProps) {
  return (
    <section className="cases-advanced" aria-label="General" data-testid="billing-general-section">
      <h2 className="cases-queue__title">General</h2>
      <div className="cases-advanced__grid">
        <label className="cases-field">
          <span>Request Type</span>
          <select
            value={values.requestType}
            onChange={(e) => onChange('requestType', e.target.value as BillingCreateFormValues['requestType'])}
            data-testid="billing-request-type"
          >
            <option value="">Select</option>
            {REQUEST_TYPE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Client Name</span>
          <select
            value={values.clientId}
            onChange={(e) => onChange('clientId', e.target.value)}
            data-testid="billing-client-name"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Campaign ID</span>
          <select
            value={values.campaignId}
            onChange={(e) => onChange('campaignId', e.target.value)}
            data-testid="billing-campaign-id"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Billing Request Case Assigned To</span>
          <select
            value={values.assignedTo}
            onChange={(e) => onChange('assignedTo', e.target.value)}
            data-testid="billing-assigned-to"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Case Owner</span>
          <input
            type="text"
            value={caseOwner}
            readOnly
            data-testid="billing-case-owner"
            aria-readonly="true"
          />
        </label>

        <label className="cases-field">
          <span>Priority</span>
          <select
            value={values.priority}
            onChange={(e) => onChange('priority', e.target.value as BillingCreateFormValues['priority'])}
            data-testid="billing-priority"
          >
            {PRIORITY_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Status</span>
          <select
            value={values.status}
            onChange={(e) => onChange('status', e.target.value as BillingCreateFormValues['status'])}
            data-testid="billing-status"
          >
            {STATUS_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field cases-field--full">
          <span>Reason for Importance</span>
          <input
            type="text"
            value={values.reasonForImportance}
            maxLength={500}
            onChange={(e) => onChange('reasonForImportance', e.target.value)}
            data-testid="billing-reason-for-importance"
          />
        </label>

        <label className="cases-field">
          <span>Daily Issue Report</span>
          <span className="cases-columns__label">
            <input
              type="checkbox"
              checked={values.dailyIssueReport}
              onChange={(e) => onChange('dailyIssueReport', e.target.checked)}
              data-testid="billing-daily-issue-report"
            />
            Yes
          </span>
        </label>

        <label className="cases-field">
          <span>Approximate Number of Coverages</span>
          <input
            type="text"
            inputMode="numeric"
            value={values.approxNumberOfCoverages}
            onChange={(e) => onChange('approxNumberOfCoverages', e.target.value)}
            data-testid="billing-approx-coverages"
          />
        </label>

        <label className="cases-field">
          <span>Approximate Revenue Impact</span>
          <input
            type="text"
            inputMode="decimal"
            value={values.approxRevenueImpact}
            onChange={(e) => onChange('approxRevenueImpact', e.target.value)}
            data-testid="billing-approx-revenue"
          />
        </label>

        <label className="cases-field">
          <span>Requested Due Date</span>
          <DatePickerField
            value={values.requestedDueDate}
            onChange={(v) => onChange('requestedDueDate', v)}
            aria-label="Requested Due Date"
            data-testid="billing-requested-due-date"
          />
        </label>

        <label className="cases-field">
          <span>Parent Case</span>
          <select
            value={values.parentCaseId}
            onChange={(e) => onChange('parentCaseId', e.target.value)}
            data-testid="billing-parent-case"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Effective Date</span>
          <DatePickerField
            value={values.effectiveDate}
            onChange={(v) => onChange('effectiveDate', v)}
            aria-label="Effective Date"
            data-testid="billing-effective-date"
          />
        </label>

        <label className="cases-field">
          <span>Segment ID</span>
          <select
            value={values.segmentId}
            onChange={(e) => onChange('segmentId', e.target.value)}
            data-testid="billing-segment-id"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Product Name</span>
          <select
            value={values.productId}
            onChange={(e) => onChange('productId', e.target.value)}
            data-testid="billing-product-name"
          >
            <option value="">Select</option>
          </select>
        </label>

        <label className="cases-field">
          <span>Anticipated Release Date</span>
          <DatePickerField
            value={values.anticipatedReleaseDate}
            onChange={(v) => onChange('anticipatedReleaseDate', v)}
            aria-label="Anticipated Release Date"
            data-testid="billing-anticipated-release-date"
          />
        </label>

        <label className="cases-field cases-field--full">
          <span>Request Description</span>
          <textarea
            rows={4}
            value={values.requestDescription}
            maxLength={5000}
            onChange={(e) => onChange('requestDescription', e.target.value)}
            data-testid="billing-request-description"
          />
        </label>
      </div>
    </section>
  )
}
