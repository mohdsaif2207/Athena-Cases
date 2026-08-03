import { useState, type DragEvent } from 'react'
import { DEFAULT_COLUMNS } from '@/features/cases/mock/casesMockData'
import type { CaseColumnKey, ColumnPreference } from '@/features/cases/types'

interface CasesColumnConfigProps {
  open: boolean
  preferences: ColumnPreference[]
  onChange: (next: ColumnPreference[]) => void
  onSave: () => void
  onReset: () => void
  onClose: () => void
}

export function CasesColumnConfig({
  open,
  preferences,
  onChange,
  onSave,
  onReset,
  onClose,
}: CasesColumnConfigProps) {
  const [dragIndex, setDragIndex] = useState<number | null>(null)

  if (!open) return null

  function labelFor(key: CaseColumnKey): string {
    return DEFAULT_COLUMNS.find((c) => c.key === key)?.label ?? key
  }

  function isHideable(key: CaseColumnKey): boolean {
    return DEFAULT_COLUMNS.find((c) => c.key === key)?.hideable !== false
  }

  function toggleVisible(key: CaseColumnKey) {
    if (!isHideable(key)) return
    onChange(
      preferences.map((p) => (p.key === key ? { ...p, visible: !p.visible } : p)),
    )
  }

  function onDragStart(index: number) {
    setDragIndex(index)
  }

  function onDragOver(event: DragEvent<HTMLLIElement>, index: number) {
    event.preventDefault()
    if (dragIndex === null || dragIndex === index) return
    const next = [...preferences]
    const [moved] = next.splice(dragIndex, 1)
    next.splice(index, 0, moved)
    setDragIndex(index)
    onChange(next)
  }

  function onDragEnd() {
    setDragIndex(null)
  }

  return (
    <aside className="cases-columns" data-testid="cases-column-config" aria-label="Configure columns">
      <div className="cases-columns__header">
        <h3>Configure Columns</h3>
        <button type="button" className="cases-btn cases-btn--ghost" onClick={onClose} aria-label="Close">
          ✕
        </button>
      </div>
      <p className="cases-columns__hint">Drag to reorder. Case ID and Action stay visible.</p>
      <ul className="cases-columns__list">
        {preferences.map((pref, index) => (
          <li
            key={pref.key}
            className={`cases-columns__item ${dragIndex === index ? 'is-dragging' : ''}`}
            draggable
            onDragStart={() => onDragStart(index)}
            onDragOver={(e) => onDragOver(e, index)}
            onDragEnd={onDragEnd}
          >
            <span className="cases-columns__handle" aria-hidden="true" title="Drag to reorder">
              ⋮⋮
            </span>
            <label className="cases-columns__label">
              <input
                type="checkbox"
                checked={pref.visible}
                disabled={!isHideable(pref.key)}
                onChange={() => toggleVisible(pref.key)}
                data-testid={`cases-col-toggle-${pref.key}`}
              />
              {labelFor(pref.key)}
            </label>
          </li>
        ))}
      </ul>
      <div className="cases-columns__footer">
        <button type="button" className="cases-btn cases-btn--ghost" onClick={onReset} data-testid="cases-cols-reset">
          Reset to Default
        </button>
        <button type="button" className="cases-btn cases-btn--primary" onClick={onSave} data-testid="cases-cols-save">
          Save Preferences
        </button>
      </div>
    </aside>
  )
}
