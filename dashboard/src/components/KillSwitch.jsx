import React from 'react';

const KillSwitch = () => {
  const handleKill = () => {
    // In a real application, this would send a signal to the backend
    // to halt all trading activity.
    console.log('!!! KILL SWITCH ACTIVATED !!!');
    alert('Trading Halted!');
  };

  return (
    <button
      onClick={handleKill}
      className="w-full bg-accent-red text-white font-bold py-2 px-4 rounded hover:bg-red-700"
    >
      KILL SWITCH
    </button>
  );
};

export default KillSwitch;
