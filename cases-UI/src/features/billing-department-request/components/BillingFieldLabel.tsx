interface BillingFieldLabelProps {
  required?: boolean
  children: string
}

/** Label text with Athena Create Case single asterisk for required fields. */
export function BillingFieldLabel({ required, children }: BillingFieldLabelProps) {
  return (
    <span className="billing-field__label">
      {children}
      {required ? <span className="billing-field__required" aria-hidden="true"> *</span> : null}
    </span>
  )
}
