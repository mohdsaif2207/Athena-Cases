import { DatePickerField } from '@/features/cases/components/DatePickerField'
import {
  DATE_VALIDATION_MESSAGES,
  isDueBeforeCreated,
  parseAppDate,
} from '@/features/cases/utils/dateFormat'
import type { CaseColumnKey, CaseRecord, ColumnFilterState, ColumnPreference } from '@/features/cases/types'

export interface CasesGridFilterOptions {
  caseTypes: string[]
  statuses: string[]
}

interface CasesGridProps {
  rows: CaseRecord[]
  columnOrder: ColumnPreference[]
  columnFilters: ColumnFilterState
  filterOptions: CasesGridFilterOptions
  onColumnFilterChange: (key: keyof ColumnFilterState, value: string) => void
  canEdit: boolean
  onView: (row: CaseRecord) => void
  onEdit: (row: CaseRecord) => void
}

export function CasesGrid({
  rows,
  columnOrder,
  columnFilters,
  filterOptions,
  onColumnFilterChange,
  canEdit,
  onView,
  onEdit,
}: CasesGridProps) {
  const visible = columnOrder.filter((c) => c.visible)

  return (
    <div className="cases-grid-wrap" data-testid="cases-grid">
      <table className="cases-grid">
        <thead>
          <tr>
            {visible.map((col) => (
              <th key={col.key}>
                <span className="cases-grid__th-inner">
                  {headerLabel(col.key)}
                  {col.key === 'createdDate' ? (
                    <span className="cases-grid__sort" aria-hidden="true">
                      ▼
                    </span>
                  ) : null}
                </span>
              </th>
            ))}
          </tr>
          <tr className="cases-grid__filters">
            {visible.map((col) => (
              <th key={`f-${col.key}`}>
                {renderFilter(col.key, columnFilters, filterOptions, onColumnFilterChange)}
              </th>
            ))}
          </tr>
        </thead>
        <tbody>
          {rows.length === 0 ? (
            <tr data-testid="cases-empty">
              <td className="cases-grid__empty" colSpan={Math.max(visible.length, 1)}>
                No cases available.
              </td>
            </tr>
          ) : (
            rows.map((row, index) => (
              <tr
                key={row.id}
                className={index % 2 === 1 ? 'is-alt' : undefined}
                data-testid={`cases-row-${row.caseId}`}
              >
                {visible.map((col) => (
                  <td key={`${row.id}-${col.key}`}>{renderCell(col.key, row, canEdit, onView, onEdit)}</td>
                ))}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  )
}

function renderFilter(
  key: CaseColumnKey,
  columnFilters: ColumnFilterState,
  filterOptions: CasesGridFilterOptions,
  onColumnFilterChange: (key: keyof ColumnFilterState, value: string) => void,
) {
  if (key === 'action') return null

  if (key === 'caseStatus' || key === 'caseType') {
    const options = key === 'caseStatus' ? filterOptions.statuses : filterOptions.caseTypes
    return (
      <select
        value={columnFilters[key]}
        onChange={(e) => onColumnFilterChange(key, e.target.value)}
        aria-label={`Filter ${headerLabel(key)}`}
        data-testid={`cases-filter-${key}`}
      >
        <option value="">All</option>
        {options.map((opt) => (
          <option key={opt} value={opt}>
            {opt}
          </option>
        ))}
      </select>
    )
  }

  if (key === 'createdDate' || key === 'requestedDueDate') {
    const createdParsed = parseAppDate(columnFilters.createdDate)
    const rangeError =
      key === 'requestedDueDate' &&
      isDueBeforeCreated(columnFilters.createdDate, columnFilters.requestedDueDate)
        ? DATE_VALIDATION_MESSAGES.dueBeforeCreated
        : null

    return (
      <DatePickerField
        compact
        value={columnFilters[key]}
        onChange={(next) => onColumnFilterChange(key, next)}
        aria-label={`Filter ${headerLabel(key)}`}
        data-testid={`cases-filter-${key}`}
        error={rangeError}
        minDate={key === 'requestedDueDate' ? createdParsed : null}
      />
    )
  }

  if (key === 'subject') {
    return (
      <div className="cases-filter-search">
        <input
          type="text"
          value={columnFilters.subject}
          onChange={(e) => onColumnFilterChange('subject', e.target.value)}
          aria-label="Filter Subject"
          data-testid="cases-filter-subject"
          placeholder="Search subject..."
        />
        <span className="cases-filter-search__icon" aria-hidden="true">
          <SearchIcon />
        </span>
      </div>
    )
  }

  return (
    <input
      type="text"
      value={columnFilters[key as keyof ColumnFilterState]}
      onChange={(e) => onColumnFilterChange(key as keyof ColumnFilterState, e.target.value)}
      aria-label={`Filter ${headerLabel(key)}`}
      data-testid={`cases-filter-${key}`}
    />
  )
}

function headerLabel(key: CaseColumnKey): string {
  const map: Record<CaseColumnKey, string> = {
    caseId: 'Case ID',
    caseType: 'Case Type',
    clientId: 'Client ID',
    subject: 'Subject',
    caseOwner: 'Case Owner',
    caseStatus: 'Case Status',
    createdDate: 'Created Date',
    requestedDueDate: 'Requested Due Date',
    action: 'Actions',
  }
  return map[key]
}

function renderCell(
  key: CaseColumnKey,
  row: CaseRecord,
  canEdit: boolean,
  onView: (row: CaseRecord) => void,
  onEdit: (row: CaseRecord) => void,
) {
  if (key === 'action') {
    return (
      <div className="cases-actions">
        <button
          type="button"
          className="cases-icon-btn"
          aria-label={`Edit ${row.caseId}`}
          title={canEdit ? 'Edit' : 'Requires Cases Edit permission'}
          data-testid={`cases-edit-${row.caseId}`}
          disabled={!canEdit}
          onClick={() => onEdit(row)}
        >
          <EditIcon />
        </button>
        <button
          type="button"
          className="cases-icon-btn"
          aria-label={`View ${row.caseId}`}
          title="View"
          data-testid={`cases-view-${row.caseId}`}
          onClick={() => onView(row)}
        >
          <EyeIcon />
        </button>
      </div>
    )
  }

  return row[key]
}

function SearchIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M10 4a6 6 0 1 1 0 12 6 6 0 0 1 0-12Zm0 2a4 4 0 1 0 0 8 4 4 0 0 0 0-8Zm6.7 9.3 4 4-1.4 1.4-4-4 1.4-1.4Z"
        fill="currentColor"
      />
    </svg>
  )
}

function EyeIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M12 5c-5 0-9.27 3.11-11 7 1.73 3.89 6 7 11 7s9.27-3.11 11-7c-1.73-3.89-6-7-11-7Zm0 12a5 5 0 1 1 5-5 5 5 0 0 1-5 5Z"
        fill="#fff"
      />
    </svg>
  )
}

function EditIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 17.25V20h2.75L18.81 8.94l-2.75-2.75L4 17.25ZM20.71 7.04a1 1 0 0 0 0-1.41L18.37 3.29a1 1 0 0 0-1.41 0l-1.83 1.83 2.75 2.75 1.83-1.83Z"
        fill="#fff"
      />
    </svg>
  )
}
