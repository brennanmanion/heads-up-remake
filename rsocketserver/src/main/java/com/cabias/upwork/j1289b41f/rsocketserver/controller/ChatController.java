package com.cabias.upwork.j1289b41f.rsocketserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.Map;

@Controller
@Slf4j
public class ChatController {
    private final Sinks.Many<byte[]> commonMessageSink = Sinks.many().multicast().directBestEffort();

    @ConnectMapping
    public void connect(@Headers Map<String, Object> headers) {
        System.out.println("connect");
        headers.forEach((key, value) -> System.out.println("connect header {" + key + "} = {" + value + "}"));
        commonMessageSink.tryEmitNext("someone connected".getBytes());
    }

    @MessageMapping("chatSend")
    public void chatSend(byte[] messagePayload) {
        String message = new String(messagePayload);
        System.out.println("'chatSend' route called: {}" + message);
        commonMessageSink.tryEmitNext(("someone said: " + message).getBytes());
    }

    @MessageMapping("chatReceive")
    public Flux<byte[]> chatReceive() {
        System.out.println("'chatReceive' route called");
        return commonMessageSink.asFlux();
    }
}
