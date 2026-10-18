import { MenuItem, TextField } from '@mui/material'
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
import { todayIsoDate } from '../format'
import { useNotify } from '../notifications/useNotify'
import { FormDialog } from './FormDialog'
import { textFieldProps } from './formFields'

/** Leer, solange keine Routine gewählt ist */
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

/** Training einplanen oder ändern: Routine und Datum ab heute (SAF7) */
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
            // JJJJ-MM-TT lässt sich als Text vergleichen (#FA-4.2)
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

interface StartFormValues {
  routineId: RoutineChoice
}

/** Routine ohne Planung direkt starten (#FA-5.1) */
export function StartSessionDialog({
  routines,
  onClose,
}: {
  routines: RoutineResponse[]
  onClose: () => void
}) {
  const navigate = useNavigate()
  const [submitError, setSubmitError] = useState<string>()
  const {
    control,
    handleSubmit,
    formState: { isSubmitting },
  } = useForm<StartFormValues>({ defaultValues: { routineId: '' } })

  const onSubmit = async (values: StartFormValues) => {
    setSubmitError(undefined)
    try {
      await startSpontaneousSession({ routineId: Number(values.routineId) })
      navigate('/training')
    } catch (error) {
      setSubmitError(getErrorMessage(error, START_ERROR_MESSAGES))
    }
  }

  return (
    <FormDialog
      title="Spontan starten"
      submitLabel="Starten"
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <RoutineSelect control={control} name="routineId" routines={routines} />
    </FormDialog>
  )
}
