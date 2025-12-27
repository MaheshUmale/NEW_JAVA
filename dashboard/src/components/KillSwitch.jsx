import React from 'react';

const KillSwitch = () => {
  const handleClick = () => {
    // Logic to send EMERGENCY_HALT signal via WebSocket
    console.log('KILL SWITCH ACTIVATED');
  };

  return (
    <button
      onClick={handleClick}
      className="bg-accent-red text-white font-bold py-4 px-4 rounded w-full hover:bg-red-700 transition-colors"
    >
      KILL SWITCH
    </button>
  );
};

export default KillSwitch;
