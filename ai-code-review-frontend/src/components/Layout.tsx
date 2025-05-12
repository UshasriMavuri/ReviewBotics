import React from 'react';
import { Box, AppBar, Toolbar, Typography, Button, Container } from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';

interface LayoutProps {
  children: React.ReactNode;
}

export const Layout: React.FC<LayoutProps> = ({ children }) => {
  return (
    <Box sx={{ minHeight: '100vh', bgcolor: 'background.default' }}>
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
              <Button component={RouterLink} to="/" color="inherit">
                Home
              </Button>
              <Button component={RouterLink} to="/review" variant="contained" color="primary">
                Review Code
              </Button>
            </Box>
          </Toolbar>
        </Container>
      </AppBar>
      <main>
        <Container maxWidth="lg" sx={{ py: 4 }}>
          {children}
        </Container>
      </main>
    </Box>
  );
}; 