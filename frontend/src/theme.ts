import { deDE } from '@mui/material/locale'
import { createTheme } from '@mui/material/styles'

export const theme = createTheme(
  {
    components: {
      // Touch-Ziele mindestens 44 × 44 Pixel (#NFA-7.3)
      MuiButton: { styleOverrides: { root: { minHeight: 44 } } },
      MuiIconButton: {
        styleOverrides: { root: { minWidth: 44, minHeight: 44 } },
      },
    },
  },
  // Deutsche Texte der MUI-Komponenten (#NFA-7.11)
  deDE,
)
