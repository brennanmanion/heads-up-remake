import { useEffect, useState } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import { encodeRoute } from 'rsocket-core';
import FlowableConsumer from '../classes/FlowableConsumer';

function ChatDemo(props) {
    const [message, setMessage] = useState('Hello everybody!');
    const [responses, setResponses] = useState([]);
    const [prompt, setPrompt] = useState('');

    const rsocket = props.rsocket;

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

    const sendMessage = (route) => {
        const metadata = encodeRoute(route);

        rsocket.fireAndForget({
            data: Buffer.from(message),
            metadata: metadata
        });
    };

    const chatRelease = () => {
        const metadata = encodeRoute('chatRelease');
        const message = 'someToken';

        const consumer = new FlowableConsumer(resp => {
            setPrompt(resp.toString());
        });

        const requestChannelSubscription = rsocket.requestStream({
            data: Buffer.from(message),
            metadata: metadata
        });
        requestChannelSubscription.subscribe(consumer);
    }

    return (
        <Stack className="mx-auto" gap={3}>
            <Form.Control type="text" value={message} onChange={(e) => setMessage(e.target.value)} />
            <Button variant="primary" onClick={() => sendMessage('chatSend')} disabled={rsocket === null}>Send chat message</Button>
            <div>Responses:</div>
            {responses.map((resp, i) => <div key={i}>{resp}</div>)}
            <h1>{prompt}</h1>
        </Stack>
    );
}

export default ChatDemo;