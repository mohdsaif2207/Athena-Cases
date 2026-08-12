import { useCallback, useEffect, useMemo, useState } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { fetchCases, updateCase } from '@/features/cases/api/casesApi'
import { type CaseTypeOption } from '@/features/cases/api/lookupApi'
import { CaseDetailsModal } from '@/features/cases/components/CaseDetailsModal'
import { CasesAdvancedFilters } from '@/features/cases/components/CasesAdvancedFilters'
import { CasesColumnConfig } from '@/features/cases/components/CasesColumnConfig'
import { CasesGrid } from '@/features/cases/components/CasesGrid'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { CasesToolbar } from '@/features/cases/components/CasesToolbar'
import { NewCaseTypeModal } from '@/features/cases/components/NewCaseTypeModal'
import {
  EMPTY_ADVANCED_FILTERS,
  EMPTY_COLUMN_FILTERS,
  LOOKUP_OPTIONS,
} from '@/features/cases/mock/casesMockData'
import {
  loadColumnPreferences,
  loadColumnPreferencesFromProfile,
  resetColumnPreferencesOnProfile,
  saveColumnPreferencesToProfile,
} from '@/features/cases/utils/columnPrefs'
import { exportCasesToExcel } from '@/features/cases/utils/exportCasesExcel'
import { applyCaseFilters, paginate } from '@/features/cases/utils/filterCases'
import { resolveCaseTypeCreatePath } from '@/features/cases/utils/caseTypeRoutes'
import {
  getDbmWorkOrder,
  updateDbmWorkOrder,
} from '@/features/dbm-work-order-request/api/workOrders'
import {
  isDbmCaseRecord,
  toUpdateRequestFromGridEdit,
} from '@/features/dbm-work-order-request/mappers/toCreateRequest'
import type {
  AdvancedFilterState,
  CaseDetailMode,
  CaseRecord,
  ColumnFilterState,
  ColumnPreference,
} from '@/features/cases/types'
import './CasesSearchPage.css'

const PAGE_SIZE = 10

function uniqueSorted(values: string[]): string[] {
  return Array.from(new Set(values.map((v) => v.trim()).filter(Boolean))).sort((a, b) =>
    a.localeCompare(b),
  )
}

