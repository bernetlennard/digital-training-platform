import Delete from '@mui/icons-material/Delete'
import Edit from '@mui/icons-material/Edit'
import EventAvailable from '@mui/icons-material/EventAvailable'
import PlayArrow from '@mui/icons-material/PlayArrow'
import {
  Alert,
  Box,
  Button,
  Card,
  CardActions,
  CardContent,
  Chip,
  IconButton,
  Stack,
  Typography,
} from '@mui/material'
import { useState } from 'react'
import { Link as RouterLink, useNavigate } from 'react-router'
import { getErrorMessage } from '../api/client'
import { getRoutines } from '../api/routines'
import {
  deletePlannedSession,
  getActiveSession,
  getPlannedSessions,
  START_ERROR_MESSAGES,
  startPlannedSession,
} from '../api/sessions'
import type { PlannedSessionResponse, SessionResponse } from '../api/types'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { EmptyState } from '../components/EmptyState'
import { PageHeader } from '../components/PageHeader'
import {
  PlanSessionDialog,
  StartSessionDialog,
} from '../components/SessionDialogs'
import {
  byName,
  formatDate,
  formatStart,
  formatWeekdayDate,
  todayIsoDate,
} from '../format'
import { useNotify } from '../notifications/useNotify'
import { NEW_ROUTINE_ID } from '../routes'

async function loadStart() {
  const [active, planned, routines] = await Promise.all([
    getActiveSession(),
    getPlannedSessions(),
    getRoutines(),
  ])
  return { active, planned, routines: routines.toSorted(byName) }
}

type DialogState =
  | { type: 'plan'; planned?: PlannedSessionResponse }
  | { type: 'start' }
  | { type: 'delete'; planned: PlannedSessionResponse }

/**
 * Start nach der Anmeldung (SAF7, SAF8): laufendes Training fortsetzen, geplante Trainings
 * mit einem Tipp starten (#NFA-4.1), einplanen oder spontan starten.
 */
