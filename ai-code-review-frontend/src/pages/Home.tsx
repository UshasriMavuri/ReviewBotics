import React from 'react';
import { Box, Container, Typography, Button, Grid, Paper } from '@mui/material';
import { useNavigate } from 'react-router-dom';
import CodeIcon from '@mui/icons-material/Code';
import AutoFixHighIcon from '@mui/icons-material/AutoFixHigh';
import SpeedIcon from '@mui/icons-material/Speed';
import SecurityIcon from '@mui/icons-material/Security';

const features = [
  {
    icon: <CodeIcon sx={{ fontSize: 40 }} />,
    title: 'Smart Code Review',
    description: 'AI-powered analysis of your code changes, providing intelligent suggestions for improvements.',
  },
  {
    icon: <AutoFixHighIcon sx={{ fontSize: 40 }} />,
    title: 'Best Practices',
    description: 'Get recommendations based on SOLID principles, design patterns, and coding standards.',
  },
  {
    icon: <SpeedIcon sx={{ fontSize: 40 }} />,
    title: 'Quick Integration',
    description: 'Simply paste your PR URL and get instant feedback on your code changes.',
  },
  {
    icon: <SecurityIcon sx={{ fontSize: 40 }} />,
    title: 'Security First',
    description: 'Your code never leaves your environment. We analyze changes securely and privately.',
  },
];

const Home: React.FC = () => {
  const navigate = useNavigate();

  return (
    <Box>
      {/* Hero Section */}
      <Box
        sx={{
          bgcolor: 'primary.main',
          color: 'white',
          py: 8,
          mb: 6,
          borderRadius: 2,
        }}
      >
        <Container maxWidth="md">
          <Typography variant="h2" component="h1" gutterBottom fontWeight="bold">
            AI-Powered Code Review
          </Typography>
          <Typography variant="h5" component="h2" gutterBottom sx={{ mb: 4, opacity: 0.9 }}>
            Get instant, intelligent feedback on your code changes. Improve code quality and catch issues early.
          </Typography>
          <Button
            variant="contained"
            size="large"
            onClick={() => navigate('/review')}
            sx={{
              bgcolor: 'white',
              color: 'primary.main',
              '&:hover': {
                bgcolor: 'grey.100',
              },
            }}
          >
            Start Reviewing Code
          </Button>
        </Container>
      </Box>

      {/* Features Section */}
      <Container maxWidth="lg">
        <Typography variant="h3" component="h2" gutterBottom textAlign="center" mb={6}>
          Why Choose ReviewBotics?
        </Typography>
        <Grid container spacing={4}>
          {features.map((feature, index) => (
            <Grid item xs={12} sm={6} md={3} key={index}>
              <Paper
                elevation={0}
                sx={{
                  p: 3,
                  height: '100%',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  textAlign: 'center',
                  border: '1px solid',
                  borderColor: 'divider',
                  borderRadius: 2,
                  '&:hover': {
                    boxShadow: 2,
                    transform: 'translateY(-4px)',
                    transition: 'all 0.3s ease-in-out',
                  },
                }}
              >
                <Box sx={{ color: 'primary.main', mb: 2 }}>{feature.icon}</Box>
                <Typography variant="h6" component="h3" gutterBottom>
                  {feature.title}
                </Typography>
                <Typography color="text.secondary">{feature.description}</Typography>
              </Paper>
            </Grid>
          ))}
        </Grid>
      </Container>
    </Box>
  );
};

export default Home; 