export function CasesSearchPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()
  const permissions = user?.permissions ?? []

  const canCreate = permissions.includes('CASES_CREATE') || permissions.includes('CASES_ACCESS')
  const canEdit = permissions.includes('CASES_EDIT') || permissions.includes('CASES_ACCESS')
  const canExport =
    permissions.includes('CASES_EXPORT') ||
    permissions.includes('CASES_ACCESS') ||
    permissions.includes('CASES_VIEW')
  const [allCases, setAllCases] = useState<CaseRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [successToast, setSuccessToast] = useState<string | null>(null)

  useEffect(() => {
    const msg = (location.state as { successMessage?: string } | null)?.successMessage
    if (!msg) {
      return
    }
    setSuccessToast(msg)
    navigate(location.pathname, { replace: true, state: {} })
    const timer = window.setTimeout(() => setSuccessToast(null), 5000)
    return () => window.clearTimeout(timer)
  }, [location.pathname, location.state, navigate])

  const [columnFilters, setColumnFilters] = useState<ColumnFilterState>({ ...EMPTY_COLUMN_FILTERS })
  const [advancedFilters, setAdvancedFilters] = useState<AdvancedFilterState>({
    ...EMPTY_ADVANCED_FILTERS,
  })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(PAGE_SIZE)
  const [advancedOpen, setAdvancedOpen] = useState(false)
  const [columnsOpen, setColumnsOpen] = useState(false)
  const [columnPrefs, setColumnPrefs] = useState<ColumnPreference[]>(() => loadColumnPreferences())
  const [prefsSavedMessage, setPrefsSavedMessage] = useState<string | null>(null)

  const [detailMode, setDetailMode] = useState<CaseDetailMode>(null)
  const [selected, setSelected] = useState<CaseRecord | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [caseTypeModalOpen, setCaseTypeModalOpen] = useState(false)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchCases()
      setAllCases(data)
    } catch {
      setError('Unable to load case data. Please try again later.')
      setAllCases([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  useEffect(() => {
    let cancelled = false
    void loadColumnPreferencesFromProfile().then((prefs) => {
      if (!cancelled) setColumnPrefs(prefs)
    })
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    const state = location.state as
      | { dbmCreateSuccess?: boolean; caseNumber?: string }
      | null
    if (!state?.dbmCreateSuccess || !state.caseNumber) {
      return
    }
    setToast(`Case created successfully with Case ID ${state.caseNumber}.`)
    window.setTimeout(() => setToast(null), 4000)
    navigate(location.pathname, { replace: true, state: null })
    void load()
  }, [location.state, location.pathname, navigate, load])

  const filtered = useMemo(
    () => applyCaseFilters(allCases, columnFilters, advancedFilters),
    [allCases, columnFilters, advancedFilters],
  )

  const advancedOptions = useMemo(
    () => ({
      carriers: uniqueSorted(allCases.map((c) => c.carrier)),
      priorities: LOOKUP_OPTIONS.priorities,
      assignees: uniqueSorted(allCases.map((c) => c.assignedTo)),
      frequencies: uniqueSorted(allCases.map((c) => c.frequency)),
    }),
    [allCases],
  )

  const gridFilterOptions = useMemo(
    () => ({
      caseTypes: uniqueSorted(allCases.map((c) => c.caseType)),
      statuses: uniqueSorted([
        ...LOOKUP_OPTIONS.statuses,
        ...allCases.map((c) => c.caseStatus),
      ]),
    }),
    [allCases],
  )

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize) || 1)
  const safePage = Math.min(page, pageCount - 1)
  const pageRows = paginate(filtered, safePage, pageSize)

  useEffect(() => {
    if (page > pageCount - 1) setPage(Math.max(0, pageCount - 1))
  }, [page, pageCount])

  async function clearAll() {
    setColumnFilters({ ...EMPTY_COLUMN_FILTERS })
    setAdvancedFilters({ ...EMPTY_ADVANCED_FILTERS })
    setPage(0)
    await load()
  }

  function showToast(message: string) {
    setToast(message)
    window.setTimeout(() => setToast(null), 2800)
  }

  function handleCaseTypeSelected(caseType: CaseTypeOption) {
    const path = resolveCaseTypeCreatePath(caseType.code)
    setCaseTypeModalOpen(false)
    if (!path) {
      showToast(`No create screen is configured for ${caseType.label}.`)
      return
    }
    navigate(path)
  }

  async function handleNewCase() {
    // Always show the shared Case Type popup (User Story). Continue stays disabled
    // until a type is selected in NewCaseTypeModal — do not auto-skip when only one type.
    setCaseTypeModalOpen(true)
  }

  return (
    <div className="cases-search" data-testid="cases-search-page">
      <CasesToolbar
        filteredCount={filtered.length}
        advancedOpen={advancedOpen}
        columnsOpen={columnsOpen}
        canCreate={canCreate}
        canExport={canExport}
        onClearAll={() => {
          void clearAll()
        }}
        onToggleAdvanced={() => {
          setAdvancedOpen((v) => !v)
          setColumnsOpen(false)
        }}
        onToggleColumns={() => {
          setColumnsOpen((v) => !v)
          setAdvancedOpen(false)
        }}
        onExport={() => {
          exportCasesToExcel(filtered)
          showToast(`Exported ${filtered.length} case(s) to Excel.`)
        }}
        onNewCase={() => {
          void handleNewCase()
        }}
      />

      <CasesAdvancedFilters
        open={advancedOpen}
        value={advancedFilters}
        options={advancedOptions}
        onChange={(next) => {
          setAdvancedFilters(next)
          setPage(0)
        }}
      />

      <div className="cases-search__body">
        <div className="cases-search__main">
          {loading ? (
            <div className="cases-state cases-state--loading" data-testid="cases-loading">
              <div className="cases-spinner" aria-hidden="true" />
              <p>Loading cases…</p>
            </div>
          ) : null}

          {!loading && error ? (
            <div className="cases-state cases-state--error" role="alert" data-testid="cases-error">
              <p>{error}</p>
              <button type="button" className="cases-btn cases-btn--primary" onClick={() => void load()}>
                Try again
              </button>
            </div>
          ) : null}

          {!loading && !error ? (
            <>
              <CasesGrid
                rows={pageRows}
                columnOrder={columnPrefs}
                columnFilters={columnFilters}
                filterOptions={gridFilterOptions}
                onColumnFilterChange={(key, value) => {
                  setColumnFilters((prev) => ({ ...prev, [key]: value }))
                  setPage(0)
                }}
                canEdit={canEdit}
                onView={(row) => {
                  setSelected(row)
                  setDetailMode('view')
                }}
                onEdit={(row) => {
                  setSelected(row)
                  setDetailMode('edit')
                }}
              />
              <CasesPagination
                page={safePage}
                pageCount={pageCount}
                pageSize={pageSize}
                totalItems={filtered.length}
                onPageChange={setPage}
                onPageSizeChange={(size) => {
                  setPageSize(size)
                  setPage(0)
                }}
              />
            </>
          ) : null}
        </div>

        <CasesColumnConfig
          open={columnsOpen}
          preferences={columnPrefs}
          onChange={setColumnPrefs}
          onClose={() => setColumnsOpen(false)}
          onSave={() => {
            void (async () => {
              try {
                await saveColumnPreferencesToProfile(columnPrefs)
                setPrefsSavedMessage('Column preferences saved to your profile.')
              } catch {
                setPrefsSavedMessage('Unable to save preferences to profile. Try again.')
              }
              window.setTimeout(() => setPrefsSavedMessage(null), 2500)
            })()
          }}
          onReset={() => {
            void (async () => {
              const defaults = await resetColumnPreferencesOnProfile()
              setColumnPrefs(defaults)
              setPrefsSavedMessage('Columns reset to default.')
              window.setTimeout(() => setPrefsSavedMessage(null), 2500)
            })()
          }}
        />
      </div>

      {prefsSavedMessage ? (
        <div className="cases-toast" role="status" data-testid="cases-prefs-toast">
          {prefsSavedMessage}
        </div>
      ) : null}
      {successToast ? (
        <div className="cases-toast" role="status" data-testid="cases-create-success-toast">
          {successToast}
        </div>
      ) : null}
      {toast ? (
        <div className="cases-toast" role="status" data-testid="cases-toast">
          {toast}
        </div>
      ) : null}

      <CaseDetailsModal
        record={selected}
        mode={detailMode}
        onClose={() => {
          setDetailMode(null)
          setSelected(null)
        }}
        onSave={async (next) => {
          try {
            if (isDbmCaseRecord(next)) {
              // DBM edit persists via DBM API so the receiving team gets a notification.
              const current = await getDbmWorkOrder(next.id)
              await updateDbmWorkOrder(
                next.id,
                toUpdateRequestFromGridEdit(current, {
                  clientId: next.clientId,
                  subject: next.subject,
                  caseStatus: next.caseStatus,
                  priority: next.priority,
                  requestedDueDate: next.requestedDueDate,
                  description: next.description,
                }),
              )
            } else {
              await updateCase(next)
            }
            setAllCases((prev) => prev.map((c) => (c.id === next.id ? next : c)))
            setDetailMode(null)
            setSelected(null)
            showToast(`Case ${next.caseId} updated.`)
            void load()
          } catch {
            showToast(`Unable to save case ${next.caseId}.`)
          }
        }}
      />

      <NewCaseTypeModal
        open={caseTypeModalOpen}
        onClose={() => setCaseTypeModalOpen(false)}
        onSelect={handleCaseTypeSelected}
      />
    </div>
  )
}
