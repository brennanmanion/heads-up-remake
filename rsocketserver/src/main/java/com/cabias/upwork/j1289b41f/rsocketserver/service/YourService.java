package com.cabias.upwork.j1289b41f.rsocketserver.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
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
    	final List<String> list = new ArrayList<>();
    	final String resp = query(input);
    	if (resp != null)
    	{
    		list.addAll(Arrays.asList(resp.split(",")));
    	}
    	return list;
    }
    
    public String query(final String input) {
        
        String modelId = "HuggingFaceH4/zephyr-7b-beta";
        String apiToken = "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";
        final JSONObject payload = new JSONObject();
        
        
//        payload.put("inputs", "<|system|>"
//        		+ "Given an input term, generate a comprehensive list of related concepts, practices, historical events, key terminology, and influential entities. Focus on aspects that are directly associated with the term, providing insights into its broader context and implications. Consider various dimensions such as historical significance, industry practices, technical terminology, and notable examples or cases. Aim to create a list that encompasses the diversity and depth of the ecosystem surrounding the input term. Aim to not re-use the input term in any of the items in the list. Provide the information in a structured, comma-separated list for easy parsing. These items should be one word ideally, and a few at the very most. Make sure the responses are fun and not depressing or antagonizing.</s>"
//        		+ "Upon receiving an input term, produce a detailed, comma-separated list of related elements. This list should encompass a wide array of categories pertinent to the term, reflecting its multifaceted nature and the ecosystem it resides in. Focus on direct associations that offer insights into its broader context and implications. Aim for a concise representation of each item, ideally one word or a few words at most, to ensure clarity and ease of parsing. Consider the following categories as they apply: Technological Innovations: Key inventions, trends. Artistic Movements or Styles: Influential artists, defining works. Historical Periods or Events: Significant dates, figures, outcomes. Scientific Concepts or Theories: Foundational principles, major discoveries. Economic Theories or Models: Influential economists, fundamental principles. Philosophical Ideologies: Key philosophers, seminal works. Environmental Ecosystems: Characteristic flora and fauna, climate features. Culinary Techniques or Cuisines: Essential dishes, cooking methods. Legal Principles or Cases: Landmark cases, fundamental concepts. Health and Medicine: Diseases, treatments, medical breakthroughs. Sports and Recreation: Important games, athletes, rules. Fashion and Design: Influential designers, style eras. Mythology and Folklore: Significant myths, characters, symbols. Languages and Dialects: Key phrases, linguistic features. Transportation and Vehicles: Modes of transport, historical developments. Each entry should reflect the term's historical significance, industry practices, technical terminology, and notable examples or cases. Strive to provide a balanced representation that captures the diversity and depth of the term's various dimensions and implications. Provide the information in a structured, comma-separated list for easy parsing. These items should be one word ideally, and a few at the very most. Make sure the responses are fun and not depressing or antagonizing"
//        		+ "<|user|>"
//        		+ input + "</s>"
//        		+ "<|assistant|>");
        
//        payload.put("inputs", "<|system|>\n"
//        		+ "Given an input term, generate a comprehensive list of related concepts, practices, historical events, key terminology, and influential entities. Consider various dimensions such as historical significance, industry practices, technical terminology, and notable examples or cases. Aim to create a list that encompasses the diversity and depth of the ecosystem surrounding the input term. Provide the information in a structured, comma-separated list for easy parsing. These items should be one word ideally, and a few at the very most. Do not describe them in parenthesis. Do not re-use the user input in any of the terms included in the list.</s>\n"
//        		+ "<|user|>\n"
//        		+ input + "</s>\n"
//        		+ "<|assistant|>\n");
        
//        payload.put("inputs", "<|system|>\n"
//        		+ "Given an input term, generate a comprehensive list of related terms. Aim to create a list that encompasses the diversity and depth of the ecosystem surrounding the input term. Also aim to answer the prompt directly. Provide the information in a structured, comma-separated list for easy parsing. These items should be one word ideally, and a few at the very most.</s>\n"
//        		+ "<|user|>\n"
//        		+ input + "</s>\n"
//        		+ "<|assistant|>");
        
        // 
        payload.put("inputs", "<|system|>You are a comma separated text generator creating words or phrases that are in the same space of the input. Make sure the response is only a list of words and phrases separated by commas. The list items should constitute a complete aspect of the category of the input phrase. Get specific and include diversity in the individual items in the list. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. The list items should include not just names or basic elements, but be diverse in the broader context of the input. Focus on contextually relevant examples that go beyond the obvious, including practices, jargon, tools, techniques, or influential elements related to the category. Ensure richness and specificity in your list, reflecting a deep understanding of the subject matter. The list items should be lower level and granular. Aim to be specific items over categories. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should illustrate a comprehensive and distinct example within the broader context of the input. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form. Here is the example structure of the comma separated list `A,B,C`. No item in the list should contain the user's input string."
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

