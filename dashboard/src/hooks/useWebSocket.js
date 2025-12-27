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
        const message = JSON.parse(event.data);
        const niftySpot = message.feeds['NSE_INDEX|Nifty 50']?.fullFeed?.indexFF?.ltpc?.ltp;

        // Find an ATM option - for now, just picking the first one
        let optionPremium = null;
        for (const key in message.feeds) {
          if (key.startsWith('NSE_FO')) {
            optionPremium = message.feeds[key]?.fullFeed?.marketFF?.ltpc?.ltp;
            break;
          }
        }

        if (niftySpot && optionPremium) {
          const newTick = {
            niftySpot,
            optionPremium,
            alpha: niftySpot / optionPremium, // Placeholder alpha calculation
            time: message.currentTs / 1000,
          };
          setData(newTick);
        }
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
