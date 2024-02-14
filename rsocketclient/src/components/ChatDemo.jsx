import { useEffect, useState, useRef } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import axios from 'axios';
import CountdownComponent from './CountdownComponent';

function ChatDemo(props) {
    const [message, setMessage] = useState('');
    const [prompt, setPrompt] = useState('');
    const [isPermissionGranted, setIsPermissionGranted] = useState(false);
    const [acceleration, setAcceleration] = useState({ x: 0, y: 0, z: 0 });
    const [isCounting, setIsCounting] = useState(false);
    const [beta, setBeta] = useState(null);

    const fingerprint = props.fingerprint;
    const url = props.url;
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
        if (audioUnlocked && audioRef.current && isCounting) {
            audioRef.current.play().catch(error => console.log('Error playing the audio:', error));
        }
    };

    useEffect(() => {
        // Define the threshold for detecting downward motion
        const downwardMotionThreshold = 150; // Adjust this value based on testing

        const throttleInterval = 1000;

        // Check if the device is moving downwards
        if (isCounting && Math.abs(beta) > downwardMotionThreshold) {
            const now = Date.now();
            if (now - lastCallTime.current > throttleInterval) {
                chatRelease(axios, fingerprint); // Call your server function
                if (audioUnlocked)
                {
                    playAudio();
                }
                lastCallTime.current = now; // Update the last call time
            }
        }
    }, [acceleration, audioUnlocked, playAudio, axios, fingerprint]); // This effect runs whenever the acceleration state changes

    const sendMessage = () => {
        const postUrl = `${url}/api/fingerprint`;
        const data = {};
        data['message'] = message;
        data['fingerprint'] = fingerprint;
        setMessage('');
        console.log(data);
        axios.post(postUrl, data);
    };

    const chatRelease = async (axios, fingerprint) => {
        const getUrl = `${url}/api/fingerprint/${fingerprint}`;
        const resp = await axios.get(getUrl);
        if (resp)
        {
            setPrompt(resp.data.response);
        }
    };  
    
    const startCountdown = () => {
        setIsCounting(true);
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
    }, [axios]);

    return (
        <Stack className="mx-auto" gap={3}>
            <audio ref={audioRef} preload="auto">
            <source src="https://cdn.pixabay.com/audio/2022/03/10/audio_8cdc56bad0.mp3" type="audio/mpeg" />
            Your browser does not support the audio element.
            </audio>
            {!isCounting && (
            <>
                <Form.Control type="text" placeholder="Write Prompts here" value={message} onChange={(e) => setMessage(e.target.value)} />
                <Button variant="primary" onClick={() => sendMessage()}>Send chat message</Button>
                {!audioUnlocked && (
                    <button onClick={unlockAudio}>Unlock Audio</button>
                )}
                {!isPermissionGranted && (
                    <button onClick={requestPermission}>Enable Motion Detection</button>
                )}
                <Button variant={audioUnlocked && isPermissionGranted ? "success" : "danger"} onClick={() => startCountdown()}>{audioUnlocked && isPermissionGranted ? "Start Countdown!" : "Grant Permissions!"}</Button>
            </>
            )}
            {isCounting && (
                <CountdownComponent setPrompt={prompt => setPrompt(prompt)} setIsCounting={isCounting => setIsCounting(isCounting)}></CountdownComponent>
            )}
            <h1 style={{ width: '100%', fontSize: '10vw' }}>{prompt}</h1>
        </Stack>
    );
}

export default ChatDemo;