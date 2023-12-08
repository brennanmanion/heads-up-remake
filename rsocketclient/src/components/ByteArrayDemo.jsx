import { useEffect, useState } from 'react';
import { Button, Form, Stack } from 'react-bootstrap';
import { encodeRoute } from 'rsocket-core';
import { Flowable } from 'rsocket-flowable';
import FlowableConsumer from '../classes/FlowableConsumer';
import PayloadUtils from '../classes/PayloadUtils'

function ByteArrayDemo(props) {
    const [message, setMessage] = useState('1,2,3');
    const [responses, setResponses] = useState([]);
    const [parsedArray, setParsedArray] = useState([]);

    const rsocket = props.rsocket;

    useEffect(() => {
        const arr = PayloadUtils.parseArrayStr(message);
        setParsedArray(arr);
    }, [message]);


    const sendMessage = (route) => {
        //in order to make Request-Channel call we need to prepare 2 things:
        //- metadata, which holds only the route information in our simple case
        //- request payload, which in Request-Channel contract must be of type Flowable
        const metadata = encodeRoute(route);

        //rsocket-js uses the following data structure in its req/resp API
        //{
        //  data: <payload>,
        //  metadata: <metadata>
        //}  
        //irrespective of the data communication type.
        //in case of byte[] payload type the actual <payload> must be of type Buffer (not just Array)
        //that is why, when we construct "payloads" structure, the "data" field is wrapped into Buffer.from(...) call
        //ultimately, our "payloads" look like
        //[
        //   {
        //      data: Buffer.from(UInt8Array),
        //      metadata: <metadata>
        //   }  
        //]  
        const payloads = PayloadUtils.createPayloadSequence(metadata, parsedArray);
        //we convert our static "pyloads" array into Flowable instance (analogue of Flux in Java) to pass to rsocket.requestChannel
        const flowablePayload = Flowable.just(...payloads);

        //"consumer" is responsible for handling reactive callbacks from Request-Channel stream. See FlowableConsumer for details.
        const consumer = new FlowableConsumer(resp => {
            setResponses(prevResponses => [...prevResponses, resp]);
        });

        //similar to what we saw in connect flaw, calling rsocket.requestChannel() does not perform any message exchange yet
        // - it just prepares reactive pipeline for future execution
        const requestChannelSubscription = rsocket.requestChannel(flowablePayload);
        //...and calling subsribe() here is what actually triggers the message exchange
        requestChannelSubscription.subscribe(consumer);
    };

    const clearResponsesLog = () => {
        setResponses([]);
    }

    return (
        <Stack className="mx-auto" gap={3}>
            <div>
                <Form.Label>Payload</Form.Label>
                <Form.Control type="text" value={message} onChange={(e) => setMessage(e.target.value)} />
                <Form.Label>Parsed Uint8Array to be sent</Form.Label>
                <Form.Control type="text" readOnly value={parsedArray.join(',')} />
            </div>
            <Button variant="primary" onClick={() => sendMessage('hello')} disabled={rsocket === null}>Send message</Button>
            <Button variant="secondary" onClick={clearResponsesLog} disabled={responses.length === 0}>Clear responses log</Button>
            <div>Responses:</div>
            {responses.map((resp, i) => <div key={i}>{resp.join(',')}</div>)}
        </Stack>
    );
}

export default ByteArrayDemo;