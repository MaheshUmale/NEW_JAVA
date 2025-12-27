import React, { useEffect, useRef } from 'react';
import { Chart } from 'lightweight-charts-react';

const TradingChart = ({ data }) => {
    const chartRef = useRef();
    const niftySeriesRef = useRef();
    const optionSeriesRef = useRef();
    const alphaSeriesRef = useRef();

    useEffect(() => {
        if (chartRef.current) {
            if (!niftySeriesRef.current) {
                niftySeriesRef.current = chartRef.current.addLineSeries({
                    color: '#00ff88',
                    title: 'Nifty Spot',
                    priceScaleId: 'left',
                });
            }
            if (!optionSeriesRef.current) {
                optionSeriesRef.current = chartRef.current.addLineSeries({
                    color: '#ff4d4d',
                    title: 'Option Premium',
                    priceScaleId: 'right',
                });
            }
            if (!alphaSeriesRef.current) {
                alphaSeriesRef.current = chartRef.current.addHistogramSeries({
                    color: '#ff4d4d',
                    title: 'Alpha',
                    priceFormat: {
                        type: 'volume',
                    },
                    priceScaleId: 'alpha',
                });
                chartRef.current.priceScale('alpha').applyOptions({
                    scaleMargins: {
                        top: 0.8,
                        bottom: 0,
                    },
                });
            }
        }
    }, []);

    useEffect(() => {
        if (chartRef.current && data && data.length > 0) {
            // Use setData for the initial load and update for subsequent ticks
            if (data.length === 1) {
                niftySeriesRef.current.setData(data.map(d => ({ time: d.time, value: d.niftySpot })));
                optionSeriesRef.current.setData(data.map(d => ({ time: d.time, value: d.optionPremium })));
                alphaSeriesRef.current.setData(data.map(d => ({ time: d.time, value: d.alpha })));
            } else {
                const lastDataPoint = data[data.length - 1];
                niftySeriesRef.current.update({ time: lastDataPoint.time, value: lastDataPoint.niftySpot });
                optionSeriesRef.current.update({ time: lastDataPoint.time, value: lastDataPoint.optionPremium });
                alphaSeriesRef.current.update({ time: lastDataPoint.time, value: lastDataPoint.alpha });
            }
        }
    }, [data]);

    return (
        <div style={{width: '100%', height: '500px'}}>
            <Chart
                ref={chartRef}
                options={{
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
                }}
            />
        </div>
    );
};

export default TradingChart;
