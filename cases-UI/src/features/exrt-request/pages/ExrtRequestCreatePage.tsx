import { useEffect, useMemo, useState } from 'react'
import { useForm, useWatch } from 'react-hook-form'
import { zodResolver } from '@hookform/resolvers/zod'
import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogContentText,
  DialogTitle,
  Stack,
  Typography,
} from '@mui/material'
import { useNavigate } from 'react-router-dom'
import { useCreateExrtCase } from '../hooks/useCreateExrtCase'
import { useExrtLookups } from '../hooks/useExrtLookups'
import { EXRT_TEAL, exrtCardSx, exrtSectionHeaderSx } from '../theme/exrtTheme'
import {
  CaseDetailsSection,
  InformationSection,
  NotesSection,
  SystemInformationSection,
} from '../components/ExrtSections'
import {
  exrtCaseCreateSchema,
  exrtFormDefaults,
  type ExrtCaseFormValues,
} from '../validation/exrtCaseCreateSchema'

function resolveCaseOwner(): string {
  try {
    const raw = localStorage.getItem('userContext')
    if (raw) {
      const parsed = JSON.parse(raw) as { displayName?: string; username?: string }
      return parsed.displayName || parsed.username || 'Logged-in User'
    }
  } catch {
    // ignore
  }
  return 'Logged-in User'
}

export function ExrtRequestCreatePage() {
  const navigate = useNavigate()
  const [cancelOpen, setCancelOpen] = useState(false)
  const [successMessage, setSuccessMessage] = useState<string | null>(null)
  const createMutation = useCreateExrtCase()

  const {
    control,
    handleSubmit,
    reset,
    formState: { errors, isDirty, isSubmitting },
  } = useForm<ExrtCaseFormValues>({
    resolver: zodResolver(exrtCaseCreateSchema),
    defaultValues: { ...exrtFormDefaults, caseOwner: resolveCaseOwner() },
    mode: 'onBlur',
  })

  const clientId = useWatch({ control, name: 'clientId' })
  const lookups = useExrtLookups(clientId)

  useEffect(() => {
    reset({ ...exrtFormDefaults, caseOwner: resolveCaseOwner() })
  }, [reset])

  const cardSx = useMemo(() => exrtCardSx, [])

  const onSubmit = handleSubmit(async (values) => {
    setSuccessMessage(null)
    const { caseOwner: _owner, ...payload } = values
    const result = await createMutation.mutateAsync({
      ...payload,
      callCenterEducation: Boolean(payload.callCenterEducation),
    })
    setSuccessMessage(result.message)
  })

  if (lookups.isLoading) {
    return (
      <Box sx={{ p: 4, display: 'flex', justifyContent: 'center' }} data-testid="exrt-loading">
        <CircularProgress sx={{ color: EXRT_TEAL.primary }} />
      </Box>
    )
  }

  if (lookups.isError) {
    return (
      <Box sx={{ p: 4 }} data-testid="exrt-lookups-error">
        <Alert severity="error">Unable to load lookup data. Please try again later.</Alert>
      </Box>
    )
  }

  return (
    <Box
      sx={{
        bgcolor: EXRT_TEAL.pageBg,
        minHeight: '100vh',
        py: 2,
        px: { xs: 2, md: 3 },
        fontFamily: EXRT_TEAL.fontFamily,
      }}
    >
      <Box
        component="form"
        onSubmit={onSubmit}
        noValidate
        sx={{ maxWidth: 1100, mx: 'auto', display: 'flex', flexDirection: 'column', gap: 1.5 }}
      >
        <Typography
          variant="h4"
          data-testid="exrt-page-title"
          sx={{
            fontWeight: 600,
            fontSize: EXRT_TEAL.titleSize,
            color: EXRT_TEAL.sectionTitle,
            fontFamily: EXRT_TEAL.fontFamily,
            mb: 0.5,
          }}
        >
          Create ExRT Request
        </Typography>

        {successMessage ? (
          <Alert severity="success" data-testid="exrt-success-message">
            {successMessage}
          </Alert>
        ) : null}
        {createMutation.isError ? (
          <Alert severity="error" data-testid="exrt-save-error">
            Unable to create case. Please correct the form and try again.
          </Alert>
        ) : null}

        <Card sx={cardSx}>
          <CardContent>
            <Typography variant="h6" sx={exrtSectionHeaderSx}>
              Case Details
            </Typography>
            <CaseDetailsSection
              control={control}
              errors={errors}
              clients={lookups.clients}
              products={lookups.products}
              carriers={lookups.carriers}
              tierIiAgents={lookups.tierIiAgents}
              statuses={lookups.statuses}
            />
          </CardContent>
        </Card>

        <Card sx={cardSx}>
          <CardContent>
            <Typography variant="h6" sx={exrtSectionHeaderSx}>
              Information
            </Typography>
            <InformationSection
              control={control}
              errors={errors}
              types={lookups.types}
              dispositions={lookups.dispositions}
              inquirySources={lookups.inquirySources}
              actionNeeded={lookups.actionNeeded}
              assignees={lookups.assignees}
              escalationReasons={lookups.escalationReasons}
              reasonCodes={lookups.reasonCodes}
            />
          </CardContent>
        </Card>

        <Card sx={cardSx}>
          <CardContent>
            <Typography variant="h6" sx={exrtSectionHeaderSx}>
              Notes
            </Typography>
            <NotesSection control={control} errors={errors} />
          </CardContent>
        </Card>

        <Card sx={cardSx}>
          <CardContent>
            <Typography variant="h6" sx={exrtSectionHeaderSx}>
              System Information
            </Typography>
            <SystemInformationSection
              control={control}
              errors={errors}
              caseOrigins={lookups.caseOrigins}
              contacts={lookups.contacts}
              priorities={lookups.priorities}
            />
          </CardContent>
        </Card>

        <Stack direction="row" spacing={1.5} sx={{ justifyContent: 'flex-end', pt: 0.5 }}>
          <Button
            variant="outlined"
            onClick={() => (isDirty ? setCancelOpen(true) : navigate(-1))}
            data-testid="exrt-cancel"
          >
            Cancel
          </Button>
          <Button
            type="submit"
            variant="contained"
            disabled={isSubmitting || createMutation.isPending}
            data-testid="exrt-save"
          >
            {createMutation.isPending ? 'Saving…' : 'Save'}
          </Button>
        </Stack>
      </Box>

      <Dialog open={cancelOpen} onClose={() => setCancelOpen(false)} data-testid="exrt-cancel-dialog">
        <DialogTitle>Discard changes?</DialogTitle>
        <DialogContent>
          <DialogContentText>Unsaved changes will be lost. Do you want to continue?</DialogContentText>
        </DialogContent>
        <DialogActions>
          <Button variant="outlined" onClick={() => setCancelOpen(false)}>
            No
          </Button>
          <Button
            variant="contained"
            onClick={() => {
              setCancelOpen(false)
              navigate(-1)
            }}
            autoFocus
            data-testid="exrt-cancel-confirm"
          >
            Yes
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  )
}
