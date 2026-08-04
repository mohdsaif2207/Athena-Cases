import {
  deleteUserPreference,
  fetchUserPreference,
  saveUserPreference,
} from '@/features/cases/api/preferencesApi'
import { defaultColumnPreferences } from '@/features/cases/mock/casesMockData'
import type { CaseColumnKey, ColumnPreference } from '@/features/cases/types'

export const CASES_SEARCH_COLUMNS_PREF_KEY = 'CASES_SEARCH_COLUMNS'
const STORAGE_KEY = 'athena.cases.gridPreferences.CASES_SEARCH'

/** Sync cache read for fast first paint; profile API is source of truth. */
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

/** Loads prefs from user profile, falling back to local cache then defaults. */
export async function loadColumnPreferencesFromProfile(): Promise<ColumnPreference[]> {
  try {
    const remote = await fetchUserPreference(CASES_SEARCH_COLUMNS_PREF_KEY)
    if (remote) {
      const parsed = JSON.parse(remote) as ColumnPreference[]
      const normalized = normalizePreferences(parsed)
      localStorage.setItem(STORAGE_KEY, JSON.stringify(normalized))
      return normalized
    }
  } catch {
    // keep local cache when profile is unreachable
  }
  return loadColumnPreferences()
}

export async function saveColumnPreferencesToProfile(prefs: ColumnPreference[]): Promise<void> {
  const normalized = normalizePreferences(prefs)
  localStorage.setItem(STORAGE_KEY, JSON.stringify(normalized))
  await saveUserPreference(CASES_SEARCH_COLUMNS_PREF_KEY, JSON.stringify(normalized))
}

export async function resetColumnPreferencesOnProfile(): Promise<ColumnPreference[]> {
  localStorage.removeItem(STORAGE_KEY)
  try {
    await deleteUserPreference(CASES_SEARCH_COLUMNS_PREF_KEY)
  } catch {
    // ignore missing remote preference
  }
  return defaultColumnPreferences()
}

/** @deprecated use saveColumnPreferencesToProfile — kept for sync callers */
export function saveColumnPreferences(prefs: ColumnPreference[]): void {
  localStorage.setItem(STORAGE_KEY, JSON.stringify(normalizePreferences(prefs)))
}

/** @deprecated use resetColumnPreferencesOnProfile */
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
