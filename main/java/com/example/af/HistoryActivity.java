package com.example.af;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.af.databinding.ActivityHistoryBinding;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ActivityHistoryBinding binding;
    private FirebaseFirestore db;
    private WeatherAdapter adapter;
    private List<WeatherRecord> recordList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(R.string.history_title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        db = FirebaseFirestore.getInstance();
        recordList = new ArrayList<>();
        
        setupRecyclerView();
        loadHistory();
    }

    private void setupRecyclerView() {
        adapter = new WeatherAdapter(recordList, new WeatherAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(WeatherRecord record) {
                showEditDialog(record);
            }

            @Override
            public void onItemLongClick(WeatherRecord record) {
                showDeleteConfirmation(record);
            }
        });
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void loadHistory() {
        binding.progressBarHistory.setVisibility(View.VISIBLE);
        db.collection("queries")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        binding.progressBarHistory.setVisibility(View.GONE);
                        Toast.makeText(HistoryActivity.this, getString(R.string.error_loading_generic, error.getMessage()), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (value != null) {
                        recordList.clear();
                        for (DocumentSnapshot doc : value.getDocuments()) {
                            try {
                                WeatherRecord record = doc.toObject(WeatherRecord.class);
                                if (record != null) {
                                    recordList.add(record);
                                }
                            } catch (Exception e) {
                                android.util.Log.e("HistoryActivity", "Error deserializing document " + doc.getId(), e);
                            }
                        }
                        adapter.notifyDataSetChanged();
                        binding.progressBarHistory.setVisibility(View.GONE);

                        if (recordList.isEmpty()) {
                            Toast.makeText(HistoryActivity.this, R.string.no_history, Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void showEditDialog(WeatherRecord record) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.edit_obs_title);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_edit_observation, null);
        final EditText input = viewInflated.findViewById(R.id.etEditObservation);
        input.setText(record.observation);
        builder.setView(viewInflated);

        builder.setPositiveButton(R.string.btn_save_dialog, (dialog, which) -> {
            String newObs = input.getText().toString();
            record.observation = newObs;
            db.collection("queries").document(record.id).set(record)
                    .addOnSuccessListener(aVoid -> Toast.makeText(HistoryActivity.this, R.string.update_success, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e -> Toast.makeText(HistoryActivity.this, R.string.update_error, Toast.LENGTH_SHORT).show());
        });
        builder.setNegativeButton(R.string.btn_cancel, (dialog, which) -> dialog.cancel());

        builder.show();
    }

    private void showDeleteConfirmation(WeatherRecord record) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_record_title)
                .setMessage(R.string.delete_record_msg)
                .setPositiveButton(R.string.btn_yes, (dialog, which) -> {
                    db.collection("queries").document(record.id).delete()
                            .addOnSuccessListener(aVoid -> Toast.makeText(HistoryActivity.this, R.string.delete_success, Toast.LENGTH_SHORT).show())
                            .addOnFailureListener(e -> Toast.makeText(HistoryActivity.this, R.string.delete_error, Toast.LENGTH_SHORT).show());
                })
                .setNegativeButton(R.string.btn_no, null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
