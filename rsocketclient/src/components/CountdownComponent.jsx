import React, { useState, useEffect } from 'react';

const CountdownComponent = (props) => {
    const setIsCounting = props.setIsCounting;
    const setPrompt = props.setPrompt;
    const [count, setCount] = useState(90); // Initialize count to 90

    useEffect(() => {
        let wakeLock = null;

        const requestWakeLock = async () => {
            try {
                wakeLock = await navigator.wakeLock.request('screen');
                wakeLock.addEventListener('release', () => {
                    console.log('Screen Wake Lock was released');
                });
                console.log('Screen Wake Lock is active');
            } catch (err) {
                console.error(`${err.name}, ${err.message}`);
            }
        };

        requestWakeLock();

        return () => {
            wakeLock && wakeLock.release();
            wakeLock = null;
        };
    }, []);    

    useEffect(() => {
    // Set up the interval
    const interval = setInterval(() => {
        setCount((currentCount) => {
            if (currentCount > 0) {
                return currentCount - 1; // Reduce count by one
            } else {
                clearInterval(interval); // Clear interval if count reaches 0
                setIsCounting(false);
                setPrompt('');
                return 0; // Set count to 0
            }
        });
    }, 1000); // Decrement count every 1000ms (1 second)

    // Clean up the interval on component unmount
    return () => {
        clearInterval(interval);
    };
    }, []); // Empty dependency array means the effect runs once on mount

    return (
        <div>
            <h1>{count}</h1>
        </div>
    );
};

export default CountdownComponent;