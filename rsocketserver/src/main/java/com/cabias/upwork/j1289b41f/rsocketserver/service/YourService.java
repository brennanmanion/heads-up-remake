package com.cabias.upwork.j1289b41f.rsocketserver.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.cabias.upwork.j1289b41f.rsocketserver.component.OpenAIApiKeyComponent;
import com.cabias.upwork.j1289b41f.rsocketserver.model.ChatSendModel;

@Service
public class YourService {

//    private final OpenAIApiKeyComponent apiKeyComponent;
//
//    @Autowired
//    public YourService(OpenAIApiKeyComponent apiKeyComponent) {
//        this.apiKeyComponent = apiKeyComponent;
//    }
	
    public void processMapHuggingFace(ChatSendModel chatSendModel, ConcurrentHashMap<String, Set<String>> outputMap) {
    	if (chatSendModel != null && chatSendModel.getFingerprint() != null && chatSendModel.getMessage() != null && !chatSendModel.getMessage().isEmpty())
    	{
    		final String fingerprint = chatSendModel.getFingerprint();
    		final String message = chatSendModel.getMessage();
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                final List<String> prompts = getHuggingFacePrompts(message);
                outputMap.compute(fingerprint, (key, existingSet) -> {
                    if (existingSet == null) {
                        existingSet = new CopyOnWriteArraySet<>();
                    } else {
                        // Make a thread-safe copy to modify
                        existingSet = new CopyOnWriteArraySet<>(existingSet);
                    }
                    System.out.println(prompts);
                    existingSet.addAll(prompts);
                    return existingSet; // Mutate the output map
                });
            });

            future.join(); // Wait for the operation to complete	
    	}
    } 
	
    @Async
    public List<String> getHuggingFacePrompts(final String input)
    {
    	final Set<String> set = new HashSet<>();
    	final String resp = query(input);
    	if (resp != null)
    	{
    		// ([a-zA-Z]+(?:\s[a-zA-Z]+)*,)+
    		final Pattern pattern = Pattern.compile("([a-zA-Z]+(?:\\s[a-zA-Z]+){0,2})(?:,\\s)", Pattern.MULTILINE);
    		Matcher matcher = pattern.matcher(resp);
            
            while (matcher.find()) {
                // Group 0 is the entire match, groups 1, 2, ... are the subgroups in the match
                for (int i = 1; i <= matcher.groupCount(); i++) {
                	final String match = matcher.group(i);
                	if (match != null)
                	{
                		set.add(match.trim());	
                	}
                }
            }
    	}
    	return new ArrayList<>(set);
    }
    
    public String query(final String input) {
        
        String modelId = "HuggingFaceH4/zephyr-7b-beta";
        String apiToken = "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";
        final JSONObject payload = new JSONObject();
        
        payload.put("inputs", "<|system|>You are a catch phrase generator. Your response should be a list of phrases of one to three words separated by commas. Your list will be split using commas as delimiter. The list items should constitute a complete aspect of the category of the input phrase. Get specific and include diversity in the individual items in the list. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. The list items should include not just names or basic elements, but be diverse in the broader context of the input. Focus on contextually relevant examples that go beyond the obvious, including practices, jargon, tools, techniques, or influential elements related to the category. Ensure richness and specificity in your list, reflecting a deep understanding of the subject matter. The list items should be lower level and granular. Aim to be specific items over categories. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should illustrate a comprehensive and distinct example within the broader context of the input. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form.</s>"
//        payload.put("inputs", "<|system|>You are a comma separated text generator creating words or phrases that are in the same space of the input. Make sure the response is only a list of words and phrases separated by commas. The list items should constitute a complete aspect of the category of the input phrase. Get specific and include diversity in the individual items in the list. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. The list items should include not just names or basic elements, but be diverse in the broader context of the input. Focus on contextually relevant examples that go beyond the obvious, including practices, jargon, tools, techniques, or influential elements related to the category. Ensure richness and specificity in your list, reflecting a deep understanding of the subject matter. The list items should be lower level and granular. Aim to be specific items over categories. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should illustrate a comprehensive and distinct example within the broader context of the input. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form. Here is the example structure of the comma separated list `A,B,C`. No item in the list should contain the user's input string."
        		+ "<|user|>"
        		+ input + "</s>"
        		+ "<|assistant|>");
        
        final JSONObject options = new JSONObject();
        payload.put("options", options);
        options.put("wait_for_model", true);
        options.put("use_cache", false);
        
        String apiUrl = "https://api-inference.huggingface.co/models/" + modelId;
//        String apiUrl = "https://oyiiu6hhv1y6cqr1.us-east-1.aws.endpoints.huggingface.cloud";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(apiUrl);

        // Set the headers
        httpPost.setHeader("Authorization", "Bearer " + apiToken);
        httpPost.setHeader("Content-type", "application/json");

        // Set the payload
        StringEntity entity = new StringEntity(payload.toString(), "UTF-8");
        httpPost.setEntity(entity);

        try {
            // Execute the request and get the response
        	CloseableHttpResponse response = client.execute(httpPost);
            String jsonResponse = EntityUtils.toString(response.getEntity());
            JSONArray arry = new JSONArray(jsonResponse);
            JSONObject jsonObject = arry.getJSONObject(0);
            final String resp = jsonObject.getString("generated_text");
            client.close();
            return resp.replace(payload.getString("inputs"),"");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

