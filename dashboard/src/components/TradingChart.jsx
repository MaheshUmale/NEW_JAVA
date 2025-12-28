import React, { useEffect, useRef } from 'react';
import { createChart } from 'lightweight-charts';

const TradingChart = ({ niftyData, optionData, alphaData }) => {
    const chartContainerRef = useRef();
    const chartRef = useRef();
    const niftySeriesRef = useRef();
    const optionSeriesRef = useRef();
    const alphaSeriesRef = useRef();

    useEffect(() => {
        if (chartContainerRef.current && !chartRef.current) {
            chartRef.current = createChart(chartContainerRef.current, {
                width: chartContainerRef.current.clientWidth,
                height: 500,
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
            });

            niftySeriesRef.current = chartRef.current.addLineSeries({
                color: '#00ff88',
                title: 'Nifty Spot',
                priceScaleId: 'left',
            });
            optionSeriesRef.current = chartRef.current.addLineSeries({
                color: '#ff4d4d',
                title: 'Option Premium',
                priceScaleId: 'right',
            });
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
    }, []);

    useEffect(() => {
        if (niftySeriesRef.current && niftyData) {
            niftySeriesRef.current.setData(niftyData);
        }
        if (optionSeriesRef.current && optionData) {
            optionSeriesRef.current.setData(optionData);
        }
        if (alphaSeriesRef.current && alphaData) {
            alphaSeriesRef.current.setData(alphaData);
        }
    }, [niftyData, optionData, alphaData]);

    return <div ref={chartContainerRef} style={{ width: '100%', height: '500px' }} />;
};

export default TradingChart;
