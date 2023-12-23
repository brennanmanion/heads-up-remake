package com.cabias.upwork.j1289b41f.rsocketserver.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.cabias.upwork.j1289b41f.rsocketserver.component.OpenAIApiKeyComponent;

@Service
public class YourService {

    private final OpenAIApiKeyComponent apiKeyComponent;

    @Autowired
    public YourService(OpenAIApiKeyComponent apiKeyComponent) {
        this.apiKeyComponent = apiKeyComponent;
    }

    public List<String> someMethod(final String input) {
    	  List<String> itemsList = new ArrayList<>();
        String apiKey = apiKeyComponent.getApiKey();
        
        CloseableHttpClient httpClient = HttpClients.createDefault();
        
        HttpPost postRequest = new HttpPost("https://api.openai.com/v1/chat/completions");
        postRequest.setHeader(HttpHeaders.AUTHORIZATION, "Bearer " + apiKey);
        postRequest.setHeader(HttpHeaders.CONTENT_TYPE, "application/json");

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("model", "gpt-3.5-turbo");
        
        JSONArray jsonArray = new JSONArray();
        jsonObject.put("messages", jsonArray);
        
        JSONObject roleSystem = new JSONObject();
        roleSystem.put("role", "system");
        roleSystem.put("content", "Given an input term, generate a comprehensive list of related concepts, practices, historical events, key terminology, and influential entities. Focus on aspects that are directly associated with the term, providing insights into its broader context and implications. Consider various dimensions such as historical significance, industry practices, technical terminology, and notable examples or cases. Aim to create a list that encompasses the diversity and depth of the ecosystem surrounding the input term. Provide the information in a structured, comma-separated list for easy parsing.");
        jsonArray.put(roleSystem);
        
        JSONObject roleUser = new JSONObject();
        roleUser.put("role", "user");

        roleUser.put("content", "What words and phrases are similar to \"" + input + "\"");
        jsonArray.put(roleUser);
                
        try {
			postRequest.setEntity(new StringEntity(jsonObject.toString()));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
        
        try {
            CloseableHttpResponse response = httpClient.execute(postRequest);
            JSONObject resp = new JSONObject(EntityUtils.toString(response.getEntity()));
            
            JSONArray choices = resp.getJSONArray("choices");
            JSONObject firstChoice = choices.getJSONObject(0);
            JSONObject message = firstChoice.getJSONObject("message");
            String content = message.getString("content");
            
            String[] itemsArray = content.split(",\\s*");

            itemsList.addAll(Arrays.asList(itemsArray));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return itemsList;
    }
    
//    public CompletableFuture<HashMap<String, List<String>>> processMap(HashMap<String, List<String>> inputMap, HashMap<String, List<String>> outputMap) {
//        List<CompletableFuture<HashMap<String, List<String>>>> futures = new ArrayList<>();
//
//        for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
//            CompletableFuture<HashMap<String, List<String>>> future = CompletableFuture.supplyAsync(() -> {
//            	List<String> results = entry.getValue().stream()
//            		    .flatMap(str -> someMethod(str).stream()) // Flatten the lists
//            		    .collect(Collectors.toList());
//
//                HashMap<String, List<String>> resultMap = new HashMap<>();
//                resultMap.put(entry.getKey(), results); // Associate the key with the cumulative list
//                return resultMap;
//            });
//            futures.add(future);
//        }
//
//        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
//                .thenApply(v -> {
//                    HashMap<String, List<String>> finalResult = new HashMap<>();
//                    futures.forEach(f -> finalResult.putAll(f.join())); // Merge all individual results
//                    return finalResult;
//                });
//    }
    
    public void processMap(Map<String, List<String>> inputMap, Map<String, List<String>> outputMap) {
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Map.Entry<String, List<String>> entry : inputMap.entrySet()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                List<String> results = entry.getValue().stream()
                        .flatMap(str -> someMethod(str).stream()) // Assuming someMethod returns List<String>
                        .collect(Collectors.toList());
                synchronized (outputMap) {
                	outputMap.put(entry.getKey(), results); // Mutate the output map
                }
            });
            futures.add(future);
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join(); // Wait for all to complete
    }
}

