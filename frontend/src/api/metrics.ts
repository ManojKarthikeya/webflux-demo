import { apiClient } from './client';
import type { MetricsSnapshot } from '../types/metrics';

export const fetchMetrics = async (): Promise<MetricsSnapshot> => {
  const response = await apiClient.get<MetricsSnapshot>('/metrics');
  return response.data;
};
