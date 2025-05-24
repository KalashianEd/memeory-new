package com.kalashianed.memeory.fragments;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.SetOptions;
import com.kalashianed.memeory.R;
import com.kalashianed.memeory.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class LeaderboardFragment extends Fragment {

    private static final String TAG = "LeaderboardFragment";
    
    private RecyclerView rvLeaderboard;
    private TextView tvFirstPlaceName, tvFirstPlaceScore, tvFirstPlaceRank;
    private TextView tvSecondPlaceName, tvSecondPlaceScore, tvSecondPlaceRank;
    private TextView tvThirdPlaceName, tvThirdPlaceScore, tvThirdPlaceRank;
    private TextView tvNoData;
    private ProgressBar progressLoading;
    
    private Button btnFilterGlobal;
    
    private LeaderboardAdapter adapter;
    private List<User> usersList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FirebaseUser currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        
        // Инициализация Firebase
        firestore = FirebaseFirestore.getInstance();
        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        
        // Инициализация UI элементов
        rvLeaderboard = root.findViewById(R.id.rvLeaderboard);
        tvNoData = root.findViewById(R.id.tvNoData);
        progressLoading = root.findViewById(R.id.progressLoading);
        
        // Инициализация элементов топ-3
        tvFirstPlaceName = root.findViewById(R.id.tvFirstPlaceName);
        tvFirstPlaceScore = root.findViewById(R.id.tvFirstPlaceScore);
        tvFirstPlaceRank = root.findViewById(R.id.tvFirstPlaceRank);
        tvSecondPlaceName = root.findViewById(R.id.tvSecondPlaceName);
        tvSecondPlaceScore = root.findViewById(R.id.tvSecondPlaceScore);
        tvSecondPlaceRank = root.findViewById(R.id.tvSecondPlaceRank);
        tvThirdPlaceName = root.findViewById(R.id.tvThirdPlaceName);
        tvThirdPlaceScore = root.findViewById(R.id.tvThirdPlaceScore);
        tvThirdPlaceRank = root.findViewById(R.id.tvThirdPlaceRank);
        
        // Инициализация кнопок
        btnFilterGlobal = root.findViewById(R.id.btnFilterGlobal);
        
        // Показываем индикатор загрузки
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);
        
        // Настройка RecyclerView
        adapter = new LeaderboardAdapter(getContext(), usersList, currentUser != null ? currentUser.getUid() : null);
        rvLeaderboard.setLayoutManager(new LinearLayoutManager(getContext()));
        rvLeaderboard.setAdapter(adapter);
        
        // Загрузка данных лидерборда
        loadLeaderboardData();
        
        // Настройка кнопки для обновления данных при нажатии
        btnFilterGlobal.setOnClickListener(v -> {
            progressLoading.setVisibility(View.VISIBLE);
            rvLeaderboard.setVisibility(View.GONE);
            tvNoData.setVisibility(View.GONE);
            loadLeaderboardData();
        });
        
        return root;
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // Обновляем данные при возвращении к фрагменту
        loadLeaderboardData();
    }
    
    /**
     * Загружает данные для лидерборда из Firestore
     */
    private void loadLeaderboardData() {
        // Очищаем текущий список
        usersList.clear();
        
        // Показываем индикатор загрузки
        progressLoading.setVisibility(View.VISIBLE);
        rvLeaderboard.setVisibility(View.GONE);
        tvNoData.setVisibility(View.GONE);
        
        Log.d(TAG, "Начало загрузки данных лидерборда");
        
        // Запрос для получения пользователей, отсортированных по лучшему счету (нисходящий порядок)
        // Увеличиваем лимит до 100 пользователей, чтобы отобразить больше участников
        firestore.collection("users")
                .orderBy("bestScore", Query.Direction.DESCENDING)
                .limit(100)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<User> topUsers = new ArrayList<>();
                    
                    Log.d(TAG, "Получены данные: " + queryDocumentSnapshots.size() + " пользователей");
                    
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        User user = document.toObject(User.class);
                        
                        // Убедимся, что у пользователя установлен ID
                        if (user.getUserId() == null) {
                            user.setUserId(document.getId());
                        }
                        
                        // Убедимся, что у пользователя есть имя
                        if (user.getUsername() == null || user.getUsername().isEmpty()) {
                            user.setUsername("Пользователь " + document.getId().substring(0, Math.min(5, document.getId().length())));
                        }
                        
                        // Проверяем, что у пользователя есть хотя бы какой-то рейтинг
                        if (user.getBestScore() <= 0) {
                            user.setBestScore(10); // Устанавливаем минимальный рейтинг
                        }
                        
                        topUsers.add(user);
                        Log.d(TAG, "Пользователь: " + user.getUsername() + ", Счет: " + user.getBestScore());
                    }
                    
                    // Скрываем индикатор загрузки
                    progressLoading.setVisibility(View.GONE);
                    
                    if (topUsers.isEmpty()) {
                        // Если данных нет, показываем сообщение
                        tvNoData.setText("В рейтинге пока нет игроков.\nИграйте в игру и занимайте первые места!");
                        tvNoData.setVisibility(View.VISIBLE);
                        rvLeaderboard.setVisibility(View.GONE);
                        Log.d(TAG, "Нет данных о пользователях");
                        
                        // Устанавливаем пустые значения для топ-3
                        tvFirstPlaceName.setText("—");
                        tvFirstPlaceScore.setText("0 pts");
                        tvSecondPlaceName.setText("—");
                        tvSecondPlaceScore.setText("0 pts");
                        tvThirdPlaceName.setText("—");
                        tvThirdPlaceScore.setText("0 pts");
                        
                        return;
                    }
                    
                    // Обновляем UI с топ-3
                    updateTopThreeUI(topUsers);
                    
                    // Если у нас есть только топ-3 (или меньше), показываем сообщение о том, что нет других пользователей
                    if (topUsers.size() <= 3) {
                        tvNoData.setText("Всего " + topUsers.size() + " пользователей в рейтинге");
                        tvNoData.setVisibility(View.VISIBLE);
                        rvLeaderboard.setVisibility(View.GONE);
                        return;
                    }
                    
                    // Убираем первых трех пользователей, так как они отображаются отдельно
                    List<User> remainingUsers = topUsers.subList(3, topUsers.size());
                    
                    // Обновляем адаптер
                    usersList.addAll(remainingUsers);
                    adapter.notifyDataSetChanged();
                    
                    // Показываем RecyclerView
                    rvLeaderboard.setVisibility(View.VISIBLE);
                })
                .addOnFailureListener(e -> {
                    // Показываем ошибку
                    progressLoading.setVisibility(View.GONE);
                    tvNoData.setText("Ошибка загрузки данных: " + e.getMessage());
                    tvNoData.setVisibility(View.VISIBLE);
                    rvLeaderboard.setVisibility(View.GONE);
                    
                    Log.e(TAG, "Ошибка при загрузке данных лидерборда", e);
                });
    }
    
    /**
     * Обновляет UI с топ-3 игроками
     */
    private void updateTopThreeUI(List<User> users) {
        if (users.size() > 0) {
            User firstPlace = users.get(0);
            tvFirstPlaceName.setText(firstPlace.getUsername());
            tvFirstPlaceScore.setText(String.format("%d pts", firstPlace.getBestScore()));
            tvFirstPlaceRank.setText(getRankText(firstPlace.getBestScore()));
            tvFirstPlaceRank.setTextColor(getRankColor(getContext(), firstPlace.getBestScore()));
            Log.d(TAG, "Первое место: " + firstPlace.getUsername() + " (" + firstPlace.getBestScore() + ")");
        } else {
            tvFirstPlaceName.setText("—");
            tvFirstPlaceScore.setText("0 pts");
            tvFirstPlaceRank.setText("—");
        }
        
        if (users.size() > 1) {
            User secondPlace = users.get(1);
            tvSecondPlaceName.setText(secondPlace.getUsername());
            tvSecondPlaceScore.setText(String.format("%d pts", secondPlace.getBestScore()));
            tvSecondPlaceRank.setText(getRankText(secondPlace.getBestScore()));
            tvSecondPlaceRank.setTextColor(getRankColor(getContext(), secondPlace.getBestScore()));
            Log.d(TAG, "Второе место: " + secondPlace.getUsername() + " (" + secondPlace.getBestScore() + ")");
        } else {
            tvSecondPlaceName.setText("—");
            tvSecondPlaceScore.setText("0 pts");
            tvSecondPlaceRank.setText("—");
        }
        
        if (users.size() > 2) {
            User thirdPlace = users.get(2);
            tvThirdPlaceName.setText(thirdPlace.getUsername());
            tvThirdPlaceScore.setText(String.format("%d pts", thirdPlace.getBestScore()));
            tvThirdPlaceRank.setText(getRankText(thirdPlace.getBestScore()));
            tvThirdPlaceRank.setTextColor(getRankColor(getContext(), thirdPlace.getBestScore()));
            Log.d(TAG, "Третье место: " + thirdPlace.getUsername() + " (" + thirdPlace.getBestScore() + ")");
        } else {
            tvThirdPlaceName.setText("—");
            tvThirdPlaceScore.setText("0 pts");
            tvThirdPlaceRank.setText("—");
        }
    }
    
    /**
     * Возвращает текстовое представление ранга на основе очков
     */
    public static String getRankText(int score) {
        if (score >= 90) return "Легенда";
        else if (score >= 80) return "Мастер";
        else if (score >= 70) return "Профи";
        else if (score >= 60) return "Знаток";
        else if (score >= 50) return "Опытный";
        else if (score >= 40) return "Средний";
        else if (score >= 30) return "Новичок";
        else return "Новобранец";
    }
    
    /**
     * Возвращает цвет для ранга на основе очков
     */
    public static int getRankColor(Context context, int score) {
        if (score >= 90) return context.getResources().getColor(android.R.color.holo_purple);
        else if (score >= 70) return context.getResources().getColor(android.R.color.holo_orange_light);
        else if (score >= 50) return context.getResources().getColor(android.R.color.holo_blue_light);
        else return context.getResources().getColor(android.R.color.white);
    }
    
    /**
     * Адаптер для отображения списка лидеров
     */
    private static class LeaderboardAdapter extends RecyclerView.Adapter<LeaderboardAdapter.LeaderboardViewHolder> {
        
        private Context context;
        private List<User> users;
        private String currentUserId;
        private boolean isShowingAllUsers;
        
        public LeaderboardAdapter(Context context, List<User> users, String currentUserId) {
            this.context = context;
            this.users = users;
            this.currentUserId = currentUserId;
            // Определяем, отображаем ли мы всех пользователей, включая топ-3
            this.isShowingAllUsers = users.size() <= 3;
        }
        
        @NonNull
        @Override
        public LeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_leaderboard, parent, false);
            return new LeaderboardViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull LeaderboardViewHolder holder, int position) {
            User user = users.get(position);
            
            // Устанавливаем ранг в зависимости от того, показываем ли мы всех пользователей или только дополнительных
            int rankPosition = isShowingAllUsers ? position + 1 : position + 4;
            holder.tvRank.setText(String.valueOf(rankPosition));
            
            // Устанавливаем имя пользователя
            holder.tvPlayerName.setText(user.getUsername());
            
            // Устанавливаем счет
            holder.tvScore.setText(String.format("%d pts", user.getBestScore()));
            
            // Устанавливаем ранг
            String rankText = getRankText(user.getBestScore());
            holder.tvPlayerRank.setText(rankText);
            
            // Задаем цвет ранга в зависимости от уровня
            holder.tvPlayerRank.setTextColor(getRankColor(context, user.getBestScore()));
            
            // Выделение текущего пользователя
            if (currentUserId != null && currentUserId.equals(user.getUserId())) {
                // Используем цвет с альфа-каналом для выделения текущего пользователя
                holder.itemView.setBackgroundResource(R.drawable.current_user_leaderboard_background);
            } else {
                // Используем прозрачный цвет для остальных пользователей
                holder.itemView.setBackgroundResource(android.R.color.transparent);
            }
            
            // Устанавливаем разный цвет фона для ранга в зависимости от позиции
            if (rankPosition == 1) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_gold_background);
            } else if (rankPosition == 2) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_silver_background);
            } else if (rankPosition == 3) {
                holder.tvRank.setBackgroundResource(R.drawable.rank_bronze_background);
            } else {
                holder.tvRank.setBackgroundResource(R.drawable.circle_rank_background);
            }
        }
        
        @Override
        public int getItemCount() {
            return users.size();
        }
        
        static class LeaderboardViewHolder extends RecyclerView.ViewHolder {
            TextView tvRank, tvPlayerName, tvScore, tvPlayerRank;
            ImageView ivPlayerAvatar;
            
            LeaderboardViewHolder(@NonNull View itemView) {
                super(itemView);
                tvRank = itemView.findViewById(R.id.tvRank);
                tvPlayerName = itemView.findViewById(R.id.tvPlayerName);
                tvScore = itemView.findViewById(R.id.tvScore);
                tvPlayerRank = itemView.findViewById(R.id.tvPlayerRank);
                ivPlayerAvatar = itemView.findViewById(R.id.ivPlayerAvatar);
            }
        }
    }
} 