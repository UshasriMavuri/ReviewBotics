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
  CircularProgress,
  Breadcrumbs,
  Link,
  List,
  ListItem,
  ListItemText,
  Accordion,
  AccordionSummary,
  AccordionDetails,
  LinearProgress,
} from '@mui/material';
import { Link as RouterLink } from 'react-router-dom';
import ExpandMoreIcon from '@mui/icons-material/ExpandMore';
import { reviewService, CodeReview, ReviewComment } from '../services/api';

export const CodeReviewPage: React.FC = () => {
  const [prUrl, setPrUrl] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [review, setReview] = useState<CodeReview | null>(null);
  const [loadingProgress, setLoadingProgress] = useState(0);

  const extractPrInfo = (url: string): { repositoryName: string; pullRequestId: string } | null => {
    try {
      // Handle GitHub PR URLs like: https://github.com/owner/repo/pull/123
      const githubMatch = url.match(/github\.com\/([^/]+)\/([^/]+)\/pull\/(\d+)/);
      if (githubMatch) {
        return {
          repositoryName: `${githubMatch[1]}/${githubMatch[2]}`,
          pullRequestId: githubMatch[3]
        };
      }
      return null;
    } catch (error) {
      return null;
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);
    setLoading(true);
    setLoadingProgress(0);

    try {
      const prInfo = extractPrInfo(prUrl);
      if (!prInfo) {
        throw new Error('Invalid GitHub PR URL. Please provide a valid URL like: https://github.com/owner/repo/pull/123');
      }

      // Start progress animation
      const progressInterval = setInterval(() => {
        setLoadingProgress((prev) => Math.min(prev + 5, 90));
      }, 1000);

      const result = await reviewService.reviewPullRequest(prInfo);
      clearInterval(progressInterval);
      setLoadingProgress(100);
      setReview(result);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Failed to review PR');
    } finally {
      setLoading(false);
    }
  };

  const renderComment = (comment: ReviewComment) => (
    <ListItem key={comment.id} alignItems="flex-start">
      <ListItemText
        primary={
          <Box sx={{ display: 'flex', alignItems: 'center', gap: 1 }}>
            <Typography variant="subtitle1" component="span">
              {comment.filePath}
            </Typography>
            <Chip
              label={comment.severity}
              size="small"
              color={comment.severity === 'HIGH' ? 'error' : 'warning'}
            />
            <Chip label={comment.type} size="small" color="primary" />
          </Box>
        }
        secondary={
          <Box sx={{ mt: 1 }}>
            <Typography variant="body2" color="text.primary" gutterBottom>
              {comment.comment}
            </Typography>
            {comment.suggestedFix && (
              <Paper variant="outlined" sx={{ p: 1, mt: 1, bgcolor: 'grey.50' }}>
                <Typography variant="body2" color="text.secondary">
                  Suggested Fix: {comment.suggestedFix}
                </Typography>
              </Paper>
            )}
          </Box>
        }
      />
    </ListItem>
  );

  return (
    <Container maxWidth="md">
      <Breadcrumbs sx={{ mt: 4, mb: 3 }}>
        <Link component={RouterLink} to="/" color="inherit">
          Home
        </Link>
        <Typography color="text.primary">Code Review</Typography>
      </Breadcrumbs>

      <Box>
        <Typography variant="h4" component="h1" gutterBottom>
          Code Review
        </Typography>
        
        <Paper sx={{ p: 3, mb: 4 }}>
          <form onSubmit={handleSubmit}>
            <TextField
              fullWidth
              label="GitHub PR URL"
              variant="outlined"
              value={prUrl}
              onChange={(e) => setPrUrl(e.target.value)}
              placeholder="https://github.com/owner/repo/pull/123"
              margin="normal"
              required
            />
            <Button
              type="submit"
              variant="contained"
              color="primary"
              disabled={loading}
              sx={{ mt: 2 }}
            >
              {loading ? <CircularProgress size={24} /> : 'Review PR'}
            </Button>
          </form>
        </Paper>

        {loading && (
          <Box sx={{ width: '100%', mb: 4 }}>
            <LinearProgress variant="determinate" value={loadingProgress} />
            <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>
              Analyzing code... {loadingProgress}%
            </Typography>
          </Box>
        )}

        {error && (
          <Alert severity="error" sx={{ mb: 4 }}>
            {error}
          </Alert>
        )}

        {review && (
          <Box>
            <Typography variant="h5" gutterBottom>
              Review Results
            </Typography>
            <Paper sx={{ p: 3, mb: 4 }}>
              <Typography variant="h6" gutterBottom>
                Review Results
              </Typography>
              <Box sx={{ mb: 2 }}>
                <Typography variant="subtitle1">
                  PR #{review.pullRequestId}: {review.title}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  Author: {review.author} | Status: {review.status}
                </Typography>
              </Box>

              <Accordion>
                <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                  <Typography>Code Review Comments ({review.comments.length})</Typography>
                </AccordionSummary>
                <AccordionDetails>
                  <List>
                    {review.comments.map(renderComment)}
                  </List>
                </AccordionDetails>
              </Accordion>

              {review.documentationSuggestions && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography>Documentation Suggestions</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
                      {review.documentationSuggestions}
                    </Typography>
                  </AccordionDetails>
                </Accordion>
              )}

              {review.testSuggestions && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography>Test Suggestions</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
                      {review.testSuggestions}
                    </Typography>
                  </AccordionDetails>
                </Accordion>
              )}

              {review.refactoringSuggestions && (
                <Accordion>
                  <AccordionSummary expandIcon={<ExpandMoreIcon />}>
                    <Typography>Refactoring Suggestions</Typography>
                  </AccordionSummary>
                  <AccordionDetails>
                    <Typography variant="body2" style={{ whiteSpace: 'pre-wrap' }}>
                      {review.refactoringSuggestions}
                    </Typography>
                  </AccordionDetails>
                </Accordion>
              )}
            </Paper>
          </Box>
        )}
      </Box>
    </Container>
  );
}; 