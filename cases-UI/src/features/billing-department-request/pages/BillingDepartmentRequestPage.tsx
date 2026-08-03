import { useSearchParams } from 'react-router-dom'
import {
  BillingRequestForm,
  type BillingPageMode,
} from '@/features/billing-department-request/components/BillingRequestForm'
import '@/features/cases/pages/CasesSearchPage.css'
import '@/features/billing-department-request/components/BillingRequestForm.css'

/**
 * Billing Department Request page — Create / View / Edit via query params.
 * Page title matches User Story: full-width navy bar, no Back to Cases.
 */
export function BillingDepartmentRequestPage() {
  const [params] = useSearchParams()
  const modeParam = params.get('mode')
  const caseIdRaw = params.get('caseId')
  const caseId = caseIdRaw && /^\d+$/.test(caseIdRaw) ? Number(caseIdRaw) : null

  let mode: BillingPageMode = 'create'
  if (caseId != null && modeParam === 'view') mode = 'view'
  else if (caseId != null && modeParam === 'edit') mode = 'edit'
  else if (caseId != null) mode = 'view'

  return (
    <div className="billing-page" data-testid="billing-case-create-page">
      <div className="billing-page__inner">
        <header className="billing-page__title-bar" data-testid="billing-page-header">
          <h1 className="billing-page__title">Billing Department Request</h1>
        </header>

        <BillingRequestForm mode={mode} caseId={mode === 'create' ? null : caseId} />
      </div>
    </div>
  )
}
