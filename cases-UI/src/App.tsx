import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { AdminDashboardPage } from '@/features/admin/pages/AdminDashboardPage'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { BillingCaseCreatePage } from '@/features/cases/pages/BillingCaseCreatePage'
import { CasesDashboardPlaceholder } from '@/features/cases/pages/CasesDashboardPlaceholder'
import { DbmWorkOrderCreatePage } from '@/features/dbm-work-order-request'
import { ExrtRequestCreatePage } from '@/features/exrt-request'
import { EXRT_ROUTE } from '@/features/exrt-request/theme/exrtTheme'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import { RequirePermission } from '@/routes/RequirePermission'
import './App.css'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/home"
            element={
              <ProtectedRoute>
                <CasesDashboardPlaceholder />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cases"
            element={
              <ProtectedRoute>
                <RequirePermission anyOf={['CASES_VIEW', 'CASES_ACCESS', 'CASES_CREATE', 'CASES_EDIT']}>
                  <CasesDashboardPlaceholder />
                </RequirePermission>
              </ProtectedRoute>
            }
          />
          <Route
            path="/utilities"
            element={
              <ProtectedRoute>
                <AdminDashboardPage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/user-management"
            element={<Navigate to="/utilities" replace />}
          />
          <Route path="/admin" element={<Navigate to="/utilities" replace />} />
          <Route
            path="/cases/new/dbm"
            element={
              <ProtectedRoute>
                <DbmWorkOrderCreatePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cases/new/dbm-work-order"
            element={<Navigate to="/cases/new/dbm" replace />}
          />
          {/* Real ExRT create form — keep both path aliases */}
          <Route
            path="/cases/new/exrt"
            element={
              <ProtectedRoute>
                <ExrtRequestCreatePage />
              </ProtectedRoute>
            }
          />
          <Route
            path={EXRT_ROUTE}
            element={
              <ProtectedRoute>
                <ExrtRequestCreatePage />
              </ProtectedRoute>
            }
          />
          <Route
            path="/cases/new/billing"
            element={
              <ProtectedRoute>
                <BillingCaseCreatePage />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/home" replace />} />
          <Route path="*" element={<Navigate to="/home" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
