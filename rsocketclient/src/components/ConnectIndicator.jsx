function ConnectIndicator(props) {
    const rsocket = props.rsocket;

    return (
        <div>Conected: {rsocket !== null ? 'yes' : 'no'}</div>
    );
}

export default ConnectIndicator;