import React from 'react';
import { motion } from 'framer-motion';
import { 
  ChartBarIcon, 
  ClockIcon, 
  CheckCircleIcon, 
  ExclamationCircleIcon 
} from '@heroicons/react/24/outline';
import { Box, Grid, Card, CardContent, Typography, Chip, Container, TableContainer, Table, TableHead, TableBody, TableRow, TableCell } from '@mui/material';
import useMediaQuery from '@mui/material/useMediaQuery';
import { useTheme } from '@mui/material/styles';
import Paper from '@mui/material/Paper';

const stats = [
  { name: 'Total Reviews', value: '24', icon: ChartBarIcon, change: '+12%', changeType: 'positive' },
  { name: 'Average Response Time', value: '2.5h', icon: ClockIcon, change: '-0.5h', changeType: 'positive' },
  { name: 'Issues Found', value: '156', icon: ExclamationCircleIcon, change: '+23', changeType: 'negative' },
  { name: 'Resolved Issues', value: '142', icon: CheckCircleIcon, change: '+18', changeType: 'positive' },
];

const recentReviews = [
  {
    id: 1,
    repository: 'frontend-app',
    pullRequest: '#123',
    status: 'completed',
    issues: 5,
    date: '2024-03-15',
  },
  {
    id: 2,
    repository: 'backend-service',
    pullRequest: '#124',
    status: 'in-progress',
    issues: 3,
    date: '2024-03-15',
  },
  {
    id: 3,
    repository: 'api-gateway',
    pullRequest: '#125',
    status: 'pending',
    issues: 0,
    date: '2024-03-14',
  },
];

const Dashboard: React.FC = () => {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down('sm'));
  return (
    <Box py={6}>
      <Container maxWidth="lg">
        <motion.div
          initial={{ opacity: 0, y: 20 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.5 }}
        >
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Dashboard
          </Typography>
        </motion.div>
        {/* Stats */}
        <Box mt={4}>
          <Grid container spacing={3}>
            {stats.map((item, index) => (
              <Grid item xs={12} sm={6} md={3} key={item.name}>
                <motion.div
                  initial={{ opacity: 0, y: 20 }}
                  animate={{ opacity: 1, y: 0 }}
                  transition={{ duration: 0.5, delay: index * 0.1 }}
                >
                  <Card
                    elevation={4}
                    sx={{
                      borderRadius: 3,
                      px: 2,
                      pt: 4,
                      pb: 2,
                      minHeight: 170,
                      position: 'relative',
                      overflow: 'visible',
                      background: 'linear-gradient(135deg, #f8fafc 0%, #e0e7ff 100%)',
                      boxShadow: '0 4px 24px 0 rgba(60,72,100,0.08)',
                    }}
                  >
                    <Box
                      sx={{
                        position: 'absolute',
                        top: -28,
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
                      <item.icon style={{ width: 28, height: 28 }} />
                    </Box>
                    <CardContent sx={{ pt: 3 }}>
                      <Typography variant="h5" fontWeight={700} sx={{ mb: 0.5 }}>
                        {item.value}
                      </Typography>
                      <Typography variant="subtitle2" color="text.secondary" sx={{ mb: 1 }}>
                        {item.name}
                      </Typography>
                      <Chip
                        label={item.change}
                        color={item.changeType === 'positive' ? 'success' : 'error'}
                        size="small"
                        sx={{ fontWeight: 600 }}
                      />
                      <Typography variant="caption" color="text.secondary" sx={{ ml: 1 }}>
                        from last month
                      </Typography>
                    </CardContent>
                  </Card>
                </motion.div>
              </Grid>
            ))}
          </Grid>
        </Box>
        {/* Recent Reviews */}
        <Box mt={6}>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.4 }}
          >
            <Typography variant="h6" fontWeight={700} gutterBottom>
              Recent Reviews
            </Typography>
          </motion.div>
          <motion.div
            initial={{ opacity: 0, y: 20 }}
            animate={{ opacity: 1, y: 0 }}
            transition={{ duration: 0.5, delay: 0.5 }}
          >
            <TableContainer component={Paper} sx={{ mt: 2, borderRadius: 3, boxShadow: 3 }}>
              <Table size={isMobile ? 'small' : 'medium'}>
                <TableHead>
                  <TableRow sx={{ background: theme.palette.grey[100] }}>
                    <TableCell sx={{ fontWeight: 700 }}>Repository</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Pull Request</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Status</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Issues</TableCell>
                    <TableCell sx={{ fontWeight: 700 }}>Date</TableCell>
                  </TableRow>
                </TableHead>
                <TableBody>
                  {recentReviews.map((review, index) => (
                    <TableRow
                      key={review.id}
                      hover
                      sx={{
                        backgroundColor: index % 2 === 0 ? theme.palette.grey[50] : 'white',
                        transition: 'background 0.2s',
                        '&:hover': { backgroundColor: theme.palette.action.hover },
                      }}
                    >
                      <TableCell>{review.repository}</TableCell>
                      <TableCell>{review.pullRequest}</TableCell>
                      <TableCell>
                        <Chip
                          label={review.status}
                          color={
                            review.status === 'completed'
                              ? 'success'
                              : review.status === 'in-progress'
                              ? 'warning'
                              : 'default'
                          }
                          size="small"
                          sx={{ fontWeight: 600, textTransform: 'capitalize' }}
                        />
                      </TableCell>
                      <TableCell>{review.issues}</TableCell>
                      <TableCell>{review.date}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </TableContainer>
          </motion.div>
        </Box>
      </Container>
    </Box>
  );
};

export default Dashboard; 