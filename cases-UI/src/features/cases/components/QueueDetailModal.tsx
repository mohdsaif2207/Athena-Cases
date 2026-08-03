import type { QueueDetailFields } from '@/features/cases/types'

interface QueueDetailModalProps {
  open: boolean
  title: string
  fields: QueueDetailFields | null
  onClose: () => void
}

/**
 * Home queue View/Edit detail popup (Workflow Logs / Notification Details).
 */
export function QueueDetailModal({ open, title, fields, onClose }: QueueDetailModalProps) {
  if (!open || !fields) return null

  const rows: Array<{ label: string; value: string }> = [
    { label: 'Message Id', value: fields.messageId },
    { label: 'Message Name', value: fields.messageName },
    { label: 'Message Key', value: fields.messageKey },
    { label: 'Message', value: fields.message },
    { label: 'Priority', value: fields.priority },
    { label: 'Owner', value: fields.owner },
    { label: 'Received Date', value: fields.receivedDate },
    { label: 'Updated Date', value: fields.updatedDate },
    { label: 'Sender System', value: fields.senderSystem },
    { label: 'Sender User Id', value: fields.senderUserId },
    { label: 'Sender User Group', value: fields.senderUserGroup },
  ]

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
          {rows.map((row) => (
            <p key={row.label} className="queue-detail-row" data-testid={`queue-detail-${row.label}`}>
              <strong>{row.label} :</strong> <span>{row.value || '—'}</span>
            </p>
          ))}
        </div>

        <footer className="cases-modal__footer">
          <button
            type="button"
            className="cases-action-btn cases-action-btn--primary"
            onClick={onClose}
            data-testid="queue-detail-ok"
          >
            OK
          </button>
        </footer>
      </div>
    </div>
  )
}
