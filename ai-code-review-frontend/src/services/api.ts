import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:8080/api';

export interface ReviewComment {
  id?: number;
  filePath: string;
  comment: string;
  suggestedFix: string;
  type: string;
  severity: string;
  category: string;
  lineNumber?: number;
  createdAt?: string;
  resolved?: boolean;
}

export interface CodeReview {
  id: number;
  repositoryName: string;
  pullRequestId: string;
  title: string;
  description: string | null;
  author: string;
  status: string;
  comments: ReviewComment[];
  documentationSuggestions: string;
  testSuggestions: string;
  refactoringSuggestions: string;
  qualityAnalysis: string;
  suggestedReviewers: string;
  createdAt: string;
  completedAt: string;
  code: string | null;
  baseBranch: string;
  headBranch: string;
  mcpContext: string | null;
  commitSha: string;
}

export interface ReviewRequest {
  repositoryName: string;
  pullRequestId: string;
}

export const reviewService = {
  async reviewPullRequest(request: ReviewRequest): Promise<CodeReview> {
    try {
      const response = await axios.post(`${API_BASE_URL}/pr/review`, request);
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to review PR');
      }
      throw error;
    }
  },

  async reviewCode(code: string, filePath: string, mcpContext?: string): Promise<ReviewComment[]> {
    try {
      const response = await axios.post(`${API_BASE_URL}/test/review`, code, {
        headers: {
          'Content-Type': 'text/plain'
        },
        params: {
          filePath,
          mcpContext
        }
      });
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to review code');
      }
      throw error;
    }
  }
}; 