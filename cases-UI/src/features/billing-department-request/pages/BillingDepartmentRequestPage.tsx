import { Link } from 'react-router-dom'
import { BillingRequestFormConnected } from '@/features/billing-department-request/components/BillingRequestForm'
import '@/features/cases/pages/CasesSearchPage.css'

/**
 * Billing Department Request Create screen — Milestone 1 UI complete.
 * No API integration, validation, or Save/Cancel/Reset behaviour yet.
 */
export function BillingDepartmentRequestPage() {
  return (
    <div className="case-create-placeholder" data-testid="billing-case-create-page">
      <header className="case-create-placeholder__header">
        <h1>Billing Department Request</h1>
        <Link to="/cases" className="cases-btn cases-btn--primary" data-testid="billing-back-to-cases">
          Back to Cases
        </Link>
      </header>

      <BillingRequestFormConnected />
    </div>
  )
}
