import { useState } from 'react';
import { Stack, Tab, Tabs } from 'react-bootstrap';
import ByteArrayDemo from './components/ByteArrayDemo';
import ChatDemo from './components/ChatDemo';
import ConnectionIndicator from './components/ConnectIndicator';
import Connector from './components/Connector';
import './App.css';

function App() {
  const [rsocket, setRSocket] = useState(null);

  return (
    <>
      <Stack className="col-md-5 mx-auto" gap={3}>
        <ConnectionIndicator rsocket={rsocket}></ConnectionIndicator>
        <Connector setRSocket={newRSocket => setRSocket(newRSocket)} rsocket={rsocket}></Connector>
        <hr/>
        <Tabs transition={false}>
          <Tab eventKey="keyChat" title="Chat">
            <ChatDemo rsocket={rsocket}></ChatDemo>
          </Tab>
          <Tab eventKey="keyByteArrays" title="Byte arrays">
            <ByteArrayDemo rsocket={rsocket}></ByteArrayDemo>
          </Tab>
        </Tabs>
      </Stack>
    </>
  );
}

export default App;
