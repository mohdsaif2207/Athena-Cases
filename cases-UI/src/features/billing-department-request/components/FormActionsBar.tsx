/**
 * Save / Cancel / Reset — Milestone 1 UI only.
 * No handlers (Save / Cancel / Reset logic deferred).
 */
export function FormActionsBar() {
  return (
    <div className="cases-toolbar" data-testid="billing-form-actions">
      <button type="button" className="cases-action-btn" data-testid="billing-save">
        Save
      </button>
      <button type="button" className="cases-btn cases-btn--ghost" data-testid="billing-cancel">
        Cancel
      </button>
      <button type="button" className="cases-btn cases-btn--ghost" data-testid="billing-reset">
        Reset
      </button>
    </div>
  )
}
