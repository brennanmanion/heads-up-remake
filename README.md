# Overview

This POC shows implementation of RSocket JavaScript client in context of React application talking to 
RSocket server based on Spring Boot.

# System requirements

- JDK 17+
- Node 18+
- Docker (required only if container packaging is used to run the solution)

# Technology stack

- Client
  - React 17.0.2
  - rsocket-js [v0.0.27](https://github.com/rsocket/rsocket-js/tree/v0.0.27)
- Server
  - Java 17 
  - Spring Boot 3.0.2
  - spring-boot-starter-rsocket

# Packaging

The POC is packaged into Docker compose application, with a separate container for client and server parts.

[docker-compose](docker-compose.yml) is used both for local operation and for ECS deployment. The need to support
AWS has introduced few specific changes, like `x-aws-protocol` property to force NLB and `x-aws-cloudformation`
section at the bottom.

# Running

There are two options to run the POC: build and start client and server manually or use Docker compose.
The options are mutually exclusive because the allocated ports are the same (3000/8080).

## Running via Docker compose

To build and start the POC run: 
```shell
docker compose build
docket compose up
```
To stop, hit `Ctrl-C` or run
```shell
docket compose down
```

# Running client and server individually

To start the server, run
```shell
cd rsocketserver
mvn clean install
java -jar target/rsocketserver-0.0.1-SNAPSHOT.jar
```
Start the client with
```shell
cd rsocketclient
npm install
npm start
```

# Exposed endpoints

The POC exposes the following endpoints:
- client: http://localhost:3000/
- server: http://localhost:8080/

# RSocket client implementation details

## rsocket-js versions

The JavaScript implementation of RSocket protocol is provided by [rsocket-js](https://github.com/rsocket/rsocket-js) library.
rsocket-js comes in two flavours:
- stable, but old [v0.0.27](https://github.com/rsocket/rsocket-js/tree/v0.0.27) version
- [unstable](https://github.com/rsocket/rsocket-js#status) and not well documented 1.0.x-alpha

This POC takes conservative approach and builds on top of v0.0.27.

## Dependencies

The key dependencies added to the client are
- `rsocket-core@0.0.27`: main RSocket type definitions, also transitively includes 
[Flawable](https://rsocket.io/guides/rsocket-js/rsocket-flowable/) API
- `rsocket-websocket-client@0.0.27`: WebSocket-based communication transport. In fact this is the only option available
for the browser-based RSocket client; another TCP transport included in rsocket-js is suitable for Node.js only
- `buffer@6.0.3`: ```rsocket-core` depends on [Buffer](https://nodejs.org/api/buffer.html) Node.js API, which is not available of course
in web browser. So we have to include [buffer](https://www.npmjs.com/package/buffer) polyfill.

## Initial connect and message exchange

Refer to inline comments in [App.js](rsocketclient/src/App.js) and [FlowableConsumer.js](rsocketclient/src/classes/FlowableConsumer.js)
for detailed description.

Key takeaways:
- rsocket-js uses `Buffer` type for payload in case of binary communication
- `subscrube(...)` call is crucial to trigger actual action, without it we are just setting up the pipeline which sits idle
