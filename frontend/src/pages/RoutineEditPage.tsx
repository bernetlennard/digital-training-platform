import Add from '@mui/icons-material/Add'
import ArrowDownward from '@mui/icons-material/ArrowDownward'
import ArrowUpward from '@mui/icons-material/ArrowUpward'
import Close from '@mui/icons-material/Close'
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormHelperText,
  IconButton,
  Link,
  List,
  ListItem,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
  TextField,
  Typography,
} from '@mui/material'
import { useCallback, useState } from 'react'
import { useFieldArray, useForm } from 'react-hook-form'
import { Link as RouterLink, useNavigate, useParams } from 'react-router'
import { getErrorMessage, parseId } from '../api/client'
import { getExercises } from '../api/exercises'
import {
  createRoutine,
  deleteRoutine,
  getRoutine,
  updateRoutine,
} from '../api/routines'
import type { ExerciseResponse, RoutineResponse } from '../api/types'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { nameRules, textFieldProps } from '../components/formFields'
import { PageHeader } from '../components/PageHeader'
import { byName } from '../format'
import { useNotify } from '../notifications/useNotify'
import { NEW_ROUTINE_ID } from '../routes'

interface RoutineFormValues {
  name: string
  exercises: { exerciseId: number; name: string; muscleGroupName: string }[]
}

async function loadEditor(routineId: string | undefined) {
  const routinePromise =
    routineId === NEW_ROUTINE_ID
      ? Promise.resolve(undefined)
      : getRoutine(parseId(routineId))
  const [catalog, routine] = await Promise.all([getExercises(), routinePromise])
  return { catalog: catalog.toSorted(byName), routine }
}

/** Routine zusammenstellen oder bearbeiten (SAF6). /routinen/neu legt eine neue an. */
export function RoutineEditPage() {
  const { routineId } = useParams()
  const load = useCallback(() => loadEditor(routineId), [routineId])
  const editor = useApiData(load)
  const isNew = routineId === NEW_ROUTINE_ID

  return (
    <>
      <PageHeader
        title={isNew ? 'Neue Routine' : 'Routine bearbeiten'}
        backTo="/routinen"
      />
      <AsyncContent
        state={editor}
        errorMessages={{ 404: 'Die Routine wurde nicht gefunden.' }}
      >
        {({ catalog, routine }) => (
          <RoutineForm catalog={catalog} routine={routine} />
        )}
      </AsyncContent>
    </>
  )
}

