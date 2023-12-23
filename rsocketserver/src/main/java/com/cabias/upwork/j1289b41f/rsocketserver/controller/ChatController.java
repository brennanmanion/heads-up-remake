package com.cabias.upwork.j1289b41f.rsocketserver.controller;

import lombok.extern.slf4j.Slf4j;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.Headers;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.rsocket.RSocketRequester;
import org.springframework.messaging.rsocket.annotation.ConnectMapping;
import org.springframework.stereotype.Controller;

import com.cabias.upwork.j1289b41f.rsocketserver.service.YourService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@Slf4j
public class ChatController {
    private final Sinks.Many<byte[]> commonMessageSink = Sinks.many().multicast().directBestEffort();
    private final List<byte[]> messages = new ArrayList<>();
    private final HashMap<String, List<String>> inputMap = new HashMap<>();
    private final HashMap<String, RSocketRequester> requesters = new HashMap<>();
    
    @Autowired
    private YourService yourService;
    
    @ConnectMapping
    public void connect(@Headers Map<String, Object> headers) {
        System.out.println("connect");
        headers.forEach((key, value) -> System.out.println("connect header {" + key + "} = {" + value + "}"));
        commonMessageSink.tryEmitNext("someone connected".getBytes());
        requesters.put("rsocketRequester", (RSocketRequester) headers.get("rsocketRequester"));
    }

    @MessageMapping("initMap")
    public void initMap(byte[] messagePayload) {
    	final String obj = new String(messagePayload);
    	JSONObject jsonObject = new JSONObject(obj);
    	 if(jsonObject != null && jsonObject.has("fingerprint") && jsonObject.get("fingerprint") instanceof String && !jsonObject.getString("fingerprint").isBlank())
    	 {
    		 final String fingerprint = jsonObject.getString("fingerprint");
    		 if (!inputMap.containsKey(fingerprint))
    		 {
    			 inputMap.put(fingerprint, new ArrayList<>());
    		 }
    	 }

    }
    
    @MessageMapping("chatSend")
    public void chatSend(byte[] messagePayload) {
        String obj = new String(messagePayload);
        JSONObject jsonObject = new JSONObject(obj);
        System.out.println("'chatSend' route called: " + jsonObject);
        if(jsonObject != null && jsonObject.has("message") && jsonObject.get("message") instanceof String && !jsonObject.getString("message").isBlank())
        {
        	final byte[] msg = jsonObject.getString("message").getBytes(); 
        	final String message = new String(msg);
            messages.add(msg);
            commonMessageSink.tryEmitNext(("someone said: " + message).getBytes());
            
            if(jsonObject != null && jsonObject.has("fingerprint") && jsonObject.get("fingerprint") instanceof String && !jsonObject.getString("fingerprint").isBlank())
            {
            	final String fingerprint = jsonObject.getString("fingerprint");
            	if (inputMap.containsKey(fingerprint))
            	{
            		inputMap.get(fingerprint).add(message);
            	}
            }
//            yourService.someMethod(message);
        }
    }

    @MessageMapping("chatReceive")
    public Flux<byte[]> chatReceive() {
        System.out.println("'chatReceive' route called");
        return commonMessageSink.asFlux();
    }
    
    @MessageMapping("chatRelease")
    public Flux<byte[]> chatRelease(byte[] messagePayload) {
        System.out.println("'chatRelease' route called");
        final List<byte[]> list = new ArrayList<>();
        if (!messages.isEmpty())
        {
        	list.add(messages.get(0));
        	messages.remove(0);
        }
        return Flux.fromIterable(list);
    }
    
    @MessageMapping("countdown")
    public Flux<byte[]> countdown(byte[] messagePayload) {
        // Start at 60, decrement every second, take 61 elements (60 to 0 inclusive)
        return Flux.interval(Duration.ofSeconds(1)) // Emits every second
                    .map(tick -> 30 - tick) // Transform the tick to the countdown number
                    .take(31) // Take 61 elements (from 60 to 0)
                    .map(number -> String.valueOf(number).getBytes()); // Convert the number to a byte array
    }
}
