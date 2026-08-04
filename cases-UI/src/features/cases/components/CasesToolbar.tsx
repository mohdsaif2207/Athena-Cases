interface CasesToolbarProps {
  onNewCase: () => void
  onExport: () => void
  onToggleAdvanced: () => void
  onToggleColumns: () => void
  onClearAll: () => void
  advancedOpen: boolean
  columnsOpen: boolean
  canCreate: boolean
  canExport: boolean
  filteredCount: number
}

export function CasesToolbar({
  onNewCase,
  onExport,
  onToggleAdvanced,
  onToggleColumns,
  onClearAll,
  advancedOpen,
  columnsOpen,
  canCreate,
  canExport,
  filteredCount,
}: CasesToolbarProps) {
  return (
    <div className="cases-toolbar" data-testid="cases-toolbar">
      <button
        type="button"
        className="cases-action-btn"
        onClick={onNewCase}
        disabled={!canCreate}
        data-testid="cases-new-case"
        title={canCreate ? 'Create a new case' : 'Requires Cases Create permission'}
      >
        New Case
      </button>
      <button
        type="button"
        className="cases-action-btn"
        onClick={onExport}
        disabled={!canExport || filteredCount === 0}
        data-testid="cases-export"
      >
        Export To Excel
      </button>
      <button
        type="button"
        className={`cases-action-btn ${advancedOpen ? 'is-active' : ''}`}
        onClick={onToggleAdvanced}
        data-testid="cases-advanced-filters"
        aria-expanded={advancedOpen}
      >
        Advanced Filters
      </button>
      <button
        type="button"
        className="cases-action-btn"
        onClick={onClearAll}
        data-testid="cases-clear-all"
      >
        Clear All Filter
      </button>
      <button
        type="button"
        className={`cases-action-btn ${columnsOpen ? 'is-active' : ''}`}
        onClick={onToggleColumns}
        data-testid="cases-configure-columns"
      >
        Configure Columns
      </button>
    </div>
  )
}
