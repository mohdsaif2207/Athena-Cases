import { BillingDateField } from '@/features/billing-department-request/components/BillingDateField'
import { BillingFieldError } from '@/features/billing-department-request/components/BillingFieldError'
import { BillingFieldLabel } from '@/features/billing-department-request/components/BillingFieldLabel'
import { BillingLookupSelect } from '@/features/billing-department-request/components/BillingLookupSelect'
import { HoldLevelDualListbox } from '@/features/billing-department-request/components/HoldLevelDualListbox'
import { BILLING_HOLD_TYPE_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'
import type { BillingHoldLevelOption } from '@/features/billing-department-request/constants/billingEnums'
import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import type { BillingFormValues } from '@/features/billing-department-request/utils/billingFormMapper'
import type { BillingFieldErrors } from '@/features/billing-department-request/validation/billingFormValidation'

interface HoldsSectionProps {
  values: BillingFormValues
  readOnly: boolean
  errors: BillingFieldErrors
  holdLevelOptions: string[]
  segments: LookupItemDto[]
  products: LookupItemDto[]
  onChange: <K extends keyof BillingFormValues>(key: K, value: BillingFormValues[K]) => void
}

/**
 * Holds section — User Story order including Segment / Product / dates / description.
 */
export function HoldsSection({
  values,
  readOnly,
  errors,
  holdLevelOptions,
  segments,
  products,
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
            <BillingFieldError message={errors.holdLevelCodes} testId="billing-hold-level-error" />
          </div>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel>Hold Reason</BillingFieldLabel>
            <textarea
              className="billing-textarea billing-textarea--medium"
              rows={3}
              value={values.holdReason}
              maxLength={1000}
              readOnly={readOnly}
              placeholder="Enter hold reason (max 1000 characters)"
              onChange={(e) => onChange('holdReason', e.target.value)}
              data-testid="billing-hold-reason"
              aria-invalid={Boolean(errors.holdReason)}
            />
            <p className="billing-field__hint">{values.holdReason.length}/1000</p>
            <BillingFieldError message={errors.holdReason} testId="billing-hold-reason-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Billing Hold by Product</BillingFieldLabel>
            <BillingLookupSelect
              value={values.billingHoldByProductId}
              options={products}
              readOnly={readOnly}
              onChange={(v) => onChange('billingHoldByProductId', v)}
              testId="billing-hold-by-product"
              ariaInvalid={Boolean(errors.billingHoldByProductId)}
            />
            <BillingFieldError
              message={errors.billingHoldByProductId}
              testId="billing-hold-by-product-error"
            />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Segment ID</BillingFieldLabel>
            <BillingLookupSelect
              value={values.segmentId}
              options={segments}
              readOnly={readOnly}
              onChange={(v) => onChange('segmentId', v)}
              testId="billing-segment-id"
              ariaInvalid={Boolean(errors.segmentId)}
              emptyLabel={values.clientId ? 'No records available' : 'Select a client first'}
            />
            <BillingFieldError message={errors.segmentId} testId="billing-segment-id-error" />
          </label>

          <label className="billing-field">
            <BillingFieldLabel>Product Name</BillingFieldLabel>
            <BillingLookupSelect
              value={values.productId}
              options={products}
              readOnly={readOnly}
              onChange={(v) => onChange('productId', v)}
              testId="billing-product-name"
              ariaInvalid={Boolean(errors.productId)}
            />
            <BillingFieldError message={errors.productId} testId="billing-product-name-error" />
          </label>

          <div className="billing-field">
            <BillingFieldLabel>Anticipated Release Date</BillingFieldLabel>
            <BillingDateField
              value={values.anticipatedReleaseDate}
              onChange={(v) => onChange('anticipatedReleaseDate', v)}
              disabled={readOnly}
              error={errors.anticipatedReleaseDate ?? null}
              aria-label="Anticipated Release Date"
              data-testid="billing-anticipated-release-date"
            />
          </div>

          <label className="billing-field billing-field--full">
            <BillingFieldLabel>Request Description</BillingFieldLabel>
            <textarea
              className="billing-textarea billing-textarea--large"
              rows={5}
              value={values.requestDescription}
              maxLength={5000}
              readOnly={readOnly}
              placeholder="Enter request description (optional, max 5000 characters)"
              onChange={(e) => onChange('requestDescription', e.target.value)}
              data-testid="billing-request-description"
              aria-invalid={Boolean(errors.requestDescription)}
            />
            <p className="billing-field__hint">{values.requestDescription.length}/5000</p>
            <BillingFieldError
              message={errors.requestDescription}
              testId="billing-request-description-error"
            />
          </label>
        </div>
      </div>
    </section>
  )
}
