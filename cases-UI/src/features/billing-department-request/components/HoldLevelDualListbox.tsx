import { useRef } from 'react'

interface HoldLevelDualListboxProps {
  availableOptions: string[]
  selected: string[]
  readOnly: boolean
  onChange: (next: string[]) => void
}

/**
 * Dual listbox styled to match Create Case multi-select / transfer patterns.
 */
export function HoldLevelDualListbox({
  availableOptions,
  selected,
  readOnly,
  onChange,
}: HoldLevelDualListboxProps) {
  const availableRef = useRef<HTMLSelectElement>(null)
  const selectedRef = useRef<HTMLSelectElement>(null)
  const available = availableOptions.filter((code) => !selected.includes(code))

  function moveToSelected(codes: string[]) {
    if (readOnly) return
    onChange([...selected, ...codes.filter((c) => !selected.includes(c))])
  }

  function moveToAvailable(codes: string[]) {
    if (readOnly) return
    onChange(selected.filter((c) => !codes.includes(c)))
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
        <p className="billing-dual-listbox__hint">Hold Ctrl/Cmd to select multiple</p>
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
