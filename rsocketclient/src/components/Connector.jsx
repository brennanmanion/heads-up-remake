import { Button } from 'react-bootstrap';

import {
    APPLICATION_OCTET_STREAM,
    MESSAGE_RSOCKET_ROUTING,
    BufferEncoders,
    IdentitySerializer,
    RSocketClient
} from 'rsocket-core';
import RSocketWebSocketClient from 'rsocket-websocket-client';

function Connector(props) {
    const rsocket = props.rsocket;
    const setRSocket = props.setRSocket;

    const connect = () => {
        // const wsUrl = 'ws://' + window.location.hostname + ':6565/';
        // const wsUrl = 'ws://' + window.location.hostname + ':8080/';
        const wsUrl = 'wss://' + '87d6-66-186-201-150.ngrok-free.app';
        // const wsUrl = 'ws://' + '34.207.91.22' + ':8080/';


        //we tell RSocket that our data will be low-level byte arrays by setting dataMimeType to APPLICATION_OCTET_STREAM

        //we use MESSAGE_RSOCKET_ROUTING as metadataMimeType because the only metadata we want to pass to the server is
        //request route. Uf we would like to pass more complex metadata (e.g. include authorization information in addition
        //to routing) the mime type would be different, and he the way we construct metadata would be different too.

        //IdentitySerializer are pass-through serializers. As long as we use low-level byte array as data mime type,
        //RSocket API expects application logic to send and receive byte[], which in turn means that the RSocket itself
        //does not have to do any (de)seriawlization.

        //by default, rsocket-js uses Utf8Encoders for data encoding which is not what we want in case of byte[] payload type:
        //there is nothing to encode and decode, rsocket-js has to simple pass our raw data in and out.
        //so we have to pass the second argument to RSocketWebSocketClient explicitly as BufferEncoders.

        //Note that no connection gets established when we create RSocketClient instance here - it is only a preparation step.
        const client = new RSocketClient({
            serializers: {
                data: IdentitySerializer,
                metadata: IdentitySerializer
            },
            setup: {
                dataMimeType: APPLICATION_OCTET_STREAM.string,
                keepAlive: 10000, //ms
                lifetime: 100000, //ms
                metadataMimeType: MESSAGE_RSOCKET_ROUTING.string
            },
            transport: new RSocketWebSocketClient({
                url: wsUrl
            }, BufferEncoders),
        })

        //when we call connect, we get back a Single instance back (from Flowable API - analogue of Mono in Java)
        //at this point the client does not even try to establish the connedtion yet: we are in reactive world, so
        //the reactive pipeline established here gets triggered only when we call subsribe()
        const connectionSubscription = client.connect();

        //and this is where the actual connection gets established - only after we call Single.subscribe() and pass
        //IFutureSubscriber instance.
        //when connection is ready, onComplete is triggered and we finally get hold on "rsocket" instance - the one
        //we will use later to communicate with the server.
        connectionSubscription.subscribe({
            onComplete: rsocket => {
                console.log('RSocketClient: connected');
                setRSocket(rsocket)
            },
            onSubscribe: () => {
                console.log('RSocketClient: onSubscribe');
            },
            onError: err => {
                console.log('RSocketClient: onError: ' + err);
            }
        });
    };

    const disconnect = () => {
        //calling rsocket.close() (RSocketClientSocket) here automatically closes the underlying WebSocket so there is no 
        //need to repeat it with  "client.close()" call.
        rsocket.close();
        setRSocket(null);
    };



    return (
        <>
            <Button variant="primary" onClick={connect} disabled={rsocket !== null}>Connect to RS server</Button>
            <Button variant="secondary" onClick={disconnect} disabled={rsocket === null}>Disconnect from RS server</Button>
        </>
    );
}

export default Connector;