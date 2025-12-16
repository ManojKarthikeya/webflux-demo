import React from 'react';
import { useQuery } from '@tanstack/react-query';
import { fetchMetrics } from '../api/metrics';
import { useMetricsStream } from '../hooks/useMetricsStream';
import {
  Box,
  Card,
  CardContent,
  Container,
  Grid,
  Typography,
  LinearProgress,
  Chip,
  Stack,
  Paper
} from '@mui/material';
import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  AreaChart,
  Area,
  BarChart,
  Bar
} from 'recharts';
import type { MetricsSnapshot } from '../types/metrics';

const MetricsDashboard: React.FC = () => {
  const { data: initialMetrics, isLoading } = useQuery({
    queryKey: ['metrics'],
    queryFn: fetchMetrics,
  });

  const { currentMetrics, history, isConnected } = useMetricsStream(initialMetrics);

  if (isLoading && !currentMetrics) {
    return <LinearProgress />;
  }

  if (!currentMetrics) {
    return <Typography>No metrics available</Typography>;
  }

  const formatBytes = (bytes: number) => {
    if (bytes === 0) return '0 B';
    const k = 1024;
    const sizes = ['B', 'KB', 'MB', 'GB', 'TB'];
    const i = Math.floor(Math.log(bytes) / Math.log(k));
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
  };

  const formatTime = (timestamp: number) => {
    return new Date(timestamp).toLocaleTimeString();
  };

  return (
    <Container maxWidth="xl" sx={{ mt: 4, mb: 4 }}>
      <Box display="flex" justifyContent="space-between" alignItems="center" mb={3}>
        <Typography variant="h4" component="h1">
          System Metrics
        </Typography>
        <Chip
          label={isConnected ? 'Live Stream Connected' : 'Connecting...'}
          color={isConnected ? 'success' : 'warning'}
          variant="outlined"
        />
      </Box>

      <Grid container spacing={3}>
        {/* Summary Cards */}
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 140 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              Heap Memory
            </Typography>
            <Typography component="p" variant="h4">
              {formatBytes(currentMetrics.jvm.heapUsed)}
            </Typography>
            <Typography color="text.secondary" sx={{ flex: 1 }}>
              of {formatBytes(currentMetrics.jvm.heapMax)}
            </Typography>
            <LinearProgress 
              variant="determinate" 
              value={(currentMetrics.jvm.heapUsed / currentMetrics.jvm.heapMax) * 100} 
            />
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 140 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              Active Threads
            </Typography>
            <Typography component="p" variant="h4">
              {currentMetrics.jvm.activeThreads}
            </Typography>
            <Typography color="text.secondary" sx={{ flex: 1 }}>
              Peak: {currentMetrics.jvm.peakThreads}
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 140 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              HTTP Requests
            </Typography>
            <Typography component="p" variant="h4">
              {currentMetrics.http.totalRequests}
            </Typography>
            <Typography color="text.secondary" sx={{ flex: 1 }}>
              Avg Response: {currentMetrics.http.averageResponseTime.toFixed(2)} ms
            </Typography>
          </Paper>
        </Grid>
        <Grid item xs={12} md={3}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 140 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              DB Performance
            </Typography>
            <Typography component="p" variant="h4">
              {currentMetrics.db.averageQueryTime.toFixed(2)} ms
            </Typography>
            <Typography color="text.secondary" sx={{ flex: 1 }}>
              Total Queries: {currentMetrics.db.totalQueries}
            </Typography>
          </Paper>
        </Grid>

        {/* Charts */}
        <Grid item xs={12} md={8}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 300 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              Memory Usage History
            </Typography>
            <ResponsiveContainer>
              <AreaChart data={history}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis 
                  dataKey="timestamp" 
                  tickFormatter={formatTime} 
                  interval="preserveStartEnd"
                />
                <YAxis tickFormatter={formatBytes} />
                <Tooltip 
                  labelFormatter={formatTime}
                  formatter={(value: number) => formatBytes(value)}
                />
                <Legend />
                <Area 
                  type="monotone" 
                  dataKey="jvm.heapUsed" 
                  name="Heap Used" 
                  stackId="1" 
                  stroke="#8884d8" 
                  fill="#8884d8" 
                />
                <Area 
                  type="monotone" 
                  dataKey="jvm.nonHeapUsed" 
                  name="Non-Heap Used" 
                  stackId="1" 
                  stroke="#82ca9d" 
                  fill="#82ca9d" 
                />
              </AreaChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={4}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 300 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              Thread Count
            </Typography>
            <ResponsiveContainer>
              <LineChart data={history}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" tickFormatter={formatTime} />
                <YAxis />
                <Tooltip labelFormatter={formatTime} />
                <Legend />
                <Line 
                  type="monotone" 
                  dataKey="jvm.activeThreads" 
                  name="Active" 
                  stroke="#8884d8" 
                  dot={false}
                />
                <Line 
                  type="monotone" 
                  dataKey="jvm.daemonThreads" 
                  name="Daemon" 
                  stroke="#82ca9d" 
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 300 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              HTTP Response Time
            </Typography>
            <ResponsiveContainer>
              <LineChart data={history}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" tickFormatter={formatTime} />
                <YAxis unit="ms" />
                <Tooltip labelFormatter={formatTime} />
                <Legend />
                <Line 
                  type="monotone" 
                  dataKey="http.averageResponseTime" 
                  name="Avg Response Time" 
                  stroke="#ff7300" 
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>

        <Grid item xs={12} md={6}>
          <Paper sx={{ p: 2, display: 'flex', flexDirection: 'column', height: 300 }}>
            <Typography component="h2" variant="h6" color="primary" gutterBottom>
              DB Query Time
            </Typography>
            <ResponsiveContainer>
              <LineChart data={history}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="timestamp" tickFormatter={formatTime} />
                <YAxis unit="ms" />
                <Tooltip labelFormatter={formatTime} />
                <Legend />
                <Line 
                  type="monotone" 
                  dataKey="db.averageQueryTime" 
                  name="Avg Query Time" 
                  stroke="#ff0000" 
                  dot={false}
                />
              </LineChart>
            </ResponsiveContainer>
          </Paper>
        </Grid>
      </Grid>
    </Container>
  );
};

export default MetricsDashboard;
