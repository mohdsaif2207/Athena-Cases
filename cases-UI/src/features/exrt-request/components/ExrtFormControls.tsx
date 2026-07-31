import {
  FormControl,
  FormHelperText,
  InputLabel,
  MenuItem,
  Select,
  TextField,
  type SelectChangeEvent,
} from '@mui/material'
import type { LookupItem } from '../types/exrt.types'
import { EXRT_TEAL } from '../theme/exrtTheme'

const controlSx = {
  '& .MuiInputBase-root': {
    backgroundColor: EXRT_TEAL.inputBg,
    borderRadius: '2px',
    fontSize: EXRT_TEAL.inputSize,
    fontFamily: EXRT_TEAL.fontFamily,
    minHeight: EXRT_TEAL.inputHeight,
  },
  '& .MuiInputBase-root.Mui-disabled': {
    backgroundColor: EXRT_TEAL.disabledBg,
  },
  '& .MuiInputLabel-root': {
    fontWeight: 600,
    fontSize: EXRT_TEAL.labelSize,
    fontFamily: EXRT_TEAL.fontFamily,
  },
  '& .MuiFormLabel-asterisk': {
    color: EXRT_TEAL.error,
  },
} as const

type TextProps = {
  label: string
  name: string
  value: string
  onChange: (value: string) => void
  error?: string
  required?: boolean
  disabled?: boolean
  multiline?: boolean
  rows?: number
  maxLength?: number
  placeholder?: string
  helperText?: string
  testId: string
}

export function ExrtTextField({
  label,
  name,
  value,
  onChange,
  error,
  required,
  disabled,
  multiline,
  rows,
  maxLength,
  placeholder,
  helperText,
  testId,
}: TextProps) {
  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      name={name}
      value={value ?? ''}
      onChange={(e) => onChange(e.target.value)}
      required={required}
      disabled={disabled}
      multiline={multiline}
      rows={rows}
      placeholder={placeholder}
      helperText={error ?? helperText}
      error={Boolean(error)}
      sx={controlSx}
      slotProps={{
        htmlInput: { maxLength, 'data-testid': testId },
        formHelperText: { role: error ? 'alert' : undefined },
      }}
    />
  )
}

type SelectProps = {
  label: string
  name: string
  value: string
  options: LookupItem[]
  onChange: (value: string) => void
  error?: string
  required?: boolean
  disabled?: boolean
  testId: string
}

export function ExrtSelectField({
  label,
  name,
  value,
  options,
  onChange,
  error,
  required,
  disabled,
  testId,
}: SelectProps) {
  const handleChange = (event: SelectChangeEvent) => onChange(event.target.value)

  return (
    <FormControl fullWidth size="small" error={Boolean(error)} required={required} disabled={disabled} sx={controlSx}>
      <InputLabel id={`${name}-label`}>{label}</InputLabel>
      <Select
        labelId={`${name}-label`}
        label={label}
        name={name}
        value={value ?? ''}
        onChange={handleChange}
        data-testid={testId}
      >
        {options.map((opt) => (
          <MenuItem key={opt.id} value={opt.code || opt.id}>
            {opt.label}
          </MenuItem>
        ))}
      </Select>
      {error ? <FormHelperText role="alert">{error}</FormHelperText> : null}
    </FormControl>
  )
}
