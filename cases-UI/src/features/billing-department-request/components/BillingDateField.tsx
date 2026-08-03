import {
  forwardRef,
  useEffect,
  useId,
  useState,
  type InputHTMLAttributes,
  type ReactNode,
} from 'react'
import DatePicker from 'react-datepicker'
import {
  APP_DATE_FORMAT,
  APP_DATE_PLACEHOLDER,
  DATE_VALIDATION_MESSAGES,
  formatAppDate,
  parseAppDate,
} from '@/features/cases/utils/dateFormat'
import 'react-datepicker/dist/react-datepicker.css'

export interface BillingDateFieldProps {
  value: string
  onChange: (next: string) => void
  error?: string | null
  disabled?: boolean
  'aria-label'?: string
  'data-testid'?: string
}

/** Digits and `/` only while typing — max mm/dd/yyyy length. */
function filterDateTyping(raw: string): string {
  return raw.replace(/[^\d/]/g, '').slice(0, 10)
}

/**
 * Billing-scoped date field (shared DatePickerField untouched).
 * Manual typing is never blocked; calendar opens only via the icon.
 */
export function BillingDateField({
  value,
  onChange,
  error = null,
  disabled = false,
  'aria-label': ariaLabel,
  'data-testid': testId,
}: BillingDateFieldProps) {
  const [open, setOpen] = useState(false)
  const [text, setText] = useState(value)
  const [localError, setLocalError] = useState<string | null>(null)
  const errorId = useId()

  useEffect(() => {
    setText(value)
    if (!value.trim()) setLocalError(null)
  }, [value])

  const displayError = error || localError
  // Prefer committed form value; fall back to in-progress typed text when already valid.
  const selected = parseAppDate(value) ?? parseAppDate(text)

  function commitTyped(raw: string) {
    const trimmed = raw.trim()
    if (!trimmed) {
      setLocalError(null)
      setText('')
      onChange('')
      return
    }
    const parsed = parseAppDate(trimmed)
    if (!parsed) {
      setLocalError(DATE_VALIDATION_MESSAGES.invalidFormat)
      return
    }
    const formatted = formatAppDate(parsed)
    setLocalError(null)
    setText(formatted)
    onChange(formatted)
  }

  function applyTyped(raw: string) {
    const next = filterDateTyping(raw)
    setText(next)
    setLocalError(null)
    if (!next.trim()) {
      onChange('')
      return
    }
    const parsed = parseAppDate(next)
    if (parsed) onChange(formatAppDate(parsed))
  }

  return (
    <div
      className={['billing-date-field', displayError ? 'billing-date-field--error' : '']
        .filter(Boolean)
        .join(' ')}
    >
      <div className="billing-date-field__control">
        <DatePicker
          selected={selected}
          onChange={(date: Date | null) => {
            setLocalError(null)
            const next = date ? formatAppDate(date) : ''
            setText(next)
            onChange(next)
            setOpen(false)
          }}
          onChangeRaw={(event) => {
            if (!event) return
            event.preventDefault()
            const target = event.target as HTMLInputElement | null
            if (target && typeof target.value === 'string') {
              applyTyped(target.value)
            }
          }}
          onBlur={() => commitTyped(text)}
          dateFormat={APP_DATE_FORMAT}
          placeholderText={APP_DATE_PLACEHOLDER}
          preventOpenOnFocus
          showMonthDropdown
          showYearDropdown
          dropdownMode="select"
          scrollableYearDropdown
          yearDropdownItemNumber={100}
          open={open}
          onClickOutside={() => setOpen(false)}
          onCalendarOpen={() => setOpen(true)}
          onCalendarClose={() => setOpen(false)}
          disabled={disabled}
          shouldCloseOnSelect
          withPortal
          portalId="billing-datepicker-portal"
          className="billing-date-field__input"
          calendarClassName="billing-date-field__calendar"
          customInput={
            <BillingDateTextInput
              aria-label={ariaLabel}
              aria-invalid={Boolean(displayError)}
              aria-describedby={displayError ? errorId : undefined}
              data-testid={testId}
              value={text}
              disabled={disabled}
              onChange={(e) => applyTyped(e.target.value)}
            />
          }
        />
        <button
          type="button"
          className="billing-date-field__icon-btn"
          aria-label={ariaLabel ? `Open calendar for ${ariaLabel}` : 'Open calendar'}
          data-testid={testId ? `${testId}-calendar` : undefined}
          disabled={disabled}
          onMouseDown={(event) => {
            event.preventDefault()
            event.stopPropagation()
          }}
          onClick={(event) => {
            event.preventDefault()
            event.stopPropagation()
            if (!disabled) setOpen((prev) => !prev)
          }}
        >
          <CalendarIcon />
        </button>
      </div>
      {displayError ? (
        <p
          id={errorId}
          className="billing-field__error"
          role="alert"
          data-testid={testId ? `${testId}-error` : undefined}
        >
          {displayError}
        </p>
      ) : null}
    </div>
  )
}

const BillingDateTextInput = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function BillingDateTextInput(props, ref) {
    return <input {...props} ref={ref} type="text" inputMode="numeric" autoComplete="off" />
  },
)

function CalendarIcon(): ReactNode {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M7 2v2H5a2 2 0 0 0-2 2v14a2 2 0 0 0 2 2h14a2 2 0 0 0 2-2V6a2 2 0 0 0-2-2h-2V2h-2v2H9V2H7Zm12 8H5v10h14V10Z"
        fill="currentColor"
      />
    </svg>
  )
}
