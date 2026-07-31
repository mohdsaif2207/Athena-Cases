/**
 * Maps authorized case-type codes to feature creation routes.
 */
export function resolveCaseTypeCreatePath(caseTypeCode: string): string | null {
  switch (caseTypeCode) {
    case 'DBM_WORK_ORDER_REQUEST':
      return '/cases/new/dbm'
    case 'EXRT_REQUEST':
      return '/cases/new/exrt'
    case 'BILLING_DEPARTMENT_REQUEST':
      return '/cases/new/billing'
    default:
      return null
  }
}
