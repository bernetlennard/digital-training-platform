import CheckCircle from '@mui/icons-material/CheckCircle'
import Edit from '@mui/icons-material/Edit'
import ExpandMore from '@mui/icons-material/ExpandMore'
import Save from '@mui/icons-material/Save'
import {
  Accordion,
  AccordionDetails,
  AccordionSummary,
  Alert,
  Button,
  List,
  ListItem,
  ListItemButton,
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
  /** Übernimmt die Antwort des Servers: alle Sätze und die offenen Vorschläge */
  onChange: (exercise: SessionExerciseResponse) => void
}

/**
 * Eine Übung im Live-Training. Aufgeklappt zeigt sie die erfassten Sätze, darunter die Eingabe
 * für den nächsten Satz und die noch offenen Sätze aus dem letzten Training.
 */
export function SessionExerciseCard({
  exercise,
  defaultExpanded,
  onChange,
}: SessionExerciseCardProps) {
  const notify = useNotify()
  const [editing, setEditing] = useState<SetResponse>()
  const [deleting, setDeleting] = useState<SetResponse>()
  const isDone = exercise.sets.length > 0 && exercise.suggestedSets.length === 0

  const handleDelete = async (set: SetResponse) => {
    onChange(await deleteSet(set.id))
    notify(`Satz ${set.setNumber} gelöscht`)
    setDeleting(undefined)
  }

  return (
    <>
      {/* MUI legt die Zusammenfassung in eine Überschrift (sonst h3), darin keine zweite */}
      <Accordion
        defaultExpanded={defaultExpanded}
        disableGutters
        slotProps={{ heading: { component: 'h2' } }}
      >
        <AccordionSummary expandIcon={<ExpandMore />}>
          <Stack sx={{ minWidth: 0 }}>
            <Typography
              variant="subtitle1"
              component="span"
              sx={{ overflowWrap: 'anywhere' }}
            >
              {exercise.exerciseName}
            </Typography>
            <Stack direction="row" spacing={0.5} sx={{ alignItems: 'center' }}>
              {/* Erledigt, sobald Sätze erfasst und keine Vorschläge mehr offen sind */}
              {isDone && (
                <CheckCircle
                  color="success"
                  sx={{ fontSize: 18 }}
                  aria-hidden
                />
              )}
              <Typography
                variant="body2"
                color={isDone ? 'success.main' : 'text.secondary'}
              >
                {progressLabel(exercise)}
              </Typography>
            </Stack>
          </Stack>
        </AccordionSummary>
        <AccordionDetails>
          <Stack spacing={2}>
            {/* Antippen öffnet Ändern, Verschieben und Löschen (SAF8). Die ganze Zeile ist das
                Touch-Ziel, und Löschen liegt nicht direkt neben Ändern. */}
            {exercise.sets.length > 0 && (
              <List disablePadding dense>
                {exercise.sets.map((set) => (
                  <ListItemButton
                    key={set.id}
                    divider
                    aria-label={`Satz ${set.setNumber} ändern: ${formatSet(set)}`}
                    onClick={() => setEditing(set)}
                    sx={{ px: 0 }}
                  >
                    <ListItemText
                      primary={`Satz ${set.setNumber}`}
                      secondary={formatSet(set)}
                    />
                    <Edit fontSize="small" color="action" />
                  </ListItemButton>
                ))}
              </List>
            )}

            {/* Neu aufbauen, sobald sich die Anzahl Sätze ändert, damit der nächste Vorschlag drinsteht */}
            <NewSetForm
              key={exercise.sets.length}
              exercise={exercise}
              onSaved={(updated) => {
                onChange(updated)
                notify(`Satz ${exercise.sets.length + 1} gespeichert`)
              }}
            />

            {/* Die übrigen Sätze aus dem letzten Training, erst mit «Speichern» erfasst */}
            {exercise.suggestedSets.length > 1 && (
              <List disablePadding dense>
                {exercise.suggestedSets.slice(1).map((suggestion) => (
                  <ListItem key={suggestion.setNumber} disableGutters divider>
                    <ListItemText
                      primary={`Satz ${suggestion.setNumber} · offen`}
                      secondary={formatSet(suggestion)}
                      slotProps={{ primary: { color: 'text.secondary' } }}
                    />
                  </ListItem>
                ))}
              </List>
            )}
          </Stack>
        </AccordionDetails>
      </Accordion>

      {editing && (
        <EditSetDialog
          set={editing}
          setCount={exercise.sets.length}
          onChange={onChange}
          onDelete={() => {
            setEditing(undefined)
            setDeleting(editing)
          }}
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

/** «Noch kein Satz», «2 Sätze» oder, solange Vorschläge offen sind, «1 von 3 Sätzen» */
function progressLabel({ sets, suggestedSets }: SessionExerciseResponse) {
  if (suggestedSets.length > 0) {
    const total = sets.length + suggestedSets.length
    return `${sets.length} von ${countLabel(total, 'Satz', 'Sätzen')}`
  }
  return sets.length === 0
    ? 'Noch kein Satz'
    : countLabel(sets.length, 'Satz', 'Sätze')
}

/**
 * Eingabe für den nächsten Satz, vorbelegt mit dem ersten offenen Satz aus dem letzten Training
 * dieser Routine, sonst leer (#FA-5.5). Mit unveränderten Werten genügt ein Tipp auf
 * «Satz speichern» (#NFA-5.3). Schlägt das Speichern fehl, bleibt die Eingabe stehen und lässt
 * sich erneut senden (#NFA-5.2).
 */
function NewSetForm({
  exercise,
  onSaved,
}: {
  exercise: SessionExerciseResponse
  onSaved: (exercise: SessionExerciseResponse) => void
}) {
  const suggestion = exercise.suggestedSets.at(0)
  const [saveError, setSaveError] = useState<string>()
  const {
    register,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<SetFormValues>({
    defaultValues: toSetFormValues(suggestion),
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
        {suggestion && (
          <Typography variant="body2" color="text.secondary">
            Werte vom letzten Training
          </Typography>
        )}
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

/** Satz ändern, verschieben (#FA-5.4) oder löschen */
function EditSetDialog({
  set,
  setCount,
  onChange,
  onDelete,
  onClose,
}: {
  set: SetResponse
  setCount: number
  onChange: (exercise: SessionExerciseResponse) => void
  /** Öffnet die Rückfrage zum Löschen (#NFA-7.5) */
  onDelete: () => void
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
      secondaryAction={
        <Button color="error" onClick={onDelete} disabled={isSubmitting}>
          Löschen
        </Button>
      }
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