function RoutineForm({
  catalog,
  routine,
}: {
  catalog: ExerciseResponse[]
  routine: RoutineResponse | undefined
}) {
  const navigate = useNavigate()
  const notify = useNotify()
  const [submitError, setSubmitError] = useState<string>()
  const [isPickerOpen, setIsPickerOpen] = useState(false)
  const [confirmDelete, setConfirmDelete] = useState(false)
  const {
    register,
    control,
    handleSubmit,
    formState: { errors, isSubmitting },
  } = useForm<RoutineFormValues>({
    defaultValues: {
      name: routine?.name ?? '',
      exercises:
        routine?.exercises.map(({ exerciseId, name, muscleGroupName }) => ({
          exerciseId,
          name,
          muscleGroupName,
        })) ?? [],
    },
  })
  const { fields, append, remove, swap } = useFieldArray({
    control,
    name: 'exercises',
    // Mindestens eine Übung (#FA-3.1)
    rules: {
      validate: (exercises) =>
        exercises.length > 0 || 'Bitte mindestens eine Übung hinzufügen.',
    },
  })

  // Jede Übung höchstens einmal (#FA-3.1), darum nur die noch nicht gewählten anbieten
  const available = catalog.filter(
    (exercise) => !fields.some((field) => field.exerciseId === exercise.id),
  )

  const onSubmit = async (values: RoutineFormValues) => {
    setSubmitError(undefined)
    const request = {
      name: values.name.trim(),
      // Die Reihenfolge der Liste ist die Position in der Routine
      exerciseIds: values.exercises.map((exercise) => exercise.exerciseId),
    }
    try {
      if (routine) {
        await updateRoutine(routine.id, request)
      } else {
        await createRoutine(request)
      }
      notify(routine ? 'Routine gespeichert' : 'Routine erstellt')
      navigate('/routinen')
    } catch (error) {
      setSubmitError(
        getErrorMessage(error, {
          404: 'Eine der Übungen wurde inzwischen gelöscht. Lade die Ansicht neu.',
          409: 'Es gibt bereits eine Routine mit diesem Namen.',
          422: 'Jede Übung darf nur einmal in der Routine vorkommen.',
        }),
      )
    }
  }

  const handleDelete = async () => {
    if (!routine) return
    await deleteRoutine(routine.id)
    notify('Routine gelöscht')
    navigate('/routinen', { replace: true })
  }

  return (
    <form noValidate onSubmit={handleSubmit(onSubmit)}>
      <Stack spacing={3}>
        {submitError && <Alert severity="error">{submitError}</Alert>}

        <TextField
          {...textFieldProps(register('name', nameRules))}
          label="Name"
          required
          fullWidth
          autoComplete="off"
          error={!!errors.name}
          helperText={errors.name?.message}
        />

        <Stack spacing={1}>
          <Typography variant="h6" component="h2">
            Übungen
          </Typography>
          {fields.length > 0 && (
            <Paper variant="outlined">
              <List disablePadding>
                {fields.map((field, index) => (
                  <ListItem
                    key={field.id}
                    divider={index < fields.length - 1}
                    secondaryAction={
                      <Stack direction="row">
                        <IconButton
                          aria-label={`${field.name} nach oben`}
                          disabled={index === 0}
                          onClick={() => swap(index, index - 1)}
                        >
                          <ArrowUpward />
                        </IconButton>
                        <IconButton
                          aria-label={`${field.name} nach unten`}
                          disabled={index === fields.length - 1}
                          onClick={() => swap(index, index + 1)}
                        >
                          <ArrowDownward />
                        </IconButton>
                        <IconButton
                          aria-label={`${field.name} entfernen`}
                          edge="end"
                          onClick={() => remove(index)}
                        >
                          <Close />
                        </IconButton>
                      </Stack>
                    }
                    // Platz für die drei Knöpfe rechts
                    sx={{ pr: 19 }}
                  >
                    <ListItemText
                      primary={`${index + 1}. ${field.name}`}
                      secondary={field.muscleGroupName}
                      sx={{ overflowWrap: 'anywhere' }}
                    />
                  </ListItem>
                ))}
              </List>
            </Paper>
          )}
          {errors.exercises?.root?.message && (
            <FormHelperText error>
              {errors.exercises.root.message}
            </FormHelperText>
          )}
          {catalog.length === 0 ? (
            <Alert severity="info">
              Dein Übungskatalog ist noch leer.{' '}
              <Link component={RouterLink} to="/uebungen">
                Erfasse zuerst Übungen.
              </Link>
            </Alert>
          ) : (
            <Button
              variant="outlined"
              startIcon={<Add />}
              onClick={() => setIsPickerOpen(true)}
              disabled={available.length === 0}
              sx={{ alignSelf: 'flex-start' }}
            >
              Übung hinzufügen
            </Button>
          )}
        </Stack>

        <Stack direction="row" spacing={1} sx={{ justifyContent: 'flex-end' }}>
          {routine && (
            <Button
              color="error"
              onClick={() => setConfirmDelete(true)}
              disabled={isSubmitting}
              sx={{ mr: 'auto' }}
            >
              Löschen
            </Button>
          )}
          <Button
            type="submit"
            variant="contained"
            size="large"
            loading={isSubmitting}
          >
            Speichern
          </Button>
        </Stack>
      </Stack>

      {isPickerOpen && (
        <ExercisePicker
          exercises={available}
          onPick={(exercise) => {
            append({
              exerciseId: exercise.id,
              name: exercise.name,
              muscleGroupName: exercise.muscleGroupName,
            })
            setIsPickerOpen(false)
          }}
          onClose={() => setIsPickerOpen(false)}
        />
      )}

      {confirmDelete && routine && (
        <ConfirmDialog
          title="Routine löschen?"
          confirmLabel="Löschen"
          destructive
          onConfirm={handleDelete}
          onClose={() => setConfirmDelete(false)}
        >
          Alle geplanten Trainings mit «{routine.name}» werden ebenfalls
          gelöscht. Abgeschlossene Trainings bleiben im Verlauf.
        </ConfirmDialog>
      )}
    </form>
  )
}

/** Auswahl einer Übung aus dem Katalog */
function ExercisePicker({
  exercises,
  onPick,
  onClose,
}: {
  exercises: ExerciseResponse[]
  onPick: (exercise: ExerciseResponse) => void
  onClose: () => void
}) {
  return (
    <Dialog open onClose={onClose} fullWidth maxWidth="xs" scroll="paper">
      <DialogTitle>Übung hinzufügen</DialogTitle>
      <DialogContent dividers sx={{ p: 0 }}>
        <List disablePadding>
          {exercises.map((exercise) => (
            <ListItemButton
              key={exercise.id}
              divider
              onClick={() => onPick(exercise)}
            >
              <ListItemText
                primary={exercise.name}
                secondary={exercise.muscleGroupName}
              />
            </ListItemButton>
          ))}
        </List>
      </DialogContent>
      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
      </DialogActions>
    </Dialog>
  )
}
