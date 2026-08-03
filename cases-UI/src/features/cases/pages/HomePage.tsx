import { useState } from 'react'
import { useAuth } from '@/contexts/AuthContext'
import { BrandWelcomePanel } from '@/components/branding/BrandWelcomePanel'
import { NotificationQueuePanel } from '@/features/cases/components/NotificationQueuePanel'
import { WorkflowQueuePanel } from '@/features/cases/components/WorkflowQueuePanel'
import './CasesSearchPage.css'

/**
 * Home screen — Workflow / Notification queues when permitted; otherwise Franklin Madison brand.
 */
export function HomePage() {
  const { user } = useAuth()
  const permissions = user?.permissions ?? []
  const canViewWorkflow = permissions.includes('WF_VIEW')
  const canViewNotification = permissions.includes('NOTIF_VIEW')
  const [toast, setToast] = useState<string | null>(null)

  function showToast(message: string) {
    setToast(message)
    window.setTimeout(() => setToast(null), 2800)
  }

  const hasQueueAccess = canViewWorkflow || canViewNotification

  return (
    <div className="cases-search" data-testid="home-page">
      {canViewWorkflow ? <WorkflowQueuePanel onToast={showToast} /> : null}
      {canViewNotification ? <NotificationQueuePanel onToast={showToast} /> : null}

      {!hasQueueAccess ? <BrandWelcomePanel testId="home-brand-welcome" /> : null}

      {toast ? (
        <div className="cases-toast" role="status" data-testid="home-toast">
          {toast}
        </div>
      ) : null}
    </div>
  )
}
