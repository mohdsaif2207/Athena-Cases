/**
 * Enterprise Insurance/Banking visual tokens for ExRT UI.
 * Visual theme only — route/API constants unchanged.
 */
export const EXRT_TEAL = {
  // Kept export name for compatibility; values are enterprise navy (not teal).
  primary: '#0D1B5E',
  secondary: '#14276B',
  background: '#F5F6FA',
  border: '#D8D8D8',
  hover: '#E8EEF7',
  card: '#FFFFFF',
  radius: '4px',

  nav: '#0D1B5E',
  navSecondary: '#14276B',
  pageBg: '#F5F6FA',
  cardBg: '#FFFFFF',
  inputBg: '#ECEAF5',
  inputBorder: '#C9C7D8',
  focusBorder: '#5A82C8',
  textPrimary: '#1E1E1E',
  textSecondary: '#555555',
  sectionTitle: '#0D1B5E',
  success: '#2E7D32',
  error: '#D32F2F',
  divider: '#D9D9D9',
  disabledBg: '#F3F3F3',
  buttonPrimary: '#E74C3C',
  buttonPrimaryHover: '#D84333',
  buttonReset: '#F05A4F',
  buttonCancel: '#E74C3C',
  modalFooter: '#F0F0F0',
  tableHeader: '#0D1B5E',
  tableHover: '#EEF3FA',
  tableAlt: '#F7F8FC',
  fontFamily: "'Segoe UI', Roboto, sans-serif",
  inputHeight: 36,
  labelSize: 13,
  inputSize: 13,
  sectionHeaderSize: 15,
  titleSize: 18,
  gridSize: 13,
} as const

export const EXRT_ROUTE = '/cases/new/exrt-request'
export const EXRT_API_BASE = '/api/v1/cases/exrt-requests'
export const EXRT_LOOKUP_BASE = '/api/v1/lookups/exrt'

/** Shared sx for section title bars (navy strip, white text). */
export const exrtSectionHeaderSx = {
  bgcolor: EXRT_TEAL.nav,
  color: '#FFFFFF',
  fontSize: EXRT_TEAL.sectionHeaderSize,
  fontWeight: 600,
  fontFamily: EXRT_TEAL.fontFamily,
  px: 1.5,
  py: 1.5,
  mx: -2,
  mt: -2,
  mb: 2,
  lineHeight: 1.3,
} as const

/** Shared sx for white section cards. */
export const exrtCardSx = {
  borderRadius: EXRT_TEAL.radius,
  border: `1px solid ${EXRT_TEAL.border}`,
  boxShadow: '0 1px 2px rgba(13, 27, 94, 0.06)',
  backgroundColor: EXRT_TEAL.cardBg,
  overflow: 'hidden',
} as const
