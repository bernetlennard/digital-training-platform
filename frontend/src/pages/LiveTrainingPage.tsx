import ArrowBack from '@mui/icons-material/ArrowBack'
import Check from '@mui/icons-material/Check'
import {
  Alert,
  AppBar,
  Box,
  Button,
  Container,
  IconButton,
  Stack,
  TextField,
  Toolbar,
  Typography,
} from '@mui/material'
import { useEffect, useState } from 'react'
import { useForm } from 'react-hook-form'
import { Link as RouterLink, useNavigate } from 'react-router'
import { getErrorMessage } from '../api/client'
import { getExercises } from '../api/exercises'
import {
  cancelSession,
  finishSession,
  getActiveSession,
  updateNote,
} from '../api/sessions'
import type {
  IsoInstant,
  SessionExerciseResponse,
  SessionResponse,
} from '../api/types'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { EmptyState } from '../components/EmptyState'
import { emptyToNull, textFieldProps } from '../components/formFields'
import { SessionExerciseCard } from '../components/SessionExerciseCard'
import { formatDuration, formatStart } from '../format'
import { useNotify } from '../notifications/useNotify'

/**
 * Beschreibungen aus dem Übungskatalog nach Übungs-ID. Die Einheit selbst liefert nur die Namen.
 * Eine inzwischen gelöschte Übung fehlt im Katalog und erscheint dann ohne Beschreibung.
 */
async function loadDescriptions() {
  const exercises = await getExercises()
  return new Map(
    exercises.map((exercise) => [exercise.id, exercise.description]),
  )
}

/**
 * Live-Training (SAF8): Vollbild ohne Hauptnavigation. Lädt immer die aktive Einheit vom Server,
 * damit sie sich nach Neuladen, Gerätewechsel oder neuer Anmeldung fortsetzen lässt (#FA-5.9).
 */
export function LiveTrainingPage() {
  const session = useApiData(getActiveSession)
  // Getrennt geladen: Klappt das nicht, läuft das Training trotzdem, nur ohne Beschreibungen
  const descriptions = useApiData(loadDescriptions)

  const replaceExercise = (updated: SessionExerciseResponse) => {
    session.setData(
      (current) =>
        current && {
          ...current,
          exercises: current.exercises.map((exercise) =>
            exercise.id === updated.id ? updated : exercise,
          ),
        },
    )
  }

  return (
    <Box sx={{ minHeight: '100dvh' }}>
      <AppBar position="sticky">
        <Toolbar>
          <IconButton
            component={RouterLink}
            to="/"
            color="inherit"
            edge="start"
            aria-label="Zum Start, das Training läuft weiter"
            sx={{ mr: 1 }}
          >
            <ArrowBack />
          </IconButton>
          <Box sx={{ minWidth: 0 }}>
            <Typography variant="h6" component="h1" noWrap>
              {session.data?.routineName ?? 'Live-Training'}
            </Typography>
            {session.data?.startedAt && (
              <StartedInfo startedAt={session.data.startedAt} />
            )}
          </Box>
        </Toolbar>
      </AppBar>
      <Container maxWidth="sm" sx={{ py: 2 }}>
        <AsyncContent state={session}>
          {(active) =>
            active ? (
              <LiveTraining
                session={active}
                descriptions={descriptions.data}
                onExerciseChange={replaceExercise}
              />
            ) : (
              <EmptyState
                title="Kein Training aktiv"
                text="Starte ein geplantes Training oder eine Routine auf der Startseite."
                action={
                  <Button component={RouterLink} to="/" variant="contained">
                    Zum Start
                  </Button>
                }
              />
            )
          }
        </AsyncContent>
      </Container>
    </Box>
  )
}

/** Startzeit und bisherige Dauer, jede halbe Minute aktualisiert */
function StartedInfo({ startedAt }: { startedAt: IsoInstant }) {
  const [now, setNow] = useState(() => new Date().toISOString())
  useEffect(() => {
    const timer = setInterval(() => setNow(new Date().toISOString()), 30_000)
    return () => clearInterval(timer)
  }, [])

  return (
    <Typography variant="body2" sx={{ opacity: 0.8 }} noWrap>
      Gestartet {formatStart(startedAt)} · {formatDuration(startedAt, now)}
    </Typography>
  )
}

interface NoteFormValues {
  note: string
}

function noteHint(state: 'saving' | 'saved' | undefined, isDirty: boolean) {
  if (state === 'saving') return 'Wird gespeichert …'
  if (isDirty) return 'Wird beim Verlassen des Felds gespeichert'
  return state === 'saved' ? 'Gespeichert' : undefined
}

