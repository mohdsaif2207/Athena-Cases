interface BillingFieldErrorProps {
  message?: string
  testId: string
}

/** Bold red inline validation — Billing form contract. */
export function BillingFieldError({ message, testId }: BillingFieldErrorProps) {
  if (!message) return null
  return (
    <p className="billing-field__error" role="alert" data-testid={testId}>
      {message}
    </p>
  )
}
