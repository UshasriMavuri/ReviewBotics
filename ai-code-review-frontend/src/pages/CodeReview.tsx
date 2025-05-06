import React, { useState } from 'react';
import {
  Box,
  Container,
  Typography,
  TextField,
  Button,
  Paper,
  Chip,
  Alert,
  Table,
  TableBody,
  TableCell,
  TableRow,
  TableHead,
  CircularProgress,
  Breadcrumbs,
  Link,
} from '@mui/material';
import { blue, green, orange, red } from '@mui/material/colors';
import { Link as RouterLink } from 'react-router-dom';
import GitHubIcon from '@mui/icons-material/GitHub';
import LinkIcon from '@mui/icons-material/Link';
import { reviewService, ReviewResponse } from '../services/api';

type DiffLine = { line: string; number?: number; type?: string };
type Suggestion = { type: string; color: string; message: string; line: number; side?: 'left' | 'right' };

const MOCK_DIFF = [
  { line: '-    public void processOrder(Order order) {', number: 1 },
  { line: '-        if (order != null) {', number: 2 },
  { line: '-            // process order', number: 3 },
  { line: '-        }', number: 4 },
  { line: '-    }', number: 5 },
  { line: '+    public void processOrder(Order order) {', number: 6 },
  { line: '+        if (order == null) {', number: 7 },
  { line: '+            throw new IllegalArgumentException("Order cannot be null");', number: 8 },
  { line: '+        }', number: 9 },
  { line: '+        // process order', number: 10 },
  { line: '+    }', number: 11 },
];

const MOCK_SUGGESTIONS: Suggestion[] = [
  {
    type: 'SOLID Principle',
    color: blue[500],
    message: 'Consider applying the Single Responsibility Principle. Extract order validation into a separate method or class.',
    line: 7,
    side: 'right',
  },
  {
    type: 'Test Coverage',
    color: green[500],
    message: 'No unit test found for null order. Add a test to ensure IllegalArgumentException is thrown.',
    line: 8,
    side: 'right',
  },
  {
    type: 'Documentation',
    color: orange[700],
    message: 'Add Javadoc to describe the behavior when order is null.',
    line: 6,
    side: 'right',
  },
  {
    type: 'Refactoring',
    color: red[500],
    message: 'Refactor to use Optional<Order> if nulls are expected, or use @NonNull annotation.',
    line: 6,
    side: 'right',
  },
];

function parseSideBySide(diff: DiffLine[]) {
  const left: DiffLine[] = [];
  const right: DiffLine[] = [];
  diff.forEach((d: DiffLine) => {
    if (d.line.startsWith('-')) {
      left.push({ ...d, line: d.line.slice(1), type: 'remove' });
    } else if (d.line.startsWith('+')) {
      right.push({ ...d, line: d.line.slice(1), type: 'add' });
    } else {
      left.push({ ...d, type: 'context' });
      right.push({ ...d, type: 'context' });
    }
  });
  const maxLen = Math.max(left.length, right.length);
  while (left.length < maxLen) left.push({ line: '', type: 'empty' });
  while (right.length < maxLen) right.push({ line: '', type: 'empty' });
  return { left, right };
}

