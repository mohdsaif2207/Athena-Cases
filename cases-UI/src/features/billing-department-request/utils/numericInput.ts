/**
 * Live numeric filters for User Story numeric-only fields.
 * Rejects alphabetic/special characters while typing (not only on submit).
 */

/** Digits only (Approximate Number of Coverages). Preserves valid pasted digits. */
export function filterIntegerInput(raw: string): string {
  return raw.replace(/[^\d]/g, '').slice(0, 18)
}

/**
 * Digits + optional single decimal point, max 2 fraction digits
 * (Approximate Revenue Impact — mirrors @Digits(integer=16, fraction=2)).
 */
export function filterDecimalInput(raw: string): string {
  const cleaned = raw.replace(/[^\d.]/g, '')
  const firstDot = cleaned.indexOf('.')
  if (firstDot === -1) {
    return cleaned.slice(0, 16)
  }
  const intPart = cleaned.slice(0, firstDot).replace(/\./g, '').slice(0, 16)
  const fracPart = cleaned
    .slice(firstDot + 1)
    .replace(/\./g, '')
    .slice(0, 2)
  return `${intPart}.${fracPart}`
}
