import { Link } from 'react-router-dom'

/**
 * Placeholder Cases screen so Cancel can navigate to the LLD route `/cases`.
 * Full Cases grid / notifications land in a later story.
 */
export function CasesListPage() {
  return (
    <div
      data-testid="cases-list-page"
      style={{ padding: 24, fontFamily: 'Segoe UI, sans-serif' }}
    >
      <h1 style={{ marginTop: 0 }}>Cases</h1>
      <p>Cases screen (placeholder).</p>
      <Link to="/cases/new/dbm-work-order" data-testid="cases-new-dbm-link">
        New DBM Work Order Request
      </Link>
    </div>
  )
}

export default CasesListPage
