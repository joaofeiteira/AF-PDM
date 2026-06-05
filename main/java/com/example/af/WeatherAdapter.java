package com.example.af;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class WeatherAdapter extends RecyclerView.Adapter<WeatherAdapter.ViewHolder> {

    private List<WeatherRecord> records;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WeatherRecord record);
        void onItemLongClick(WeatherRecord record);
    }

    public WeatherAdapter(List<WeatherRecord> records, OnItemClickListener listener) {
        this.records = records;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_weather, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WeatherRecord record = records.get(position);
        
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        holder.tvDate.setText(holder.itemView.getContext().getString(R.string.date_format, sdf.format(new Date(record.timestamp))));
        holder.tvTemp.setText(holder.itemView.getContext().getString(R.string.temp_format, record.temperature));
        holder.tvCondition.setText(holder.itemView.getContext().getString(R.string.condition_format, record.condition));
        holder.tvLocation.setText(holder.itemView.getContext().getString(R.string.location_format, record.latitude, record.longitude));
        holder.tvObservation.setText(holder.itemView.getContext().getString(R.string.obs_format, (record.observation.isEmpty() ? "-" : record.observation)));
        
        holder.ivFavorite.setVisibility(record.favorite ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(v -> listener.onItemClick(record));
        holder.itemView.setOnLongClickListener(v -> {
            listener.onItemLongClick(record);
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return records.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvDate, tvTemp, tvCondition, tvLocation, tvObservation;
        ImageView ivFavorite;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDate = itemView.findViewById(R.id.tvItemDate);
            tvTemp = itemView.findViewById(R.id.tvItemTemp);
            tvCondition = itemView.findViewById(R.id.tvItemCondition);
            tvLocation = itemView.findViewById(R.id.tvItemLocation);
            tvObservation = itemView.findViewById(R.id.tvItemObservation);
            ivFavorite = itemView.findViewById(R.id.ivFavorite);
        }
    }
}
