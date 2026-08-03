/** Small orange-red square View (eye) / Edit (pencil) actions for Home queues. */

export function QueueViewButton({
  onClick,
  testId,
  label = 'View',
}: {
  onClick: () => void
  testId: string
  label?: string
}) {
  return (
    <button
      type="button"
      className="queue-icon-btn"
      onClick={onClick}
      data-testid={testId}
      title={label}
      aria-label={label}
    >
      <EyeIcon />
    </button>
  )
}

export function QueueEditButton({
  onClick,
  testId,
  label = 'Edit',
}: {
  onClick: () => void
  testId: string
  label?: string
}) {
  return (
    <button
      type="button"
      className="queue-icon-btn"
      onClick={onClick}
      data-testid={testId}
      title={label}
      aria-label={label}
    >
      <PencilIcon />
    </button>
  )
}

function EyeIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path
        fill="currentColor"
        d="M12 5c-5.5 0-9.7 4.1-11 7 1.3 2.9 5.5 7 11 7s9.7-4.1 11-7c-1.3-2.9-5.5-7-11-7zm0 11.5A4.5 4.5 0 1 1 12 7.5a4.5 4.5 0 0 1 0 9zm0-7a2.5 2.5 0 1 0 0 5 2.5 2.5 0 0 0 0-5z"
      />
    </svg>
  )
}

function PencilIcon() {
  return (
    <svg width="15" height="15" viewBox="0 0 24 24" aria-hidden="true" focusable="false">
      <path
        fill="currentColor"
        d="M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04a1 1 0 0 0 0-1.41l-2.34-2.34a1 1 0 0 0-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z"
      />
    </svg>
  )
}
