import Delete from '@mui/icons-material/Delete'
import {
  Box,
  Button,
  Chip,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  Typography,
} from '@mui/material'
import { useCallback, useState } from 'react'
import { useNavigate, useParams } from 'react-router'
import { parseId } from '../api/client'
import { deleteHistorySession, getHistorySession } from '../api/history'
import type { SessionExerciseResponse, SessionResponse } from '../api/types'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { ConfirmDialog } from '../components/ConfirmDialog'
import { PageHeader } from '../components/PageHeader'
import { formatDateTime, formatDuration, formatWeight } from '../format'
import { useNotify } from '../notifications/useNotify'

export function SessionDetailPage() {
  const { sessionId } = useParams()
  const load = useCallback(
    () => getHistorySession(parseId(sessionId)),
    [sessionId],
  )
  const session = useApiData(load)
  const routineId = session.data?.routineId

  return (
    <>
      <PageHeader
        title="Training"
        backTo={routineId ? `/verlauf/${routineId}` : '/verlauf'}
      />
      <AsyncContent
        state={session}
        errorMessages={{ 404: 'Das Training wurde nicht gefunden.' }}
      >
        {(detail) => <SessionDetail session={detail} />}
      </AsyncContent>
    </>
  )
}

function SessionDetail({ session }: { session: SessionResponse }) {
  const navigate = useNavigate()
  const notify = useNotify()
  const [confirmDelete, setConfirmDelete] = useState(false)

  const handleDelete = async () => {
    await deleteHistorySession(session.id)
    notify('Training aus dem Verlauf gelöscht')
    navigate(`/verlauf/${session.routineId}`, { replace: true })
  }

  return (
    <Stack spacing={3}>
      <Box>
        <Stack
          direction="row"
          spacing={1}
          sx={{ alignItems: 'center', flexWrap: 'wrap' }}
        >
          <Typography
            variant="h6"
            component="h2"
            sx={{ overflowWrap: 'anywhere' }}
          >
            {session.routineName}
          </Typography>
          {session.routineDeleted && <Chip size="small" label="gelöscht" />}
        </Stack>
        {session.startedAt && (
          <Typography color="text.secondary">
            {formatDateTime(session.startedAt)} Uhr
            {session.finishedAt &&
              ` · ${formatDuration(session.startedAt, session.finishedAt)}`}
          </Typography>
        )}
      </Box>

      {/* Ausgelassene Übungen fehlen, darum fortlaufend nummerieren statt mit position */}
      {session.exercises.map((exercise, index) => (
        <ExerciseSets
          key={exercise.id}
          number={index + 1}
          exercise={exercise}
        />
      ))}

      {session.note && (
        <Stack spacing={1} component="section">
          <Typography variant="subtitle1" component="h3">
            Notiz
          </Typography>
          <Paper variant="outlined" sx={{ p: 2 }}>
            <Typography
              sx={{ whiteSpace: 'pre-wrap', overflowWrap: 'anywhere' }}
            >
              {session.note}
            </Typography>
          </Paper>
        </Stack>
      )}

      <Box>
        <Button
          color="error"
          variant="outlined"
          startIcon={<Delete />}
          onClick={() => setConfirmDelete(true)}
        >
          Training löschen
        </Button>
      </Box>

      {confirmDelete && (
        <ConfirmDialog
          title="Training löschen?"
          confirmLabel="Löschen"
          destructive
          onConfirm={handleDelete}
          onClose={() => setConfirmDelete(false)}
        >
          Das Training erscheint danach nicht mehr im Verlauf.
        </ConfirmDialog>
      )}
    </Stack>
  )
}

function ExerciseSets({
  number,
  exercise,
}: {
  number: number
  exercise: SessionExerciseResponse
}) {
  return (
    <Stack spacing={1} component="section">
      <Typography
        variant="subtitle1"
        component="h3"
        sx={{ overflowWrap: 'anywhere' }}
      >
        {number}. {exercise.exerciseName}
      </Typography>
      <Paper variant="outlined">
        <Table size="small" aria-label={`Sätze ${exercise.exerciseName}`}>
          <TableHead>
            <TableRow>
              <TableCell>Satz</TableCell>
              <TableCell align="right">Gewicht</TableCell>
              <TableCell align="right">Wdh.</TableCell>
              <TableCell align="right">Pause</TableCell>
            </TableRow>
          </TableHead>
          <TableBody>
            {exercise.sets.map((set) => (
              <TableRow key={set.id}>
                <TableCell>{set.setNumber}</TableCell>
                <TableCell align="right">
                  {formatWeight(set.weightKg)}
                </TableCell>
                <TableCell align="right">{set.repetitions}</TableCell>
                <TableCell align="right">
                  {set.restSeconds === null ? '–' : `${set.restSeconds} s`}
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>
    </Stack>
  )
}
