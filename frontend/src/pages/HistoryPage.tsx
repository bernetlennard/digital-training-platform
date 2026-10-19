import {
  Chip,
  List,
  ListItemButton,
  ListItemText,
  Paper,
  Stack,
} from '@mui/material'
import { Link as RouterLink } from 'react-router'
import { getHistoryRoutines } from '../api/history'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { EmptyState } from '../components/EmptyState'
import { PageHeader } from '../components/PageHeader'
import { byName } from '../format'

async function loadHistoryRoutines() {
  return (await getHistoryRoutines()).toSorted(byName)
}

export function HistoryPage() {
  const routines = useApiData(loadHistoryRoutines)

  return (
    <>
      <PageHeader title="Verlauf" />
      <AsyncContent state={routines}>
        {(list) =>
          list.length === 0 ? (
            <EmptyState
              title="Noch keine abgeschlossenen Trainings"
              text="Sobald du ein Training abschliesst, erscheint es hier."
            />
          ) : (
            <Paper variant="outlined">
              <List disablePadding>
                {list.map((routine) => (
                  <ListItemButton
                    key={routine.routineId}
                    divider
                    component={RouterLink}
                    to={`/verlauf/${routine.routineId}`}
                  >
                    <Stack
                      direction="row"
                      spacing={1}
                      sx={{ alignItems: 'center', minWidth: 0 }}
                    >
                      <ListItemText
                        primary={routine.name}
                        sx={{ overflowWrap: 'anywhere' }}
                      />
                      {routine.deleted && (
                        <Chip size="small" label="gelöscht" />
                      )}
                    </Stack>
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