function SideBySideDiff({ diff, suggestions }: { diff: DiffLine[]; suggestions: Suggestion[] }) {
  const { left, right } = parseSideBySide(diff);
  return (
    <Paper
      variant="outlined"
      sx={{
        fontFamily: 'Menlo, Monaco, Consolas, "Liberation Mono", "Courier New", monospace',
        fontSize: 14,
        lineHeight: 1.5,
        p: 0,
        bgcolor: '#fff',
        overflowX: 'auto',
        mb: 2,
        borderRadius: 2,
        boxShadow: '0 2px 8px 0 rgba(60,72,100,0.06)',
        border: '1px solid #d0d7de',
      }}
    >
      <Table size="small" sx={{ minWidth: 700, borderCollapse: 'separate', borderSpacing: 0 }}>
        <TableHead>
          <TableRow>
            <TableCell sx={{ width: 48, bgcolor: '#f6f8fa', border: 0, color: '#57606a', fontWeight: 700, fontSize: 12, textAlign: 'right', borderTopLeftRadius: 8 }}>Old</TableCell>
            <TableCell sx={{ width: '45%', bgcolor: '#f6f8fa', border: 0, color: '#57606a', fontWeight: 700, fontSize: 12, borderRight: '1px solid #d0d7de', borderTop: 0 }}>Before</TableCell>
            <TableCell sx={{ width: 48, bgcolor: '#f6f8fa', border: 0, color: '#57606a', fontWeight: 700, fontSize: 12, textAlign: 'right' }}>New</TableCell>
            <TableCell sx={{ width: '45%', bgcolor: '#f6f8fa', border: 0, color: '#57606a', fontWeight: 700, fontSize: 12, borderTopRightRadius: 8 }}>After</TableCell>
          </TableRow>
        </TableHead>
        <TableBody>
          {left.map((l, idx) => {
            const r = right[idx];
            const leftLineNum = l.type !== 'empty' ? l.number ?? '' : '';
            const rightLineNum = r.type !== 'empty' ? r.number ?? '' : '';
            const leftSuggestions = suggestions.filter(s => l.number !== undefined && s.line === l.number && s.side !== 'right');
            const rightSuggestions = suggestions.filter(s => r.number !== undefined && s.line === r.number && s.side !== 'left');
            const rowHover = {
              transition: 'background 0.15s',
              '&:hover': { bgcolor: '#f6f8fa' },
            };
            return (
              <React.Fragment key={idx}>
                <TableRow sx={rowHover}>
                  <TableCell
                    sx={{
                      color: '#8c959f',
                      bgcolor: '#f6f8fa',
                      border: 0,
                      fontSize: 12,
                      textAlign: 'right',
                      borderRight: '1px solid #d0d7de',
                      p: '2px 8px',
                      width: 48,
                    }}
                  >
                    {leftLineNum}
                  </TableCell>
                  <TableCell
                    sx={{
                      bgcolor: l.type === 'remove' ? '#ffebe9' : '#fff',
                      color: l.type === 'remove' ? '#cf222e' : '#24292f',
                      fontFamily: 'inherit',
                      border: 0,
                      borderRight: '1px solid #d0d7de',
                      p: '2px 8px',
                      whiteSpace: 'pre',
                      borderLeft: l.type === 'remove' ? '4px solid #cf222e' : '4px solid transparent',
                      borderRadius: l.type === 'remove' ? '4px' : 0,
                    }}
                  >
                    {l.line}
                  </TableCell>
                  <TableCell
                    sx={{
                      color: '#8c959f',
                      bgcolor: '#f6f8fa',
                      border: 0,
                      fontSize: 12,
                      textAlign: 'right',
                      borderRight: '1px solid #d0d7de',
                      p: '2px 8px',
                      width: 48,
                    }}
                  >
                    {rightLineNum}
                  </TableCell>
                  <TableCell
                    sx={{
                      bgcolor: r.type === 'add' ? '#dafbe1' : '#fff',
                      color: r.type === 'add' ? '#116329' : '#24292f',
                      fontFamily: 'inherit',
                      border: 0,
                      p: '2px 8px',
                      whiteSpace: 'pre',
                      borderLeft: r.type === 'add' ? '4px solid #116329' : '4px solid transparent',
                      borderRadius: r.type === 'add' ? '4px' : 0,
                    }}
                  >
                    {r.line}
                  </TableCell>
                </TableRow>
                {/* Inline comments for left */}
                {leftSuggestions.map((s, i) => (
                  <TableRow key={'l' + i}>
                    <TableCell colSpan={2} sx={{ border: 0, bgcolor: 'transparent', p: 0 }}>
                      <Box sx={{ ml: 6, mt: 0.5, mb: 1, maxWidth: 420, display: 'flex', alignItems: 'flex-start' }}>
                        <Box
                          sx={{
                            bgcolor: '#fff',
                            border: `1px solid ${s.color}`,
                            borderRadius: 2,
                            boxShadow: '0 2px 8px 0 rgba(60,72,100,0.08)',
                            p: 1.5,
                            color: s.color,
                            fontSize: 13,
                            position: 'relative',
                            '&:before': {
                              content: '""',
                              position: 'absolute',
                              left: -12,
                              top: 16,
                              width: 0,
                              height: 0,
                              borderTop: '8px solid transparent',
                              borderBottom: '8px solid transparent',
                              borderRight: `12px solid ${s.color}`,
                            },
                          }}
                        >
                          <Chip label={s.type} size="small" sx={{ bgcolor: s.color, color: 'white', fontWeight: 700, mr: 2, fontSize: 11 }} />
                          {s.message}
                        </Box>
                      </Box>
                    </TableCell>
                    <TableCell colSpan={2} sx={{ p: 0, border: 0, bgcolor: 'transparent' }} />
                  </TableRow>
                ))}
                {/* Inline comments for right */}
                {rightSuggestions.map((s, i) => (
                  <TableRow key={'r' + i}>
                    <TableCell colSpan={2} sx={{ p: 0, border: 0, bgcolor: 'transparent' }} />
                    <TableCell colSpan={2} sx={{ border: 0, bgcolor: 'transparent', p: 0 }}>
                      <Box sx={{ ml: 6, mt: 0.5, mb: 1, maxWidth: 420, display: 'flex', alignItems: 'flex-start' }}>
                        <Box
                          sx={{
                            bgcolor: '#fff',
                            border: `1px solid ${s.color}`,
                            borderRadius: 2,
                            boxShadow: '0 2px 8px 0 rgba(60,72,100,0.08)',
                            p: 1.5,
                            color: s.color,
                            fontSize: 13,
                            position: 'relative',
                            '&:before': {
                              content: '""',
                              position: 'absolute',
                              left: -12,
                              top: 16,
                              width: 0,
                              height: 0,
                              borderTop: '8px solid transparent',
                              borderBottom: '8px solid transparent',
                              borderRight: `12px solid ${s.color}`,
                            },
                          }}
                        >
                          <Chip label={s.type} size="small" sx={{ bgcolor: s.color, color: 'white', fontWeight: 700, mr: 2, fontSize: 11 }} />
                          {s.message}
                        </Box>
                      </Box>
                    </TableCell>
                  </TableRow>
                ))}
              </React.Fragment>
            );
          })}
        </TableBody>
      </Table>
    </Paper>
  );
}

