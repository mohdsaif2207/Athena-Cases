import { DatePickerField } from '@/features/cases/components/DatePickerField'
import { BillingFieldLabel } from '@/features/billing-department-request/components/BillingFieldLabel'
import {
  BILLING_EXTRACT_TYPE_OPTIONS,
  PRE_NOTE_REQUEST_TYPE_OPTIONS,
  PRIOR_HARD_DECLINES_OPTIONS,
} from '@/features/billing-department-request/constants/billingEnums'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'

interface ExtractsSectionProps {
  values: BillingFormValues
  readOnly: boolean
  errors: BillingFieldErrors
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

export function ExtractsSection({ values, readOnly, errors, onChange }: ExtractsSectionProps) {
  return (
    <section className="billing-section" aria-label="Extracts" data-testid="billing-extracts-section">
      <h2 className="billing-section__header">Extracts</h2>
      <div className="billing-section__body">
        <div className="billing-grid">
          <label className="billing-field">
            <BillingFieldLabel>Billing Extract Type</BillingFieldLabel>
            <select
              value={values.billingExtractType}
              disabled={readOnly}
              onChange={(e) =>
                onChange('billingExtractType', e.target.value as BillingFormValues['billingExtractType'])
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

          <label className="billing-field">
            <BillingFieldLabel>Pre Note Request Type</BillingFieldLabel>
            <select
              value={values.preNoteRequestType}
              disabled={readOnly}
              onChange={(e) =>
                onChange('preNoteRequestType', e.target.value as BillingFormValues['preNoteRequestType'])
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

          <label className="billing-field">
            <BillingFieldLabel>Billing Institution</BillingFieldLabel>
            <input
              type="text"
              value={values.billingInstitution}
              maxLength={255}
              readOnly={readOnly}
              onChange={(e) => onChange('billingInstitution', e.target.value)}
              data-testid="billing-institution"
              aria-invalid={Boolean(errors.billingInstitution)}
            />
            <FieldError message={errors.billingInstitution} testId="billing-institution-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Target Post Date</BillingFieldLabel>
            <DatePickerField
              value={values.targetPostDate}
              onChange={(v) => onChange('targetPostDate', v)}
              disabled={readOnly}
              error={errors.targetPostDate ?? null}
              aria-label="Target Post Date"
              data-testid="billing-target-post-date"
            />
          </div>

          <label className="billing-field">
            <BillingFieldLabel>Bill Set</BillingFieldLabel>
            <input
              type="text"
              value={values.billSet}
              maxLength={255}
              readOnly={readOnly}
              onChange={(e) => onChange('billSet', e.target.value)}
              data-testid="billing-bill-set"
              aria-invalid={Boolean(errors.billSet)}
            />
            <FieldError message={errors.billSet} testId="billing-bill-set-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Billing Cycle</BillingFieldLabel>
            <input
              type="text"
              value={values.billingCycle}
              maxLength={255}
              readOnly={readOnly}
              onChange={(e) => onChange('billingCycle', e.target.value)}
              data-testid="billing-cycle"
              aria-invalid={Boolean(errors.billingCycle)}
            />
            <FieldError message={errors.billingCycle} testId="billing-cycle-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Do Coverages Have Prior Hard Declines?</BillingFieldLabel>
            <select
              value={values.priorHardDeclines}
              disabled={readOnly}
              onChange={(e) =>
                onChange('priorHardDeclines', e.target.value as BillingFormValues['priorHardDeclines'])
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

          <label className="billing-field">
            <BillingFieldLabel>Hard Decline Codes</BillingFieldLabel>
            <input
              type="text"
              value={values.hardDeclineCodes}
              maxLength={500}
              readOnly={readOnly}
              onChange={(e) => onChange('hardDeclineCodes', e.target.value)}
              data-testid="billing-hard-decline-codes"
              aria-invalid={Boolean(errors.hardDeclineCodes)}
            />
            <FieldError message={errors.hardDeclineCodes} testId="billing-hard-decline-codes-error" />
          </label>
        </div>
      </div>
    </section>
  )
}
