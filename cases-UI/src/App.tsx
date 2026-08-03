import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom'
import { CasesListPage } from './features/cases/pages/CasesListPage'
import { DbmWorkOrderCreatePage } from './features/dbm-work-order-request'
import './App.css'

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/cases" element={<CasesListPage />} />
        <Route
          path="/cases/new/dbm-work-order"
          element={<DbmWorkOrderCreatePage />}
        />
        <Route path="/" element={<Navigate to="/cases/new/dbm-work-order" replace />} />
        <Route path="*" element={<Navigate to="/cases" replace />} />
      </Routes>
    </BrowserRouter>
  )
}

export default App
