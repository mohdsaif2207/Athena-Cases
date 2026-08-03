import type { LookupItemDto } from '@/features/billing-department-request/types/billingTypes'

interface BillingLookupSelectProps {
  value: string
  options: LookupItemDto[]
  readOnly: boolean
  onChange: (value: string) => void
  testId: string
  ariaInvalid?: boolean
  /** Prefer id for Long-backed fields; code for string codes (campaigns). */
  valueKey?: 'id' | 'code'
  emptyLabel?: string
}

/**
 * Lookup dropdown: Select when options exist; "No records available" when empty.
 */
export function BillingLookupSelect({
  value,
  options,
  readOnly,
  onChange,
  testId,
  ariaInvalid,
  valueKey = 'id',
  emptyLabel = 'No records available',
}: BillingLookupSelectProps) {
  const hasOptions = options.length > 0

  return (
    <select
      value={hasOptions ? value : ''}
      disabled={readOnly || !hasOptions}
      onChange={(e) => onChange(e.target.value)}
      data-testid={testId}
      aria-invalid={ariaInvalid}
    >
      <option value="">{hasOptions ? 'Select' : emptyLabel}</option>
      {options.map((opt) => {
        const optValue = valueKey === 'code' ? opt.code : opt.id
        return (
          <option key={`${opt.id}-${opt.code}`} value={optValue}>
            {opt.label || opt.code || opt.id}
          </option>
        )
      })}
    </select>
  )
}
