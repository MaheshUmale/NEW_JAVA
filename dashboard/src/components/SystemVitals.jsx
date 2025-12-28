import React from 'react';

const SystemVitals = () => {
  // In a real application, these values would come from the backend.
  const status = 'OPERATIONAL';
  const latency = '12ms';
  const lastSignal = 'FAILED_AUCTION @ 10:45:12';

  return (
    <div className="mt-4 p-4 bg-gray-800 rounded">
      <h3 className="text-lg font-bold mb-2">System Vitals</h3>
      <p>Status: <span className="text-accent-green">{status}</span></p>
      <p>Latency: {latency}</p>
      <p>Last Signal: {lastSignal}</p>
    </div>
  );
};

export default SystemVitals;
