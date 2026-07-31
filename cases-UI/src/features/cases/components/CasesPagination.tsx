interface CasesPaginationProps {
  page: number
  pageCount: number
  pageSize: number
  totalItems: number
  onPageChange: (page: number) => void
  onPageSizeChange?: (size: number) => void
}

export function CasesPagination({
  page,
  pageCount,
  pageSize,
  totalItems,
  onPageChange,
  onPageSizeChange,
}: CasesPaginationProps) {
  const safeCount = Math.max(pageCount, 1)
  const from = totalItems === 0 ? 0 : page * pageSize + 1
  const to = Math.min((page + 1) * pageSize, totalItems)

  const maxButtons = 5
  let start = Math.max(0, page - 2)
  const end = Math.min(safeCount, start + maxButtons)
  start = Math.max(0, end - maxButtons)
  const pages = Array.from({ length: end - start }, (_, i) => start + i)

  return (
    <nav className="cases-pagination" aria-label="Cases pagination" data-testid="cases-pagination">
      <div className="cases-pagination__left">
        <button
          type="button"
          className="cases-page-nav"
          disabled={page <= 0 || totalItems === 0}
          onClick={() => onPageChange(0)}
          aria-label="First page"
          data-testid="cases-page-first"
        >
          |&lt;
        </button>
        <button
          type="button"
          className="cases-page-nav"
          disabled={page <= 0 || totalItems === 0}
          onClick={() => onPageChange(page - 1)}
          aria-label="Previous page"
          data-testid="cases-page-prev"
        >
          &lt;
        </button>

        {pages.map((p) => (
          <button
            key={p}
            type="button"
            className={`cases-page-num ${p === page ? 'is-active' : ''}`}
            onClick={() => onPageChange(p)}
            aria-current={p === page ? 'page' : undefined}
            disabled={totalItems === 0}
            data-testid={`cases-page-${p + 1}`}
          >
            {p + 1}
          </button>
        ))}
        {end < safeCount ? <span className="cases-page-ellipsis">...</span> : null}

        <button
          type="button"
          className="cases-page-nav"
          disabled={page >= safeCount - 1 || totalItems === 0}
          onClick={() => onPageChange(page + 1)}
          aria-label="Next page"
          data-testid="cases-page-next"
        >
          &gt;
        </button>
        <button
          type="button"
          className="cases-page-nav"
          disabled={page >= safeCount - 1 || totalItems === 0}
          onClick={() => onPageChange(safeCount - 1)}
          aria-label="Last page"
          data-testid="cases-page-last"
        >
          &gt;|
        </button>

        <label className="cases-page-size">
          <select
            value={pageSize}
            onChange={(e) => onPageSizeChange?.(Number(e.target.value))}
            disabled={!onPageSizeChange}
            data-testid="cases-page-size"
          >
            <option value={10}>10</option>
            <option value={25}>25</option>
            <option value={50}>50</option>
          </select>
          <span>items per page</span>
        </label>
      </div>

      <div className="cases-pagination__right" data-testid="cases-filtered-count">
        {totalItems === 0 ? '0 items' : `${from} - ${to} of ${totalItems} items`}
      </div>
    </nav>
  )
}
