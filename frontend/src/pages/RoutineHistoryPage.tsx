import {
  Chip,
  List,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
} from '@mui/material'
import { useCallback } from 'react'
import { Link as RouterLink, useParams } from 'react-router'
import { parseId } from '../api/client'
import { getHistoryRoutines, getRoutineHistory } from '../api/history'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { EmptyState } from '../components/EmptyState'
import { PageHeader } from '../components/PageHeader'
import { formatDuration, formatInstantWeekdayDate, formatTime } from '../format'

async function loadRoutineHistory(routineIdParam: string | undefined) {
  const routineId = parseId(routineIdParam)
  // Den Namen gibt es nur in der Liste der Routinen, darum beides laden
  const [routines, sessions] = await Promise.all([
    getHistoryRoutines(),
    getRoutineHistory(routineId),
  ])
  return {
    routine: routines.find((routine) => routine.routineId === routineId),
    sessions,
  }
}

export function RoutineHistoryPage() {
  const { routineId } = useParams()
  const load = useCallback(() => loadRoutineHistory(routineId), [routineId])
  const history = useApiData(load)
  const routine = history.data?.routine

  return (
    <>
      <PageHeader
        title={
          <Stack
            direction="row"
            spacing={1}
            component="span"
            sx={{ alignItems: 'center', flexWrap: 'wrap' }}
          >
            <span>{routine?.name ?? 'Verlauf'}</span>
            {routine?.deleted && <Chip size="small" label="gelöscht" />}
          </Stack>
        }
        backTo="/verlauf"
      />
      <AsyncContent state={history}>
        {({ sessions }) =>
          sessions.length === 0 ? (
            <EmptyState
              title="Keine abgeschlossenen Trainings"
              text="Für diese Routine gibt es keine abgeschlossenen Trainings."
            />
          ) : (
            <Paper variant="outlined">
              <List disablePadding>
                {sessions.map((session) => (
                  <ListItemButton
                    key={session.id}
                    divider
                    component={RouterLink}
                    to={`/verlauf/einheit/${session.id}`}
                  >
                    <ListItemText
                      primary={formatInstantWeekdayDate(session.startedAt)}
                      secondary={`${formatTime(session.startedAt)} Uhr · ${formatDuration(session.startedAt, session.finishedAt)}`}
                    />
                  </ListItemButton>
                ))}
              </List>
            </Paper>
          )
        }
      </AsyncContent>
    </>
  )
}
