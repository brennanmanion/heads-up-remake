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
  const [acceleration, setAcceleration] = useState({ x: 0, y: 0, z: 0 });
  const [beta, setBeta] = useState();


  return (
    <>
      <Stack className="col-md-5 mx-auto" gap={3}>
        <Connector setBeta={newBeta => setBeta(newBeta)} beta={beta} setAcceleration={newAcceleration => setAcceleration(newAcceleration)} acceleration={acceleration} setFingerprint={newFingerprint => setFingerprint(newFingerprint)} fingerprint={fingerprint} setRSocket={newRSocket => setRSocket(newRSocket)} rsocket={rsocket}></Connector>
        <ChatDemo beta={beta} acceleration={acceleration} fingerprint={fingerprint} rsocket={rsocket}></ChatDemo>
      </Stack>
    </>
  );
}

export default App;
