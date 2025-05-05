import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { CodeBracketIcon } from '@heroicons/react/24/outline';
import AppBar from '@mui/material/AppBar';
import Toolbar from '@mui/material/Toolbar';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import IconButton from '@mui/material/IconButton';
import MenuIcon from '@mui/icons-material/Menu';
import Drawer from '@mui/material/Drawer';
import List from '@mui/material/List';
import ListItem from '@mui/material/ListItem';
import ListItemButton from '@mui/material/ListItemButton';
import ListItemText from '@mui/material/ListItemText';
import Box from '@mui/material/Box';

const navLinks = [
  { path: '/', label: 'Home' },
  { path: '/dashboard', label: 'Dashboard' },
  { path: '/reviews', label: 'Reviews' },
  { path: '/settings', label: 'Settings' },
];

const Navbar: React.FC = () => {
  const [drawerOpen, setDrawerOpen] = useState(false);
  const location = useLocation();

  const isActive = (path: string) => location.pathname === path;

  return (
    <AppBar position="sticky" color="default" elevation={2}>
      <Toolbar>
        <Box display="flex" alignItems="center" flexGrow={1}>
          <CodeBracketIcon style={{ height: 28, width: 28, color: '#6366f1' }} />
          <Typography variant="h6" component="div" sx={{ ml: 1, fontWeight: 700 }}>
            ReviewBotics
          </Typography>
          <Box sx={{ display: { xs: 'none', sm: 'flex' }, ml: 4 }}>
            {navLinks.map((link) => (
              <Button
                key={link.path}
                component={Link}
                to={link.path}
                color={isActive(link.path) ? 'primary' : 'inherit'}
                sx={{ ml: 2, fontWeight: isActive(link.path) ? 700 : 400 }}
              >
                {link.label}
              </Button>
            ))}
          </Box>
        </Box>
        <Box sx={{ display: { xs: 'none', sm: 'block' } }}>
          <Button variant="contained" color="primary" sx={{ ml: 2 }}>
            Connect Repository
          </Button>
        </Box>
        <Box sx={{ display: { xs: 'block', sm: 'none' } }}>
          <IconButton edge="end" color="inherit" onClick={() => setDrawerOpen(true)}>
            <MenuIcon />
          </IconButton>
        </Box>
        <Drawer anchor="left" open={drawerOpen} onClose={() => setDrawerOpen(false)}>
          <Box sx={{ width: 250 }} role="presentation" onClick={() => setDrawerOpen(false)}>
            <List>
              {navLinks.map((link) => (
                <ListItem key={link.path} disablePadding>
                  <ListItemButton component={Link} to={link.path} selected={isActive(link.path)}>
                    <ListItemText primary={link.label} />
                  </ListItemButton>
                </ListItem>
              ))}
              <ListItem>
                <Button variant="contained" color="primary" fullWidth>
                  Connect Repository
                </Button>
              </ListItem>
            </List>
          </Box>
        </Drawer>
      </Toolbar>
    </AppBar>
  );
};

export default Navbar; 