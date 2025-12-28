import React from 'react';
import { Chart, LineSeries, HistogramSeries } from 'lightweight-charts-react-components';

const chartOptions = {
    layout: {
        backgroundColor: '#0a0a0a',
        textColor: '#e0e0e0',
    },
    grid: {
        vertLines: { color: '#2a2a2a' },
        horzLines: { color: '#2a2a2a' },
    },
    timeScale: {
        timeVisible: true,
        secondsVisible: true,
    },
    rightPriceScale: {
        visible: true,
    },
    leftPriceScale: {
        visible: true,
    },
};

const TradingChart = ({ niftyData, optionData, alphaData }) => {
    return (
        <div style={{ width: '100%', height: '500px' }}>
            <Chart {...chartOptions} autoSize>
                <LineSeries
                    data={niftyData}
                    color="#00ff88"
                    title="Nifty Spot"
                    priceScaleId="left"
                />
                <LineSeries
                    data={optionData}
                    color="#ff4d4d"
                    title="Option Premium"
                    priceScaleId="right"
                />
                <HistogramSeries
                    data={alphaData}
                    color="#ffae42"
                    title="Alpha"
                    priceFormat={{ type: 'volume' }}
                    priceScaleId="alpha"
                />
            </Chart>
        </div>
    );
};

export default TradingChart;
