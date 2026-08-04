import { NavLink, useLocation } from 'react-router-dom'
import { useAuth } from '@/contexts/AuthContext'
import { FranklinMadisonLogo } from '@/components/branding/FranklinMadisonLogo'
import { CasesSearchPage } from '@/features/cases/pages/CasesSearchPage'
import { HomePage } from '@/features/cases/pages/HomePage'
import './CasesDashboardPlaceholder.css'

const APP_TABS = [
  'Home',
  'Communications',
  'Products',
  'Revenue Processing',
  'Marketing',
  'Client Services',
  'Response Processing',
  'Claims',
  'DBM',
  'Utilities',
  'Miscellaneous',
  'Legal',
  'Bill Track',
  'Vanity',
  'Forecasting',
  'Cases',
  'Contact Center',
  'Marketing Calendar',
] as const

/**
 * Athena Nextgen shell matching Cases Dashboard UI reference.
 */
export function CasesDashboardPlaceholder() {
  const { user, logout } = useAuth()
  const location = useLocation()
  const isHome = location.pathname === '/home'
  const welcomeName = user?.displayName?.trim() || 'User'
  const permissions = user?.permissions ?? []
  const canAccessCases = ['CASES_VIEW', 'CASES_ACCESS', 'CASES_CREATE', 'CASES_EDIT'].some((p) =>
    permissions.includes(p),
  )

  return (
    <div className="athena-shell" data-testid="cases-dashboard-placeholder">
      <header className="athena-topbar">
        <div className="athena-topbar__brand">
          <span className="athena-topbar__app">Athena Nextgen</span>
          <span className="athena-topbar__sep">|</span>
          <span className="athena-topbar__franklin">Franklin</span>
          <FranklinMadisonLogo className="athena-topbar__logo" />
          <span className="athena-topbar__madison">Madison</span>
        </div>
        <div className="athena-topbar__right">
          <p className="athena-topbar__welcome" data-testid="dashboard-welcome">
            Welcome, <strong>{welcomeName}</strong>
            <span className="athena-topbar__pipe">|</span>
            <button type="button" className="athena-topbar__logout" onClick={logout} data-testid="dashboard-logout">
              Log Out
            </button>
          </p>
        </div>
      </header>

      <div className="athena-tabs-row">
        <nav className="athena-tabs" aria-label="Primary" data-testid="dashboard-nav">
          {APP_TABS.map((tab) => {
            if (tab === 'Home') {
              return (
                <NavLink
                  key={tab}
                  to="/home"
                  end
                  className={({ isActive }) => `athena-tab ${isActive ? 'is-active' : ''}`}
                  data-testid="nav-home"
                >
                  {tab}
                </NavLink>
              )
            }
            if (tab === 'Cases') {
              if (!canAccessCases) {
                return (
                  <span key={tab} className="athena-tab is-inert" title="Requires Cases Access permission">
                    {tab}
                  </span>
                )
              }
              return (
                <NavLink
                  key={tab}
                  to="/cases"
                  end
                  className={({ isActive }) => `athena-tab ${isActive ? 'is-active' : ''}`}
                  data-testid="nav-cases"
                >
                  {tab}
                </NavLink>
              )
            }
            if (tab === 'Utilities') {
              return (
                <NavLink
                  key={tab}
                  to="/utilities"
                  className={({ isActive }) => `athena-tab ${isActive ? 'is-active' : ''}`}
                  data-testid="nav-utilities"
                >
                  {tab}
                </NavLink>
              )
            }
            return (
              <span key={tab} className="athena-tab is-inert" title="Not available in this module">
                {tab}
              </span>
            )
          })}
        </nav>
        <button type="button" className="athena-other-links" data-testid="other-links">
          <GridIcon />
          Other Links
        </button>
      </div>

      <div className="athena-page-title" data-testid={isHome ? 'home-page-title' : 'cases-page-title'}>
        {isHome ? 'Home' : 'Cases'}
      </div>

      <main className="athena-main">{isHome ? <HomePage /> : <CasesSearchPage />}</main>
    </div>
  )
}

function GridIcon() {
  return (
    <svg width="14" height="14" viewBox="0 0 24 24" fill="none" aria-hidden="true">
      <path
        d="M4 4h6v6H4V4Zm10 0h6v6h-6V4ZM4 14h6v6H4v-6Zm10 0h6v6h-6v-6Z"
        fill="currentColor"
      />
    </svg>
  )
}
