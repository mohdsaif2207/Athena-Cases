import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { fetchNotifications, updateNotification } from '@/features/cases/api/notificationApi'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { QueueEditButton, QueueViewButton } from '@/features/cases/components/QueueActionButtons'
import { QueueDetailModal } from '@/features/cases/components/QueueDetailModal'
import { exportNotificationsToExcel } from '@/features/cases/utils/exportQueueExcel'
import { paginate } from '@/features/cases/utils/filterCases'
import { canEditQueueRecord } from '@/features/cases/utils/queueEditAccess'
import type { NotificationRecord, QueueDetailFields, QueueDetailMode } from '@/features/cases/types'

const PAGE_SIZE = 10

const EMPTY_FILTERS = {
  notificationId: '',
  messageKey: '',
  messageId: '',
  messageName: '',
  messageObject: '',
  message: '',
  receivedDate: '',
  details: '',
}

type FilterKey = keyof typeof EMPTY_FILTERS

interface NotificationQueuePanelProps {
  onToast: (message: string) => void
}

export function NotificationQueuePanel({ onToast }: NotificationQueuePanelProps) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const [rows, setRows] = useState<NotificationRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filters, setFilters] = useState({ ...EMPTY_FILTERS })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(PAGE_SIZE)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailMode, setDetailMode] = useState<QueueDetailMode>('view')
  const [detailTitle, setDetailTitle] = useState('View Notification Details')
  const [detailFields, setDetailFields] = useState<QueueDetailFields | null>(null)
  const [saving, setSaving] = useState(false)
  const [saveError, setSaveError] = useState<string | null>(null)

  const load = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await fetchNotifications()
      setRows(data)
    } catch {
      setError('Unable to load notification queue.')
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
          if (key === 'details') return true
          return matches(String(row[key as keyof NotificationRecord] ?? ''), filters[key])
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

  function openDetail(row: NotificationRecord, mode: QueueDetailMode) {
    // Deep links (stored in details) open the case page — e.g. Billing view route.
    if (mode === 'view' && row.details?.startsWith('/')) {
      navigate(row.details)
      return
    }
    setDetailMode(mode)
    setSaveError(null)
    setDetailTitle(mode === 'view' ? 'View Notification Details' : 'Edit Notification Details')
    setDetailFields({
      id: row.id,
      messageId: row.messageId,
      messageName: row.messageName,
      messageKey: row.messageKey,
      message: row.message,
      status: '',
      decision: '',
      priority: row.priority,
      owner: row.owner,
      details: row.details,
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
      const updated = await updateNotification(next.id, {
        messageName: next.messageName,
        message: next.message,
        details: next.details,
      })
      setRows((prev) => prev.map((r) => (r.id === updated.id ? updated : r)))
      setDetailOpen(false)
      setDetailFields(null)
      onToast(`Notification ${updated.notificationId} updated.`)
    } catch {
      setSaveError('Unable to save notification changes. Please try again.')
    } finally {
      setSaving(false)
    }
  }

  return (
    <section className="cases-queue" data-testid="notification-queue">
      <div className="cases-queue__title">Notification Queue</div>
      <div className="cases-toolbar cases-queue__toolbar">
        <button
          type="button"
          className="cases-action-btn"
          data-testid="notification-export"
          onClick={() => {
            exportNotificationsToExcel(filtered)
            onToast(`Exported ${filtered.length} notification(s) to Excel.`)
          }}
        >
          Export to Excel
        </button>
        <button
          type="button"
          className="cases-action-btn"
          data-testid="notification-clear-filters"
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
          <div className="cases-state cases-state--loading" data-testid="notification-loading">
            <div className="cases-spinner" aria-hidden="true" />
            <p>Loading notifications…</p>
          </div>
        ) : null}

        {!loading && error ? (
          <div className="cases-state cases-state--error" role="alert" data-testid="notification-error">
            <p>{error}</p>
          </div>
        ) : null}

        {!loading && !error ? (
          <>
            <div className="cases-grid-wrap">
              <table className="cases-grid cases-queue__grid" data-testid="notification-grid">
                <thead>
                  <tr>
                    <th>Notification ID</th>
                    <th>Message Key</th>
                    <th>Message ID</th>
                    <th>Message Name</th>
                    <th>Message Object</th>
                    <th>Message</th>
                    <th>Received Date</th>
                    <th>Details</th>
                  </tr>
                  <tr className="cases-grid__filters">
                    {(Object.keys(EMPTY_FILTERS) as FilterKey[]).map((key) => (
                      <th key={key}>
                        <input
                          value={filters[key]}
                          onChange={(e) => setFilter(key, e.target.value)}
                          aria-label={`Filter ${key}`}
                          data-testid={`notification-filter-${key}`}
                          disabled={key === 'details'}
                        />
                      </th>
                    ))}
                  </tr>
                </thead>
                <tbody>
                  {pageRows.length === 0 ? (
                    <tr data-testid="notification-empty">
                      <td className="cases-grid__empty" colSpan={8}>
                        No notifications available.
                      </td>
                    </tr>
                  ) : (
                    pageRows.map((row, index) => (
                      <tr
                        key={row.id}
                        className={index % 2 === 1 ? 'is-alt' : undefined}
                        data-testid={`notification-row-${row.notificationId}`}
                      >
                        <td>{row.notificationId}</td>
                        <td>{row.messageKey}</td>
                        <td>{row.messageId}</td>
                        <td>{row.messageName}</td>
                        <td>{row.messageObject}</td>
                        <td>{row.message}</td>
                        <td>{row.receivedDate}</td>
                        <td>
                          <span className="queue-actions" data-testid={`notification-details-${row.notificationId}`}>
                            <QueueViewButton
                              testId={`notification-view-${row.notificationId}`}
                              onClick={() => openDetail(row, 'view')}
                            />
                            {canEditQueueRecord(user, row.createdBy, row.receivingTeamCode) ? (
                              <QueueEditButton
                                testId={`notification-edit-${row.notificationId}`}
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
        kind="notification"
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
