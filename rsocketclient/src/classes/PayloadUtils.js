class PayloadUtils {
    static parseArrayStr(arrayStr) {
        const tokens = arrayStr.split(',');
        const arr = new Uint8Array(tokens.length);
        for (let i = 0; i < tokens.length; i ++) {
          arr[i] = parseInt(tokens[i]);
        }
        return arr;
    }

    static createPayloadSequence(metadata, basePayload) {
        const payloads = [];
        for (let i = 0; i < 3; i ++) {
          let dataArr;
          if (i === 0) {
            dataArr = basePayload;
          } else {
            const prevDataArr = payloads[payloads.length - 1].data;
            dataArr = new Uint8Array(prevDataArr.length + 1);
            dataArr.set(prevDataArr, 0);
            dataArr[dataArr.length - 1] = prevDataArr[prevDataArr.length - 1] + 1;
          }

          payloads.push({
            data: Buffer.from(dataArr),
            metadata: metadata
          });
        }
        return payloads;
    }
}

export default PayloadUtils;