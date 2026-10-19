import AccountCircle from '@mui/icons-material/AccountCircle'
import FitnessCenter from '@mui/icons-material/FitnessCenter'
import FormatListBulleted from '@mui/icons-material/FormatListBulleted'
import History from '@mui/icons-material/History'
import Home from '@mui/icons-material/Home'
import {
  AppBar,
  BottomNavigation,
  BottomNavigationAction,
  Box,
  Container,
  Drawer,
  GlobalStyles,
  IconButton,
  List,
  ListItemButton,
  ListItemIcon,
  ListItemText,
  Paper,
  Toolbar,
  Typography,
} from '@mui/material'
import { useEffect } from 'react'
import { Link as RouterLink, Outlet, useLocation } from 'react-router'

const DRAWER_WIDTH = 220

const SECTIONS = [
  { label: 'Start', path: '/', icon: <Home /> },
  { label: 'Routinen', path: '/routinen', icon: <FormatListBulleted /> },
  { label: 'Übungen', path: '/uebungen', icon: <FitnessCenter /> },
  { label: 'Verlauf', path: '/verlauf', icon: <History /> },
]

// /routinen/3 gehört zu /routinen, /konto zu keinem Bereich
function getSection(pathname: string) {
  const firstSegment = `/${pathname.split('/')[1] ?? ''}`
  return SECTIONS.find((section) => section.path === firstSegment)?.path ?? null
}

export function AppLayout() {
  const { pathname } = useLocation()
  const section = getSection(pathname)

  useEffect(() => {
    window.scrollTo(0, 0)
  }, [pathname])

  return (
    <Box sx={{ display: 'flex', minHeight: '100dvh' }}>
      {/* Damit die Snackbar über der unteren Navigation steht */}
      <GlobalStyles
        styles={(theme) => ({
          [theme.breakpoints.down('md')]: {
            ':root': {
              '--bottom-nav-height': 'calc(56px + env(safe-area-inset-bottom))',
            },
          },
        })}
      />
      <AppBar
        position="fixed"
        sx={{ zIndex: (theme) => theme.zIndex.drawer + 1 }}
      >
        <Toolbar>
          <FitnessCenter sx={{ mr: 1 }} />
          <Typography variant="h6" component="p" sx={{ flexGrow: 1 }} noWrap>
            Trainingsplattform
          </Typography>
          <IconButton
            component={RouterLink}
            to="/konto"
            color="inherit"
            edge="end"
            aria-label="Konto"
          >
            <AccountCircle />
          </IconButton>
        </Toolbar>
      </AppBar>

      <Drawer
        variant="permanent"
        sx={{
          display: { xs: 'none', md: 'block' },
          width: DRAWER_WIDTH,
          flexShrink: 0,
        }}
        slotProps={{
          paper: { sx: { width: DRAWER_WIDTH, boxSizing: 'border-box' } },
        }}
      >
        <Toolbar />
        <nav aria-label="Hauptnavigation">
          <List>
            {SECTIONS.map((item) => (
              <ListItemButton
                key={item.path}
                component={RouterLink}
                to={item.path}
                selected={section === item.path}
              >
                <ListItemIcon>{item.icon}</ListItemIcon>
                <ListItemText primary={item.label} />
              </ListItemButton>
            ))}
          </List>
        </nav>
      </Drawer>

      <Box component="main" sx={{ flexGrow: 1, minWidth: 0 }}>
        <Toolbar />
        {/* Unten Platz für die Navigationsleiste */}
        <Container maxWidth="md" sx={{ pt: 3, pb: { xs: 12, md: 4 } }}>
          <Outlet />
        </Container>
      </Box>

      <Paper
        component="nav"
        aria-label="Hauptnavigation"
        elevation={3}
        sx={{
          display: { md: 'none' },
          position: 'fixed',
          bottom: 0,
          left: 0,
          right: 0,
          zIndex: (theme) => theme.zIndex.appBar,
          pb: 'env(safe-area-inset-bottom)',
        }}
      >
        <BottomNavigation showLabels value={section}>
          {SECTIONS.map((item) => (
            <BottomNavigationAction
              key={item.path}
              component={RouterLink}
              to={item.path}
              value={item.path}
              label={item.label}
              icon={item.icon}
            />
          ))}
        </BottomNavigation>
      </Paper>
    </Box>
  )
}
