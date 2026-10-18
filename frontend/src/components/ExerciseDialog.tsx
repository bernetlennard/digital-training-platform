import { Button, MenuItem, TextField } from '@mui/material'
import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { getErrorMessage } from '../api/client'
import {
  createExercise,
  deleteExercise,
  updateExercise,
} from '../api/exercises'
import type { ExerciseResponse, MuscleGroupResponse } from '../api/types'
import { useNotify } from '../notifications/useNotify'
import { ConfirmDialog } from './ConfirmDialog'
import { FormDialog } from './FormDialog'
import { emptyToNull, nameRules, textFieldProps } from './formFields'

interface ExerciseFormValues {
  name: string
  /** Leer, solange keine Muskelgruppe gewählt ist */
  muscleGroupId: number | ''
  description: string
}

interface ExerciseDialogProps {
  muscleGroups: MuscleGroupResponse[]
  /** Fehlt beim Erfassen einer neuen Übung */
  exercise?: ExerciseResponse
  onClose: () => void
  /** Nach dem Speichern oder Löschen */
  onChanged: () => void
}

/** Übung erfassen, bearbeiten oder löschen (SAF5) */
export function ExerciseDialog({
  muscleGroups,
  exercise,
  onClose,
  onChanged,
}: ExerciseDialogProps) {
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const [confirmDelete, setConfirmDelete] = useState(false)
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<ExerciseFormValues>({
    defaultValues: {
      name: exercise?.name ?? '',
      muscleGroupId: exercise?.muscleGroupId ?? '',
      description: exercise?.description ?? '',
    },
  })

  const onSubmit = async (values: ExerciseFormValues) => {
    setSubmitError(undefined)
    const request = {
      name: values.name.trim(),
      muscleGroupId: Number(values.muscleGroupId),
      description: emptyToNull(values.description),
    }
    try {
      if (exercise) {
        await updateExercise(exercise.id, request)
      } else {
        await createExercise(request)
      }
      notify(exercise ? 'Übung gespeichert' : 'Übung erfasst')
      onChanged()
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          409: 'Es gibt bereits eine Übung mit diesem Namen.',
        }),
      )
    }
  }

  const handleDelete = async () => {
    if (!exercise) return
    await deleteExercise(exercise.id)
    notify('Übung gelöscht')
    onChanged()
  }

  return (
    <>
      <FormDialog
        title={exercise ? 'Übung bearbeiten' : 'Neue Übung'}
        isSubmitting={isSubmitting}
        error={submitError}
        onSubmit={handleSubmit(onSubmit)}
        onClose={onClose}
        secondaryAction={
          exercise && (
            <Button
              color="error"
              onClick={() => setConfirmDelete(true)}
              disabled={isSubmitting}
            >
              Löschen
            </Button>
          )
        }
      >
        <TextField
          {...textFieldProps(register('name', nameRules))}
          label="Name"
          required
          fullWidth
          autoComplete="off"
          error={!!errors.name}
          helperText={errors.name?.message}
        />
        <Controller
          name="muscleGroupId"
          control={control}
          rules={{ required: 'Bitte Muskelgruppe wählen.' }}
          render={({ field: { ref, ...field } }) => (
            <TextField
              {...field}
              inputRef={ref}
              select
              label="Muskelgruppe"
              required
              fullWidth
              error={!!errors.muscleGroupId}
              helperText={errors.muscleGroupId?.message}
            >
              {muscleGroups.map((group) => (
                <MenuItem key={group.id} value={group.id}>
                  {group.name}
                </MenuItem>
              ))}
            </TextField>
          )}
        />
        <TextField
          {...textFieldProps(
            register('description', {
              maxLength: { value: 1000, message: 'Höchstens 1000 Zeichen.' },
            }),
          )}
          label="Beschreibung"
          multiline
          minRows={3}
          fullWidth
          error={!!errors.description}
          helperText={errors.description?.message ?? 'Freiwillig'}
        />
      </FormDialog>

      {confirmDelete && exercise && (
        <ConfirmDialog
          title="Übung löschen?"
          confirmLabel="Löschen"
          destructive
          errorMessages={{
            409: 'Die Übung kommt noch in einer Routine vor. Entferne sie zuerst aus allen Routinen.',
          }}
          onConfirm={handleDelete}
          onClose={() => setConfirmDelete(false)}
        >
          «{exercise.name}» verschwindet aus dem Katalog. Abgeschlossene
          Trainings im Verlauf bleiben vollständig.
        </ConfirmDialog>
      )}
    </>
  )
}
