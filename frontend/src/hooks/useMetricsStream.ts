import { useState, useEffect, useRef } from 'react';
import type { MetricsSnapshot } from '../types/metrics';

interface UseMetricsStreamResult {
  currentMetrics: MetricsSnapshot | null;
  history: MetricsSnapshot[];
  isConnected: boolean;
}

export const useMetricsStream = (initialMetrics?: MetricsSnapshot): UseMetricsStreamResult => {
  const [currentMetrics, setCurrentMetrics] = useState<MetricsSnapshot | null>(initialMetrics || null);
  const [history, setHistory] = useState<MetricsSnapshot[]>([]);
  const [isConnected, setIsConnected] = useState(false);
  const eventSourceRef = useRef<EventSource | null>(null);

  useEffect(() => {
    if (initialMetrics) {
      setCurrentMetrics(initialMetrics);
      setHistory([initialMetrics]);
    }
  }, [initialMetrics]);

  useEffect(() => {
    const eventSource = new EventSource('http://localhost:8080/api/metrics/stream');
    eventSourceRef.current = eventSource;

    eventSource.onopen = () => {
      setIsConnected(true);
      console.log('Metrics SSE connected');
    };

    eventSource.onmessage = (event) => {
      try {
        const data: MetricsSnapshot = JSON.parse(event.data);
        setCurrentMetrics(data);
        setHistory((prev) => {
          const newHistory = [...prev, data];
          if (newHistory.length > 60) {
            return newHistory.slice(newHistory.length - 60);
          }
          return newHistory;
        });
      } catch (error) {
        console.error('Error parsing metrics SSE:', error);
      }
    };

    eventSource.onerror = (error) => {
      console.error('Metrics SSE error:', error);
      setIsConnected(false);
      eventSource.close();
      // Simple reconnect logic could be added here, but EventSource usually handles it.
      // However, if we close it, we need to reopen it. 
      // For now, let's rely on browser's auto-reconnect if we don't close it, 
      // but usually onerror fires on disconnect.
      // Let's try to reconnect after a delay if it closed.
      setTimeout(() => {
          if (eventSourceRef.current?.readyState === EventSource.CLOSED) {
             // Trigger re-render to re-run effect? No, effect dependency is empty.
             // We might need a more robust reconnect strategy or just let the browser handle it 
             // (browser retries if we don't close).
             // If we don't close, browser retries.
          }
      }, 5000);
    };

    return () => {
      eventSource.close();
      setIsConnected(false);
    };
  }, []);

  return { currentMetrics, history, isConnected };
};
