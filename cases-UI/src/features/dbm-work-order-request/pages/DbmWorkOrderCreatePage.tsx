import { useState, type FormEvent, type ReactNode } from 'react'
import './DbmWorkOrderCreatePage.css'

/** Values enumerated in the DBM User Story / LLD. */
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
/** LLD specifies default "Once"; no other values are listed in the story. */
const FREQUENCY_OPTIONS = ['Once'] as const

/**
 * Mock option lists for multi-selects / mock lookups (story allows mock data).
 * Labels are presentation values only — not additional form fields.
 */
const COVERAGE_LEVEL_OPTIONS = ['Platinum', 'Gold', 'Silver', 'Bronze'] as const
const ACCOUNT_TYPE_OPTIONS = [
  'Checking',
  'Savings',
  'Credit Card',
  'Loan',
  'Mortgage',
] as const
const CLIENT_OPTIONS = [
  { code: 'CLIENT001', label: 'ABC Bank' },
  { code: 'CLIENT002', label: 'XYZ Finance' },
  { code: 'CLIENT003', label: 'First National Credit Union' },
] as const
const SPOKEN_KEY_OPTIONS = [
  { code: 'SPK001', label: 'English' },
  { code: 'SPK002', label: 'Spanish' },
  { code: 'SPK003', label: 'French' },
] as const
const EVENT_ID_OPTIONS = [
  { code: 'EVT1001', label: 'Summer Campaign' },
  { code: 'EVT1002', label: 'Winter Campaign' },
  { code: 'EVT1003', label: 'Fall Acquisition' },
] as const

type FormState = {
  vendor: string
  caseOwner: string
  requestedDueDate: string
  priority: string
  subject: string
  status: string
  description: string
  coreProcessorConversion: boolean
  transferType: string
  coverageLevels: string[]
  returnFileExpected: string
  pgpKeyAtAcxiom: string
  requestedAccountTypes: string[]
  expectedQuantity: string
  frequency: string
  specialInstructions: string
  clientId: string
  spokenKeys: string[]
  eventId: string
  mediaIds: string
  mailMonth: string
  mediaOutQuantity: string
  changesToMatchbackDb: boolean
  selectionCriteria: string
  field: string
  changeTo: string
  dbmWorkOrderNumber: string
  dbmCompletionNotes: string
  totalRecordsUpdated: string
}

const DEFAULT_FORM: FormState = {
  vendor: '',
  caseOwner: 'Logged-in user',
  requestedDueDate: '',
  priority: 'Medium',
  subject: '',
  status: 'Requested',
  description: '',
  coreProcessorConversion: false,
  transferType: '',
  coverageLevels: [],
  returnFileExpected: '',
  pgpKeyAtAcxiom: '',
  requestedAccountTypes: [],
  expectedQuantity: '',
  frequency: 'Once',
  specialInstructions: '',
  clientId: '',
  spokenKeys: [],
  eventId: '',
  mediaIds: '',
  mailMonth: '',
  mediaOutQuantity: '',
  changesToMatchbackDb: false,
  selectionCriteria: '',
  field: '',
  changeTo: '',
  dbmWorkOrderNumber: '',
  dbmCompletionNotes: '',
  totalRecordsUpdated: '',
}

function toggleMultiValue(current: string[], value: string): string[] {
  return current.includes(value)
    ? current.filter((item) => item !== value)
    : [...current, value]
}

/**
 * Visual-only DBM Work Order Request Create screen (Athena styling).
 * Field set / labels / defaults follow the DBM User Story & LLD only.
 */
