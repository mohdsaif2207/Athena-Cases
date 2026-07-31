import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { fetchCases } from '@/features/cases/api/casesApi'
import type { CaseTypeOption } from '@/features/cases/api/lookupApi'
import { CaseDetailsModal } from '@/features/cases/components/CaseDetailsModal'
import { CasesAdvancedFilters } from '@/features/cases/components/CasesAdvancedFilters'
import { CasesColumnConfig } from '@/features/cases/components/CasesColumnConfig'
import { CasesGrid } from '@/features/cases/components/CasesGrid'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { CasesToolbar } from '@/features/cases/components/CasesToolbar'
import { NewCaseTypeModal } from '@/features/cases/components/NewCaseTypeModal'
import { NotificationQueuePanel } from '@/features/cases/components/NotificationQueuePanel'
import { WorkflowQueuePanel } from '@/features/cases/components/WorkflowQueuePanel'
import {
  EMPTY_ADVANCED_FILTERS,
  EMPTY_COLUMN_FILTERS,
} from '@/features/cases/mock/casesMockData'
import {
  loadColumnPreferences,
  resetColumnPreferences,
  saveColumnPreferences,
} from '@/features/cases/utils/columnPrefs'
import { exportCasesToExcel } from '@/features/cases/utils/exportCasesExcel'
import { applyCaseFilters, paginate } from '@/features/cases/utils/filterCases'
import { resolveCaseTypeCreatePath } from '@/features/cases/utils/caseTypeRoutes'
import type {
  AdvancedFilterState,
  CaseDetailMode,
  CaseRecord,
  ColumnFilterState,
  ColumnPreference,
} from '@/features/cases/types'
import './CasesSearchPage.css'

const PAGE_SIZE = 10

export function CasesSearchPage() {
  const { user } = useAuth()
  const navigate = useNavigate()
  const permissions = user?.permissions ?? []

  const canCreate = permissions.includes('CASES_CREATE') || permissions.includes('CASES_ACCESS')
  const canEdit = permissions.includes('CASES_EDIT') || permissions.includes('CASES_ACCESS')
  const canExport =
    permissions.includes('CASES_EXPORT') ||
    permissions.includes('CASES_ACCESS') ||
    permissions.includes('CASES_VIEW')
  const canViewWorkflow = permissions.includes('WF_VIEW')
  const canViewNotification = permissions.includes('NOTIF_VIEW')

  const [allCases, setAllCases] = useState<CaseRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

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

  const filtered = useMemo(
    () => applyCaseFilters(allCases, columnFilters, advancedFilters),
    [allCases, columnFilters, advancedFilters],
  )

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize) || 1)
  const safePage = Math.min(page, pageCount - 1)
  const pageRows = paginate(filtered, safePage, pageSize)

  useEffect(() => {
    if (page > pageCount - 1) setPage(Math.max(0, pageCount - 1))
  }, [page, pageCount])

  function clearAll() {
    setColumnFilters({ ...EMPTY_COLUMN_FILTERS })
    setAdvancedFilters({ ...EMPTY_ADVANCED_FILTERS })
    setPage(0)
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

  return (
    <div className="cases-search" data-testid="cases-search-page">
      <CasesToolbar
        filteredCount={filtered.length}
        advancedOpen={advancedOpen}
        columnsOpen={columnsOpen}
        canCreate={canCreate}
        canExport={canExport}
        onClearAll={clearAll}
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
        onNewCase={() => setCaseTypeModalOpen(true)}
      />

      <CasesAdvancedFilters
        open={advancedOpen}
        value={advancedFilters}
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
            saveColumnPreferences(columnPrefs)
            setPrefsSavedMessage('Column preferences saved.')
            window.setTimeout(() => setPrefsSavedMessage(null), 2500)
          }}
          onReset={() => {
            setColumnPrefs(resetColumnPreferences())
            setPrefsSavedMessage('Columns reset to default.')
            window.setTimeout(() => setPrefsSavedMessage(null), 2500)
          }}
        />
      </div>

      {canViewWorkflow ? <WorkflowQueuePanel onToast={showToast} /> : null}
      {canViewNotification ? <NotificationQueuePanel onToast={showToast} /> : null}

      {prefsSavedMessage ? (
        <div className="cases-toast" role="status" data-testid="cases-prefs-toast">
          {prefsSavedMessage}
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
        onSave={(next) => {
          setAllCases((prev) => prev.map((c) => (c.id === next.id ? next : c)))
          setDetailMode(null)
          setSelected(null)
          showToast(`Case ${next.caseId} updated.`)
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
