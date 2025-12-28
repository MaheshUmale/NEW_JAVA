import { useState, useEffect } from 'react';

const useWebSocket = (url) => {
  const [data, setData] = useState(null);

  useEffect(() => {
    const ws = new WebSocket(url);

    ws.onopen = () => {
      console.log('WebSocket connected');
    };

    ws.onmessage = (event) => {
      try {
        // The replay server now sends simplified, individual ticks.
        // We just need to parse the JSON and pass it along.
        const message = JSON.parse(event.data);

        // The TradingChart component expects a 'time' field, but the data has 'exchange_ts'.
        // We'll rename it here to match what the chart expects.
        if (message.exchange_ts) {
          message.time = message.exchange_ts / 1000; // Convert to seconds
          delete message.exchange_ts;
        }

        setData(message);
      } catch (error) {
        console.error('Error parsing WebSocket message:', error);
      }
    };

    ws.onerror = (error) => {
      console.error('WebSocket error:', error);
    };

    ws.onclose = () => {
      console.log('WebSocket disconnected');
    };

    return () => {
      ws.close();
    };
  }, [url]);

  return data;
};

export default useWebSocket;
