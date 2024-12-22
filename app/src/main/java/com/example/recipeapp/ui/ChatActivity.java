package com.example.recipeapp.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.api.OpenAIService;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.ChatMessage;

import java.util.List;

public class ChatActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private ChatAdapter adapter;
    private EditText messageInput;
    private ImageButton sendButton;
    private OpenAIService openAIService;
    private Handler handler;
    private DatabaseHelper dbHelper;
    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        dbHelper = new DatabaseHelper(this);
        openAIService = new OpenAIService();
        handler = new Handler(Looper.getMainLooper());

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userId = prefs.getInt("userId", -1);

        // Configuration de la toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Assistant Culinaire");
        }

        openAIService = new OpenAIService();
        handler = new Handler(Looper.getMainLooper());

        recyclerView = findViewById(R.id.chatRecyclerView);
        messageInput = findViewById(R.id.messageInput);
        sendButton = findViewById(R.id.sendButton);

        setupToolbar();
        setupRecyclerView();
        setupMessageInput();
        loadChatHistory();

        // Message de bienvenue
        if (adapter.getItemCount() == 0) {
            addBotMessage("Bonjour ! Je suis votre assistant culinaire. Comment puis-je vous aider ?");
        }
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Assistant Culinaire");
        }
    }


    private void loadChatHistory() {
        if (userId != -1 && dbHelper != null) {
            List<ChatMessage> history = dbHelper.getChatHistory(userId);
            adapter.setMessages(history);
            recyclerView.scrollToPosition(adapter.getItemCount() - 1);
        }
    }

    private void processUserMessage(String message) {
        // Éviter les messages vides
        if (message.trim().isEmpty()) return;

        // Ajouter et sauvegarder le message utilisateur une seule fois
        ChatMessage userMessage = new ChatMessage(message, true);
        adapter.addMessage(userMessage);
        recyclerView.scrollToPosition(adapter.getItemCount() - 1);

        // Sauvegarder dans la base de données
        if (userId != -1) {
            dbHelper.saveChatMessage(userId, message, true);
        }

        // Vider le champ de saisie
        messageInput.setText("");

        // Traiter la réponse du chatbot
        new Thread(() -> {
            try {
                String response = openAIService.getChatResponse(message);
                handler.post(() -> {
                    // Ajouter et sauvegarder la réponse du bot une seule fois
                    ChatMessage botMessage = new ChatMessage(response, false);
                    adapter.addMessage(botMessage);
                    recyclerView.scrollToPosition(adapter.getItemCount() - 1);

                    if (userId != -1) {
                        dbHelper.saveChatMessage(userId, response, false);
                    }
                });
            } catch (Exception e) {
                handler.post(() -> {
                    Toast.makeText(ChatActivity.this, "Erreur: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            return activeNetworkInfo != null && activeNetworkInfo.isConnected();
        }
        return false;
    }


    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ChatAdapter(this);
        recyclerView.setAdapter(adapter);
    }

    private void setupMessageInput() {
        sendButton.setOnClickListener(v -> sendMessage());
        messageInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        String message = messageInput.getText().toString().trim();
        if (!message.isEmpty()) {
            messageInput.setText("");
            processUserMessage(message);
        }
    }


    private void addBotMessage(String message) {
        adapter.addMessage(new ChatMessage(message, false));
        recyclerView.smoothScrollToPosition(adapter.getItemCount() - 1);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    protected void onDestroy() {
        super.onDestroy();
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}