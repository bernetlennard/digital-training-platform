import Delete from '@mui/icons-material/Delete'
import Edit from '@mui/icons-material/Edit'
import ExpandMore from '@mui/icons-material/ExpandMore'
import Save from '@mui/icons-material/Save'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Button,
  IconButton,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { Controller, useForm } from 'react-hook-form'
import { getErrorMessage } from '../api/client'
import { addSet, deleteSet, moveSet, updateSet } from '../api/sessions'
import type { SessionExerciseResponse, SetResponse } from '../api/types'
import { countLabel, formatSet } from '../format'
import { useNotify } from '../notifications/useNotify'
import { ConfirmDialog } from './ConfirmDialog'
import { FormDialog } from './FormDialog'
import { SetFields } from './SetFields'
import { toSetFormValues, toSetRequest, type SetFormValues } from './setForm'

const SET_ERRORS = {
  404: 'Das Training ist nicht mehr aktiv. Lade die Ansicht neu.',
  422: 'Diese Position gibt es nicht.',
}

interface SessionExerciseCardProps {
  exercise: SessionExerciseResponse
  defaultExpanded: boolean
  /** Übernimmt die Antwort des Servers: alle Sätze und der neue Vorschlag */
  onChange: (exercise: SessionExerciseResponse) => void
}

/**
 * Eine Übung im Live-Training. Aufgeklappt zeigt sie die erfassten Sätze mit Ändern und Löschen
 * und darunter die Eingabe für den nächsten Satz.
 */
export function SessionExerciseCard({
  exercise,
  defaultExpanded,
  onChange,
}: SessionExerciseCardProps) {
  const notify = useNotify()
  const [editing, setEditing] = useState<SetResponse>()
  const [deleting, setDeleting] = useState<SetResponse>()

  const handleDelete = async (set: SetResponse) => {
    onChange(await deleteSet(set.id))
    notify(`Satz ${set.setNumber} gelöscht`)
    setDeleting(undefined)
  }

  return (
    <>
      <Accordion defaultExpanded={defaultExpanded} disableGutters>
        <AccordionSummary expandIcon={<ExpandMore />}>
          <Stack sx={{ minWidth: 0 }}>
            <Typography
              variant="subtitle1"
              component="h2"
              sx={{ overflowWrap: 'anywhere' }}
            >
              {exercise.exerciseName}
            </Typography>
            <Typography variant="body2" color="text.secondary">
              {exercise.sets.length === 0
                ? 'Noch kein Satz'
                : countLabel(exercise.sets.length, 'Satz', 'Sätze')}
            </Typography>
          </Stack>
        </AccordionSummary>
        <AccordionDetails>
          <Stack spacing={2}>
            {exercise.sets.length > 0 && (
              <List disablePadding dense>
                {exercise.sets.map((set) => (
                  <ListItem
                    key={set.id}
                    disableGutters
                    divider
                    secondaryAction={
                      <Stack direction="row">
                        <IconButton
                          aria-label={`Satz ${set.setNumber} ändern`}
                          onClick={() => setEditing(set)}
                        >
                          <Edit />
                        </IconButton>
                        <IconButton
                          aria-label={`Satz ${set.setNumber} löschen`}
                          edge="end"
                          onClick={() => setDeleting(set)}
                        >
                          <Delete />
                        </IconButton>
                      </Stack>
                    }
                    sx={{ pr: 12 }}
                  >
                    <ListItemText
                      primary={`Satz ${set.setNumber}`}
                      secondary={formatSet(set)}
                    />
                  </ListItem>
                ))}
              </List>
            )}

            {/* Neu aufbauen, sobald sich die Anzahl Sätze ändert, damit der neue Vorschlag drinsteht */}
            <NewSetForm
              key={exercise.sets.length}
              exercise={exercise}
              onSaved={(updated) => {
                onChange(updated)
                notify(`Satz ${exercise.sets.length + 1} gespeichert`)
              }}
            />
          </Stack>
        </AccordionDetails>
      </Accordion>

      {editing && (
        <EditSetDialog
          set={editing}
          setCount={exercise.sets.length}
          onChange={onChange}
          onClose={() => setEditing(undefined)}
        />
      )}
      {deleting && (
        <ConfirmDialog
          title={`Satz ${deleting.setNumber} löschen?`}
          confirmLabel="Löschen"
          destructive
          errorMessages={SET_ERRORS}
          onConfirm={() => handleDelete(deleting)}
          onClose={() => setDeleting(undefined)}
        >
          {formatSet(deleting)}. Die folgenden Sätze rücken nach.
        </ConfirmDialog>
      )}
    </>
  )
}

