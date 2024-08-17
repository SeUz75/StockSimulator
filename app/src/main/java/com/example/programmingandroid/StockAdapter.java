package com.example.programmingandroid;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StockAdapter extends RecyclerView.Adapter<StockAdapter.StockViewHolder> {

    private List<StockResponse.StockData> stockDataList;
    private static String symbol;

    public StockAdapter(List<StockResponse.StockData> stockDataList, String symbol) {
        this.stockDataList = stockDataList;
        this.symbol = symbol;
    }

    @Override
    public StockViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.activity_market, parent, false);
        return new StockViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StockViewHolder holder, int position) {
        StockResponse.StockData stockData = stockDataList.get(position);
        holder.bind(stockData);
    }

    @Override
    public int getItemCount() {
        return stockDataList != null ? stockDataList.size() : 0;
    }

    public static class StockViewHolder extends RecyclerView.ViewHolder {

        private TextView textViewSymbol;
        private TextView textViewPrice;
        private TextView textViewQuantity;

        public StockViewHolder(View itemView) {
            super(itemView);
            textViewSymbol = itemView.findViewById(R.id.text_view_symbol);
            textViewPrice = itemView.findViewById(R.id.text_view_price);
            textViewQuantity = itemView.findViewById(R.id.text_view_quantity);
        }

        public void bind(StockResponse.StockData stockData) {
            textViewSymbol.setText(symbol); // Display the symbol for each item
            textViewPrice.setText("Price: " + (stockData.getClose() != null ? stockData.getClose() : "N/A"));
            textViewQuantity.setText("Volume: " + (stockData.getVolume() != null ? stockData.getVolume() : "N/A"));
        }
    }
}

