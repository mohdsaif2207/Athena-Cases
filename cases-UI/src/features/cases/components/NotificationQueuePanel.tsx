import { useEffect, useMemo, useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { fetchNotifications } from '@/features/cases/api/notificationApi'
import { CasesPagination } from '@/features/cases/components/CasesPagination'
import { QueueEditButton, QueueViewButton } from '@/features/cases/components/QueueActionButtons'
import { QueueDetailModal } from '@/features/cases/components/QueueDetailModal'
import { exportNotificationsToExcel } from '@/features/cases/utils/exportQueueExcel'
import { paginate } from '@/features/cases/utils/filterCases'
import { canEditQueueRecord } from '@/features/cases/utils/queueEditAccess'
import type { NotificationRecord, QueueDetailFields } from '@/features/cases/types'

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
  const [rows, setRows] = useState<NotificationRecord[]>([])
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [filters, setFilters] = useState({ ...EMPTY_FILTERS })
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(PAGE_SIZE)
  const [detailOpen, setDetailOpen] = useState(false)
  const [detailTitle, setDetailTitle] = useState('View Notification Details')
  const [detailFields, setDetailFields] = useState<QueueDetailFields | null>(null)

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLoading(true)
      setError(null)
      try {
        const data = await fetchNotifications()
        if (!cancelled) setRows(data)
      } catch {
        if (!cancelled) {
          setError('Unable to load notification queue.')
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

  function openDetail(row: NotificationRecord, mode: 'view' | 'edit') {
    setDetailTitle(mode === 'view' ? 'View Notification Details' : 'Edit Notification Details')
    setDetailFields({
      messageId: row.messageId,
      messageName: row.messageName,
      messageKey: row.messageKey,
      message: row.message,
      priority: row.priority,
      owner: row.owner,
      receivedDate: row.receivedDate,
      updatedDate: row.updatedDate,
      senderSystem: 'ATHENA',
      senderUserId: row.createdBy,
      senderUserGroup: '',
    })
    setDetailOpen(true)
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
        <button
          type="button"
          className="cases-action-btn is-disabled"
          disabled
          title="Coming soon"
          data-testid="notification-reply"
        >
          Reply
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
        fields={detailFields}
        onClose={() => {
          setDetailOpen(false)
          setDetailFields(null)
        }}
      />
    </section>
  )
}

function matches(value: string, filter: string): boolean {
  if (!filter.trim()) return true
  return value.toLowerCase().includes(filter.trim().toLowerCase())
}
