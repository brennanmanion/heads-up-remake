//One of the purposes of this class is to maintain a context to store the "subscription" instance.
//We receive "subscription" in the onSubscribe(), but need to use it later in onNext() to request
//next data chunks.
class FlowableConsumer {
    handler;
    subscription = null;

    constructor(handler) {
        this.handler = handler;
    }

    onSubscribe(subscription) {
        console.log('FlowableConsumer: onSubscribe');
        this.subscription = subscription;
        //naive back pressure: we are always ready to receive exactly one data chunk.
        this.subscription.request(1);
    }

    onNext(resp) {
        console.log('FlowableConsumer: onNext');
        this.handler(resp.data);
        //naive back pressure: we are always ready to receive exactly one data chunk.
        this.subscription.request(1);
    }

    onError(err) {
        console.log('FlowableConsumer: onError: ' + err);
    }
}

export default FlowableConsumer;