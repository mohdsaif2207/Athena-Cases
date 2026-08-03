interface BillingConfirmDialogProps {
  open: boolean
  title?: string
  message: string
  confirmLabel?: string
  cancelLabel?: string
  testId: string
  onConfirm: () => void
  onCancel: () => void
}

/**
 * Athena Create-Case confirmation dialog (matches DBM reference):
 * dimmed overlay, centered white card, coral No/Yes — not browser alert styling.
 */
export function BillingConfirmDialog({
  open,
  title,
  message,
  confirmLabel = 'Yes',
  cancelLabel = 'No',
  testId,
  onConfirm,
  onCancel,
}: BillingConfirmDialogProps) {
  if (!open) return null

  return (
    <div className="billing-dialog-backdrop" role="presentation" onClick={onCancel}>
      <div
        className="billing-dialog"
        role="dialog"
        aria-modal="true"
        aria-labelledby={title ? `${testId}-title` : undefined}
        aria-describedby={`${testId}-message`}
        data-testid={testId}
        onClick={(e) => e.stopPropagation()}
      >
        {title ? (
          <h3 className="billing-dialog__title" id={`${testId}-title`}>
            {title}
          </h3>
        ) : null}
        <p className="billing-dialog__message" id={`${testId}-message`}>
          {message}
        </p>
        <div className="billing-dialog__actions">
          <button
            type="button"
            className="billing-btn"
            onClick={onCancel}
            data-testid={`${testId}-no`}
          >
            {cancelLabel}
          </button>
          <button
            type="button"
            className="billing-btn"
            onClick={onConfirm}
            data-testid={`${testId}-yes`}
          >
            {confirmLabel}
          </button>
        </div>
      </div>
    </div>
  )
}
