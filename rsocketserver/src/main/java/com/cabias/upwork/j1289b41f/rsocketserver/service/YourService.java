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
	private final String system1 = "You are a catch phrase generator. Your response should be a list of phrases of one to three words separated by commas. Your list will be split using commas as delimiter. Avoid using adjectives or the user input in any of the phrases unless they are a fundamental piece of the phrase. Get specific and include diversity in the individual items in the list. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form.";
	private final String system2 = "You are a catch phrase generator. Your response should be a list of phrases of one to three words separated by commas. Your list will be split using commas as delimiter. Get specific and include diversity in the individual items in the list. The phrases must have distinct entity level, rather than general or broad categories. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form.";

	public void processMapHuggingFace(ChatSendModel chatSendModel, ConcurrentHashMap<String, Set<String>> outputMap) {
		if (chatSendModel != null && chatSendModel.getFingerprint() != null && chatSendModel.getMessage() != null
				&& !chatSendModel.getMessage().isEmpty()) {
			final String fingerprint = chatSendModel.getFingerprint();
			final String message = chatSendModel.getMessage();
			CompletableFuture<Set<String>> future1 = CompletableFuture.supplyAsync(() -> {
				return new HashSet<>(getHuggingFacePrompts(message, system1));
			});

			CompletableFuture<Set<String>> future2 = CompletableFuture.supplyAsync(() -> {
				return new HashSet<>(getHuggingFacePrompts(message, system2));
			});

			CompletableFuture<Void> combinedFuture = future1.thenCombine(future2, (set1, set2) -> {
				Set<String> prompts = new HashSet<>();
				prompts.addAll(set1);
				prompts.addAll(set2);
				return prompts;
			}).thenAccept((prompts) -> {
				outputMap.compute(fingerprint, (key, existingSet) -> {
					if (existingSet == null) {
						existingSet = new CopyOnWriteArraySet<>();
					} else {
						// Make a thread-safe copy to modify
						existingSet = new CopyOnWriteArraySet<>(existingSet);
					}
					System.out.println("prompts : " + prompts);
					existingSet.addAll(prompts);
					return existingSet; // Mutate the output map
				});
			});

			combinedFuture.join(); // Wait for the operation to complete
			boolean test = false;
		}
	}

	@Async
	public List<String> getHuggingFacePrompts(final String input, final String system) {
		final Set<String> set = new HashSet<>();
		final String resp = query(input, system);
		if (resp != null) {
			System.out.println("resp : " + resp);
			final Pattern pattern1 = Pattern.compile("(?<=^|,\\s)([a-zA-Z]+(?:\\s[a-zA-Z]+){0,2})(?:,|$)", Pattern.MULTILINE);
			final Pattern pattern2 = Pattern.compile("(\\b\\w+)(?:\\s+\\w+)");
			Matcher matcher1 = pattern1.matcher(resp.replaceAll("\"", ""));
			Matcher matcher2;
			while (matcher1.find()) {
				for (int i = 1; i <= matcher1.groupCount(); i++) {
					final String match = matcher1.group(i);
					if (match != null) {
						matcher2 = pattern2.matcher(match);
						addItemToSet(set, input, match, matcher2);
					}
				}
			}
		}
		System.out.println("clean : " + set);
		return new ArrayList<>(set);
	}

	public void addItemToSet(final Set<String> set, final String input, final String item, final Matcher matcher) {
		if (item.isEmpty() || item.isBlank() || item.toLowerCase().contains(input.toLowerCase())
				|| (matcher.find() && input.contains(matcher.group(1)))) {
			return;
		}
		set.add(item.trim());
	}

	public String query(final String input, final String system) {

		String modelId = "HuggingFaceH4/zephyr-7b-beta";
		String apiToken = "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";
		final JSONObject payload = new JSONObject();

		payload.put("inputs", "<|system|>" + system + "</s>"
//        payload.put("inputs", "<|system|>You are a catch phrase generator. Your response should be a list of phrases of one to three words separated by commas. Your list will be split using commas as delimiter. Avoid using adjectives or the user input in any of the phrases unless they are a fundamental piece of the phrase. Get specific and include diversity in the individual items in the list. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form.</s>"
//        payload.put("inputs", "<|system|>You are a catch phrase generator. Your response should be a list of phrases of one to three words separated by commas. Your list will be split using commas as delimiter. Get specific and include diversity in the individual items in the list. The phrases must have distinct entity level, rather than general or broad categories. These should be real world names, places, sayings, events, concepts, practices, terminology, influential entities, and notable aspects within the category. Avoid general or overarching categories, and instead, delve into the nuanced, specific, and contextually relevant aspects of the theme. Each item should be a prominent and widely recognized example, ensuring the list captures the essence of the category in its most general and universally accepted form.</s>"
				+ "<|user|>" + input + "</s>" + "<|assistant|>");

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
			return resp.replace(payload.getString("inputs"), "");
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
