import { createTheme } from '@mui/material'
import { EXRT_TEAL } from './exrtTheme'

const inputOutlinedSx = {
  backgroundColor: EXRT_TEAL.inputBg,
  borderRadius: '2px',
  fontSize: EXRT_TEAL.inputSize,
  fontFamily: EXRT_TEAL.fontFamily,
  '& .MuiOutlinedInput-notchedOutline': {
    borderColor: EXRT_TEAL.inputBorder,
    borderWidth: '1px',
  },
  '&:hover .MuiOutlinedInput-notchedOutline': {
    borderColor: EXRT_TEAL.inputBorder,
  },
  '&.Mui-focused .MuiOutlinedInput-notchedOutline': {
    borderColor: EXRT_TEAL.focusBorder,
    borderWidth: '1px',
  },
  '&.Mui-disabled': {
    backgroundColor: EXRT_TEAL.disabledBg,
  },
  '&.Mui-disabled .MuiOutlinedInput-notchedOutline': {
    borderColor: EXRT_TEAL.inputBorder,
  },
}

/**
 * MUI theme matching enterprise Insurance/Banking portal look.
 * Visual only — does not change form behavior.
 */
export function createExrtMuiTheme() {
  return createTheme({
    typography: {
      fontFamily: EXRT_TEAL.fontFamily,
      fontSize: 13,
      h4: {
        fontSize: EXRT_TEAL.titleSize,
        fontWeight: 600,
        color: EXRT_TEAL.sectionTitle,
        fontFamily: EXRT_TEAL.fontFamily,
      },
      h6: {
        fontSize: EXRT_TEAL.sectionHeaderSize,
        fontWeight: 600,
        fontFamily: EXRT_TEAL.fontFamily,
      },
      body1: {
        fontSize: 13,
        color: EXRT_TEAL.textPrimary,
      },
      body2: {
        fontSize: 13,
        color: EXRT_TEAL.textSecondary,
      },
      button: {
        textTransform: 'none',
        fontWeight: 600,
        fontSize: 13,
        fontFamily: EXRT_TEAL.fontFamily,
      },
    },
    palette: {
      mode: 'light',
      primary: {
        main: EXRT_TEAL.primary,
        dark: EXRT_TEAL.secondary,
        contrastText: '#FFFFFF',
      },
      secondary: {
        main: EXRT_TEAL.buttonPrimary,
        dark: EXRT_TEAL.buttonPrimaryHover,
        contrastText: '#FFFFFF',
      },
      error: { main: EXRT_TEAL.error },
      success: { main: EXRT_TEAL.success },
      text: {
        primary: EXRT_TEAL.textPrimary,
        secondary: EXRT_TEAL.textSecondary,
      },
      background: {
        default: EXRT_TEAL.pageBg,
        paper: EXRT_TEAL.cardBg,
      },
      divider: EXRT_TEAL.divider,
    },
    shape: { borderRadius: 4 },
    shadows: [
      'none',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
      '0 1px 2px rgba(13, 27, 94, 0.06)',
    ],
    components: {
      MuiCssBaseline: {
        styleOverrides: {
          body: {
            backgroundColor: EXRT_TEAL.pageBg,
            color: EXRT_TEAL.textPrimary,
            fontFamily: EXRT_TEAL.fontFamily,
            fontSize: 13,
          },
          '#root': {
            width: '100%',
            maxWidth: '100%',
            margin: 0,
            textAlign: 'left',
            minHeight: '100vh',
            border: 'none',
            display: 'block',
          },
        },
      },
      MuiButton: {
        defaultProps: {
          disableElevation: true,
        },
        styleOverrides: {
          root: {
            height: EXRT_TEAL.inputHeight,
            minHeight: EXRT_TEAL.inputHeight,
            borderRadius: EXRT_TEAL.radius,
            boxShadow: 'none',
            paddingLeft: 16,
            paddingRight: 16,
            '&:hover': { boxShadow: 'none' },
          },
          contained: {
            backgroundColor: EXRT_TEAL.buttonPrimary,
            color: '#FFFFFF',
            '&:hover': {
              backgroundColor: EXRT_TEAL.buttonPrimaryHover,
            },
            '&.Mui-disabled': {
              backgroundColor: '#F5A9A3',
              color: '#FFFFFF',
            },
          },
          outlined: {
            backgroundColor: EXRT_TEAL.buttonCancel,
            borderColor: EXRT_TEAL.buttonCancel,
            color: '#FFFFFF',
            '&:hover': {
              backgroundColor: EXRT_TEAL.buttonPrimaryHover,
              borderColor: EXRT_TEAL.buttonPrimaryHover,
              color: '#FFFFFF',
            },
          },
        },
      },
      MuiTextField: {
        defaultProps: {
          size: 'small',
          variant: 'outlined',
        },
      },
      MuiOutlinedInput: {
        styleOverrides: {
          root: {
            ...inputOutlinedSx,
            minHeight: EXRT_TEAL.inputHeight,
          },
          input: {
            paddingTop: '8px',
            paddingBottom: '8px',
            fontSize: EXRT_TEAL.inputSize,
            height: 'auto',
            boxSizing: 'border-box',
          },
          multiline: {
            paddingTop: 0,
            paddingBottom: 0,
            minHeight: 'auto',
          },
        },
      },
      MuiInputLabel: {
        styleOverrides: {
          root: {
            fontSize: EXRT_TEAL.labelSize,
            fontWeight: 600,
            color: EXRT_TEAL.textPrimary,
            fontFamily: EXRT_TEAL.fontFamily,
            '&.Mui-focused': {
              color: EXRT_TEAL.focusBorder,
            },
            '&.Mui-error': {
              color: EXRT_TEAL.error,
            },
          },
          asterisk: {
            color: EXRT_TEAL.error,
          },
        },
      },
      MuiFormLabel: {
        styleOverrides: {
          asterisk: {
            color: EXRT_TEAL.error,
          },
        },
      },
      MuiSelect: {
        styleOverrides: {
          select: {
            fontSize: EXRT_TEAL.inputSize,
            minHeight: 'auto',
            display: 'flex',
            alignItems: 'center',
          },
        },
      },
      MuiFormHelperText: {
        styleOverrides: {
          root: {
            fontSize: 12,
            marginLeft: 0,
            fontFamily: EXRT_TEAL.fontFamily,
          },
        },
      },
      MuiCard: {
        styleOverrides: {
          root: {
            borderRadius: EXRT_TEAL.radius,
            border: `1px solid ${EXRT_TEAL.border}`,
            boxShadow: '0 1px 2px rgba(13, 27, 94, 0.06)',
            backgroundColor: EXRT_TEAL.cardBg,
          },
        },
      },
      MuiCardContent: {
        styleOverrides: {
          root: {
            padding: 16,
            '&:last-child': { paddingBottom: 16 },
          },
        },
      },
      MuiDialogTitle: {
        styleOverrides: {
          root: {
            backgroundColor: EXRT_TEAL.nav,
            color: '#FFFFFF',
            fontSize: EXRT_TEAL.sectionHeaderSize,
            fontWeight: 600,
            fontFamily: EXRT_TEAL.fontFamily,
            padding: '12px 16px',
          },
        },
      },
      MuiDialogContent: {
        styleOverrides: {
          root: {
            backgroundColor: EXRT_TEAL.cardBg,
            padding: '16px',
            fontSize: 13,
          },
        },
      },
      MuiDialogActions: {
        styleOverrides: {
          root: {
            backgroundColor: EXRT_TEAL.modalFooter,
            borderTop: `1px solid ${EXRT_TEAL.divider}`,
            padding: '10px 16px',
            justifyContent: 'flex-end',
          },
        },
      },
      MuiDialogContentText: {
        styleOverrides: {
          root: {
            color: EXRT_TEAL.textPrimary,
            fontSize: 13,
            fontFamily: EXRT_TEAL.fontFamily,
          },
        },
      },
      MuiTableHead: {
        styleOverrides: {
          root: {
            backgroundColor: EXRT_TEAL.tableHeader,
            '& .MuiTableCell-head': {
              color: '#FFFFFF',
              fontWeight: 600,
              fontSize: EXRT_TEAL.gridSize,
              fontFamily: EXRT_TEAL.fontFamily,
              borderBottom: `1px solid ${EXRT_TEAL.border}`,
              padding: '8px 12px',
              whiteSpace: 'nowrap',
            },
          },
        },
      },
      MuiTableBody: {
        styleOverrides: {
          root: {
            '& .MuiTableRow-root:nth-of-type(even)': {
              backgroundColor: EXRT_TEAL.tableAlt,
            },
            '& .MuiTableRow-root:hover': {
              backgroundColor: EXRT_TEAL.tableHover,
            },
          },
        },
      },
      MuiTableCell: {
        styleOverrides: {
          root: {
            fontSize: EXRT_TEAL.gridSize,
            fontFamily: EXRT_TEAL.fontFamily,
            color: EXRT_TEAL.textPrimary,
            borderBottom: `1px solid ${EXRT_TEAL.divider}`,
            padding: '6px 12px',
          },
        },
      },
      MuiCheckbox: {
        styleOverrides: {
          root: {
            color: EXRT_TEAL.inputBorder,
            '&.Mui-checked': {
              color: EXRT_TEAL.primary,
            },
          },
        },
      },
      MuiAlert: {
        styleOverrides: {
          root: {
            fontSize: 13,
            fontFamily: EXRT_TEAL.fontFamily,
            borderRadius: EXRT_TEAL.radius,
          },
        },
      },
      MuiCircularProgress: {
        styleOverrides: {
          root: {
            color: EXRT_TEAL.primary,
          },
        },
      },
    },
  })
}
