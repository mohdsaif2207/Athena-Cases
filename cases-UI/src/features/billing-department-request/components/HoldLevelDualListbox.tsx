import { useRef } from 'react'

interface HoldLevelDualListboxProps {
  availableOptions: string[]
  selected: string[]
  readOnly: boolean
  onChange: (next: string[]) => void
}

const ALL_HOLD_LEVEL = 'All'

function uniquePreserveOrder(values: string[]): string[] {
  const seen = new Set<string>()
  const out: string[] = []
  for (const v of values) {
    if (!v || seen.has(v)) continue
    seen.add(v)
    out.push(v)
  }
  return out
}

/**
 * Hold Level dual listbox.
 * - Individual moves: only highlighted values.
 * - Selecting "All": moves every Hold Level into Selected; Available becomes empty.
 * - Removing "All": clears Selected and restores the full Available list.
 */
export function HoldLevelDualListbox({
  availableOptions,
  selected,
  readOnly,
  onChange,
}: HoldLevelDualListboxProps) {
  const availableRef = useRef<HTMLSelectElement>(null)
  const selectedRef = useRef<HTMLSelectElement>(null)

  const masterOptions = uniquePreserveOrder(availableOptions)
  const selectedUnique = uniquePreserveOrder(selected)
  const availableUnique = masterOptions.filter((code) => !selectedUnique.includes(code))

  function moveToSelected(codes: string[]) {
    if (readOnly || !codes.length) return

    if (codes.includes(ALL_HOLD_LEVEL)) {
      // Selecting All selects every Hold Level (including All).
      onChange([...masterOptions])
      return
    }

    onChange(uniquePreserveOrder([...selectedUnique, ...codes]))
  }

  function moveToAvailable(codes: string[]) {
    if (readOnly || !codes.length) return

    if (codes.includes(ALL_HOLD_LEVEL)) {
      // Removing All clears Selected so Available is fully restored.
      onChange([])
      return
    }

    let next = selectedUnique.filter((c) => !codes.includes(c))
    // Individual removals invalidate an "All" selection.
    if (next.includes(ALL_HOLD_LEVEL)) {
      next = next.filter((c) => c !== ALL_HOLD_LEVEL)
    }
    onChange(next)
  }

  return (
    <div className="billing-dual-listbox" data-testid="billing-hold-level-dual-listbox">
      <label className="billing-field">
        <span className="billing-field__label">Available</span>
        <select
          ref={availableRef}
          multiple
          size={6}
          disabled={readOnly}
          className="billing-dual-listbox__list"
          data-testid="billing-hold-level-available"
          aria-label="Available hold levels"
          onDoubleClick={(e) => {
            const option = e.target as HTMLOptionElement
            if (option?.value) moveToSelected([option.value])
          }}
        >
          {availableUnique.map((code) => (
            <option key={code} value={code}>
              {code}
            </option>
          ))}
        </select>
      </label>

      <div className="billing-dual-listbox__actions" aria-label="Move hold levels">
        <button
          type="button"
          className="billing-btn"
          data-testid="billing-hold-level-add"
          disabled={readOnly}
          onClick={() => {
            if (!availableRef.current) return
            moveToSelected(Array.from(availableRef.current.selectedOptions).map((o) => o.value))
          }}
        >
          Add →
        </button>
        <button
          type="button"
          className="billing-btn"
          data-testid="billing-hold-level-remove"
          disabled={readOnly}
          onClick={() => {
            if (!selectedRef.current) return
            moveToAvailable(Array.from(selectedRef.current.selectedOptions).map((o) => o.value))
          }}
        >
          ← Remove
        </button>
      </div>

      <label className="billing-field">
        <span className="billing-field__label">Selected</span>
        <select
          ref={selectedRef}
          multiple
          size={6}
          disabled={readOnly}
          className="billing-dual-listbox__list"
          data-testid="billing-hold-level-selected"
          aria-label="Selected hold levels"
          onDoubleClick={(e) => {
            const option = e.target as HTMLOptionElement
            if (option?.value) moveToAvailable([option.value])
          }}
        >
          {selectedUnique.map((code) => (
            <option key={code} value={code}>
              {code}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}
