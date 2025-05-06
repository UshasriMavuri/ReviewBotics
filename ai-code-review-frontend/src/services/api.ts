import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_BASE_URL || 'http://localhost:3001/api';

export interface ReviewResponse {
  diff: {
    line: string;
    number?: number;
    type?: string;
  }[];
  suggestions: {
    type: string;
    color: string;
    message: string;
    line: number;
    side?: 'left' | 'right';
  }[];
}

export const reviewService = {
  async analyzePR(prUrl: string): Promise<ReviewResponse> {
    try {
      const response = await axios.post(`${API_BASE_URL}/review`, { prUrl });
      return response.data;
    } catch (error) {
      if (axios.isAxiosError(error)) {
        throw new Error(error.response?.data?.message || 'Failed to analyze PR');
      }
      throw error;
    }
  },
}; 