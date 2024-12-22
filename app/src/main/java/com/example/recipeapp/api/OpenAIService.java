package com.example.recipeapp.api;

import android.util.Log;

import com.example.recipeapp.models.ChatRequest;
import com.example.recipeapp.models.ChatResponse;
import com.example.recipeapp.models.Message;

import java.io.IOException;
import java.util.Arrays;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.POST;

public class OpenAIService {
    private static final String BASE_URL = "https://api.openai.com/v1/";
    private static final String API_KEY = ""; // Remplacer par ta clé

    private final OpenAIApi api;

    public OpenAIService() {
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request request = chain.request().newBuilder()
                            .addHeader("Authorization", "Bearer " + API_KEY)
                            .build();
                    return chain.proceed(request);
                })
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(OpenAIApi.class);
    }

    private final OkHttpClient client = new OkHttpClient.Builder()
            .addInterceptor(chain -> {
                Request request = chain.request();
                okhttp3.Response response = chain.proceed(request);
                if (response.code() == 429) {
                    response.close(); // Fermer la réponse avant de réessayer
                    try {
                        Thread.sleep(5000);
                        return chain.proceed(request);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
                return response;
            })
            .build();

    public interface OpenAIApi {
        @POST("chat/completions")
        Call<ChatResponse> getChatCompletion(@Body ChatRequest request);
    }

    public String getChatResponse(String message) throws IOException {
        try {
            ChatRequest request = new ChatRequest("gpt-3.5-turbo", Arrays.asList(
                    new Message("system", "Tu es un assistant culinaire expert. Tu donnes des conseils de cuisine et des recettes."),
                    new Message("user", message)
            ));

            Response<ChatResponse> response = api.getChatCompletion(request).execute();

            // Ajout de logs
            if (!response.isSuccessful()) {
                Log.e("OpenAIService", "Erreur: " + response.errorBody().string());
                return "Erreur de l'API: " + response.code();
            }

            if (response.body() == null) {
                Log.e("OpenAIService", "Response body est null");
                return "Pas de réponse de l'API";
            }

            return response.body().getChoices().get(0).getMessage().getContent();
        } catch (Exception e) {
            Log.e("OpenAIService", "Exception: " + e.getMessage());
            throw e;
        }
    }
}