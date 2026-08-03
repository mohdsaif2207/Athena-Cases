import { useState } from 'react'
import { ExtractsSection } from '@/features/billing-department-request/components/ExtractsSection'
import { FormActionsBar } from '@/features/billing-department-request/components/FormActionsBar'
import { GeneralSection } from '@/features/billing-department-request/components/GeneralSection'
import { HoldsSection } from '@/features/billing-department-request/components/HoldsSection'
import {
  createDefaultBillingFormValues,
  type BillingCreateFormValues,
} from '@/features/billing-department-request/types/billingTypes'
import { useAuth } from '@/contexts/AuthContext'

interface BillingRequestFormProps {
  caseOwner: string
}

/**
 * Create-mode form shell — local render state only (no validation / API / save).
 */
export function BillingRequestForm({ caseOwner }: BillingRequestFormProps) {
  const [values, setValues] = useState<BillingCreateFormValues>(createDefaultBillingFormValues)

  function onChange<K extends keyof BillingCreateFormValues>(
    key: K,
    value: BillingCreateFormValues[K],
  ) {
    setValues((prev) => ({ ...prev, [key]: value }))
  }

  return (
    <div className="cases-search" data-testid="billing-request-form">
      <GeneralSection values={values} caseOwner={caseOwner} onChange={onChange} />
      <ExtractsSection values={values} onChange={onChange} />
      <HoldsSection values={values} onChange={onChange} />
      <FormActionsBar />
    </div>
  )
}

/** Convenience wrapper that resolves Case Owner from auth for Create mode. */
export function BillingRequestFormConnected() {
  const { user } = useAuth()
  const caseOwner = user?.displayName?.trim() || user?.username || ''
  return <BillingRequestForm caseOwner={caseOwner} />
}
