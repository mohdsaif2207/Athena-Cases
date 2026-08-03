import { Link } from 'react-router-dom'
import { ExtractsSection } from '@/features/billing-department-request/components/ExtractsSection'
import { FormActionsBar } from '@/features/billing-department-request/components/FormActionsBar'
import { GeneralSection } from '@/features/billing-department-request/components/GeneralSection'
import { HoldsSection } from '@/features/billing-department-request/components/HoldsSection'
import '@/features/cases/pages/CasesSearchPage.css'

/**
 * Billing Department Request Create screen skeleton — Phase 2.
 *
 * Visual shell only: matches existing Create Case layout
 * (`case-create-placeholder` + shared `cases-*` classes).
 * No form state, validation, or API calls yet.
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

      <div className="cases-search" data-testid="billing-request-form">
        <GeneralSection />
        <ExtractsSection />
        <HoldsSection />
        <FormActionsBar />
      </div>
    </div>
  )
}
