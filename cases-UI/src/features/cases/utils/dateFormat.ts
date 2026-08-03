/** Application display / filter date format: mm/dd/yyyy */

export const APP_DATE_FORMAT = 'MM/dd/yyyy'
export const APP_DATE_PLACEHOLDER = 'mm/dd/yyyy'

const APP_DATE_PATTERN = /^(0[1-9]|1[0-2])\/(0[1-9]|[12]\d|3[01])\/\d{4}$/

/**
 * Parses an mm/dd/yyyy string into a local Date at midnight.
 * Returns null when the string is empty, malformed, or not a real calendar day.
 */
export function parseAppDate(value: string): Date | null {
  const trimmed = value.trim()
  if (!trimmed) return null
  if (!APP_DATE_PATTERN.test(trimmed)) return null

  const [mm, dd, yyyy] = trimmed.split('/').map(Number)
  const date = new Date(yyyy, mm - 1, dd)
  if (
    date.getFullYear() !== yyyy ||
    date.getMonth() !== mm - 1 ||
    date.getDate() !== dd
  ) {
    return null
  }
  return date
}

/** Formats a Date as mm/dd/yyyy. */
export function formatAppDate(date: Date): string {
  const mm = String(date.getMonth() + 1).padStart(2, '0')
  const dd = String(date.getDate()).padStart(2, '0')
  const yyyy = String(date.getFullYear())
  return `${mm}/${dd}/${yyyy}`
}

export function isValidAppDateString(value: string): boolean {
  return parseAppDate(value) !== null
}

/** True when due is strictly earlier than created (both must parse). */
export function isDueBeforeCreated(createdDate: string, dueDate: string): boolean {
  const created = parseAppDate(createdDate)
  const due = parseAppDate(dueDate)
  if (!created || !due) return false
  return startOfDay(due).getTime() < startOfDay(created).getTime()
}

export function startOfDay(date: Date): Date {
  return new Date(date.getFullYear(), date.getMonth(), date.getDate())
}

export const DATE_VALIDATION_MESSAGES = {
  invalidFormat: 'Enter a valid date as mm/dd/yyyy.',
  emptyRequired: 'Date is required.',
  dueBeforeCreated: 'Requested Due Date cannot be earlier than Created Date.',
} as const
