import React, { useState, useEffect } from 'react';

const CountdownComponent = (props) => {
    const setIsCounting = props.setIsCounting;
    const timeOriginal = new Date().getTime();
    const [count, setCount] = useState(90); // Initialize count to 90
    // alert(timeOriginal);
    useEffect(() => {
    // Set up the interval
    const interval = setInterval(() => {
        setCount(() => {
            const timeElapsed = Math.floor((new Date().getTime() - timeOriginal)/1000);
            // alert(timeElapsed);
            if (timeElapsed < count) {
                return count - timeElapsed; // Reduce count by one
            } else {
                clearInterval(interval); // Clear interval if count reaches 0
                setIsCounting(false);
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