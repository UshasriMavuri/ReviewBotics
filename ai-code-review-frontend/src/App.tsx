import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import { Box, AppBar, Toolbar, Typography, Button, Container } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import Home from './pages/Home';
import CodeReview from './pages/CodeReview';

function Navbar() {
  return (
    <AppBar position="sticky" color="default" elevation={1}>
      <Container maxWidth="lg">
        <Toolbar disableGutters>
          <Typography
            variant="h6"
            component={RouterLink}
            to="/"
            sx={{
              textDecoration: 'none',
              color: 'primary.main',
              fontWeight: 700,
              flexGrow: 1,
            }}
          >
            ReviewBotics
          </Typography>
          <Box sx={{ display: 'flex', gap: 2 }}>
            <Button
              component={RouterLink}
              to="/"
              color="inherit"
            >
              Home
            </Button>
            <Button
              component={RouterLink}
              to="/review"
              variant="contained"
              color="primary"
            >
              Review Code
            </Button>
          </Box>
        </Toolbar>
      </Container>
    </AppBar>
  );
}

function App() {
  return (
    <Router>
      <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
        <Navbar />
        <main>
          <Routes>
            <Route path="/" element={<Home />} />
            <Route path="/review" element={<CodeReview />} />
          </Routes>
        </main>
      </Box>
    </Router>
  );
}

export default App;
