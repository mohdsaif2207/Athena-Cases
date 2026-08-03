import { HoldLevelDualListbox } from '@/features/billing-department-request/components/HoldLevelDualListbox'
import { BILLING_HOLD_TYPE_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'
import type { BillingCreateFormValues } from '@/features/billing-department-request/types/billingTypes'

interface HoldsSectionProps {
  values: BillingCreateFormValues
  onChange: <K extends keyof BillingCreateFormValues>(
    key: K,
    value: BillingCreateFormValues[K],
  ) => void
}

/**
 * Holds section fields — LLD FR-012 / FR-019 / user story.
 * Segment ID and Product Name live in General (same bindings); Hold by Product is separate.
 */
export function HoldsSection({ values, onChange }: HoldsSectionProps) {
  return (
    <section className="cases-advanced" aria-label="Holds" data-testid="billing-holds-section">
      <h2 className="cases-queue__title">Holds</h2>
      <div className="cases-advanced__grid">
        <label className="cases-field">
          <span>Billing Hold Type</span>
          <select
            value={values.billingHoldType}
            onChange={(e) =>
              onChange('billingHoldType', e.target.value as BillingCreateFormValues['billingHoldType'])
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

        <div className="cases-field cases-field--full">
          <span>Billing Hold Level</span>
          <HoldLevelDualListbox
            holdType={values.billingHoldType}
            selected={values.holdLevelCodes}
            onChange={(next) => onChange('holdLevelCodes', next)}
          />
        </div>

        <label className="cases-field cases-field--full">
          <span>Hold Reason</span>
          <input
            type="text"
            value={values.holdReason}
            maxLength={1000}
            onChange={(e) => onChange('holdReason', e.target.value)}
            data-testid="billing-hold-reason"
          />
        </label>

        <label className="cases-field">
          <span>Billing Hold by Product</span>
          <select
            value={values.billingHoldByProductId}
            onChange={(e) => onChange('billingHoldByProductId', e.target.value)}
            data-testid="billing-hold-by-product"
          >
            <option value="">Select</option>
          </select>
        </label>
      </div>
    </section>
  )
}
