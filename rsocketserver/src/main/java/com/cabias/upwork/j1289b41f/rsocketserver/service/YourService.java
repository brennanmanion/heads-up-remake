package com.cabias.upwork.j1289b41f.rsocketserver.service;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

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

            List<String> itemsList = Arrays.asList(itemsArray);
            return Arrays.asList(itemsArray); 
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

