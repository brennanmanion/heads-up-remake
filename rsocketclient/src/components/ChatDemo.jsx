import { useEffect, useState } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import { encodeRoute } from 'rsocket-core';
import FlowableConsumer from '../classes/FlowableConsumer';

function ChatDemo(props) {
    const [message, setMessage] = useState('Hello everybody!');
    const [responses, setResponses] = useState([]);
    const [prompt, setPrompt] = useState('');

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

    const sendMessage = () => {
        const metadata = encodeRoute('chatSend');

        const obj = {};
        obj['message'] = message;
        obj['token'] = fingerprint;

        rsocket.fireAndForget({
            data: Buffer.from(JSON.stringify(obj)),
            metadata: metadata
        });
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

    const initMap = () => {
        const metadata = encodeRoute('initMap');

        rsocket.fireAndForget({
            data: Buffer.from(fingerprint),
            metadata: metadata
        });
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
            <Form.Control type="text" value={message} onChange={(e) => setMessage(e.target.value)} />
            <Button variant="primary" onClick={() => sendMessage()} disabled={rsocket === null}>Send chat message</Button>
            <div>Responses:</div>
            {responses.map((resp, i) => <div key={i}>{resp}</div>)}
            <Button variant="primary" onClick={() => initMap()} disabled={rsocket === null}>Release Chat Message</Button>
            <h1>{prompt}</h1>
            {panelAcceleration}
        </Stack>
    );
}

export default ChatDemo;