export function DbmWorkOrderCreatePage() {
  const [form, setForm] = useState<FormState>(DEFAULT_FORM)

  const setField = <K extends keyof FormState>(key: K, value: FormState[K]) => {
    setForm((prev) => ({ ...prev, [key]: value }))
  }

  const handleReset = () => {
    setForm(DEFAULT_FORM)
  }

  const handleSubmit = (event: FormEvent) => {
    event.preventDefault()
  }

  return (
    <div className="dbm-page" data-testid="dbm-work-order-create-page">
      <div className="dbm-page-title">DBM Work Order Request</div>

      <form className="dbm-form" onSubmit={handleSubmit} noValidate>
        {/* Section 1 — Request Information */}
        <section
          className="dbm-section"
          data-testid="dbm-section-request-information"
        >
          <h2 className="dbm-section-header">Request Information</h2>
          <div className="dbm-section-body">
            <div className="dbm-grid">
              <Field label="Vendor" required>
                <select
                  data-testid="dbm-vendor"
                  value={form.vendor}
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

              <Field label="Requested Due Date" required>
                <input
                  data-testid="dbm-requested-due-date"
                  type="date"
                  value={form.requestedDueDate}
                  onChange={(e) => setField('requestedDueDate', e.target.value)}
                />
              </Field>

              <Field label="Priority" required>
                <select
                  data-testid="dbm-priority"
                  value={form.priority}
                  onChange={(e) => setField('priority', e.target.value)}
                >
                  {PRIORITY_OPTIONS.map((opt) => (
                    <option key={opt} value={opt}>
                      {opt}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Subject" required className="dbm-span-2">
                <input
                  data-testid="dbm-subject"
                  type="text"
                  maxLength={200}
                  value={form.subject}
                  onChange={(e) => setField('subject', e.target.value)}
                />
              </Field>

              <Field label="Status" required>
                <select
                  data-testid="dbm-status"
                  value={form.status}
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
              <Field label="Transfer Type" required>
                <select
                  data-testid="dbm-transfer-type"
                  value={form.transferType}
                  onChange={(e) => setField('transferType', e.target.value)}
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

              <Field label="Return File Expected" required>
                <select
                  data-testid="dbm-return-file-expected"
                  value={form.returnFileExpected}
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

              <Field label="Expected Quantity">
                <input
                  data-testid="dbm-expected-quantity"
                  type="number"
                  min={0}
                  value={form.expectedQuantity}
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

              <Field label="Special Instructions" className="dbm-span-2">
                <input
                  data-testid="dbm-special-instructions"
                  type="text"
                  value={form.specialInstructions}
                  onChange={(e) => setField('specialInstructions', e.target.value)}
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
              <Field label="Client" required>
                <select
                  data-testid="dbm-client"
                  value={form.clientId}
                  onChange={(e) => setField('clientId', e.target.value)}
                >
                  <option value="">Select Client</option>
                  {CLIENT_OPTIONS.map((opt) => (
                    <option key={opt.code} value={opt.code}>
                      {opt.label}
                    </option>
                  ))}
                </select>
              </Field>

              <Field label="Spoken Keys">
                <MultiSelectChips
                  testId="dbm-spoken-keys"
                  options={SPOKEN_KEY_OPTIONS.map((o) => ({
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
                />
              </Field>

              <Field label="Event ID">
                <select
                  data-testid="dbm-event-id"
                  value={form.eventId}
                  onChange={(e) => setField('eventId', e.target.value)}
                >
                  <option value="">Select Event ID</option>
                  {EVENT_ID_OPTIONS.map((opt) => (
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

              <Field label="Media Out Quantity">
                <input
                  data-testid="dbm-media-out-quantity"
                  type="number"
                  min={0}
                  value={form.mediaOutQuantity}
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

        {/* Section 4 — For DBM Use Only */}
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
                  onChange={(e) => setField('dbmWorkOrderNumber', e.target.value)}
                />
              </Field>

              <Field label="Total Records Updated">
                <input
                  data-testid="dbm-total-records-updated"
                  type="number"
                  min={0}
                  value={form.totalRecordsUpdated}
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
                  onChange={(e) => setField('dbmCompletionNotes', e.target.value)}
                />
              </Field>
            </div>
          </div>
        </section>

        <footer className="dbm-actions">
          <button
            type="button"
            className="dbm-btn dbm-btn-secondary"
            data-testid="dbm-cancel"
          >
            Cancel
          </button>
          <button
            type="button"
            className="dbm-btn dbm-btn-secondary"
            data-testid="dbm-reset"
            onClick={handleReset}
          >
            Reset
          </button>
          <button
            type="submit"
            className="dbm-btn dbm-btn-primary"
            data-testid="dbm-save"
          >
            Save
          </button>
        </footer>
      </form>
    </div>
  )
}

function Field({
  label,
  required,
  className,
  children,
}: {
  label: string
  required?: boolean
  className?: string
  children: ReactNode
}) {
  return (
    <label className={`dbm-field ${className ?? ''}`.trim()}>
      <span className="dbm-label">
        {label}
        {required ? <span className="dbm-required"> *</span> : null}
      </span>
      {children}
    </label>
  )
}

function MultiSelectChips({
  testId,
  options,
  selected,
  onToggle,
}: {
  testId: string
  options: ReadonlyArray<{ value: string; label: string }>
  selected: string[]
  onToggle: (value: string) => void
}) {
  return (
    <div
      className="dbm-chip-multi"
      data-testid={testId}
      role="group"
      aria-label={testId}
    >
      {options.map((opt) => (
        <label key={opt.value} className="dbm-chip">
          <input
            type="checkbox"
            checked={selected.includes(opt.value)}
            onChange={() => onToggle(opt.value)}
          />
          <span>{opt.label}</span>
        </label>
      ))}
    </div>
  )
}

export default DbmWorkOrderCreatePage