function LiveTraining({
  session,
  descriptions,
  onExerciseChange,
}: {
  session: SessionResponse
  descriptions: Map<number, string | null> | undefined
  onExerciseChange: (exercise: SessionExerciseResponse) => void
}) {
  const navigate = useNavigate()
  const notify = useNotify()
  const [confirm, setConfirm] = useState<'finish' | 'cancel'>()
  const [noteError, setNoteError] = useState<string>()
  const [noteState, setNoteState] = useState<'saving' | 'saved'>()
  const {
    register,
    handleSubmit,
    reset,
    getValues,
    getFieldState,
    formState: { errors, isDirty },
  } = useForm<NoteFormValues>({ defaultValues: { note: session.note ?? '' } })

  // Beim Fortsetzen ist die Übung aufgeklappt, an der zuletzt gearbeitet wurde, sonst die erste
  const [openExerciseId] = useState(
    () =>
      (
        session.exercises.findLast((exercise) => exercise.sets.length > 0) ??
        session.exercises[0]
      )?.id,
  )

  // reset macht den gespeicherten Text zum neuen Ausgangswert, danach ist das Feld nicht mehr «dirty»
  const saveNote = async (note: string) => {
    await updateNote({ note: emptyToNull(note) })
    reset({ note })
  }

  // Die Notiz speichert sich beim Verlassen des Felds. So geht sie auch nicht verloren, wenn man
  // zwischendurch zum Start wechselt oder das Handy sperrt.
  const onSaveNote = async ({ note }: NoteFormValues) => {
    if (!getFieldState('note').isDirty) return
    setNoteError(undefined)
    setNoteState('saving')
    try {
      await saveNote(note)
      setNoteState('saved')
    } catch (error) {
      setNoteError(getErrorMessage(error))
      setNoteState(undefined)
    }
  }

  const handleFinish = async () => {
    // Eine noch nicht gespeicherte Notiz geht mit
    if (getFieldState('note').isDirty) {
      await saveNote(getValues('note'))
    }
    await finishSession()
    notify('Training abgeschlossen')
    navigate(`/verlauf/einheit/${session.id}`, { replace: true })
  }

  const handleCancel = async () => {
    await cancelSession()
    notify('Training abgebrochen')
    navigate('/', { replace: true })
  }

  return (
    <Stack spacing={3}>
      <Box>
        {session.exercises.map((exercise) => (
          <SessionExerciseCard
            key={exercise.id}
            exercise={exercise}
            description={descriptions?.get(exercise.exerciseId)}
            defaultExpanded={exercise.id === openExerciseId}
            onChange={onExerciseChange}
          />
        ))}
      </Box>

      <Stack spacing={1}>
        {noteError && <Alert severity="error">{noteError}</Alert>}
        <TextField
          {...textFieldProps(
            register('note', {
              maxLength: { value: 1000, message: 'Höchstens 1000 Zeichen.' },
              onBlur: handleSubmit(onSaveNote),
            }),
          )}
          label="Notiz zum Training"
          multiline
          minRows={2}
          fullWidth
          error={!!errors.note}
          helperText={errors.note?.message ?? noteHint(noteState, isDirty)}
        />
      </Stack>

      <Stack spacing={1}>
        <Button
          variant="contained"
          size="large"
          startIcon={<Check />}
          onClick={() => setConfirm('finish')}
          fullWidth
        >
          Abschliessen
        </Button>
        <Button
          variant="outlined"
          color="error"
          size="large"
          onClick={() => setConfirm('cancel')}
          fullWidth
        >
          Abbrechen
        </Button>
      </Stack>

      {confirm === 'finish' && (
        <ConfirmDialog
          title="Training abschliessen?"
          confirmLabel="Abschliessen"
          cancelLabel="Weiter trainieren"
          errorMessages={{
            409: 'Erfasse zuerst mindestens einen Satz.',
          }}
          onConfirm={handleFinish}
          onClose={() => setConfirm(undefined)}
        >
          Übungen ohne Satz werden nicht gespeichert. Danach lässt sich das
          Training nicht mehr ändern.
        </ConfirmDialog>
      )}
      {confirm === 'cancel' && (
        <ConfirmDialog
          title="Training abbrechen?"
          confirmLabel="Training abbrechen"
          cancelLabel="Weiter trainieren"
          destructive
          onConfirm={handleCancel}
          onClose={() => setConfirm(undefined)}
        >
          Alle erfassten Sätze werden verworfen. Ein geplantes Training bleibt
          eingeplant.
        </ConfirmDialog>
      )}
    </Stack>
  )
}
