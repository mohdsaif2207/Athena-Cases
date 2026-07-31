import { defaultColumnPreferences } from '@/features/cases/mock/casesMockData'
import type { CaseColumnKey, ColumnPreference } from '@/features/cases/types'

const STORAGE_KEY = 'athena.cases.gridPreferences.CASES_SEARCH'

export function loadColumnPreferences(): ColumnPreference[] {
  try {
    const raw = localStorage.getItem(STORAGE_KEY)
    if (!raw) return defaultColumnPreferences()
    const parsed = JSON.parse(raw) as ColumnPreference[]
    if (!Array.isArray(parsed) || parsed.length === 0) return defaultColumnPreferences()
    return normalizePreferences(parsed)
  } catch {
    return defaultColumnPreferences()
  }
}

export function saveColumnPreferences(prefs: ColumnPreference[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(normalizePreferences(prefs)))
}

export function resetColumnPreferences(): ColumnPreference[] {
  localStorage.removeItem(STORAGE_KEY)
  return defaultColumnPreferences()
}

function normalizePreferences(prefs: ColumnPreference[]): ColumnPreference[] {
  const defaults = defaultColumnPreferences()
  const byKey = new Map(prefs.map((p) => [p.key, p.visible]))
  const ordered: ColumnPreference[] = []

  for (const pref of prefs) {
    if (defaults.some((d) => d.key === pref.key)) {
      ordered.push({
        key: pref.key,
        visible: pref.key === 'action' || pref.key === 'caseId' ? true : Boolean(pref.visible),
      })
    }
  }

  for (const d of defaults) {
    if (!ordered.some((o) => o.key === d.key)) {
      ordered.push({ key: d.key as CaseColumnKey, visible: byKey.get(d.key) ?? d.visible })
    }
  }

  return ordered
}
