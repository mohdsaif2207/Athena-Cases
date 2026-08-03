import { useCallback, useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { fetchActiveClients } from '@/features/billing-department-request/api/billingLookupApi'
import {
  createBillingDepartmentRequest,
  fetchBillingAssignees,
  fetchBillingHoldLevels,
  getBillingDepartmentRequest,
  updateBillingDepartmentRequest,
} from '@/features/billing-department-request/api/billingRequestApi'
import { BillingConfirmDialog } from '@/features/billing-department-request/components/BillingConfirmDialog'
import { ExtractsSection } from '@/features/billing-department-request/components/ExtractsSection'
import { FormActionsBar } from '@/features/billing-department-request/components/FormActionsBar'
import { GeneralSection } from '@/features/billing-department-request/components/GeneralSection'
import { HoldsSection } from '@/features/billing-department-request/components/HoldsSection'
import type { BillingAssigneeDto, LookupItemDto } from '@/features/billing-department-request/types/billingTypes'
import {
  getBillingErrorMessage,
  mapApiErrorsToFields,
} from '@/features/billing-department-request/utils/billingApiErrors'
import {
  createDefaultBillingFormValues,
  formValuesToCreatePayload,
  formValuesToUpdatePayload,
  responseToFormValues,
  type BillingFormValues,
} from '@/features/billing-department-request/utils/billingFormMapper'
import {
  validateBillingForm,
  type BillingFieldErrors,
} from '@/features/billing-department-request/validation/billingFormValidation'
import { BILLING_HOLD_LEVEL_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'

export type BillingPageMode = 'create' | 'view' | 'edit'

interface BillingRequestFormProps {
  mode: BillingPageMode
  caseId: number | null
}

export function BillingRequestForm({ mode, caseId }: BillingRequestFormProps) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const caseOwnerUsername = user?.username?.trim() || ''

  const [values, setValues] = useState<BillingFormValues>(createDefaultBillingFormValues)
  const [errors, setErrors] = useState<BillingFieldErrors>({})
  const [clients, setClients] = useState<LookupItemDto[]>([])
  const [assignees, setAssignees] = useState<BillingAssigneeDto[]>([])
  const [holdLevelOptions, setHoldLevelOptions] = useState<string[]>([...BILLING_HOLD_LEVEL_OPTIONS])
  const [loading, setLoading] = useState(mode !== 'create')
  const [lookupsLoading, setLookupsLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [confirm, setConfirm] = useState<'cancel' | 'reset' | null>(null)

  const readOnly = mode === 'view'
  const validationMode = mode === 'edit' ? 'edit' : 'create'

  const showToast = useCallback((message: string) => {
    setToast(message)
    window.setTimeout(() => setToast(null), 3200)
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLookupsLoading(true)
      try {
        const [clientRows, assigneeRows, holdLevels] = await Promise.all([
          fetchActiveClients(),
          fetchBillingAssignees().catch(() => [] as BillingAssigneeDto[]),
          fetchBillingHoldLevels().catch(() => [] as string[]),
        ])
        if (cancelled) return
        setClients(clientRows)
        setAssignees(assigneeRows)
        if (holdLevels.length) setHoldLevelOptions(holdLevels)
      } finally {
        if (!cancelled) setLookupsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    if (mode === 'create' || caseId == null) {
      setLoading(false)
      setLoadError(null)
      setValues(createDefaultBillingFormValues())
      return
    }

    let cancelled = false
    ;(async () => {
      setLoading(true)
      setLoadError(null)
      try {
        const dto = await getBillingDepartmentRequest(caseId)
        if (cancelled) return
        setValues(responseToFormValues(dto))
      } catch (err) {
        if (cancelled) return
        setLoadError(getBillingErrorMessage(err))
      } finally {
        if (!cancelled) setLoading(false)
      }
    })()

    return () => {
      cancelled = true
    }
  }, [mode, caseId])

  function onChange<K extends keyof BillingFormValues>(key: K, value: BillingFormValues[K]) {
    setValues((prev) => ({ ...prev, [key]: value }))
    setErrors((prev) => {
      if (!prev[key]) return prev
      const next = { ...prev }
      delete next[key]
      return next
    })
  }

  async function handleSave() {
    const clientErrors = validateBillingForm(values, validationMode)
    if (Object.keys(clientErrors).length) {
      setErrors(clientErrors)
      showToast('Please correct the highlighted fields.')
      return
    }

    setSaving(true)
    setErrors({})
    try {
      if (mode === 'edit') {
        if (caseId == null) throw new Error('Missing caseId')
        const updated = await updateBillingDepartmentRequest(caseId, formValuesToUpdatePayload(values))
        setValues(responseToFormValues(updated))
        showToast(`Case ${updated.caseNumber} updated.`)
        navigate('/cases')
        return
      }

      const created = await createBillingDepartmentRequest(formValuesToCreatePayload(values))
      showToast(`Case created successfully with Case ID ${created.caseNumber}.`)
      navigate('/cases')
    } catch (err) {
      const fieldErrors = mapApiErrorsToFields(err)
      if (Object.keys(fieldErrors).length) setErrors(fieldErrors)
      showToast(getBillingErrorMessage(err))
    } finally {
      setSaving(false)
    }
  }

  function handleCancelRequest() {
    if (mode === 'view') {
      navigate('/cases')
      return
    }
    setConfirm('cancel')
  }

  function handleResetRequest() {
    setConfirm('reset')
  }

  function confirmAction() {
    if (confirm === 'cancel') {
      setConfirm(null)
      navigate('/cases')
      return
    }
    if (confirm === 'reset') {
      setConfirm(null)
      const defaults = createDefaultBillingFormValues()
      if (mode === 'edit') {
        defaults.version = values.version
      }
      setValues(defaults)
      setErrors({})
    }
  }

  if (loading || lookupsLoading) {
    return (
      <div className="cases-state cases-state--loading" data-testid="billing-loading">
        <div className="cases-spinner" aria-hidden="true" />
        <p>Loading Billing Department Request…</p>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="cases-state cases-state--error" role="alert" data-testid="billing-load-error">
        <p>{loadError}</p>
        <button type="button" className="cases-btn cases-btn--primary" onClick={() => navigate('/cases')}>
          Back to Cases
        </button>
      </div>
    )
  }

  return (
    <div className="billing-form" data-testid="billing-request-form">
      <GeneralSection
        values={values}
        caseOwner={caseOwnerUsername}
        readOnly={readOnly}
        errors={errors}
        clients={clients}
        assignees={assignees}
        onChange={onChange}
      />
      <ExtractsSection values={values} readOnly={readOnly} errors={errors} onChange={onChange} />
      <HoldsSection
        values={values}
        readOnly={readOnly}
        errors={errors}
        holdLevelOptions={holdLevelOptions}
        onChange={onChange}
      />
      <FormActionsBar
        mode={mode}
        saving={saving}
        onSave={() => void handleSave()}
        onCancel={handleCancelRequest}
        onReset={handleResetRequest}
      />

      <BillingConfirmDialog
        open={confirm === 'cancel'}
        message="Unsaved changes will be lost. Do you want to continue?"
        testId="billing-cancel-confirm"
        onConfirm={confirmAction}
        onCancel={() => setConfirm(null)}
      />
      <BillingConfirmDialog
        open={confirm === 'reset'}
        title="Reset form?"
        message="Reset will clear all non-default field values. Do you want to continue?"
        testId="billing-reset-confirm"
        onConfirm={confirmAction}
        onCancel={() => setConfirm(null)}
      />

      {toast ? (
        <div className="cases-toast" role="status" data-testid="billing-toast">
          {toast}
        </div>
      ) : null}
    </div>
  )
}
