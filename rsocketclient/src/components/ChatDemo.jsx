import { useEffect, useState, useRef } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import { encodeRoute } from 'rsocket-core';
import FlowableConsumer from '../classes/FlowableConsumer';

function ChatDemo(props) {
    const [message, setMessage] = useState('');
    const [responses, setResponses] = useState([]);
    const [prompt, setPrompt] = useState('');
    const [countdown, setCountdown] = useState(null);
    const [isPermissionGranted, setIsPermissionGranted] = useState(false);
    const [acceleration, setAcceleration] = useState({ x: 0, y: 0, z: 0 });
    const [beta, setBeta] = useState(null);

    const rsocket = props.rsocket;
    const fingerprint = props.fingerprint;
    const lastCallTime = useRef(Date.now());

    const [audioUnlocked, setAudioUnlocked] = useState(false);
    const audioRef = useRef();

    const unlockAudio = () => {
        // Play and immediately pause the audio to unlock it
        audioRef.current.play()
            .then(() => {
                audioRef.current.pause();
                audioRef.current.currentTime = 0; // Reset audio to start
                setAudioUnlocked(true);
            })
            .catch(error => console.log('Error unlocking the audio:', error));
    };

    const playAudio = () => {
        if (audioUnlocked && audioRef.current) {
            audioRef.current.play().catch(error => console.log('Error playing the audio:', error));
        }
    };

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
        const downwardMotionThreshold = 150; // Adjust this value based on testing

        const throttleInterval = 1000;

        // Check if the device is moving downwards
        if (countdown && Math.abs(beta) > downwardMotionThreshold) {
            const now = Date.now();
            if (now - lastCallTime.current > throttleInterval) {
                chatRelease(); // Call your server function
                if (audioUnlocked)
                {
                    playAudio();
                }
                lastCallTime.current = now; // Update the last call time
            }
        }
    }, [rsocket, acceleration, audioUnlocked, playAudio]); // This effect runs whenever the acceleration state changes

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

            const obj = {};
            obj['fingerprint'] = fingerprint;

            const requestChannelSubscription = rsocket.requestStream({
                metadata: metadata,
                data: Buffer.from(JSON.stringify(obj))
            });
            requestChannelSubscription.subscribe(consumer);
        }
    };  
    
    const startCountdown = () => {
        if (rsocket !== null) {
            const metadata = encodeRoute('countdown');

            const consumer = new FlowableConsumer(resp => {
                setCountdown(resp.toString() === '0' ? null : resp.toString());
            });

            const obj = {};
            obj['fingerprint'] = fingerprint;

            const requestChannelSubscription = rsocket.requestStream({
                metadata: metadata,
                data: Buffer.from(JSON.stringify(obj))
            });
            requestChannelSubscription.subscribe(consumer);
        }
    };  

    const panelAcceleration = (
        <>
            <p>Acceleration X: {acceleration.x.toFixed(2)}</p>
            <p>Acceleration Y: {acceleration.y.toFixed(2)}</p>
            <p>Acceleration Z: {acceleration.z.toFixed(2)}</p>
            <p>Beta : {beta}</p>
        </>
    );

    const requestPermission = () => {
        if (typeof DeviceMotionEvent.requestPermission === 'function') {
            DeviceMotionEvent.requestPermission()
                .then(permissionState => {
                    if (permissionState === 'granted') {
                        setIsPermissionGranted(true);
                        window.addEventListener('devicemotion', handleMotionEvent);
                    } else {
                        alert('Device Motion Permission Denied');
                    }
                })
                .catch(console.error);
        } else {
            setIsPermissionGranted(true);
            window.addEventListener('devicemotion', handleMotionEvent);
        }
    };

    const handleMotionEvent = (event) => {
        // const { x, y, z } = event.accelerationIncludingGravity;
        const { x, y, z } = event.acceleration;
        // const { alpha, beta, gamma } = event.rotationRate;
        const beta = event.rotationRate.beta;

        setAcceleration({ x, y, z });
        setBeta(beta);
    };

    useEffect(() => {

        if (isPermissionGranted) {
            window.addEventListener('devicemotion', handleMotionEvent);
        }

        return () => {
            if (isPermissionGranted) {
                window.removeEventListener('devicemotion', handleMotionEvent);
            }
        };
    }, []);

    return (
        <Stack className="mx-auto" gap={3}>
            <audio ref={audioRef} preload="auto">
            <source src="https://cdn.pixabay.com/audio/2022/03/10/audio_8cdc56bad0.mp3" type="audio/mpeg" />
            Your browser does not support the audio element.
            </audio>
            {!countdown && (
            <>
                <Form.Control type="text" placeholder="Write Prompts here" value={message} onChange={(e) => setMessage(e.target.value)} />
                <Button variant="primary" onClick={() => sendMessage()} disabled={rsocket === null}>Send chat message</Button>
                {!audioUnlocked && (
                    <button onClick={unlockAudio}>Unlock Audio</button>
                )}
                {!isPermissionGranted && (
                    <button onClick={requestPermission}>Enable Motion Detection</button>
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