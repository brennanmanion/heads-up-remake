import { useState } from 'react';
import { Stack, Tab, Tabs } from 'react-bootstrap';
import ByteArrayDemo from './components/ByteArrayDemo';
import ChatDemo from './components/ChatDemo';
import ConnectionIndicator from './components/ConnectIndicator';
import Connector from './components/Connector';
import './App.css';

function App() {
  const [rsocket, setRSocket] = useState(null);
  const [fingerprint, setFingerprint] = useState(null);

  return (
    <>
      <Stack className="col-md-5 mx-auto" gap={3}>
        <Connector setFingerprint={newFingerprint => setFingerprint(newFingerprint)} fingerprint={fingerprint} setRSocket={newRSocket => setRSocket(newRSocket)} rsocket={rsocket}></Connector>
        <ChatDemo fingerprint={fingerprint} rsocket={rsocket}></ChatDemo>
      </Stack>
    </>
  );
}

export default App;
