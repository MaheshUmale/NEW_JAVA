import React from 'react';
import KillSwitch from './KillSwitch';
import SystemVitals from './SystemVitals';

const Sidebar = () => {
  return (
    <div className="w-64 bg-gray-900 p-4 flex flex-col">
      <h1 className="text-2xl font-bold mb-8 text-primary-text">AlphaPulse</h1>
      <KillSwitch />
      <SystemVitals />
    </div>
  );
};

export default Sidebar;
