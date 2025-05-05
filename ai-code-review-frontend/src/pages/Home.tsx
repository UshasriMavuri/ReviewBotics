import React from 'react';
import { motion } from 'framer-motion';
import { 
  CodeBracketIcon, 
  ShieldCheckIcon, 
  RocketLaunchIcon, 
  DocumentTextIcon 
} from '@heroicons/react/24/outline';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import Card from '@mui/material/Card';
import CardContent from '@mui/material/CardContent';
import Grid from '@mui/material/Grid';
import Box from '@mui/material/Box';

const features = [
  {
    name: 'AI-Powered Code Review',
    description: 'Automated code reviews using advanced AI to catch bugs, security issues, and suggest improvements.',
    icon: CodeBracketIcon,
  },
  {
    name: 'Security Analysis',
    description: 'Comprehensive security vulnerability scanning and best practice enforcement.',
    icon: ShieldCheckIcon,
  },
  {
    name: 'Performance Optimization',
    description: 'Identify performance bottlenecks and suggest optimizations for better code efficiency.',
    icon: RocketLaunchIcon,
  },
  {
    name: 'Documentation & Testing',
    description: 'Automated suggestions for documentation updates and test coverage improvements.',
    icon: DocumentTextIcon,
  },
];

const Home: React.FC = () => {
  return (
    <Box>
      {/* Hero Section */}
      <Box sx={{ bgcolor: 'background.paper', py: { xs: 6, md: 10 } }}>
        <Container maxWidth="lg">
          <Box sx={{ maxWidth: 700, mx: { xs: 0, md: 'unset' } }}>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5 }}
            >
              <Typography variant="h2" component="h1" fontWeight={800} gutterBottom>
                AI-Powered
              </Typography>
              <Typography variant="h2" component="span" fontWeight={800} color="primary" gutterBottom>
                Code Review Assistant
              </Typography>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.2 }}
            >
              <Typography variant="h6" color="text.secondary" sx={{ mt: 2, mb: 4 }}>
                Automate your code review process with our intelligent AI assistant. Get instant feedback on code quality, security, and best practices.
              </Typography>
            </motion.div>
            <motion.div
              initial={{ opacity: 0, y: 20 }}
              animate={{ opacity: 1, y: 0 }}
              transition={{ duration: 0.5, delay: 0.4 }}
            >
              <Button
                variant="contained"
                color="primary"
                size="large"
                href="/dashboard"
                sx={{ px: 5, py: 1.5, fontWeight: 700 }}
              >
                Get Started
              </Button>
            </motion.div>
          </Box>
        </Container>
      </Box>

      {/* Features Section */}
      <Box sx={{ bgcolor: 'grey.50', py: 8 }}>
        <Container maxWidth="lg">
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            whileInView={{ opacity: 1, y: 0 }}
            viewport={{ once: true }}
            transition={{ duration: 0.5 }}
          >
            <Typography variant="overline" color="primary" fontWeight={700}>
              Features
            </Typography>
            <Typography variant="h4" fontWeight={800} sx={{ mt: 1, mb: 4 }}>
              Everything you need for better code reviews
            </Typography>
          </motion.div>
          <Grid container spacing={4}>
            {features.map((feature, index) => (
              <Grid item xs={12} md={6} key={feature.name}>
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  whileInView={{ opacity: 1, y: 0 }}
                  viewport={{ once: true }}
                  transition={{ duration: 0.5, delay: index * 0.1 }}
                >
                  <Card elevation={2} sx={{ position: 'relative', pt: 7, pb: 2, px: 2, minHeight: 180 }}>
                    <Box
                      sx={{
                        position: 'absolute',
                        top: 12,
                        left: 16,
                        bgcolor: 'primary.main',
                        borderRadius: '50%',
                        width: 56,
                        height: 56,
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'center',
                        boxShadow: 3,
                        color: 'white',
                        zIndex: 1,
                      }}
                    >
                      <feature.icon style={{ width: 28, height: 28 }} />
                    </Box>
                    <CardContent>
                      <Typography variant="h6" fontWeight={700} gutterBottom>
                        {feature.name}
                      </Typography>
                      <Typography color="text.secondary">{feature.description}</Typography>
                    </CardContent>
                  </Card>
                </motion.div>
              </Grid>
            ))}
          </Grid>
        </Container>
      </Box>
    </Box>
  );
};

export default Home; 