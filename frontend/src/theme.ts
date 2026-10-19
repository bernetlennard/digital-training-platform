import { deDE } from '@mui/material/locale'
import { createTheme } from '@mui/material/styles'

export const theme = createTheme(
  {
    // Ohne Grossbuchstaben: Lange deutsche Wörter bleiben lesbar und passen auf 360 Pixel
    typography: { button: { textTransform: 'none', fontWeight: 600 } },
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
