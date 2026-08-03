/** Franklin Madison logo mark — red circle with stylized M (matches brand reference). */
export function FranklinMadisonLogo({ className }: { className?: string }) {
  return (
    <svg
      className={className}
      viewBox="0 0 64 64"
      width="44"
      height="44"
      aria-hidden="true"
      focusable="false"
    >
      <circle cx="32" cy="32" r="30" fill="#C8102E" />
      <path
        fill="#FFFFFF"
        d="M18 42V22h6.2l5.4 12.6L35 22H41v20h-5.2V29.8L31.2 42h-4.4l-4.6-12.2V42H18z"
      />
      <rect x="18" y="44" width="28" height="3.5" rx="1" fill="#FFFFFF" />
    </svg>
  )
}
