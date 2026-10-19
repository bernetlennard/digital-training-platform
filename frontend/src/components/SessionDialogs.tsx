import PlayArrow from '@mui/icons-material/PlayArrow'
import {
  Alert,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  List,
  ListItemButton,
  ListItemText,
  MenuItem,
  TextField,
} from '@mui/material'
import { useState } from 'react'
import {
  Controller,
  useForm,
  type Control,
  type FieldValues,
  type Path,
} from 'react-hook-form'
import { useNavigate } from 'react-router'
import { getErrorMessage } from '../api/client'
import {
  planSession,
  START_ERROR_MESSAGES,
  startSpontaneousSession,
  updatePlannedSession,
} from '../api/sessions'
import type { PlannedSessionResponse, RoutineResponse } from '../api/types'
import { formatExerciseList, todayIsoDate } from '../format'
import { useNotify } from '../notifications/useNotify'
import { FormDialog } from './FormDialog'
import { textFieldProps } from './formFields'

type RoutineChoice = number | ''

function RoutineSelect<T extends FieldValues>({
  control,
  name,
  routines,
}: {
  control: Control<T>
  name: Path<T>
  routines: RoutineResponse[]
}) {
  return (
    <Controller
      name={name}
      control={control}
      rules={{ required: 'Bitte Routine wählen.' }}
      render={({ field: { ref, ...field }, fieldState: { error } }) => (
        <TextField
          {...field}
          inputRef={ref}
          select
          label="Routine"
          required
          fullWidth
          error={!!error}
          helperText={error?.message}
        >
          {routines.map((routine) => (
            <MenuItem key={routine.id} value={routine.id}>
              {routine.name}
            </MenuItem>
          ))}
        </TextField>
      )}
    />
  )
}

interface PlanFormValues {
  routineId: RoutineChoice
  plannedDate: string
}

interface PlanSessionDialogProps {
  routines: RoutineResponse[]
  /** Fehlt beim Einplanen, gesetzt beim Ändern */
  planned?: PlannedSessionResponse
  onClose: () => void
  onSaved: () => void
}

export function PlanSessionDialog({
  routines,
  planned,
  onClose,
  onSaved,
}: PlanSessionDialogProps) {
  const notify = useNotify()
  const [today] = useState(todayIsoDate)
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<PlanFormValues>({
    defaultValues: {
      routineId: planned?.routineId ?? '',
      plannedDate: planned?.plannedDate ?? today,
    },
  })

  const onSubmit = async (values: PlanFormValues) => {
    setSubmitError(undefined)
    const request = {
      routineId: Number(values.routineId),
      plannedDate: values.plannedDate,
    }
    try {
      if (planned) {
        await updatePlannedSession(planned.id, request)
      } else {
        await planSession(request)
      }
      notify(planned ? 'Training geändert' : 'Training eingeplant')
      onSaved()
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          404: 'Die Routine oder das Training wurde nicht gefunden.',
          422: 'Ein Training lässt sich nur für heute oder später planen.',
        }),
      )
    }
  }

  return (
    <FormDialog
      title={planned ? 'Training ändern' : 'Training einplanen'}
      submitLabel={planned ? 'Speichern' : 'Einplanen'}
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <RoutineSelect control={control} name="routineId" routines={routines} />
      <TextField
        {...textFieldProps(
          register('plannedDate', {
            required: 'Bitte Datum wählen.',
            // JJJJ-MM-TT lässt sich als Text vergleichen
            validate: (value) =>
              value >= today ||
              'Das Datum darf nicht in der Vergangenheit liegen.',
          }),
        )}
        label="Datum"
        type="date"
        required
        fullWidth
        error={!!errors.plannedDate}
        helperText={errors.plannedDate?.message}
        slotProps={{ inputLabel: { shrink: true }, htmlInput: { min: today } }}
      />
    </FormDialog>
  )
}

export function StartSessionDialog({
  routines,
  onClose,
}: {
  routines: RoutineResponse[]
  onClose: () => void
}) {
  const navigate = useNavigate()
  const [startingId, setStartingId] = useState<number>()
  const [startError, setStartError] = useState<string>()
  const isStarting = startingId !== undefined

  const handleStart = async (routine: RoutineResponse) => {
    setStartError(undefined)
    setStartingId(routine.id)
    try {
      await startSpontaneousSession({ routineId: routine.id })
      navigate('/training')
    } catch (error) {
      setStartError(getErrorMessage(error, START_ERROR_MESSAGES))
      setStartingId(undefined)
    }
  }

  return (
    <Dialog
      open
      onClose={isStarting ? undefined : onClose}
      fullWidth
      maxWidth="xs"
      scroll="paper"
    >
      <DialogTitle>Spontan starten</DialogTitle>
      <DialogContent dividers sx={{ p: 0 }}>
        {startError && (
          <Alert severity="error" sx={{ m: 2 }}>
            {startError}
          </Alert>
        )}
        <List disablePadding>
          {routines.map((routine) => (
            <ListItemButton
              key={routine.id}
              divider
              disabled={isStarting}
              onClick={() => handleStart(routine)}
            >
              <ListItemText
                primary={routine.name}
                secondary={formatExerciseList(routine.exercises)}
                slotProps={{ secondary: { noWrap: true } }}
                sx={{ minWidth: 0, mr: 1 }}
              />
              {startingId === routine.id ? (
                <CircularProgress size={24} aria-label="Wird gestartet" />
              ) : (
                <PlayArrow color="primary" />
              )}
            </ListItemButton>
          ))}
        </List>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose} disabled={isStarting}>
          Abbrechen
        </Button>
      </DialogActions>
    </Dialog>
  )
}
