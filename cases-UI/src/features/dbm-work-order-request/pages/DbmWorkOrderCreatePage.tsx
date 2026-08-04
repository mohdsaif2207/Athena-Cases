import { useState, useEffect, type FormEvent, type ReactNode } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import type { EventIdLookupItem } from '../api/lookups'
import { getDbmWorkOrder } from '../api/workOrders'
import { useIsDbmUser } from '../auth/dbmAccess'
import { useCreateDbmWorkOrder, toUserFriendlyError } from '../hooks/useCreateDbmWorkOrder'
import { useDbmLookups } from '../hooks/useDbmLookups'
import {
  toCreateDbmWorkOrderRequest,
  toFormFromDbmResponse,
} from '../mappers/toCreateRequest'
import {
  DEFAULT_DBM_CREATE_FORM,
  type DbmCreateFormState,
} from '../types/form'
import {
  hasDbmCreateFieldErrors,
  isSpecialInstructionsRequired,
  showsCustomApprovalHint,
  validateDbmCreateForm,
  type DbmCreateFieldErrors,
  type DbmCreateValidatedField,
} from '../validation/createValidation'
import './DbmWorkOrderCreatePage.css'

/** Values enumerated in the DBM User Story / LLD (not served by lookup APIs). */
const VENDOR_OPTIONS = ['Acxiom', 'Other'] as const
const PRIORITY_OPTIONS = ['High', 'Medium', 'Low'] as const
const STATUS_OPTIONS = [
  'Requested',
  'In Progress',
  'On Hold',
  'Assigned',
  'Completed',
  'Canceled',
  'File Sent to Partner',
  'File Received from Partner',
] as const
const TRANSFER_TYPE_OPTIONS = [
  'Account Update File',
  'Cancel File',
  'Custom (Requires Approval)',
  'Non/None File to Partner',
  'Other (Requires Description)',
  'Termination File',
] as const
const YES_NO_OPTIONS = ['Yes', 'No'] as const
const FREQUENCY_OPTIONS = [
  'Once',
  'Daily',
  'Weekly',
  'Monthly',
  'Quarterly',
  'Yearly',
] as const

/**
 * Static option lists for fields not covered by GET /api/lookups/*.
 * Labels are presentation values only — not additional form fields.
 */
const COVERAGE_LEVEL_OPTIONS = [
  'Complementary',
  'Voluntary',
  'Cancels',
] as const
const ACCOUNT_TYPE_OPTIONS = [
  'Share/ESHAR',
  'Share Draft/ES/DR',
  'Checking/ECHK',
  'Savings/ESAV',
] as const

function toggleMultiValue(current: string[], value: string): string[] {
  return current.includes(value)
    ? current.filter((item) => item !== value)
    : [...current, value]
}

/**
 * DBM Work Order Request Create screen (Athena styling).
 * Create: POST /api/dbm/work-orders → redirect to Cases grid with success toast.
 * Optional load via ?caseId=&mode=view remains for deep-links; primary UX is create → grid.
 */
