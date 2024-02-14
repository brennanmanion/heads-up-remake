import { useState } from 'react';
import { Stack} from 'react-bootstrap';
import ChatDemo from './components/ChatDemo';
import Connector from './components/Connector';
import './App.css';

function App() {
  const url = 'https://697a-2600-100e-b036-a24f-6c23-b635-a73b-a77f.ngrok-free.app';
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
