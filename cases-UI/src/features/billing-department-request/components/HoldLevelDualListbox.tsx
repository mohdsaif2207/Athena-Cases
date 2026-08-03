import { useRef } from 'react'
import type { BillingHoldLevelOption } from '@/features/billing-department-request/constants/billingEnums'
import { BILLING_HOLD_LEVEL_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'
import './HoldLevelDualListbox.css'

interface HoldLevelDualListboxProps {
  selected: BillingHoldLevelOption[]
  onChange: (next: BillingHoldLevelOption[]) => void
  /** Reserved for Phase 4 — Available set is flat until BA matrix exists. */
  holdType: string
}

/**
 * Billing Hold Level dual listbox — LLD FR-019 / user story.
 * Available list is the flat enum set (matches current backend interim).
 */
export function HoldLevelDualListbox({ selected, onChange, holdType: _holdType }: HoldLevelDualListboxProps) {
  const availableRef = useRef<HTMLSelectElement>(null)
  const selectedRef = useRef<HTMLSelectElement>(null)
  const available = BILLING_HOLD_LEVEL_OPTIONS.filter((code) => !selected.includes(code))

  function moveToSelected(codes: string[]) {
    const additions = codes.filter((c): c is BillingHoldLevelOption =>
      (BILLING_HOLD_LEVEL_OPTIONS as readonly string[]).includes(c),
    )
    onChange([...selected, ...additions.filter((c) => !selected.includes(c))])
  }

  function moveToAvailable(codes: string[]) {
    onChange(selected.filter((c) => !codes.includes(c)))
  }

  return (
    <div className="billing-dual-listbox" data-testid="billing-hold-level-dual-listbox">
      <label className="cases-field">
        <span>Available</span>
        <select
          ref={availableRef}
          multiple
          size={6}
          className="billing-dual-listbox__list"
          data-testid="billing-hold-level-available"
          aria-label="Available hold levels"
          onDoubleClick={(e) => {
            const value = (e.target as HTMLSelectElement).value
            if (value) moveToSelected([value])
          }}
        >
          {available.map((code) => (
            <option key={code} value={code}>
              {code}
            </option>
          ))}
        </select>
      </label>

      <div className="billing-dual-listbox__actions" aria-label="Move hold levels">
        <button
          type="button"
          className="cases-btn cases-btn--ghost"
          data-testid="billing-hold-level-add"
          onClick={() => {
            if (!availableRef.current) return
            moveToSelected(Array.from(availableRef.current.selectedOptions).map((o) => o.value))
          }}
        >
          Add →
        </button>
        <button
          type="button"
          className="cases-btn cases-btn--ghost"
          data-testid="billing-hold-level-remove"
          onClick={() => {
            if (!selectedRef.current) return
            moveToAvailable(Array.from(selectedRef.current.selectedOptions).map((o) => o.value))
          }}
        >
          ← Remove
        </button>
      </div>

      <label className="cases-field">
        <span>Selected</span>
        <select
          ref={selectedRef}
          multiple
          size={6}
          className="billing-dual-listbox__list"
          data-testid="billing-hold-level-selected"
          aria-label="Selected hold levels"
          onDoubleClick={(e) => {
            const value = (e.target as HTMLSelectElement).value
            if (value) moveToAvailable([value])
          }}
        >
          {selected.map((code) => (
            <option key={code} value={code}>
              {code}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