export function StartPage() {
  const navigate = useNavigate()
  const notify = useNotify()
  const start = useApiData(loadStart)
  const [today] = useState(todayIsoDate)
  const [dialog, setDialog] = useState<DialogState>()
  const [startingId, setStartingId] = useState<number>()

  const handleStart = async (planned: PlannedSessionResponse) => {
    setStartingId(planned.id)
    try {
      await startPlannedSession(planned.id)
      navigate('/training')
    } catch (error) {
      notify(getErrorMessage(error, START_ERROR_MESSAGES), 'error')
      setStartingId(undefined)
      start.reload()
    }
  }

  const handleDelete = async (planned: PlannedSessionResponse) => {
    await deletePlannedSession(planned.id)
    notify('Geplantes Training gelöscht')
    setDialog(undefined)
    start.reload()
  }

  return (
    <>
      <PageHeader title="Start" />
      <AsyncContent state={start}>
        {({ active, planned, routines }) => {
          const hasRoutines = routines.length > 0
          // Das Backend sortiert nach Datum, vergangene stehen also zuoberst
          const nextId = planned.find((item) => item.plannedDate >= today)?.id

          return (
            <Stack spacing={3}>
              {active && <ActiveSessionCard session={active} />}

              {hasRoutines ? (
                // Breite nach Inhalt, damit «Spontan starten» auch auf 360 Pixel einzeilig bleibt
                <Stack direction="row" spacing={1}>
                  <Button
                    variant="outlined"
                    startIcon={<EventAvailable />}
                    onClick={() => setDialog({ type: 'plan' })}
                    sx={{ flex: '1 1 auto' }}
                  >
                    Einplanen
                  </Button>
                  <Button
                    variant="outlined"
                    startIcon={<PlayArrow />}
                    onClick={() => setDialog({ type: 'start' })}
                    disabled={!!active}
                    sx={{ flex: '1 1 auto' }}
                  >
                    Spontan starten
                  </Button>
                </Stack>
              ) : (
                <Alert
                  severity="info"
                  action={
                    <Button
                      color="inherit"
                      component={RouterLink}
                      to={`/routinen/${NEW_ROUTINE_ID}`}
                    >
                      Routine erstellen
                    </Button>
                  }
                >
                  Erstelle zuerst eine Routine, um zu trainieren.
                </Alert>
              )}

              <Stack spacing={1} component="section">
                <Typography variant="h6" component="h2">
                  Geplante Trainings
                </Typography>
                {planned.length === 0 ? (
                  <EmptyState
                    title="Keine Trainings geplant"
                    text={
                      hasRoutines
                        ? 'Plane dein nächstes Training mit «Einplanen».'
                        : undefined
                    }
                  />
                ) : (
                  planned.map((item) => {
                    const isPast = item.plannedDate < today
                    const isNext = item.id === nextId
                    return (
                      <Card key={item.id} variant="outlined">
                        <CardContent sx={{ pb: 0 }}>
                          <Stack
                            direction="row"
                            spacing={1}
                            sx={{ alignItems: 'center', flexWrap: 'wrap' }}
                          >
                            <Typography variant="subtitle1" component="h3">
                              {formatWeekdayDate(item.plannedDate)}
                            </Typography>
                            {isPast && (
                              <Chip
                                size="small"
                                color="warning"
                                label="vergangen"
                              />
                            )}
                            {isNext && (
                              <Chip
                                size="small"
                                color="primary"
                                label={
                                  item.plannedDate === today
                                    ? 'heute'
                                    : 'nächstes'
                                }
                              />
                            )}
                          </Stack>
                          <Typography
                            color="text.secondary"
                            sx={{ overflowWrap: 'anywhere' }}
                          >
                            {item.routineName}
                          </Typography>
                        </CardContent>
                        <CardActions>
                          {/* Nur das nächste Training ist hervorgehoben, eine Hauptaktion pro Ansicht (#NFA-7.3) */}
                          <Button
                            variant={
                              isNext && !active ? 'contained' : 'outlined'
                            }
                            startIcon={<PlayArrow />}
                            loading={startingId === item.id}
                            disabled={!!active || startingId !== undefined}
                            onClick={() => handleStart(item)}
                          >
                            Starten
                          </Button>
                          <Box sx={{ flexGrow: 1 }} />
                          {/* Vergangene Trainings lassen sich nur noch löschen (SAF7) */}
                          {!isPast && (
                            <IconButton
                              aria-label={`Training vom ${formatDate(item.plannedDate)} ändern`}
                              onClick={() =>
                                setDialog({ type: 'plan', planned: item })
                              }
                            >
                              <Edit />
                            </IconButton>
                          )}
                          <IconButton
                            aria-label={`Training vom ${formatDate(item.plannedDate)} löschen`}
                            onClick={() =>
                              setDialog({ type: 'delete', planned: item })
                            }
                          >
                            <Delete />
                          </IconButton>
                        </CardActions>
                      </Card>
                    )
                  })
                )}
              </Stack>

              {dialog?.type === 'plan' && (
                <PlanSessionDialog
                  routines={routines}
                  planned={dialog.planned}
                  onClose={() => setDialog(undefined)}
                  onSaved={() => {
                    setDialog(undefined)
                    start.reload()
                  }}
                />
              )}
              {dialog?.type === 'start' && (
                <StartSessionDialog
                  routines={routines}
                  onClose={() => setDialog(undefined)}
                />
              )}
              {dialog?.type === 'delete' && (
                <ConfirmDialog
                  title="Geplantes Training löschen?"
                  confirmLabel="Löschen"
                  destructive
                  onConfirm={() => handleDelete(dialog.planned)}
                  onClose={() => setDialog(undefined)}
                >
                  «{dialog.planned.routineName}» am{' '}
                  {formatDate(dialog.planned.plannedDate)} wird aus der Planung
                  entfernt.
                </ConfirmDialog>
              )}
            </Stack>
          )
        }}
      </AsyncContent>
    </>
  )
}

/** Laufendes Training zuoberst, damit es sich nach Neuladen oder Gerätewechsel fortsetzen lässt (#FA-5.9) */
function ActiveSessionCard({ session }: { session: SessionResponse }) {
  return (
    <Card
      variant="outlined"
      sx={{ borderColor: 'primary.main', borderWidth: 2 }}
    >
      <CardContent>
        <Typography variant="overline" color="primary">
          Training läuft
        </Typography>
        <Typography
          variant="h6"
          component="h2"
          sx={{ overflowWrap: 'anywhere' }}
        >
          {session.routineName}
        </Typography>
        {session.startedAt && (
          <Typography color="text.secondary">
            Gestartet {formatStart(session.startedAt)}
          </Typography>
        )}
      </CardContent>
      <CardActions sx={{ px: 2, pb: 2 }}>
        <Button
          component={RouterLink}
          to="/training"
          variant="contained"
          size="large"
          startIcon={<PlayArrow />}
          fullWidth
        >
          Fortsetzen
        </Button>
      </CardActions>
    </Card>
  )
}