const CodeReview: React.FC = () => {
  const [prUrl, setPrUrl] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [reviewData, setReviewData] = useState<ReviewResponse | null>(null);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setIsLoading(true);
    setError(null);
    setReviewData(null);

    try {
      const data = await reviewService.analyzePR(prUrl);
      setReviewData(data);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to analyze PR');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Container maxWidth="lg" sx={{ py: 4 }}>
      <Breadcrumbs sx={{ mb: 3 }}>
        <Link component={RouterLink} to="/" color="inherit" underline="hover">
          Home
        </Link>
        <Typography color="text.primary">Code Review</Typography>
      </Breadcrumbs>

      <Paper sx={{ p: 3, mb: 4 }}>
        <Typography variant="h5" component="h1" gutterBottom fontWeight="bold">
          Review Pull Request
        </Typography>
        <Typography variant="body1" color="text.secondary" sx={{ mb: 3 }}>
          Enter a Pull Request URL from GitHub, GitLab, or Bitbucket to analyze code changes and get AI-powered suggestions.
        </Typography>

        <form onSubmit={handleSubmit}>
          <Box display="flex" gap={2} alignItems="flex-start">
            <TextField
              label="Pull Request URL"
              variant="outlined"
              fullWidth
              value={prUrl}
              onChange={e => setPrUrl(e.target.value)}
              placeholder="https://github.com/username/repo/pull/123"
              required
              InputProps={{
                startAdornment: <LinkIcon sx={{ mr: 1, color: 'text.secondary' }} />,
              }}
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              size="large"
              disabled={isLoading}
              sx={{ minWidth: 140, height: 56 }}
            >
              {isLoading ? <CircularProgress size={24} /> : 'Review PR'}
            </Button>
          </Box>
        </form>

        {error && (
          <Alert severity="error" sx={{ mt: 2 }}>
            {error}
          </Alert>
        )}
      </Paper>

      {reviewData && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <GitHubIcon sx={{ mr: 1, color: 'text.secondary' }} />
            <Typography variant="h6" component="h2">
              Code Review Results
            </Typography>
          </Box>
          <SideBySideDiff diff={reviewData.diff} suggestions={reviewData.suggestions} />
        </Paper>
      )}

      {/* Example PR Review - Only show when no review data is available */}
      {!reviewData && !isLoading && (
        <Paper sx={{ p: 3 }}>
          <Box sx={{ display: 'flex', alignItems: 'center', mb: 3 }}>
            <GitHubIcon sx={{ mr: 1, color: 'text.secondary' }} />
            <Typography variant="h6" component="h2">
              Example PR Review
            </Typography>
          </Box>
          <SideBySideDiff diff={MOCK_DIFF} suggestions={MOCK_SUGGESTIONS} />
        </Paper>
      )}
    </Container>
  );
};

export default CodeReview; 