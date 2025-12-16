import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { QueryClient, QueryClientProvider } from '@tanstack/react-query';
import Layout from './components/Layout';
import ChatRoom from './pages/ChatRoom';
import MetricsDashboard from './pages/MetricsDashboard';

// Create a client
const queryClient = new QueryClient();

// Placeholder components for other pages
const StockDashboard = () => <div>Stock Dashboard (Coming Soon)</div>;

function App() {
  return (
    <QueryClientProvider client={queryClient}>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<Layout />}>
            <Route index element={<StockDashboard />} />
            <Route path="chat" element={<ChatRoom />} />
            <Route path="metrics" element={<MetricsDashboard />} />
            <Route path="*" element={<Navigate to="/" replace />} />
          </Route>
        </Routes>
      </BrowserRouter>
    </QueryClientProvider>
  );
}

export default App;
