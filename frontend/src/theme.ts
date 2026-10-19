import { deDE } from '@mui/material/locale'
import { createTheme } from '@mui/material/styles'

export const theme = createTheme(
  {
    typography: { button: { textTransform: 'none', fontWeight: 600 } },
    components: {
      MuiButton: { styleOverrides: { root: { minHeight: 44 } } },
      MuiIconButton: {
        styleOverrides: { root: { minWidth: 44, minHeight: 44 } },
      },
    },
  },
  deDE,
)
