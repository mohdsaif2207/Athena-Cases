/**
 * Save / Cancel / Reset action bar — Phase 2 skeleton only.
 * Buttons are non-functional placeholders (no handlers, no validation, no API).
 */
export function FormActionsBar() {
  return (
    <div className="cases-toolbar" data-testid="billing-form-actions">
      <button
        type="button"
        className="cases-action-btn"
        data-testid="billing-save"
        disabled
      >
        Save
      </button>
      <button
        type="button"
        className="cases-btn cases-btn--ghost"
        data-testid="billing-cancel"
        disabled
      >
        Cancel
      </button>
      <button
        type="button"
        className="cases-btn cases-btn--ghost"
        data-testid="billing-reset"
        disabled
      >
        Reset
      </button>
    </div>
  )
}
