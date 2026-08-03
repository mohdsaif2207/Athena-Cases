import { useEffect, useState } from 'react'
import { fetchAuthorizedCaseTypes, type CaseTypeOption } from '@/features/cases/api/lookupApi'

interface NewCaseTypeModalProps {
  open: boolean
  onClose: () => void
  onSelect: (caseType: CaseTypeOption) => void
}

export function NewCaseTypeModal({ open, onClose, onSelect }: NewCaseTypeModalProps) {
  const [options, setOptions] = useState<CaseTypeOption[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [selectedCode, setSelectedCode] = useState<string | null>(null)
  const [validationMessage, setValidationMessage] = useState<string | null>(null)

  useEffect(() => {
    if (!open) return

    let cancelled = false
    setSelectedCode(null)
    setValidationMessage(null)

    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const rows = await fetchAuthorizedCaseTypes()
        if (!cancelled) setOptions(rows)
      } catch {
        if (!cancelled) {
          setOptions([])
          setError('Unable to load case types. Please try again.')
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [open])

  if (!open) return null

  function handleContinue() {
    if (!selectedCode) {
      setValidationMessage('Please select a Case Type.')
      return
    }
    const selected = options.find((option) => option.code === selectedCode)
    if (!selected) {
      setValidationMessage('Please select a Case Type.')
      return
    }
    setValidationMessage(null)
    onSelect(selected)
  }

  const showContinue = Boolean(selectedCode) && !loading && !error && options.length > 0

  return (
    <div className="cases-modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="cases-modal cases-modal--case-type"
        role="dialog"
        aria-modal="true"
        aria-labelledby="new-case-type-title"
        data-testid="new-case-type-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <header className="cases-modal__header">
          <h3 id="new-case-type-title">Select Case Type</h3>
          <button
            type="button"
            className="cases-btn cases-btn--ghost"
            onClick={onClose}
            aria-label="Close"
            data-testid="new-case-type-close"
          >
            ✕
          </button>
        </header>

        <div className="cases-modal__body cases-modal__body--single">
          {loading ? (
            <div className="cases-state cases-state--loading" data-testid="new-case-type-loading">
              <div className="cases-spinner" aria-hidden="true" />
              <p>Loading case types…</p>
            </div>
          ) : null}

          {!loading && error ? (
            <div className="cases-state cases-state--error" role="alert" data-testid="new-case-type-error">
              <p>{error}</p>
            </div>
          ) : null}

          {!loading && !error && options.length === 0 ? (
            <p className="cases-case-type-empty" data-testid="new-case-type-empty">
              No Case Types are available for your account.
            </p>
          ) : null}

          {!loading && !error && options.length > 0 ? (
            <fieldset className="cases-case-type-fieldset" data-testid="new-case-type-list">
              <legend className="cases-sr-only">Case Type</legend>
              <ul className="cases-case-type-list">
                {options.map((option) => {
                  const isSelected = selectedCode === option.code
                  return (
                    <li key={option.code}>
                      <label
                        className={`cases-case-type-option ${isSelected ? 'is-selected' : ''}`}
                        data-testid={`new-case-type-option-${option.code}`}
                      >
                        <input
                          type="radio"
                          name="new-case-type"
                          value={option.code}
                          checked={isSelected}
                          onChange={() => {
                            setSelectedCode(option.code)
                            setValidationMessage(null)
                          }}
                          data-testid={`new-case-type-radio-${option.code}`}
                        />
                        <span>{option.label}</span>
                      </label>
                    </li>
                  )
                })}
              </ul>
            </fieldset>
          ) : null}

          {validationMessage ? (
            <p className="cases-case-type-validation" role="alert" data-testid="new-case-type-validation">
              {validationMessage}
            </p>
          ) : null}
        </div>

        <footer className="cases-modal__footer">
          <button
            type="button"
            className="cases-btn"
            onClick={onClose}
            data-testid="new-case-type-cancel"
          >
            Cancel
          </button>
          {showContinue ? (
            <button
              type="button"
              className="cases-action-btn"
              onClick={handleContinue}
              data-testid="new-case-type-continue"
            >
              Continue
            </button>
          ) : null}
        </footer>
      </div>
    </div>
  )
}
