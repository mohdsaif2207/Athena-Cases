interface FormActionsBarProps {
  mode: 'create' | 'view' | 'edit'
  saving: boolean
  onSave: () => void
  onCancel: () => void
  onReset: () => void
}

/** Bottom-right actions — User Story: Save, Reset, Cancel (view: Back only). */
export function FormActionsBar({ mode, saving, onSave, onCancel, onReset }: FormActionsBarProps) {
  const readOnly = mode === 'view'

  return (
    <div className="billing-actions" data-testid="billing-form-actions">
      {!readOnly ? (
        <button
          type="button"
          className="billing-btn"
          data-testid="billing-save"
          disabled={saving}
          onClick={onSave}
        >
          {saving ? 'Saving…' : 'Save'}
        </button>
      ) : null}
      {!readOnly ? (
        <button
          type="button"
          className="billing-btn"
          data-testid="billing-reset"
          disabled={saving}
          onClick={onReset}
        >
          Reset
        </button>
      ) : null}
      <button
        type="button"
        className="billing-btn"
        data-testid="billing-cancel"
        disabled={saving}
        onClick={onCancel}
      >
        {readOnly ? 'Back' : 'Cancel'}
      </button>
    </div>
  )
}
