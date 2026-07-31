import { Route } from 'react-router-dom'
import { ExrtRequestCreatePage } from './pages/ExrtRequestCreatePage'
import { EXRT_ROUTE } from './theme/exrtTheme'

/** Lead-owned router should import and spread/register these routes. */
export const exrtRequestRoutes = (
  <Route path={EXRT_ROUTE} element={<ExrtRequestCreatePage />} />
)

export { EXRT_ROUTE, ExrtRequestCreatePage }
