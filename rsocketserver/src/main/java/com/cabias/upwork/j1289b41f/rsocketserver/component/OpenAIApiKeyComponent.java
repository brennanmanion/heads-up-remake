package com.cabias.upwork.j1289b41f.rsocketserver.component;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

@Component
public class OpenAIApiKeyComponent {

//    @Value("${openai.apikey}")
    private String apiKey;

    public String getApiKey() {
        return apiKey;
    }
}

