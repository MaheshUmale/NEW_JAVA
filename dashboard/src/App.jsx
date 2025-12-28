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
            const time = latestTick.time / 1000;
            setNiftyData(prev => [...prev, { time, value: latestTick.niftySpot }].slice(-500));
            setOptionData(prev => [...prev, { time, value: latestTick.optionPremium }].slice(-500));
            setAlphaData(prev => [...prev, { time, value: latestTick.alpha, color: latestTick.alpha > 0 ? '#00ff88' : '#ff4d4d' }].slice(-500));
        }
    }, [latestTick]);

    return (
        <div className="flex h-screen bg-background text-primary-text">
            <Sidebar />
            <div className="flex-1 p-4">
                <h2 className="text-xl mb-4">Trading Dashboard</h2>
                <TradingChart
                    niftyData={niftyData}
                    optionData={optionData}
                    alphaData={alphaData}
                />
            </div>
            <div className="w-64 bg-gray-900 p-4">
                <KillSwitch />
                <SystemVitals />
            </div>
        </div>
    );
}

export default App;
