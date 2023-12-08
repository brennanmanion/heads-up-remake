package com.cabias.upwork.j1289b41f.rsocketserver.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.stream.IntStream;

@Controller
@Slf4j
public class SampleController {
    @MessageMapping("hello")
    public Flux<byte[]> responseStream(Flux<byte[]> rsMessageStream) {
        System.out.println("'hello' route called");
        return rsMessageStream
                .log()
                .flatMap(rsMessage -> Flux.range(1, 2)
                        .map(cnt -> {
                            byte[] resp = new byte[rsMessage.length];
                            IntStream.range(0, rsMessage.length)
                                    .forEach(i -> {
                                        resp[rsMessage.length - i - 1] = (byte) (rsMessage[i] + cnt);
                                    });
                            return resp;
                        }))
                .delayElements(Duration.ofMillis(500));
    }
}
