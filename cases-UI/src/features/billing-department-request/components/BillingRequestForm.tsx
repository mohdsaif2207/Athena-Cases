import { useCallback, useEffect, useMemo, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import {
  fetchActiveClients,
  fetchCampaigns,
  fetchParentCases,
  fetchProducts,
  fetchSegmentsForClient,
} from '@/features/billing-department-request/api/billingLookupApi'
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
import { BILLING_HOLD_LEVEL_OPTIONS } from '@/features/billing-department-request/constants/billingEnums'
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

export type BillingPageMode = 'create' | 'view' | 'edit'

interface BillingRequestFormProps {
  mode: BillingPageMode
  caseId: number | null
}

function dedupeHoldLevels(levels: string[]): string[] {
  const seen = new Set<string>()
  const out: string[] = []
  for (const level of levels) {
    if (!level || seen.has(level)) continue
    seen.add(level)
    out.push(level)
  }
  return out
}

export function BillingRequestForm({ mode, caseId }: BillingRequestFormProps) {
  const { user } = useAuth()
  const navigate = useNavigate()
  const caseOwnerUsername = user?.username?.trim() || ''

  const [values, setValues] = useState<BillingFormValues>(createDefaultBillingFormValues)
  const [errors, setErrors] = useState<BillingFieldErrors>({})
  const [clients, setClients] = useState<LookupItemDto[]>([])
  const [campaigns, setCampaigns] = useState<LookupItemDto[]>([])
  const [products, setProducts] = useState<LookupItemDto[]>([])
  const [parentCases, setParentCases] = useState<LookupItemDto[]>([])
  const [segments, setSegments] = useState<LookupItemDto[]>([])
  const [assignees, setAssignees] = useState<BillingAssigneeDto[]>([])
  const [holdLevelOptions, setHoldLevelOptions] = useState<string[]>([
    ...BILLING_HOLD_LEVEL_OPTIONS,
  ])
  const [loading, setLoading] = useState(mode !== 'create')
  const [lookupsLoading, setLookupsLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [toast, setToast] = useState<string | null>(null)
  const [toastTone, setToastTone] = useState<'info' | 'error'>('info')
  const [confirm, setConfirm] = useState<'cancel' | 'reset' | null>(null)

  const readOnly = mode === 'view'
  const validationMode = mode === 'edit' ? 'edit' : 'create'

  const showToast = useCallback((message: string, tone: 'info' | 'error' = 'info') => {
    setToastTone(tone)
    setToast(message)
    window.setTimeout(() => setToast(null), 3200)
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      setLookupsLoading(true)
      try {
        const [clientRows, campaignRows, productRows, parentRows, assigneeRows, holdLevels] =
          await Promise.all([
            fetchActiveClients(),
            fetchCampaigns(),
            fetchProducts(),
            fetchParentCases(),
            fetchBillingAssignees().catch(() => [] as BillingAssigneeDto[]),
            fetchBillingHoldLevels().catch(() => [] as string[]),
          ])
        if (cancelled) return
        setClients(clientRows)
        setCampaigns(campaignRows)
        setProducts(productRows)
        setParentCases(parentRows)
        setAssignees(assigneeRows)
        const levels = dedupeHoldLevels(
          holdLevels.length ? holdLevels : [...BILLING_HOLD_LEVEL_OPTIONS],
        )
        setHoldLevelOptions(levels)
      } finally {
        if (!cancelled) setLookupsLoading(false)
      }
    })()
    return () => {
      cancelled = true
    }
  }, [])

  useEffect(() => {
    let cancelled = false
    ;(async () => {
      const rows = await fetchSegmentsForClient(values.clientId)
      if (!cancelled) setSegments(rows)
    })()
    return () => {
      cancelled = true
    }
  }, [values.clientId])

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

  function onClientChange(clientId: string) {
    setValues((prev) => ({ ...prev, clientId, segmentId: '' }))
    setErrors((prev) => {
      const next = { ...prev }
      delete next.clientId
      delete next.segmentId
      return next
    })
  }

  async function handleSave() {
    const clientErrors = validateBillingForm(values, validationMode, {
      caseOwner: caseOwnerUsername,
    })
    if (Object.keys(clientErrors).length) {
      setErrors(clientErrors)
      showToast('Please correct the highlighted fields.', 'error')
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
      const displayId = created.businessCaseId || created.caseNumber
      showToast(`Case created successfully with Case ID ${displayId}.`)
      // Keep toast visible — navigating immediately unmounts this page's toast.
      window.setTimeout(() => navigate('/cases'), 1800)
    } catch (err) {
      const fieldErrors = mapApiErrorsToFields(err)
      if (Object.keys(fieldErrors).length) setErrors(fieldErrors)
      showToast(getBillingErrorMessage(err), 'error')
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
      setSegments([])
    }
  }

  const holdLevelsForUi = useMemo(
    () => dedupeHoldLevels(holdLevelOptions.length ? holdLevelOptions : [...BILLING_HOLD_LEVEL_OPTIONS]),
    [holdLevelOptions],
  )

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
        campaigns={campaigns}
        assignees={assignees}
        parentCases={parentCases}
        onChange={onChange}
        onClientChange={onClientChange}
      />
      <ExtractsSection values={values} readOnly={readOnly} errors={errors} onChange={onChange} />
      <HoldsSection
        values={values}
        readOnly={readOnly}
        errors={errors}
        holdLevelOptions={holdLevelsForUi}
        segments={segments}
        products={products}
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
        <div
          className={`billing-toast ${toastTone === 'error' ? 'billing-toast--error' : ''}`}
          role="status"
          data-testid="billing-toast"
        >
          {toast}
        </div>
      ) : null}
    </div>
  )
}