export function DbmWorkOrderCreatePage() {
  const navigate = useNavigate()
  const [searchParams] = useSearchParams()
  const { user } = useAuth()
  const loggedInOwner =
    user?.displayName?.trim() || user?.username?.trim() || 'Logged-in user'

  const caseIdRaw = searchParams.get('caseId')
  const caseId =
    caseIdRaw && /^\d+$/.test(caseIdRaw) ? Number(caseIdRaw) : null
  const modeParam = searchParams.get('mode')
  const isViewMode = caseId != null && (modeParam === 'view' || modeParam == null)
  const readOnly = isViewMode

  const [form, setForm] = useState<DbmCreateFormState>(() => ({
    ...DEFAULT_DBM_CREATE_FORM,
    caseOwner: loggedInOwner,
  }))
  const [fieldErrors, setFieldErrors] = useState<DbmCreateFieldErrors>({})
  const [saveError, setSaveError] = useState<string | null>(null)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const [cancelConfirmOpen, setCancelConfirmOpen] = useState(false)
  const [loadError, setLoadError] = useState<string | null>(null)
  const [loadingCase, setLoadingCase] = useState(isViewMode)

  useEffect(() => {
    if (isViewMode) {
      return
    }
    setForm((prev) =>
      prev.caseOwner === loggedInOwner ? prev : { ...prev, caseOwner: loggedInOwner },
    )
  }, [loggedInOwner, isViewMode])

  useEffect(() => {
    if (!isViewMode || caseId == null) {
      setLoadingCase(false)
      return
    }
    let cancelled = false
    setLoadingCase(true)
    setLoadError(null)
    getDbmWorkOrder(caseId)
      .then((response) => {
        if (cancelled) return
        setForm(toFormFromDbmResponse(response))
        setSuccessMessage(
          `Case created successfully with Case ID ${response.caseNumber}.`,
        )
      })
      .catch((error: unknown) => {
        if (cancelled) return
        setLoadError(toUserFriendlyError(error))
      })
      .finally(() => {
        if (!cancelled) setLoadingCase(false)
      })
    return () => {
      cancelled = true
    }
  }, [isViewMode, caseId])

  const {
    data: lookups,
    isLoading: lookupsLoading,
    isError: lookupsFailed,
    error: lookupsError,
    refetch: refetchLookups,
  } = useDbmLookups()

  const createMutation = useCreateDbmWorkOrder()
  const isSaving = createMutation.isPending
  const isDbmUser = useIsDbmUser()

  const clients = lookups?.clients ?? []
  const eventIds = lookups?.eventIds ?? []
  const spokenKeys = lookups?.spokenKeys ?? []
  const lookupsReady = !lookupsLoading && !lookupsFailed
  const specialInstructionsRequired = isSpecialInstructionsRequired(
    form.transferType,
  )
  const customApprovalHint = showsCustomApprovalHint(form.transferType)

  const clearFieldError = (key: DbmCreateValidatedField) => {
    setFieldErrors((prev) => {
      if (!prev[key]) {
        return prev
      }
      const next = { ...prev }
      delete next[key]
      return next
    })
  }

  const setField = <K extends keyof DbmCreateFormState>(
    key: K,
    value: DbmCreateFormState[K],
  ) => {
    setForm((prev) => ({ ...prev, [key]: value }))
    setSuccessMessage(null)
    setSaveError(null)
    if (
      key === 'vendor' ||
      key === 'requestedDueDate' ||
      key === 'priority' ||
      key === 'subject' ||
      key === 'status' ||
      key === 'transferType' ||
      key === 'returnFileExpected' ||
      key === 'clientId' ||
      key === 'specialInstructions' ||
      key === 'expectedQuantity' ||
      key === 'mediaOutQuantity' ||
      key === 'totalRecordsUpdated'
    ) {
      clearFieldError(key)
    }
  }

  const handleTransferTypeChange = (value: string) => {
    setForm((prev) => ({ ...prev, transferType: value }))
    setSuccessMessage(null)
    setSaveError(null)
    clearFieldError('transferType')
    // Conditional Special Instructions rule no longer applies.
    if (!isSpecialInstructionsRequired(value)) {
      clearFieldError('specialInstructions')
    }
  }

  const handleEventIdChange = (code: string) => {
    const selected: EventIdLookupItem | undefined = eventIds.find(
      (item) => item.code === code,
    )
    setForm((prev) => ({
      ...prev,
      eventId: code,
      mailMonth: selected?.mailMonth ?? '',
    }))
    setSuccessMessage(null)
    setSaveError(null)
  }

  const handleReset = () => {
    if (readOnly) {
      return
    }
    setForm({ ...DEFAULT_DBM_CREATE_FORM, caseOwner: loggedInOwner })
    setFieldErrors({})
    setSaveError(null)
    setSuccessMessage(null)
    setCancelConfirmOpen(false)
  }

  const handleCancelClick = () => {
    if (isSaving) {
      return
    }
    if (readOnly) {
      navigate('/cases')
      return
    }
    setCancelConfirmOpen(true)
  }

  const handleCancelConfirmNo = () => {
    setCancelConfirmOpen(false)
  }

  const handleCancelConfirmYes = () => {
    setCancelConfirmOpen(false)
    navigate('/cases')
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
    if (isSaving || readOnly) {
      return
    }

    setSuccessMessage(null)
    setSaveError(null)

    const errors = validateDbmCreateForm(form)
    setFieldErrors(errors)
    if (hasDbmCreateFieldErrors(errors)) {
      return
    }

    const payload = toCreateDbmWorkOrderRequest(form, isDbmUser)
    createMutation.mutate(payload, {
      onSuccess: (created) => {
        setSaveError(null)
        // Return to Cases grid — toast handled by CasesSearchPage via location state.
        navigate('/cases', {
          replace: true,
          state: {
            dbmCreateSuccess: true,
            caseNumber: created.caseNumber,
            caseId: created.caseId,
          },
        })
      },
      onError: (error) => {
        setSaveError(toUserFriendlyError(error))
      },
    })
  }

  const lookupErrorMessage =
    lookupsError instanceof Error
      ? lookupsError.message
      : 'Failed to load lookup data.'

  if (loadingCase) {
    return (
      <div className="dbm-page" data-testid="dbm-work-order-create-page">
        <div className="dbm-page-title">DBM Work Order Request</div>
        <div className="dbm-lookup-status dbm-lookup-loading" role="status">
          Loading case…
        </div>
      </div>
    )
  }

  if (loadError) {
    return (
      <div className="dbm-page" data-testid="dbm-work-order-create-page">
        <div className="dbm-page-title">DBM Work Order Request</div>
        <div className="dbm-lookup-status dbm-lookup-error" role="alert">
          <span>{loadError}</span>
          <button
            type="button"
            className="dbm-btn dbm-btn-secondary"
            onClick={() => navigate('/cases')}
          >
            Back to Cases
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="dbm-page" data-testid="dbm-work-order-create-page">
      <div className="dbm-page-title">
        {readOnly ? 'DBM Work Order Request — Case Details' : 'DBM Work Order Request'}
      </div>

      <form className="dbm-form" onSubmit={handleSubmit} noValidate>
        <fieldset className="dbm-fieldset" disabled={readOnly}>
        {lookupsLoading && (
          <div
            className="dbm-lookup-status dbm-lookup-loading"
            role="status"
            data-testid="dbm-lookups-loading"
          >
            Loading lookup data…
          </div>
        )}

        {lookupsFailed && (
          <div
            className="dbm-lookup-status dbm-lookup-error"
            role="alert"
            data-testid="dbm-lookups-error"
          >
            <span>{lookupErrorMessage}</span>
            <button
              type="button"
              className="dbm-btn dbm-btn-secondary"
              data-testid="dbm-lookups-retry"
              onClick={() => void refetchLookups()}
            >
              Retry
            </button>
          </div>
        )}

        {successMessage && (
          <div
            className="dbm-lookup-status dbm-save-success"
            role="status"
            data-testid="dbm-save-success"
          >
            {successMessage}
          </div>
        )}

        {saveError && (
          <div
            className="dbm-lookup-status dbm-lookup-error"
            role="alert"
            data-testid="dbm-save-error"
          >
            {saveError}
          </div>
        )}

        {/* Section 1 — Request Information */}
        <section
          className="dbm-section"
          data-testid="dbm-section-request-information"
        >
          <h2 className="dbm-section-header">Request Information</h2>
          <div className="dbm-section-body">
            <div className="dbm-grid">
              <Field
                label="Vendor"
                required
                error={fieldErrors.vendor}
                invalid={!!fieldErrors.vendor}
              >
                <select
                  data-testid="dbm-vendor"
                  value={form.vendor}
                  aria-invalid={!!fieldErrors.vendor}
                  onChange={(e) => setField('vendor', e.target.value)}
                >
                  <option value="">Select Vendor</option>
                  {VENDOR_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <div className="dbm-field">
                <span className="dbm-label">
                  Case Owner<span className="dbm-required"> *</span>
                </span>
                <div className="dbm-readonly-label" data-testid="dbm-case-owner">
                  {form.caseOwner}
                </div>
              </div>

              <Field
                label="Requested Due Date"
                required
                error={fieldErrors.requestedDueDate}
                invalid={!!fieldErrors.requestedDueDate}
              >
                <input
                  data-testid="dbm-requested-due-date"
                  type="date"
                  value={form.requestedDueDate}
                  aria-invalid={!!fieldErrors.requestedDueDate}
                  onChange={(e) => setField('requestedDueDate', e.target.value)}
                />
              </Field>

              <Field
                label="Priority"
                required
                error={fieldErrors.priority}
                invalid={!!fieldErrors.priority}
              >
                <select
                  data-testid="dbm-priority"
                  value={form.priority}
                  aria-invalid={!!fieldErrors.priority}
                  onChange={(e) => setField('priority', e.target.value)}
                >
                  {PRIORITY_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                label="Subject"
                required
                className="dbm-span-2"
                error={fieldErrors.subject}
                invalid={!!fieldErrors.subject}
              >
                <input
                  data-testid="dbm-subject"
                  type="text"
                  maxLength={200}
                  value={form.subject}
                  aria-invalid={!!fieldErrors.subject}
                  onChange={(e) => setField('subject', e.target.value)}
                />
              </Field>

              <Field
                label="Status"
                required
                error={fieldErrors.status}
                invalid={!!fieldErrors.status}
              >
                <select
                  data-testid="dbm-status"
                  value={form.status}
                  aria-invalid={!!fieldErrors.status}
                  onChange={(e) => setField('status', e.target.value)}
                >
                  {STATUS_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Description" className="dbm-span-3">
                <textarea
                  data-testid="dbm-description"
                  rows={4}
                  maxLength={1000}
                  value={form.description}
                  onChange={(e) => setField('description', e.target.value)}
                />
              </Field>

              <div className="dbm-checkbox-row dbm-span-3">
                <label className="dbm-checkbox">
                  <input
                    data-testid="dbm-core-processor-conversion"
                    type="checkbox"
                    checked={form.coreProcessorConversion}
                    onChange={(e) =>
                      setField('coreProcessorConversion', e.target.checked)
                    }
                  />
                  <span>Core Processor Conversion?</span>
                </label>
              </div>
            </div>
          </div>
        </section>

        {/* Section 2 — File Request */}
        <section className="dbm-section" data-testid="dbm-section-file-request">
          <h2 className="dbm-section-header">File Request</h2>
          <div className="dbm-section-body">
            <div className="dbm-grid">
              <Field
                label="Transfer Type"
                required
                error={fieldErrors.transferType}
                invalid={!!fieldErrors.transferType}
                hint={
                  customApprovalHint
                    ? 'This request will require DBM Manager approval.'
                    : undefined
                }
                hintTestId="dbm-custom-approval-hint"
              >
                <select
                  data-testid="dbm-transfer-type"
                  value={form.transferType}
                  aria-invalid={!!fieldErrors.transferType}
                  onChange={(e) => handleTransferTypeChange(e.target.value)}
                >
                  <option value="">Select Transfer Type</option>
                  {TRANSFER_TYPE_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Coverage Level">
                <MultiSelectChips
                  testId="dbm-coverage-levels"
                  options={COVERAGE_LEVEL_OPTIONS.map((v) => ({
                    value: v,
                    label: v,
                  }))}
                  selected={form.coverageLevels}
                  onToggle={(value) =>
                    setField(
                      'coverageLevels',
                      toggleMultiValue(form.coverageLevels, value),
                    )
                  }
                />
              </Field>

              <Field
                label="Return File Expected"
                required
                error={fieldErrors.returnFileExpected}
                invalid={!!fieldErrors.returnFileExpected}
              >
                <select
                  data-testid="dbm-return-file-expected"
                  value={form.returnFileExpected}
                  aria-invalid={!!fieldErrors.returnFileExpected}
                  onChange={(e) => setField('returnFileExpected', e.target.value)}
                >
                  <option value="">Select</option>
                  {YES_NO_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="PGP Key at Acxiom">
                <select
                  data-testid="dbm-pgp-key-at-acxiom"
                  value={form.pgpKeyAtAcxiom}
                  onChange={(e) => setField('pgpKeyAtAcxiom', e.target.value)}
                >
                  <option value="">Select</option>
                  {YES_NO_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Requested Account Types">
                <MultiSelectChips
                  testId="dbm-requested-account-types"
                  options={ACCOUNT_TYPE_OPTIONS.map((v) => ({
                    value: v,
                    label: v,
                  }))}
                  selected={form.requestedAccountTypes}
                  onToggle={(value) =>
                    setField(
                      'requestedAccountTypes',
                      toggleMultiValue(form.requestedAccountTypes, value),
                    )
                  }
                />
              </Field>

              <Field
                label="Expected Quantity"
                error={fieldErrors.expectedQuantity}
                invalid={!!fieldErrors.expectedQuantity}
              >
                <input
                  data-testid="dbm-expected-quantity"
                  type="text"
                  inputMode="numeric"
                  value={form.expectedQuantity}
                  aria-invalid={!!fieldErrors.expectedQuantity}
                  onChange={(e) => setField('expectedQuantity', e.target.value)}
                />
              </Field>

              <Field label="Frequency">
                <select
                  data-testid="dbm-frequency"
                  value={form.frequency}
                  onChange={(e) => setField('frequency', e.target.value)}
                >
                  {FREQUENCY_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field
                label="Special Instructions"
                className="dbm-span-2"
                required={specialInstructionsRequired}
                error={fieldErrors.specialInstructions}
                invalid={!!fieldErrors.specialInstructions}
              >
                <input
                  data-testid="dbm-special-instructions"
                  type="text"
                  value={form.specialInstructions}
                  aria-invalid={!!fieldErrors.specialInstructions}
                  onChange={(e) =>
                    setField('specialInstructions', e.target.value)
                  }
                />
              </Field>
            </div>
          </div>
        </section>

        {/* Section 3 — Marketing Research Request */}
        <section
          className="dbm-section"
          data-testid="dbm-section-marketing-research"
        >
          <h2 className="dbm-section-header">Marketing Research Request</h2>
          <div className="dbm-section-body">
            <div className="dbm-grid">
              <Field
                label="Client"
                required
                error={fieldErrors.clientId}
                invalid={!!fieldErrors.clientId}
              >
                <select
                  data-testid="dbm-client"
                  value={form.clientId}
                  aria-invalid={!!fieldErrors.clientId}
                  onChange={(e) => setField('clientId', e.target.value)}
                  disabled={!lookupsReady}
                >
                  <option value="">Select Client</option>
                  {clients.map((opt) => (
                    <option key={opt.code} value={opt.code}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Spoken Keys">
                <MultiSelectChips
                  testId="dbm-spoken-keys"
                  options={spokenKeys.map((o) => ({
                    value: o.code,
                    label: o.label,
                  }))}
                  selected={form.spokenKeys}
                  onToggle={(value) =>
                    setField(
                      'spokenKeys',
                      toggleMultiValue(form.spokenKeys, value),
                    )
                  }
                  disabled={!lookupsReady}
                />
              </Field>

              <Field label="Event ID">
                <select
                  data-testid="dbm-event-id"
                  value={form.eventId}
                  onChange={(e) => handleEventIdChange(e.target.value)}
                  disabled={!lookupsReady}
                >
                  <option value="">Select Event ID</option>
                  {eventIds.map((opt) => (
                    <option key={opt.code} value={opt.code}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Media IDs">
                <input
                  data-testid="dbm-media-ids"
                  type="text"
                  value={form.mediaIds}
                  onChange={(e) => setField('mediaIds', e.target.value)}
                />
              </Field>

              <Field label="Mail Month">
                <input
                  data-testid="dbm-mail-month"
                  type="text"
                  value={form.mailMonth}
                  readOnly
                  disabled
                />
              </Field>

              <Field
                label="Media Out Quantity"
                error={fieldErrors.mediaOutQuantity}
                invalid={!!fieldErrors.mediaOutQuantity}
              >
                <input
                  data-testid="dbm-media-out-quantity"
                  type="text"
                  inputMode="numeric"
                  value={form.mediaOutQuantity}
                  aria-invalid={!!fieldErrors.mediaOutQuantity}
                  onChange={(e) => setField('mediaOutQuantity', e.target.value)}
                />
              </Field>

              <div className="dbm-checkbox-row dbm-span-3">
                <label className="dbm-checkbox">
                  <input
                    data-testid="dbm-changes-to-matchback-db"
                    type="checkbox"
                    checked={form.changesToMatchbackDb}
                    onChange={(e) =>
                      setField('changesToMatchbackDb', e.target.checked)
                    }
                  />
                  <span>Changes to Matchback DB</span>
                </label>
              </div>

              <Field label="Selection Criteria">
                <input
                  data-testid="dbm-selection-criteria"
                  type="text"
                  value={form.selectionCriteria}
                  onChange={(e) => setField('selectionCriteria', e.target.value)}
                />
              </Field>

              <Field label="Field">
                <input
                  data-testid="dbm-field"
                  type="text"
                  maxLength={200}
                  value={form.field}
                  onChange={(e) => setField('field', e.target.value)}
                />
              </Field>

              <Field label="Change To">
                <input
                  data-testid="dbm-change-to"
                  type="text"
                  maxLength={200}
                  value={form.changeTo}
                  onChange={(e) => setField('changeTo', e.target.value)}
                />
              </Field>
            </div>
          </div>
        </section>

        {/* Section 4 — For DBM Use Only (role-gated via isDbmUser) */}
        {isDbmUser ? (
          <section className="dbm-section" data-testid="dbm-section-dbm-use-only">
            <h2 className="dbm-section-header">For DBM Use Only</h2>
            <div className="dbm-section-body">
              <div className="dbm-grid">
                <Field label="DBM Work Order Number">
                  <input
                    data-testid="dbm-work-order-number"
                    type="text"
                    maxLength={64}
                    value={form.dbmWorkOrderNumber}
                    onChange={(e) =>
                      setField('dbmWorkOrderNumber', e.target.value)
                    }
                  />
                </Field>

                <Field
                  label="Total Records Updated"
                  error={fieldErrors.totalRecordsUpdated}
                  invalid={!!fieldErrors.totalRecordsUpdated}
                >
                  <input
                    data-testid="dbm-total-records-updated"
                    type="text"
                    inputMode="numeric"
                    value={form.totalRecordsUpdated}
                    aria-invalid={!!fieldErrors.totalRecordsUpdated}
                    onChange={(e) =>
                      setField('totalRecordsUpdated', e.target.value)
                    }
                  />
                </Field>

                <div className="dbm-field-spacer" aria-hidden />

                <Field label="DBM Completion Notes" className="dbm-span-3">
                  <textarea
                    data-testid="dbm-completion-notes"
                    rows={4}
                    value={form.dbmCompletionNotes}
                    onChange={(e) =>
                      setField('dbmCompletionNotes', e.target.value)
                    }
                  />
                </Field>
              </div>
            </div>
          </section>
        ) : null}

        </fieldset>

        <footer className="dbm-actions">
          <button
            type="button"
            className="dbm-btn dbm-btn-secondary"
            data-testid="dbm-cancel"
            disabled={isSaving}
            onClick={handleCancelClick}
          >
            {readOnly ? 'Back to Cases' : 'Cancel'}
          </button>
          {!readOnly ? (
            <>
              <button
                type="button"
                className="dbm-btn dbm-btn-secondary"
                data-testid="dbm-reset"
                onClick={handleReset}
                disabled={isSaving}
              >
                Reset
              </button>
              <button
                type="submit"
                className="dbm-btn dbm-btn-primary"
                data-testid="dbm-save"
                disabled={isSaving}
                aria-busy={isSaving}
              >
                {isSaving ? 'Saving…' : 'Save'}
              </button>
            </>
          ) : null}
        </footer>
      </form>

      {cancelConfirmOpen && (
        <div
          className="dbm-confirm-overlay"
          role="presentation"
          data-testid="dbm-cancel-confirm-overlay"
        >
          <div
            className="dbm-confirm-dialog"
            role="dialog"
            aria-modal="true"
            aria-labelledby="dbm-cancel-confirm-title"
            data-testid="dbm-cancel-confirm-dialog"
          >
            <p id="dbm-cancel-confirm-title" className="dbm-confirm-message">
              Unsaved changes will be lost. Do you want to continue?
            </p>
            <div className="dbm-confirm-actions">
              <button
                type="button"
                className="dbm-btn dbm-btn-secondary"
                data-testid="dbm-cancel-confirm-no"
                onClick={handleCancelConfirmNo}
              >
                No
              </button>
              <button
                type="button"
                className="dbm-btn dbm-btn-primary"
                data-testid="dbm-cancel-confirm-yes"
                onClick={handleCancelConfirmYes}
              >
                Yes
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

function Field({
  label,
  required,
  className,
  error,
  invalid,
  hint,
  hintTestId,
  children,
}: {
  label: string
  required?: boolean
  className?: string
  error?: string
  invalid?: boolean
  hint?: string
  hintTestId?: string
  children: ReactNode
}) {
  return (
    <label
      className={`dbm-field ${invalid ? 'dbm-field-invalid' : ''} ${className ?? ''}`.trim()}
    >
      <span className="dbm-label">
        {label}
        {required ? <span className="dbm-required"> *</span> : null}
      </span>
      {children}
      {hint ? (
        <span className="dbm-field-hint" data-testid={hintTestId}>
          {hint}
        </span>
      ) : null}
      {error ? (
        <span className="dbm-field-error" role="alert">
          {error}
        </span>
      ) : null}
    </label>
  )
}

function MultiSelectChips({
  testId,
  options,
  selected,
  onToggle,
  disabled = false,
}: {
  testId: string
  options: ReadonlyArray<{ value: string; label: string }>
  selected: string[]
  onToggle: (value: string) => void
  disabled?: boolean
}) {
  return (
    <div
      className="dbm-chip-multi"
      data-testid={testId}
      role="group"
      aria-label={testId}
      aria-disabled={disabled || undefined}
    >
      {options.map((opt) => (
        <label key={opt.value} className="dbm-chip">
          <input
            type="checkbox"
            checked={selected.includes(opt.value)}
            onChange={() => onToggle(opt.value)}
            disabled={disabled}
          />
          <span>{opt.label}</span>
        </label>
      ))}
    </div>
  )
}

export default DbmWorkOrderCreatePage
