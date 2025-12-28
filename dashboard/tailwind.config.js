/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        background: '#0a0a0a',
        'primary-text': '#e0e0e0',
        'accent-red': '#ff4d4d',
        'accent-green': '#00ff88',
      },
    },
  },
  plugins: [],
}
