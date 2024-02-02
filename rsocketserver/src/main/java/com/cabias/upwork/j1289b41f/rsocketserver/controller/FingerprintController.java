package com.cabias.upwork.j1289b41f.rsocketserver.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cabias.upwork.j1289b41f.rsocketserver.model.ChatReceiveModel;
import com.cabias.upwork.j1289b41f.rsocketserver.model.ChatSendModel;
import com.cabias.upwork.j1289b41f.rsocketserver.service.YourService;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@CrossOrigin(origins = "https://fbb8-71-205-171-144.ngrok-free.app")
@RestController
@RequestMapping("/api/fingerprint")
public class FingerprintController {

    @Autowired
    private YourService yourService;
    
    private final ConcurrentHashMap<String, Set<String>> outputMap = new ConcurrentHashMap<>();
    
//    @PostMapping("/{fingerprint}")
//    public ResponseEntity<?> getFingerprintMessage(@PathVariable String fingerprint) {
//    	if (fingerprint != null && !fingerprint.isEmpty())
//    	{
//    		outputMap.put(fingerprint, new HashSet<>());
//    	}
//    	return ResponseEntity.ok().build();
//    }

    @PostMapping
    @Consumes(MediaType.APPLICATION_JSON)
    public ResponseEntity<?> createFingerprintMessage(@RequestBody ChatSendModel chatSendModel) {
    	System.out.println(chatSendModel != null ? chatSendModel.getMessage() : null);
    	CompletableFuture.runAsync(() -> yourService.processMapHuggingFace(chatSendModel, outputMap));
    	return ResponseEntity.ok().build();
    }
    
    @GetMapping("/{fingerprint}")
    @Produces(MediaType.APPLICATION_JSON)
    public ChatReceiveModel releaseMessage(@PathVariable String fingerprint) {
    	String response = null;
    	if (fingerprint != null && !fingerprint.isEmpty())
    	{
    		if (outputMap.containsKey(fingerprint))
    		{
    			final Set<String> set = outputMap.get(fingerprint);
    	        if (!set.isEmpty())
    	        {
        			for (String item : set) {
        	        	response = item;
        	            break; // Break after retrieving the first item
        	        }
        	        if (response != null) {
        	            set.remove(response);
        	        }
    	        }
    			else
    			{
    				// its empty, go get more
    			}
    		}
    	}
    	return new ChatReceiveModel(response);
    }
    
}

