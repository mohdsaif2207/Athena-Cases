import { BillingDepartmentRequestPage } from '@/features/billing-department-request'

/**
 * Route entry for `/cases/new/billing`.
 * Delegates to the Billing feature module; keeps the shared cases route map stable.
 */
export function BillingCaseCreatePage() {
  return <BillingDepartmentRequestPage />
}
