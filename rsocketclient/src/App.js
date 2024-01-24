import { useState } from 'react';
import { Stack, Tab, Tabs } from 'react-bootstrap';
import ChatDemo from './components/ChatDemo';
import Connector from './components/Connector';
import './App.css';

function App() {
  const url = 'https://31e1-66-186-201-150.ngrok-free.app';
  const [fingerprint, setFingerprint] = useState(null);

  return (
    <>
      <Stack className="col-md-5 mx-auto" gap={3}>
        <Connector url={url} setFingerprint={newFingerprint => setFingerprint(newFingerprint)}></Connector>
        <ChatDemo url={url} fingerprint={fingerprint}></ChatDemo>
      </Stack>
    </>
  );
}

export default App;
