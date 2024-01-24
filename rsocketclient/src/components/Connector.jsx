
import { useEffect, useState} from 'react';
import axios from 'axios';
import FingerprintJS from '@fingerprintjs/fingerprintjs';

function Connector(props) {
    const url = props.url;
    const setFingerprint = props.setFingerprint;

    const getVisitorId = async () => {
        try {
            const fp = await FingerprintJS.load();
            const result = await fp.get();
            setFingerprint(result.visitorId);
            return result.visitorId; 
        } catch (error) {
            console.error("Error loading FingerprintJS", error);
        }
    };

    // const initMap = async (fingerprint) => {
    //     const postUrl = `${url}/api/fingerprint/${fingerprint}`;
    //     try {
    //       const response = await axios.post(postUrl);
    //       // Handle the response data in here
    //     } catch (error) {
    //       console.error('Error sending fingerprint:', error);
    //     }
    // };

    // const handleInit = async () => {
    //     try {
    //       const fingerprint = await getVisitorId();
    //       if (fingerprint) {
    //         await initMap(fingerprint);
    //       }
    //     } catch (error) {
    //       console.error('Error initializing:', error);
    //     }
    //   };

    useEffect(() => {
      getVisitorId();
    }, []);

    return (
        <>
        </>
    );
}

export default Connector;