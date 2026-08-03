import { LOOKUP_OPTIONS } from '@/features/cases/mock/casesMockData'
import type { AdvancedFilterState } from '@/features/cases/types'

interface CasesAdvancedFiltersProps {
  open: boolean
  value: AdvancedFilterState
  onChange: (next: AdvancedFilterState) => void
}

export function CasesAdvancedFilters({ open, value, onChange }: CasesAdvancedFiltersProps) {
  if (!open) return null

  function setField<K extends keyof AdvancedFilterState>(key: K, fieldValue: AdvancedFilterState[K]) {
    onChange({ ...value, [key]: fieldValue })
  }

  return (
    <section className="cases-advanced" data-testid="cases-advanced-filters-panel" aria-label="Advanced filters">
      <div className="cases-advanced__grid">
        <label className="cases-field">
          <span>Carrier</span>
          <select
            value={value.carrier}
            onChange={(e) => setField('carrier', e.target.value)}
            data-testid="adv-filter-carrier"
          >
            <option value="">All</option>
            {LOOKUP_OPTIONS.carriers.map((c) => (
              <option key={c} value={c}>
                {c}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Priority</span>
          <select
            value={value.priority}
            onChange={(e) => setField('priority', e.target.value)}
            data-testid="adv-filter-priority"
          >
            <option value="">All</option>
            {LOOKUP_OPTIONS.priorities.map((p) => (
              <option key={p} value={p}>
                {p}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Assigned To</span>
          <select
            value={value.assignedTo}
            onChange={(e) => setField('assignedTo', e.target.value)}
            data-testid="adv-filter-assigned-to"
          >
            <option value="">All</option>
            {LOOKUP_OPTIONS.assignees.map((a) => (
              <option key={a} value={a}>
                {a}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Segment ID</span>
          <input
            type="text"
            value={value.segmentId}
            onChange={(e) => setField('segmentId', e.target.value)}
            placeholder="e.g. SEG-100"
            data-testid="adv-filter-segment-id"
          />
        </label>

        <label className="cases-field">
          <span>Frequency</span>
          <select
            value={value.frequency}
            onChange={(e) => setField('frequency', e.target.value)}
            data-testid="adv-filter-frequency"
          >
            <option value="">All</option>
            {LOOKUP_OPTIONS.frequencies.map((f) => (
              <option key={f} value={f}>
                {f}
              </option>
            ))}
          </select>
        </label>

        <label className="cases-field">
          <span>Spoken Key</span>
          <input
            type="text"
            value={value.spokenKey}
            onChange={(e) => setField('spokenKey', e.target.value)}
            data-testid="adv-filter-spoken-key"
          />
        </label>

        <label className="cases-field">
          <span>Event ID</span>
          <input
            type="text"
            value={value.eventId}
            onChange={(e) => setField('eventId', e.target.value)}
            data-testid="adv-filter-event-id"
          />
        </label>

        <label className="cases-field">
          <span>Mail Month (MM-YYYY)</span>
          <input
            type="text"
            value={value.mailMonth}
            onChange={(e) => setField('mailMonth', e.target.value)}
            placeholder="MM-YYYY"
            data-testid="adv-filter-mail-month"
          />
        </label>
      </div>
    </section>
  )
}
