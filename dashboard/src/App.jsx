import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import TradingChart from './components/TradingChart';
import useWebSocket from './hooks/useWebSocket';

function App() {
  const latestTick = useWebSocket('ws://localhost:8080');
  const [chartData, setChartData] = useState([]);

  useEffect(() => {
    if (latestTick) {
      setChartData(prevData => [...prevData, latestTick].slice(-500));
    }
  }, [latestTick]);

  return (
    <div className="flex h-screen bg-background text-primary-text">
      <Sidebar />
      <div className="flex-1 p-4">
        <h2 className="text-xl mb-4">Trading Dashboard</h2>
        <TradingChart data={chartData} />
      </div>
    </div>
  );
}

export default App;
