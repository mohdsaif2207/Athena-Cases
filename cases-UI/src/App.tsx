import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { AuthProvider } from '@/contexts/AuthContext'
import { LoginPage } from '@/features/auth/pages/LoginPage'
import { CasesDashboardPlaceholder } from '@/features/cases/pages/CasesDashboardPlaceholder'
import { ProtectedRoute } from '@/routes/ProtectedRoute'
import './App.css'

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/login" element={<LoginPage />} />
          <Route
            path="/cases"
            element={
              <ProtectedRoute>
                <CasesDashboardPlaceholder />
              </ProtectedRoute>
            }
          />
          <Route path="/" element={<Navigate to="/cases" replace />} />
          <Route path="*" element={<Navigate to="/cases" replace />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
