import React from 'react';

const SystemVitals = () => {
  const vitals = {
    latency: '12ms',
    memory: '1.2GB',
  };

  return (
    <div className="mt-8 p-4 bg-gray-800 rounded">
      <h3 className="text-lg font-bold mb-4">System Vitals</h3>
      <div className="flex justify-between">
        <span>Latency:</span>
        <span>{vitals.latency}</span>
      </div>
      <div className="flex justify-between mt-2">
        <span>Memory:</span>
        <span>{vitals.memory}</span>
      </div>
    </div>
  );
};

export default SystemVitals;
