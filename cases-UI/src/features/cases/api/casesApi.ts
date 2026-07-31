import { MOCK_CASES } from '@/features/cases/mock/casesMockData'
import type { CaseRecord } from '@/features/cases/types'

/**
 * Loads cases for the Search grid.
 * Currently mock-only — no backend change. Swap for apiClient.get when list API lands.
 */
export async function fetchCases(): Promise<CaseRecord[]> {
  await delay(450)
  return [...MOCK_CASES].sort((a, b) => {
    const byUpdated = b.updatedDate.localeCompare(a.updatedDate)
    if (byUpdated !== 0) return byUpdated
    return b.createdDate.localeCompare(a.createdDate)
  })
}

function delay(ms: number): Promise<void> {
  return new Promise((resolve) => {
    window.setTimeout(resolve, ms)
  })
}
