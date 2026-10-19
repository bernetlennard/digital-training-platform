import Add from '@mui/icons-material/Add'
import {
  Box,
  Button,
  List,
  ListItemButton,
  ListItemText,
  Paper,
} from '@mui/material'
import { useState } from 'react'
import { getExercises, getMuscleGroups } from '../api/exercises'
import type { ExerciseResponse } from '../api/types'
import { useApiData } from '../api/useApiData'
import { AsyncContent } from '../components/AsyncContent'
import { EmptyState } from '../components/EmptyState'
import { ExerciseDialog } from '../components/ExerciseDialog'
import { PageHeader } from '../components/PageHeader'
import { byName } from '../format'

async function loadCatalog() {
  const [exercises, muscleGroups] = await Promise.all([
    getExercises(),
    getMuscleGroups(),
  ])
  return { exercises: exercises.toSorted(byName), muscleGroups }
}

/** Offener Dialog: neue Übung (null) oder die gewählte Übung */
type DialogState = { exercise: ExerciseResponse | null } | undefined

/** Übungskatalog, alphabetisch mit Muskelgruppe (SAF5, #FA-2.3) */
export function ExercisesPage() {
  const catalog = useApiData(loadCatalog)
  const [dialog, setDialog] = useState<DialogState>()

  const newExerciseButton = (
    <Button
      variant="contained"
      startIcon={<Add />}
      onClick={() => setDialog({ exercise: null })}
      disabled={!catalog.data}
    >
      Neue Übung
    </Button>
  )

  return (
    <>
      <PageHeader title="Übungen" action={newExerciseButton} />
      <AsyncContent state={catalog}>
        {({ exercises, muscleGroups }) => (
          <>
            {exercises.length === 0 ? (
              <EmptyState
                title="Noch keine Übungen"
                text="Erfasse deine erste Übung. Danach kannst du sie in Routinen verwenden."
              />
            ) : (
              <Paper variant="outlined">
                <List disablePadding>
                  {exercises.map((exercise) => (
                    <ListItemButton
                      key={exercise.id}
                      divider
                      onClick={() => setDialog({ exercise })}
                    >
                      <ListItemText
                        primary={exercise.name}
                        secondary={
                          <>
                            {exercise.muscleGroupName}
                            {/* Gekürzt auf zwei Zeilen, ganz im Dialog nach dem Antippen */}
                            {exercise.description && (
                              <Box
                                component="span"
                                sx={{
                                  display: '-webkit-box',
                                  WebkitLineClamp: 2,
                                  WebkitBoxOrient: 'vertical',
                                  overflow: 'hidden',
                                  overflowWrap: 'anywhere',
                                }}
                              >
                                {exercise.description}
                              </Box>
                            )}
                          </>
                        }
                      />
                    </ListItemButton>
                  ))}
                </List>
              </Paper>
            )}
            {dialog && (
              <ExerciseDialog
                muscleGroups={muscleGroups}
                exercise={dialog.exercise ?? undefined}
                onClose={() => setDialog(undefined)}
                onChanged={() => {
                  setDialog(undefined)
                  catalog.reload()
                }}
              />
            )}
          </>
        )}
      </AsyncContent>
    </>
  )
}
