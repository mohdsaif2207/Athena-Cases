import { BillingFieldLabel } from '@/features/billing-department-request/components/BillingFieldLabel'
import { HoldLevelDualListbox } from '@/features/billing-department-request/components/HoldLevelDualListbox'
import { BILLING_HOLD_TYPE_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'
import type { BillingHoldLevelOption } from '@/features/billing-department-request/constants/billingEnums'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'

interface HoldsSectionProps {
  values: BillingFormValues
  readOnly: boolean
  errors: BillingFieldErrors
  holdLevelOptions: string[]
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

export function HoldsSection({
  values,
  readOnly,
  errors,
  holdLevelOptions,
  onChange,
}: HoldsSectionProps) {
  return (
    <section className="billing-section" aria-label="Holds" data-testid="billing-holds-section">
      <h2 className="billing-section__header">Holds</h2>
      <div className="billing-section__body">
        <div className="billing-grid">
          <label className="billing-field">
            <BillingFieldLabel>Billing Hold Type</BillingFieldLabel>
            <select
              value={values.billingHoldType}
              disabled={readOnly}
              onChange={(e) =>
                onChange('billingHoldType', e.target.value as BillingFormValues['billingHoldType'])
              }
              data-testid="billing-hold-type"
            >
              <option value="">Select</option>
              {BILLING_HOLD_TYPE_OPTIONS.map((opt) => (
                <option key={opt} value={opt}>
                  {opt}
                </option>
              ))}
            </select>
          </label>

          <div className="billing-field billing-field--full">
            <BillingFieldLabel>Billing Hold Level</BillingFieldLabel>
            <HoldLevelDualListbox
              availableOptions={holdLevelOptions}
              selected={values.holdLevelCodes}
              readOnly={readOnly}
              onChange={(next) => onChange('holdLevelCodes', next as BillingHoldLevelOption[])}
            />
            <FieldError message={errors.holdLevelCodes} testId="billing-hold-level-error" />
          </div>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel>Hold Reason</BillingFieldLabel>
            <input
              type="text"
              value={values.holdReason}
              maxLength={1000}
              readOnly={readOnly}
              onChange={(e) => onChange('holdReason', e.target.value)}
              data-testid="billing-hold-reason"
              aria-invalid={Boolean(errors.holdReason)}
            />
            <FieldError message={errors.holdReason} testId="billing-hold-reason-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Billing Hold by Product</BillingFieldLabel>
            <select
              value={values.billingHoldByProductId}
              disabled={readOnly}
              onChange={(e) => onChange('billingHoldByProductId', e.target.value)}
              data-testid="billing-hold-by-product"
            >
              <option value="">Select</option>
            </select>
            <FieldError message={errors.billingHoldByProductId} testId="billing-hold-by-product-error" />
          </label>
        </div>
      </div>
    </section>
  )
}
