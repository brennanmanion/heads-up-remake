import { useEffect, useState } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import { encodeRoute } from 'rsocket-core';
import FlowableConsumer from '../classes/FlowableConsumer';

function ChatDemo(props) {
    const [message, setMessage] = useState('Hello everybody!');
    const [responses, setResponses] = useState([]);
    const [prompt, setPrompt] = useState('');
    const [countdown, setCountdown] = useState('');

    const rsocket = props.rsocket;
    const fingerprint = props.fingerprint;
    const acceleration = props.acceleration;

    useEffect(() => {
        if (rsocket !== null) {
            const metadata = encodeRoute('chatReceive');

            const consumer = new FlowableConsumer(resp => {
                setResponses(prevResponses => [...prevResponses, resp.toString()]);
            });

            const requestChannelSubscription = rsocket.requestStream({
                metadata: metadata
            });
            requestChannelSubscription.subscribe(consumer);
        }
    }, [rsocket]);

    useEffect(() => {
        // Define the threshold for detecting downward motion
        const downwardMotionThreshold = 6; // Adjust this value based on testing

        // Check if the device is moving downwards
        if (acceleration.z > downwardMotionThreshold) {
            chatRelease();
        }
    }, [rsocket, acceleration]); // This effect runs whenever the acceleration state changes

    const sendMessage = () => {
        const metadata = encodeRoute('chatSend');

        const obj = {};
        obj['message'] = message;
        obj['fingerprint'] = fingerprint;

        rsocket.fireAndForget({
            data: Buffer.from(JSON.stringify(obj)),
            metadata: metadata
        });
        setMessage('');
    };

    const chatRelease = () => {
        if (rsocket !== null) {
            const metadata = encodeRoute('chatRelease');

            const consumer = new FlowableConsumer(resp => {
                setPrompt(resp.toString());
            });

            const requestChannelSubscription = rsocket.requestStream({
                metadata: metadata,
                data: Buffer.from(fingerprint)
            });
            requestChannelSubscription.subscribe(consumer);
        }
    };  
    
    const startCountdown = () => {
        if (rsocket !== null) {
            const metadata = encodeRoute('countdown');

            const consumer = new FlowableConsumer(resp => {
                setCountdown(resp.toString() === '0' ? '' : resp.toString());
            });

            const requestChannelSubscription = rsocket.requestStream({
                metadata: metadata,
                data: Buffer.from(fingerprint)
            });
            requestChannelSubscription.subscribe(consumer);
        }
    };  

    const panelAcceleration = (
        <>
            <p>Acceleration X: {acceleration.x.toFixed(2)}</p>
            <p>Acceleration Y: {acceleration.y.toFixed(2)}</p>
            <p>Acceleration Z: {acceleration.z.toFixed(2)}</p>
        </>
    );

    return (
        <Stack className="mx-auto" gap={3}>
            <audio ref={audioRef} preload="auto">
            <source src="https://cdn.pixabay.com/audio/2022/03/10/audio_8cdc56bad0.mp3" type="audio/mpeg" />
            Your browser does not support the audio element.
            </audio>
            {!countdown && (
            <>
                <Form.Control type="text" value={message} onChange={(e) => setMessage(e.target.value)} />
                <Button variant="primary" onClick={() => sendMessage()} disabled={rsocket === null}>Send chat message</Button>
                {!audioUnlocked && (
                    <button onClick={unlockAudio}>Unlock Audio</button>
                )}
                <Button variant="primary" onClick={() => startCountdown()} disabled={rsocket === null}>Start Countdown!</Button>
            </>
            )}
            <h1>{countdown}</h1>
            <h1 style={{ width: '100%', fontSize: '10vw' }}>{prompt}</h1>
        </Stack>
    );
}

export default ChatDemo;