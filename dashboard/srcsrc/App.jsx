import React, { useState, useEffect } from 'react';
import Sidebar from './components/Sidebar';
import TradingChart from './components/TradingChart';
import useWebSocket from './hooks/useWebSocket';
import KillSwitch from './components/KillSwitch';
import SystemVitals from './components/SystemVitals';

function App() {
  const latestTick = useWebSocket('ws://localhost:8080');
  const [niftyData, setNiftyData] = useState([]);
  const [optionData, setOptionData] = useState([]);
  const [alphaData, setAlphaData] = useState([]);

  useEffect(() => {
    if (latestTick) {
      const formattedNiftyData = {
        time: latestTick.time / 1000,
        value: latestTick.niftySpot,
      };
      const formattedOptionData = {
        time: latestTick.time / 1000,
        value: latestTick.optionPremium,
      };
      const formattedAlphaData = {
        time: latestTick.time / 1000,
        value: latestTick.alpha,
      };
      setNiftyData(prevData => [...prevData, formattedNiftyData].slice(-500));
      setOptionData(prevData => [...prevData, formattedOptionData].slice(-500));
      setAlphaData(prevData => [...prevData, formattedAlphaData].slice(-500));
    }
  }, [latestTick]);

  return (
    <div className="flex h-screen bg-background text-primary-text">
      <Sidebar />
      <div className="flex-1 p-4">
        <h2 className="text-xl mb-4">Trading Dashboard</h2>
        <TradingChart niftyData={niftyData} optionData={optionData} alphaData={alphaData} />
      </div>
      <div className="w-64 bg-gray-900 p-4">
        <KillSwitch />
        <SystemVitals />
      </div>
    </div>
  );
}

export default App;
