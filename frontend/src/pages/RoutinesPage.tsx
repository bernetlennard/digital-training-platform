import Add from '@mui/icons-material/Add'
import {
  Button,
  List,
  ListItemButton,
  ListItemText,
  Paper,
} from '@mui/material'
import { Link as RouterLink } from 'react-router'
import { getRoutines } from '../api/routines'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { EmptyState } from '../components/EmptyState'
import { PageHeader } from '../components/PageHeader'
import { byName, countLabel } from '../format'
import { NEW_ROUTINE_ID } from '../routes'

async function loadRoutines() {
  return (await getRoutines()).toSorted(byName)
}

/** Routinen mit ihren Übungen (SAF6, #FA-3.2) */
export function RoutinesPage() {
  const routines = useApiData(loadRoutines)

  return (
    <>
      <PageHeader
        title="Routinen"
        action={
          <Button
            component={RouterLink}
            to={`/routinen/${NEW_ROUTINE_ID}`}
            variant="contained"
            startIcon={<Add />}
          >
            Neue Routine
          </Button>
        }
      />
      <AsyncContent state={routines}>
        {(list) =>
          list.length === 0 ? (
            <EmptyState
              title="Noch keine Routinen"
              text="Stelle aus deinen Übungen eine Routine zusammen. Danach kannst du sie einplanen und trainieren."
            />
          ) : (
            <Paper variant="outlined">
              <List disablePadding>
                {list.map((routine) => (
                  <ListItemButton
                    key={routine.id}
                    divider
                    component={RouterLink}
                    to={`/routinen/${routine.id}`}
                  >
                    <ListItemText
                      primary={routine.name}
                      secondary={`${countLabel(routine.exercises.length, 'Übung', 'Übungen')}: ${routine.exercises
                        .map((exercise) => exercise.name)
                        .join(', ')}`}
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
