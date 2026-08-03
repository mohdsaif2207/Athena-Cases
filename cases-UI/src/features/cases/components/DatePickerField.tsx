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
import './DatePickerField.css'

export interface DatePickerFieldProps {
  value: string
  onChange: (next: string) => void
  /** External validation message (e.g. due-before-created). */
  error?: string | null
  /** Called whenever local format validation changes. */
  onValidationError?: (message: string | null) => void
  minDate?: Date | null
  maxDate?: Date | null
  required?: boolean
  disabled?: boolean
  'aria-label'?: string
  'data-testid'?: string
  className?: string
  compact?: boolean
}

/**
 * Date filter/input using react-datepicker.
 * Display/storage format: mm/dd/yyyy. Opens on input or calendar icon click.
 */
export function DatePickerField({
  value,
  onChange,
  error = null,
  onValidationError,
  minDate = null,
  maxDate = null,
  required = false,
  disabled = false,
  'aria-label': ariaLabel,
  'data-testid': testId,
  className = '',
  compact = false,
}: DatePickerFieldProps) {
  const selected = parseAppDate(value)
  const [open, setOpen] = useState(false)
  const [localError, setLocalError] = useState<string | null>(null)
  const errorId = useId()

  useEffect(() => {
    onValidationError?.(localError)
  }, [localError, onValidationError])

  const displayError = error || localError

  function commitRawInput(raw: string) {
    const trimmed = raw.trim()
    if (!trimmed) {
      if (required) {
        setLocalError(DATE_VALIDATION_MESSAGES.emptyRequired)
        return
      }
      setLocalError(null)
      onChange('')
      return
    }

    const parsed = parseAppDate(trimmed)
    if (!parsed) {
      setLocalError(DATE_VALIDATION_MESSAGES.invalidFormat)
      return
    }

    if (minDate && startOfDay(parsed).getTime() < startOfDay(minDate).getTime()) {
      setLocalError(DATE_VALIDATION_MESSAGES.dueBeforeCreated)
      return
    }

    setLocalError(null)
    onChange(formatAppDate(parsed))
  }

  return (
    <div
      className={[
        'cases-date-field',
        compact ? 'cases-date-field--compact' : '',
        displayError ? 'cases-date-field--error' : '',
        className,
      ]
        .filter(Boolean)
        .join(' ')}
    >
      <div className="cases-date-field__control">
        <DatePicker
          selected={selected}
          onChange={(date: Date | null) => {
            setLocalError(null)
            onChange(date ? formatAppDate(date) : '')
            setOpen(false)
          }}
          onBlur={(event) => {
            const target = event.target as HTMLInputElement
            if (typeof target?.value === 'string') {
              commitRawInput(target.value)
            }
          }}
          onInputError={() => {
            setLocalError(DATE_VALIDATION_MESSAGES.invalidFormat)
          }}
          dateFormat={APP_DATE_FORMAT}
          placeholderText={APP_DATE_PLACEHOLDER}
          strictParsing
          open={open}
          onInputClick={() => {
            if (!disabled) setOpen(true)
          }}
          onClickOutside={() => setOpen(false)}
          onCalendarOpen={() => setOpen(true)}
          onCalendarClose={() => setOpen(false)}
          minDate={minDate ?? undefined}
          maxDate={maxDate ?? undefined}
          disabled={disabled}
          shouldCloseOnSelect
          withPortal
          portalId="cases-datepicker-portal"
          className="cases-date-field__input"
          calendarClassName="cases-date-field__calendar"
          customInput={
            <DateTextInput
              aria-label={ariaLabel}
              aria-invalid={Boolean(displayError)}
              aria-describedby={displayError ? errorId : undefined}
              data-testid={testId}
            />
          }
        />
        <button
          type="button"
          className="cases-date-field__icon-btn"
          aria-label={ariaLabel ? `Open calendar for ${ariaLabel}` : 'Open calendar'}
          data-testid={testId ? `${testId}-calendar` : undefined}
          disabled={disabled}
          onMouseDown={(event) => {
            // Keep focus on the input so blur/outside-click do not immediately close.
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
          className="cases-date-field__error"
          role="alert"
          data-testid={testId ? `${testId}-error` : undefined}
        >
          {displayError}
        </p>
      ) : null}
    </div>
  )
}

const DateTextInput = forwardRef<HTMLInputElement, InputHTMLAttributes<HTMLInputElement>>(
  function DateTextInput(props, ref) {
    return <input {...props} ref={ref} type="text" autoComplete="off" />
  },
)

function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

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
