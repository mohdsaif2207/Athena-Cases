import { DatePickerField } from '@/features/cases/components/DatePickerField'
import {
  BILLING_EXTRACT_TYPE_OPTIONS,
  PRE_NOTE_REQUEST_TYPE_OPTIONS,
  PRIOR_HARD_DECLINES_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'
import type { BillingCreateFormValues } from '@/features/billing-department-request/types/billingTypes'

interface ExtractsSectionProps {
  values: BillingCreateFormValues
  onChange: <K extends keyof BillingCreateFormValues>(
    key: K,
    value: BillingCreateFormValues[K],
  ) => void
}

/**
 * Extracts section fields — LLD FR-011 / user story.
 */
export function ExtractsSection({ values, onChange }: ExtractsSectionProps) {
  return (
    <section className="cases-advanced" aria-label="Extracts" data-testid="billing-extracts-section">
      <h2 className="cases-queue__title">Extracts</h2>
      <div className="cases-advanced__grid">
        <label className="cases-field">
          <span>Billing Extract Type</span>
          <select
            value={values.billingExtractType}
            onChange={(e) =>
              onChange('billingExtractType', e.target.value as BillingCreateFormValues['billingExtractType'])
            }
            data-testid="billing-extract-type"
          >
            <option value="">Select</option>
            {BILLING_EXTRACT_TYPE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Pre Note Request Type</span>
          <select
            value={values.preNoteRequestType}
            onChange={(e) =>
              onChange('preNoteRequestType', e.target.value as BillingCreateFormValues['preNoteRequestType'])
            }
            data-testid="billing-pre-note-request-type"
          >
            <option value="">Select</option>
            {PRE_NOTE_REQUEST_TYPE_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Billing Institution</span>
          <input
            type="text"
            value={values.billingInstitution}
            maxLength={255}
            onChange={(e) => onChange('billingInstitution', e.target.value)}
            data-testid="billing-institution"
          />
        </label>

        <label className="cases-field">
          <span>Target Post Date</span>
          <DatePickerField
            value={values.targetPostDate}
            onChange={(v) => onChange('targetPostDate', v)}
            aria-label="Target Post Date"
            data-testid="billing-target-post-date"
          />
        </label>

        <label className="cases-field">
          <span>Bill Set</span>
          <input
            type="text"
            value={values.billSet}
            maxLength={255}
            onChange={(e) => onChange('billSet', e.target.value)}
            data-testid="billing-bill-set"
          />
        </label>

        <label className="cases-field">
          <span>Billing Cycle</span>
          <input
            type="text"
            value={values.billingCycle}
            maxLength={255}
            onChange={(e) => onChange('billingCycle', e.target.value)}
            data-testid="billing-cycle"
          />
        </label>

        <label className="cases-field">
          <span>Do Coverages Have Prior Hard Declines?</span>
          <select
            value={values.priorHardDeclines}
            onChange={(e) =>
              onChange('priorHardDeclines', e.target.value as BillingCreateFormValues['priorHardDeclines'])
            }
            data-testid="billing-prior-hard-declines"
          >
            <option value="">Select</option>
            {PRIOR_HARD_DECLINES_OPTIONS.map((opt) => (
              <option key={opt} value={opt}>
                {opt}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Hard Decline Codes</span>
          <input
            type="text"
            value={values.hardDeclineCodes}
            maxLength={500}
            onChange={(e) => onChange('hardDeclineCodes', e.target.value)}
            data-testid="billing-hard-decline-codes"
          />
        </label>
      </div>
    </section>
  )
}