/**
 * Eingabe für den nächsten Satz, vorbelegt mit dem Vorschlag (#FA-5.5). Mit unveränderten Werten
 * genügt ein Tipp auf «Satz speichern» (#NFA-5.3). Schlägt das Speichern fehl, bleibt die Eingabe
 * stehen und lässt sich erneut senden (#NFA-5.2).
 */
function NewSetForm({
  exercise,
  onSaved,
}: {
  exercise: SessionExerciseResponse
  onSaved: (exercise: SessionExerciseResponse) => void
}) {
  const [saveError, setSaveError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SetFormValues>({
    defaultValues: toSetFormValues(exercise.nextSet),
  })

  const onSubmit = async (values: SetFormValues) => {
    setSaveError(undefined)
    try {
      onSaved(await addSet(exercise.id, toSetRequest(values)))
    } catch (error) {
      setSaveError(
        getErrorMessage(error, {
          ...SET_ERRORS,
          0: 'Keine Verbindung. Der Satz ist noch nicht gespeichert, bitte erneut senden.',
        }),
      )
    }
  }

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={1}>
        {saveError && <Alert severity="error">{saveError}</Alert>}
        <SetFields register={register} errors={errors} />
        <Button
          type="submit"
          variant="contained"
          size="large"
          startIcon={<Save />}
          loading={isSubmitting}
          fullWidth
        >
          Satz {exercise.sets.length + 1} speichern
        </Button>
      </Stack>
    </form>
  )
}

interface EditSetFormValues extends SetFormValues {
  setNumber: number
}

/** Satz ändern und verschieben (#FA-5.4) */
function EditSetDialog({
  set,
  setCount,
  onChange,
  onClose,
}: {
  set: SetResponse
  setCount: number
  onChange: (exercise: SessionExerciseResponse) => void
  onClose: () => void
}) {
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting, dirtyFields },
  } = useForm<EditSetFormValues>({
    defaultValues: { ...toSetFormValues(set), setNumber: set.setNumber },
  })

  const onSubmit = async (values: EditSetFormValues) => {
    const valuesChanged =
      dirtyFields.weightKg || dirtyFields.repetitions || dirtyFields.restSeconds
    const newSetNumber = Number(values.setNumber)
    if (!valuesChanged && newSetNumber === set.setNumber) {
      onClose()
      return
    }
    setSubmitError(undefined)
    try {
      // Erst die Werte, dann die Position. Jede Antwort wird gleich übernommen, damit die
      // Anzeige auch dann stimmt, wenn nur der erste Schritt klappt.
      if (valuesChanged) {
        onChange(await updateSet(set.id, toSetRequest(values)))
      }
      if (newSetNumber !== set.setNumber) {
        onChange(await moveSet(set.id, { newSetNumber }))
      }
      notify('Satz gespeichert')
      onClose()
    } catch (error) {
      setSubmitError(getErrorMessage(error, SET_ERRORS))
    }
  }

  return (
    <FormDialog
      title={`Satz ${set.setNumber} ändern`}
      isSubmitting={isSubmitting}
      error={submitError}
      onSubmit={handleSubmit(onSubmit)}
      onClose={onClose}
    >
      <SetFields register={register} errors={errors} />
      {setCount > 1 && (
        <Controller
          name="setNumber"
          control={control}
          render={({ field: { ref, ...field } }) => (
            <TextField
              {...field}
              inputRef={ref}
              select
              label="Position"
              fullWidth
              helperText="An diese Stelle verschieben"
            >
              {Array.from({ length: setCount }, (_, index) => index + 1).map(
                (number) => (
                  <MenuItem key={number} value={number}>
                    Satz {number}
                  </MenuItem>
                ),
              )}
            </TextField>
          )}
        />
      )}
    </FormDialog>
  )
}
