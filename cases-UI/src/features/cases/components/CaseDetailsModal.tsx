import { useEffect, useState } from 'react'
import { DatePickerField } from '@/features/cases/components/DatePickerField'
import {
  DATE_VALIDATION_MESSAGES,
  isDueBeforeCreated,
  parseAppDate,
} from '@/features/cases/utils/dateFormat'
import type { CaseDetailMode, CaseRecord } from '@/features/cases/types'

interface CaseDetailsModalProps {
  record: CaseRecord | null
  mode: CaseDetailMode
  onClose: () => void
  onSave: (next: CaseRecord) => void
}

export function CaseDetailsModal({ record, mode, onClose, onSave }: CaseDetailsModalProps) {
  const [draft, setDraft] = useState<CaseRecord | null>(record)
  const [dueError, setDueError] = useState<string | null>(null)

  useEffect(() => {
    setDraft(record)
    setDueError(null)
  }, [record])

  if (!record || !mode || !draft) return null

  const readOnly = mode === 'view'
  const createdParsed = parseAppDate(draft.createdDate)
  const rangeError =
    isDueBeforeCreated(draft.createdDate, draft.requestedDueDate)
      ? DATE_VALIDATION_MESSAGES.dueBeforeCreated
      : null

  function handleSave() {
    if (!draft) return
    if (draft.requestedDueDate.trim() && !parseAppDate(draft.requestedDueDate)) {
      setDueError(DATE_VALIDATION_MESSAGES.invalidFormat)
      return
    }
    if (isDueBeforeCreated(draft.createdDate, draft.requestedDueDate)) {
      setDueError(DATE_VALIDATION_MESSAGES.dueBeforeCreated)
      return
    }
    onSave(draft)
  }

  return (
    <div className="cases-modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="cases-modal"
        role="dialog"
        aria-modal="true"
        aria-labelledby="case-detail-title"
        data-testid="cases-detail-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <header className="cases-modal__header">
          <h3 id="case-detail-title">{readOnly ? 'View Case' : 'Edit Case'} — {draft.caseId}</h3>
          <button type="button" className="cases-btn cases-btn--ghost" onClick={onClose} aria-label="Close">
            ✕
          </button>
        </header>

        <div className="cases-modal__body">
          <Field label="Case Type" value={draft.caseType} readOnly />
          <Field label="Client ID" value={draft.clientId} readOnly={readOnly} onChange={(v) => setDraft({ ...draft, clientId: v })} />
          <Field label="Subject" value={draft.subject} readOnly={readOnly} onChange={(v) => setDraft({ ...draft, subject: v })} />
          <Field label="Case Owner" value={draft.caseOwner} readOnly />
          <Field label="Case Status" value={draft.caseStatus} readOnly={readOnly} onChange={(v) => setDraft({ ...draft, caseStatus: v as CaseRecord['caseStatus'] })} />
          <Field label="Priority" value={draft.priority} readOnly={readOnly} onChange={(v) => setDraft({ ...draft, priority: v as CaseRecord['priority'] })} />

          <label className="cases-field">
            <span>Created Date</span>
            <DatePickerField
              value={draft.createdDate}
              onChange={() => undefined}
              disabled
              aria-label="Created Date"
              data-testid="cases-detail-created-date"
            />
          </label>

          <label className="cases-field">
            <span>Requested Due Date</span>
            <DatePickerField
              value={draft.requestedDueDate}
              onChange={(v) => {
                setDueError(null)
                setDraft({ ...draft, requestedDueDate: v })
              }}
              onValidationError={setDueError}
              error={rangeError}
              minDate={createdParsed}
              disabled={readOnly}
              aria-label="Requested Due Date"
              data-testid="cases-detail-due-date"
            />
          </label>

          <label className="cases-field cases-field--full">
            <span>Description</span>
            <textarea
              value={draft.description}
              readOnly={readOnly}
              rows={4}
              onChange={(e) => setDraft({ ...draft, description: e.target.value })}
              data-testid="cases-detail-description"
            />
          </label>
        </div>

        <footer className="cases-modal__footer">
          <button type="button" className="cases-btn cases-btn--ghost" onClick={onClose} data-testid="cases-detail-close">
            {readOnly ? 'Close' : 'Cancel'}
          </button>
          {!readOnly ? (
            <button
              type="button"
              className="cases-btn cases-btn--primary"
              onClick={handleSave}
              disabled={Boolean(dueError || rangeError)}
              data-testid="cases-detail-save"
            >
              Save
            </button>
          ) : null}
        </footer>
      </div>
    </div>
  )
}

function Field({
  label,
  value,
  readOnly,
  onChange,
}: {
  label: string
  value: string
  readOnly?: boolean
  onChange?: (value: string) => void
}) {
  return (
    <label className="cases-field">
      <span>{label}</span>
      <input type="text" value={value} readOnly={readOnly} onChange={(e) => onChange?.(e.target.value)} />
    </label>
  )
}
