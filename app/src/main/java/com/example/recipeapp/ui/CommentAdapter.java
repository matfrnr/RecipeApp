package com.example.recipeapp.ui;

import android.content.Context;
import android.icu.text.SimpleDateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.recipeapp.R;
import com.example.recipeapp.database.DatabaseHelper;
import com.example.recipeapp.models.Comment;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import java.util.Date;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.ViewHolder> {
    private List<Comment> comments;
    private Context context;
    private DatabaseHelper dbHelper;
    private int currentUserId;

    public CommentAdapter(Context context, List<Comment> comments, int currentUserId) {
        this.context = context;
        this.comments = comments;
        this.dbHelper = new DatabaseHelper(context);
        this.currentUserId = currentUserId;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comment, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Comment comment = comments.get(position);
        holder.usernameText.setText(comment.getUsername());
        holder.contentText.setText(comment.getContent());

        // Formater la date
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String dateStr = sdf.format(new Date(comment.getTimestamp()));
        holder.timestampText.setText(dateStr);

        // Afficher l'indication "modifié" si nécessaire
        if (comment.isEdited()) {
            holder.editedText.setVisibility(View.VISIBLE);
        } else {
            holder.editedText.setVisibility(View.GONE);
        }

        // Montrer les boutons d'édition et de suppression uniquement pour les commentaires de l'utilisateur actuel
        boolean isCommentOwner = comment.getUserId() == currentUserId;
        holder.editButton.setVisibility(isCommentOwner ? View.VISIBLE : View.GONE);
        holder.deleteButton.setVisibility(isCommentOwner ? View.VISIBLE : View.GONE);

        // Ne configurer les listeners que si l'utilisateur est le propriétaire du commentaire
        if (isCommentOwner) {
            // Gérer la suppression
            holder.deleteButton.setOnClickListener(v -> {
                new AlertDialog.Builder(context)
                        .setTitle("Supprimer le commentaire")
                        .setMessage("Êtes-vous sûr de vouloir supprimer ce commentaire ?")
                        .setPositiveButton("Oui", (dialog, which) -> {
                            dbHelper.deleteComment(comment.getId(), currentUserId); // Ajout de currentUserId
                            comments.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, comments.size());
                        })
                        .setNegativeButton("Non", null)
                        .show();
            });

            // Gérer la modification
            holder.editButton.setOnClickListener(v -> {
                View editView = LayoutInflater.from(context).inflate(R.layout.dialog_edit_comment, null);
                EditText editText = editView.findViewById(R.id.editCommentInput);
                editText.setText(comment.getContent());

                new AlertDialog.Builder(context)
                        .setTitle("Modifier le commentaire")
                        .setView(editView)
                        .setPositiveButton("Enregistrer", (dialog, which) -> {
                            String newContent = editText.getText().toString().trim();
                            if (!newContent.isEmpty()) {
                                dbHelper.updateComment(comment.getId(), newContent, currentUserId); // Ajout de currentUserId
                                comment.setContent(newContent);
                                comment.setTimestamp(System.currentTimeMillis());
                                comment.setEdited(true);
                                notifyItemChanged(position);
                            }
                        })
                        .setNegativeButton("Annuler", null)
                        .show();
            });
        }
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public void addComment(Comment comment) {
        comments.add(0, comment);
        notifyItemInserted(0);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView usernameText;
        TextView contentText;
        TextView timestampText;
        TextView editedText;
        ImageButton editButton;
        ImageButton deleteButton;

        ViewHolder(View view) {
            super(view);
            usernameText = view.findViewById(R.id.usernameText);
            contentText = view.findViewById(R.id.contentText);
            timestampText = view.findViewById(R.id.timestampText);
            editedText = view.findViewById(R.id.editedText);
            editButton = view.findViewById(R.id.editButton);
            deleteButton = view.findViewById(R.id.deleteButton);
        }
    }
}