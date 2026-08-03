import { Link } from 'react-router-dom'
import '@/features/cases/pages/CasesSearchPage.css'

interface CaseCreatePlaceholderProps {
  title: string
  testId: string
}

/**
 * Temporary shell for feature case-creation screens until those modules ship forms.
 */
export function CaseCreatePlaceholder({ title, testId }: CaseCreatePlaceholderProps) {
  return (
    <div className="case-create-placeholder" data-testid={testId}>
      <header className="case-create-placeholder__header">
        <h1>{title}</h1>
        <Link to="/cases" className="cases-btn cases-btn--primary" data-testid="case-create-back">
          Back to Cases
        </Link>
      </header>
      <p className="case-create-placeholder__body">
        Case creation fields for this type will be completed in the feature module.
      </p>
    </div>
  )
}
