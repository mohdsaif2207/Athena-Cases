import { Link, useSearchParams } from 'react-router-dom'
import {
  BillingRequestForm,
  type BillingPageMode,
} from '@/features/billing-department-request/components/BillingRequestForm'
import '@/features/cases/pages/CasesSearchPage.css'
import '@/features/billing-department-request/components/BillingRequestForm.css'

/**
 * Billing Department Request page — Create / View / Edit via query params.
 * Visual layout aligned to teammate Create Case screenshots (navy section cards).
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

  const title =
    mode === 'view'
      ? 'View Billing Department Request'
      : mode === 'edit'
        ? 'Edit Billing Department Request'
        : 'Create Billing Department Request'

  return (
    <div className="billing-page" data-testid="billing-case-create-page">
      <div className="billing-page__inner">
        <header className="billing-page__header">
          <h1 className="billing-page__title">{title}</h1>
          <Link to="/cases" className="billing-page__back" data-testid="billing-back-to-cases">
            Back to Cases
          </Link>
        </header>

        <BillingRequestForm mode={mode} caseId={mode === 'create' ? null : caseId} />
      </div>
    </div>
  )
}
