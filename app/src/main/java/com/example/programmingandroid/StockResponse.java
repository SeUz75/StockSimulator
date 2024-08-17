package com.example.programmingandroid;

import com.google.gson.annotations.SerializedName;

import java.util.Map;

public class StockResponse {
    // Metadata about the stock
    @SerializedName("Meta Data")
    private MetaData metadata;

    // Time series data for the stock
    @SerializedName("Time Series (1min)")
    private Map<String, StockData> timeSeries;

    // Getters
    public MetaData getMetadata() {
        return metadata;
    }

    public Map<String, StockData> getTimeSeries() {
        return timeSeries;
    }

    // Nested class to represent metadata
    public class MetaData {
        @SerializedName("2. Symbol")
        private String symbol;

        // Getter
        public String getSymbol() {
            return symbol;
        }
    }

    // Nested class to represent data for each time point
    public class StockData {
        @SerializedName("4. close")
        private String close;

        @SerializedName("5. volume")
        private String volume;  // Add this line for volume

        // Getters
        public String getClose() {
            return close;
        }

        public String getVolume() {  // Add this getter for volume
            return volume;
        }
    }
}