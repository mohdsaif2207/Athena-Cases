/**
 * Athena enterprise visual tokens for ExRT create form (aligned with Cases Search shell).
 */
export const EXRT_TEAL = {
  primary: '#0D1B5E',
  secondary: '#14276B',
  background: '#F5F6FA',
  border: '#D0D5DD',
  hover: '#E8EEF7',
  card: '#FFFFFF',
  radius: '4px',

  nav: '#0D1B5E',
  navSecondary: '#14276B',
  pageBg: '#F5F6FA',
  cardBg: '#FFFFFF',
  inputBg: '#FFFFFF',
  inputBorder: '#C9CED6',
  focusBorder: '#0D1B5E',
  textPrimary: '#1E1E1E',
  textSecondary: '#5F6B7A',
  sectionTitle: '#0D1B5E',
  success: '#2E7D32',
  error: '#C62828',
  divider: '#E4E7EC',
  disabledBg: '#F2F4F7',
  buttonPrimary: '#C62828',
  buttonPrimaryHover: '#B71C1C',
  buttonReset: '#C62828',
  buttonCancel: '#C62828',
  modalFooter: '#F0F0F0',
  tableHeader: '#0D1B5E',
  tableHover: '#EEF3FA',
  tableAlt: '#F7F8FC',
  fontFamily: "'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif",
  inputHeight: 32,
  labelSize: 11,
  inputSize: 12,
  sectionHeaderSize: 13,
  titleSize: 16,
  gridSize: 12,
} as const

export const EXRT_ROUTE = '/cases/new/exrt-request'
export const EXRT_API_BASE = '/api/v1/cases/exrt-requests'
export const EXRT_LOOKUP_BASE = '/api/v1/lookups/exrt'

/** Shared sx for section title bars (compact navy strip). */
export const exrtSectionHeaderSx = {
  bgcolor: EXRT_TEAL.nav,
  color: '#FFFFFF',
  fontSize: EXRT_TEAL.sectionHeaderSize,
  fontWeight: 600,
  fontFamily: EXRT_TEAL.fontFamily,
  letterSpacing: '0.02em',
  px: 1.5,
  py: 0.875,
  mx: -2,
  mt: -2,
  mb: 1.5,
  lineHeight: 1.25,
} as const

/** Shared sx for white section cards. */
export const exrtCardSx = {
  borderRadius: EXRT_TEAL.radius,
  border: `1px solid ${EXRT_TEAL.border}`,
  boxShadow: 'none',
  backgroundColor: EXRT_TEAL.cardBg,
  overflow: 'hidden',
} as const
