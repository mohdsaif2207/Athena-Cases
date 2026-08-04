import { useCallback, useEffect, useMemo, useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { fetchWorkflows, updateWorkflow } from '@/features/cases/api/workflowApi'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { QueueEditButton, QueueViewButton } from '@/features/cases/components/QueueActionButtons'
import { QueueDetailModal } from '@/features/cases/components/QueueDetailModal'
import { exportWorkflowsToExcel } from '@/features/cases/utils/exportQueueExcel'
import { paginate } from '@/features/cases/utils/filterCases'
import { canEditQueueRecord } from '@/features/cases/utils/queueEditAccess'
import type { QueueDetailFields, QueueDetailMode, WorkflowRecord } from '@/features/cases/types'

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
  const { user } = useAuth()
  const [rows, setRows] = useState<WorkflowRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filters, setFilters] = useState({ ...EMPTY_FILTERS })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(PAGE_SIZE)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailMode, setDetailMode] = useState<QueueDetailMode>('view')
  const [detailTitle, setDetailTitle] = useState('View Workflow Details')
  const [detailFields, setDetailFields] = useState<QueueDetailFields | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchWorkflows()
      setRows(data)
    } catch {
      setError('Unable to load workflow queue.')
      setRows([])
    } finally {
      setLoading(false)
    }
  }, [])

  useEffect(() => {
    void load()
  }, [load])

  const filtered = useMemo(
    () =>
      rows.filter((row) =>
        (Object.keys(filters) as FilterKey[]).every((key) => {
          if (key === 'logs') return true
          return matches(String(row[key as keyof WorkflowRecord] ?? ''), filters[key])
        }),
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

  function openDetail(row: WorkflowRecord, mode: QueueDetailMode) {
    setDetailMode(mode)
    setSaveError(null)
    setDetailTitle(mode === 'view' ? 'View Workflow Details' : 'Edit Workflow Details')
    setDetailFields({
      id: row.id,
      messageId: row.messageId,
      messageName: row.messageName,
      messageKey: row.messageKey,
      message: row.logs || row.messageObject || row.messageName,
      status: row.status,
      decision: row.decision,
      priority: row.priority,
      owner: row.owner,
      details: '',
      receivedDate: row.receivedDate,
      updatedDate: row.updatedDate,
      senderSystem: 'ATHENA',
      senderUserId: row.createdBy,
      senderUserGroup: '',
    })
    setDetailOpen(true)
  }

  async function handleSave(next: QueueDetailFields) {
    setSaving(true)
    setSaveError(null)
    try {
      const updated = await updateWorkflow(next.id, {
        status: next.status,
        decision: next.decision,
        priority: next.priority,
        owner: next.owner,
      })
      setRows((prev) => prev.map((r) => (r.id === updated.id ? updated : r)))
      setDetailOpen(false)
      setDetailFields(null)
      onToast(`Workflow ${updated.workflowId} updated.`)
    } catch {
      setSaveError('Unable to save workflow changes. Please try again.')
    } finally {
      setSaving(false)
    }
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
                          disabled={key === 'logs'}
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
                        <td>
                          <span className="queue-actions" data-testid={`workflow-logs-${row.workflowId}`}>
                            <QueueViewButton
                              testId={`workflow-view-${row.workflowId}`}
                              onClick={() => openDetail(row, 'view')}
                            />
                            {canEditQueueRecord(user, row.createdBy, row.receivingTeamCode) ? (
                              <QueueEditButton
                                testId={`workflow-edit-${row.workflowId}`}
                                onClick={() => openDetail(row, 'edit')}
                              />
                            ) : null}
                          </span>
                        </td>
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

      <QueueDetailModal
        open={detailOpen}
        title={detailTitle}
        mode={detailMode}
        kind="workflow"
        fields={detailFields}
        saving={saving}
        error={saveError}
        onClose={() => {
          setDetailOpen(false)
          setDetailFields(null)
          setSaveError(null)
        }}
        onSave={handleSave}
      />
    </section>
  )
}

function matches(value: string, filter: string): boolean {
  if (!filter.trim()) return true
  return value.toLowerCase().includes(filter.trim().toLowerCase())
}
