import { useEffect, useMemo, useState } from 'react'
import { fetchWorkflows } from '@/features/cases/api/workflowApi'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { exportWorkflowsToExcel } from '@/features/cases/utils/exportQueueExcel'
import { paginate } from '@/features/cases/utils/filterCases'
import type { WorkflowRecord } from '@/features/cases/types'

const PAGE_SIZE = 10

const EMPTY_FILTERS = {
  workflowId: '',
  messageKey: '',
  messageId: '',
  messageName: '',
  messageObject: '',
  status: '',
  decision: '',
  owner: '',
  priority: '',
  receivedDate: '',
  action: '',
  logs: '',
}

type FilterKey = keyof typeof EMPTY_FILTERS

interface WorkflowQueuePanelProps {
  onToast: (message: string) => void
}

export function WorkflowQueuePanel({ onToast }: WorkflowQueuePanelProps) {
  const [rows, setRows] = useState<WorkflowRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filters, setFilters] = useState({ ...EMPTY_FILTERS })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(PAGE_SIZE)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await fetchWorkflows()
        if (!cancelled) setRows(data)
      } catch {
        if (!cancelled) {
          setError('Unable to load workflow queue.')
          setRows([])
        }
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  const filtered = useMemo(
    () =>
      rows.filter((row) =>
        (Object.keys(filters) as FilterKey[]).every((key) =>
          matches(String(row[key as keyof WorkflowRecord] ?? ''), filters[key]),
        ),
      ),
    [rows, filters],
  )

  const pageCount = Math.max(1, Math.ceil(filtered.length / pageSize) || 1)
  const safePage = Math.min(page, pageCount - 1)
  const pageRows = paginate(filtered, safePage, pageSize)

  useEffect(() => {
    if (page > pageCount - 1) setPage(Math.max(0, pageCount - 1))
  }, [page, pageCount])

  function setFilter(key: FilterKey, value: string) {
    setFilters((prev) => ({ ...prev, [key]: value }))
    setPage(0)
  }

  return (
    <section className="cases-queue" data-testid="workflow-queue">
      <div className="cases-queue__title">Workflow Queue</div>
      <div className="cases-toolbar cases-queue__toolbar">
        <button
          type="button"
          className="cases-action-btn"
          data-testid="workflow-export"
          onClick={() => {
            exportWorkflowsToExcel(filtered)
            onToast(`Exported ${filtered.length} workflow(s) to Excel.`)
          }}
        >
          Export to Excel
        </button>
        <button
          type="button"
          className="cases-action-btn"
          data-testid="workflow-clear-filters"
          onClick={() => {
            setFilters({ ...EMPTY_FILTERS })
            setPage(0)
          }}
        >
          Clear Filters
        </button>
        <button
          type="button"
          className="cases-action-btn is-disabled"
          disabled
          title="Coming soon"
          data-testid="workflow-reply"
        >
          Reply
        </button>
      </div>

      <div className="cases-search__main">
        {loading ? (
          <div className="cases-state cases-state--loading" data-testid="workflow-loading">
            <div className="cases-spinner" aria-hidden="true" />
            <p>Loading workflows…</p>
          </div>
        ) : null}

        {!loading && error ? (
          <div className="cases-state cases-state--error" role="alert" data-testid="workflow-error">
            <p>{error}</p>
          </div>
        ) : null}

        {!loading && !error ? (
          <>
            <div className="cases-grid-wrap">
              <table className="cases-grid cases-queue__grid" data-testid="workflow-grid">
                <thead>
                  <tr>
                    <th>Workflow ID</th>
                    <th>Message Key</th>
                    <th>Message ID</th>
                    <th>Message Name</th>
                    <th>Message Object</th>
                    <th>Status</th>
                    <th>Decision</th>
                    <th>Owner</th>
                    <th>Priority</th>
                    <th>Received Date</th>
                    <th>Action</th>
                    <th>Logs</th>
                  </tr>
                  <tr className="cases-grid__filters">
                    {(Object.keys(EMPTY_FILTERS) as FilterKey[]).map((key) => (
                      <th key={key}>
                        <input
                          value={filters[key]}
                          onChange={(e) => setFilter(key, e.target.value)}
                          aria-label={`Filter ${key}`}
                          data-testid={`workflow-filter-${key}`}
                        />
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {pageRows.length === 0 ? (
                    <tr data-testid="workflow-empty">
                      <td className="cases-grid__empty" colSpan={12}>
                        No workflows available.
                      </td>
                    </tr>
                  ) : (
                    pageRows.map((row, index) => (
                      <tr
                        key={row.id}
                        className={index % 2 === 1 ? 'is-alt' : undefined}
                        data-testid={`workflow-row-${row.workflowId}`}
                      >
                        <td>{row.workflowId}</td>
                        <td>{row.messageKey}</td>
                        <td>{row.messageId}</td>
                        <td>{row.messageName}</td>
                        <td>{row.messageObject}</td>
                        <td>{row.status}</td>
                        <td>{row.decision}</td>
                        <td>{row.owner}</td>
                        <td>{row.priority}</td>
                        <td>{row.receivedDate}</td>
                        <td>{row.action}</td>
                        <td>{row.logs}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
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
    </section>
  )
}

function matches(value: string, filter: string): boolean {
  if (!filter.trim()) return true
  return value.toLowerCase().includes(filter.trim().toLowerCase())
}
