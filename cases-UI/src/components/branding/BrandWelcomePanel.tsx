import { FranklinMadisonLogo } from '@/components/branding/FranklinMadisonLogo'
import './BrandWelcomePanel.css'

/**
 * Centered Franklin Madison brand mark for pages the user can open but has no module access.
 */
export function BrandWelcomePanel({ testId = 'brand-welcome-panel' }: { testId?: string }) {
  return (
    <div className="brand-welcome" data-testid={testId} role="img" aria-label="Franklin Madison">
      <div className="brand-welcome__mark">
        <span className="brand-welcome__franklin">Franklin</span>
        <FranklinMadisonLogo className="brand-welcome__logo" />
        <span className="brand-welcome__madison">Madison</span>
      </div>
    </div>
  )
}
