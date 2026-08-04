import { useEffect, useState } from 'react'
import type { QueueDetailFields, QueueDetailKind, QueueDetailMode } from '@/features/cases/types'

const WORKFLOW_STATUSES = [
  'Pending Assignment',
  'Assigned',
  'In Progress',
  'Completed',
  'Cancelled',
  'On Hold',
]

const PRIORITIES = ['Low', 'Medium', 'High']

interface QueueDetailModalProps {
  open: boolean
  title: string
  mode: QueueDetailMode
  kind: QueueDetailKind
  fields: QueueDetailFields | null
  saving?: boolean
  error?: string | null
  onClose: () => void
  onSave?: (fields: QueueDetailFields) => void
}

/**
 * Home queue View/Edit detail popup (Workflow Logs / Notification Details).
 */
export function QueueDetailModal({
  open,
  title,
  mode,
  kind,
  fields,
  saving = false,
  error = null,
  onClose,
  onSave,
}: QueueDetailModalProps) {
  const [draft, setDraft] = useState<QueueDetailFields | null>(fields)
  const readOnly = mode === 'view'

  useEffect(() => {
    setDraft(fields)
  }, [fields, open])

  if (!open || !draft) return null

  function handleSave() {
    if (!draft || !onSave) return
    onSave(draft)
  }

  return (
    <div className="cases-modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="cases-modal cases-modal--queue-detail"
        role="dialog"
        aria-modal="true"
        aria-labelledby="queue-detail-title"
        data-testid="queue-detail-modal"
        onClick={(e) => e.stopPropagation()}
      >
        <header className="cases-modal__header">
          <h3 id="queue-detail-title">{title}</h3>
          <button
            type="button"
            className="cases-btn cases-btn--ghost"
            onClick={onClose}
            aria-label="Close"
            data-testid="queue-detail-close"
          >
            ✕
          </button>
        </header>

        <div className="cases-modal__body cases-modal__body--queue-detail">
          <ReadField label="Message Id" value={draft.messageId} testId="queue-detail-messageId" />
          <ReadField label="Message Key" value={draft.messageKey} testId="queue-detail-messageKey" />

          {kind === 'workflow' ? (
            <>
              <EditSelect
                label="Status"
                value={draft.status}
                readOnly={readOnly}
                options={WORKFLOW_STATUSES}
                testId="queue-detail-status"
                onChange={(v) => setDraft({ ...draft, status: v })}
              />
              <EditText
                label="Decision"
                value={draft.decision}
                readOnly={readOnly}
                testId="queue-detail-decision"
                onChange={(v) => setDraft({ ...draft, decision: v })}
              />
              <EditSelect
                label="Priority"
                value={draft.priority}
                readOnly={readOnly}
                options={PRIORITIES}
                testId="queue-detail-priority"
                onChange={(v) => setDraft({ ...draft, priority: v })}
              />
              <EditText
                label="Owner"
                value={draft.owner}
                readOnly={readOnly}
                testId="queue-detail-owner"
                onChange={(v) => setDraft({ ...draft, owner: v })}
              />
              <ReadField label="Message Name" value={draft.messageName} testId="queue-detail-messageName" />
              <ReadField label="Message" value={draft.message} testId="queue-detail-message" />
            </>
          ) : (
            <>
              <EditText
                label="Message Name"
                value={draft.messageName}
                readOnly={readOnly}
                testId="queue-detail-messageName"
                onChange={(v) => setDraft({ ...draft, messageName: v })}
              />
              <EditTextArea
                label="Message"
                value={draft.message}
                readOnly={readOnly}
                testId="queue-detail-message"
                onChange={(v) => setDraft({ ...draft, message: v })}
              />
              <EditText
                label="Details"
                value={draft.details}
                readOnly={readOnly}
                testId="queue-detail-details"
                onChange={(v) => setDraft({ ...draft, details: v })}
              />
            </>
          )}

          <ReadField label="Received Date" value={draft.receivedDate} testId="queue-detail-receivedDate" />
          <ReadField label="Updated Date" value={draft.updatedDate} testId="queue-detail-updatedDate" />
          <ReadField label="Sender System" value={draft.senderSystem} testId="queue-detail-senderSystem" />
          <ReadField label="Sender User Id" value={draft.senderUserId} testId="queue-detail-senderUserId" />
          <ReadField label="Sender User Group" value={draft.senderUserGroup} testId="queue-detail-senderUserGroup" />

          {error ? (
            <p className="queue-detail-error" role="alert" data-testid="queue-detail-error">
              {error}
            </p>
          ) : null}
        </div>

        <footer className="cases-modal__footer">
          {readOnly ? (
            <button
              type="button"
              className="cases-action-btn cases-action-btn--primary"
              onClick={onClose}
              data-testid="queue-detail-ok"
            >
              OK
            </button>
          ) : (
            <>
              <button
                type="button"
                className="cases-btn cases-btn--ghost"
                onClick={onClose}
                disabled={saving}
                data-testid="queue-detail-cancel"
              >
                Cancel
              </button>
              <button
                type="button"
                className="cases-action-btn cases-action-btn--primary"
                onClick={handleSave}
                disabled={saving}
                data-testid="queue-detail-save"
              >
                {saving ? 'Saving…' : 'Save'}
              </button>
            </>
          )}
        </footer>
      </div>
    </div>
  )
}

function ReadField({ label, value, testId }: { label: string; value: string; testId: string }) {
  return (
    <p className="queue-detail-row" data-testid={testId}>
      <strong>{label} :</strong> <span>{value || '—'}</span>
    </p>
  )
}

function EditText({
  label,
  value,
  readOnly,
  testId,
  onChange,
}: {
  label: string
  value: string
  readOnly: boolean
  testId: string
  onChange: (value: string) => void
}) {
  if (readOnly) return <ReadField label={label} value={value} testId={testId} />
  return (
    <label className="cases-field" data-testid={testId}>
      <span>{label}</span>
      <input type="text" value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  )
}

function EditTextArea({
  label,
  value,
  readOnly,
  testId,
  onChange,
}: {
  label: string
  value: string
  readOnly: boolean
  testId: string
  onChange: (value: string) => void
}) {
  if (readOnly) return <ReadField label={label} value={value} testId={testId} />
  return (
    <label className="cases-field cases-field--full" data-testid={testId}>
      <span>{label}</span>
      <textarea rows={3} value={value} onChange={(e) => onChange(e.target.value)} />
    </label>
  )
}

function EditSelect({
  label,
  value,
  readOnly,
  options,
  testId,
  onChange,
}: {
  label: string
  value: string
  readOnly: boolean
  options: string[]
  testId: string
  onChange: (value: string) => void
}) {
  if (readOnly) return <ReadField label={label} value={value} testId={testId} />
  const opts = value && !options.includes(value) ? [value, ...options] : options
  return (
    <label className="cases-field" data-testid={testId}>
      <span>{label}</span>
      <select value={value} onChange={(e) => onChange(e.target.value)}>
        {opts.map((o) => (
          <option key={o} value={o}>
            {o}
          </option>
        ))}
      </select>
    </label>
  )
}
