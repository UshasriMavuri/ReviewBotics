import React, { useState } from 'react';
import { ArrowPathIcon, MagnifyingGlassIcon } from '@heroicons/react/24/outline';
import Container from '@mui/material/Container';
import Typography from '@mui/material/Typography';
import Button from '@mui/material/Button';
import TextField from '@mui/material/TextField';
import Select from '@mui/material/Select';
import MenuItem from '@mui/material/MenuItem';
import InputAdornment from '@mui/material/InputAdornment';
import Grid from '@mui/material/Grid';
import Table from '@mui/material/Table';
import TableBody from '@mui/material/TableBody';
import TableCell from '@mui/material/TableCell';
import TableContainer from '@mui/material/TableContainer';
import TableHead from '@mui/material/TableHead';
import TableRow from '@mui/material/TableRow';
import Paper from '@mui/material/Paper';
import Box from '@mui/material/Box';

const reviews = [
  {
    id: 1,
    title: 'Add login page',
    description: 'Implements login page with OAuth',
    repository: 'frontend-app',
    author: 'alice',
    status: 'completed',
    issues: 2,
    date: '2024-03-15',
  },
  {
    id: 2,
    title: 'Refactor API layer',
    description: 'Refactors API layer for better error handling',
    repository: 'backend-service',
    author: 'bob',
    status: 'in-progress',
    issues: 1,
    date: '2024-03-14',
  },
  {
    id: 3,
    title: 'Update dependencies',
    description: 'Updates all npm dependencies',
    repository: 'api-gateway',
    author: 'carol',
    status: 'pending',
    issues: 0,
    date: '2024-03-13',
  },
];

const Reviews: React.FC = () => {
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');

  const filteredReviews = reviews.filter(review => {
    const matchesSearch = review.title.toLowerCase().includes(searchTerm.toLowerCase()) ||
                         review.repository.toLowerCase().includes(searchTerm.toLowerCase());
    const matchesStatus = statusFilter === 'all' || review.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  return (
    <Box py={6}>
      <Container maxWidth="lg">
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Typography variant="h4" fontWeight={700} gutterBottom>
            Code Reviews
          </Typography>
          <Button variant="contained" color="primary" startIcon={<ArrowPathIcon style={{ width: 20, height: 20 }} />}>
            Refresh
          </Button>
        </Box>
        {/* Filters */}
        <Grid container spacing={2} sx={{ mt: 2 }}>
          <Grid item xs={12} sm={8}>
            <TextField
              fullWidth
              variant="outlined"
              placeholder="Search reviews..."
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
              InputProps={{
                startAdornment: (
                  <InputAdornment position="start">
                    <MagnifyingGlassIcon style={{ width: 20, height: 20, color: '#888' }} />
                  </InputAdornment>
                ),
              }}
            />
          </Grid>
          <Grid item xs={12} sm={4}>
            <Select
              fullWidth
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value)}
              variant="outlined"
              displayEmpty
            >
              <MenuItem value="all">All Status</MenuItem>
              <MenuItem value="completed">Completed</MenuItem>
              <MenuItem value="in-progress">In Progress</MenuItem>
              <MenuItem value="pending">Pending</MenuItem>
            </Select>
          </Grid>
        </Grid>
        {/* Reviews List */}
        <TableContainer component={Paper} sx={{ mt: 4 }}>
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Title</TableCell>
                <TableCell>Repository</TableCell>
                <TableCell>Author</TableCell>
                <TableCell>Status</TableCell>
                <TableCell>Issues</TableCell>
                <TableCell>Date</TableCell>
              </TableRow>
            </TableHead>
            <TableBody>
              {filteredReviews.map((review) => (
                <TableRow key={review.id} hover sx={{ cursor: 'pointer' }}>
                  <TableCell>
                    <Typography fontWeight={600}>{review.title}</Typography>
                    <Typography variant="body2" color="text.secondary">{review.description}</Typography>
                  </TableCell>
                  <TableCell>{review.repository}</TableCell>
                  <TableCell>{review.author}</TableCell>
                  <TableCell>
                    <Box
                      component="span"
                      sx={{
                        px: 1.5,
                        py: 0.5,
                        borderRadius: 2,
                        fontWeight: 600,
                        fontSize: 12,
                        bgcolor:
                          review.status === 'completed'
                            ? 'success.light'
                            : review.status === 'in-progress'
                            ? 'warning.light'
                            : 'grey.200',
                        color:
                          review.status === 'completed'
                            ? 'success.main'
                            : review.status === 'in-progress'
                            ? 'warning.main'
                            : 'text.secondary',
                      }}
                    >
                      {review.status}
                    </Box>
                  </TableCell>
                  <TableCell>{review.issues}</TableCell>
                  <TableCell>{review.date}</TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      </Container>
    </Box>
  );
};

export default Reviews; 