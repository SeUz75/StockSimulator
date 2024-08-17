package com.example.programmingandroid;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DataBaseHelper extends SQLiteOpenHelper {

    // Database Name and Version
    private static final String DATABASE_NAME = "stock_simulation.db";
    private static final int DATABASE_VERSION = 2; // Incremented version for schema change

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String USER_ID = "user_id";
    public static final String USERNAME = "username";
    public static final String PASSWORD = "password";
    public static final String BALANCE = "balance";

    // Stocks table
    public static final String TABLE_STOCKS = "stocks";
    public static final String STOCK_ID = "stock_id";
    public static final String STOCK_SYMBOL = "stock_symbol";
    public static final String PRICE = "purchase_price";

    // User_Stocks table
    public static final String TABLE_USER_STOCKS = "user_stocks";
    public static final String USERTOSTOCK_ID = "user_id";
    public static final String STOCKTOUSER_ID = "stock_id";
    public static final String QUANTITYPOSSESED = "quantity";

    // SQL to create Users table
    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + " ("
            + USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USERNAME + " TEXT UNIQUE, "
            + PASSWORD + " TEXT, "
            + BALANCE + " REAL)";

    // SQL to create Stocks table
    private static final String CREATE_TABLE_STOCKS = "CREATE TABLE " + TABLE_STOCKS + " ("
            + STOCK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + STOCK_SYMBOL + " TEXT UNIQUE, "
            + PRICE + " REAL)";

    // SQL to create User_Stocks table
    // Updated User_Stocks table creation with purchase_price
    private static final String CREATE_TABLE_USER_STOCKS = "CREATE TABLE " + TABLE_USER_STOCKS + " ("
            + USERTOSTOCK_ID + " INTEGER, "
            + STOCKTOUSER_ID + " INTEGER, "
            + QUANTITYPOSSESED + " INTEGER, "
            + "purchase_price REAL, " // New column to store the price at purchase
            + "PRIMARY KEY (" + USERTOSTOCK_ID + ", " + STOCKTOUSER_ID + "), "
            + "FOREIGN KEY (" + USERTOSTOCK_ID + ") REFERENCES " + TABLE_USERS + "(" + USER_ID + "), "
            + "FOREIGN KEY (" + STOCKTOUSER_ID + ") REFERENCES " + TABLE_STOCKS + "(" + STOCK_ID + "))";

    public DataBaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create users, stocks, and user_stocks tables
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_STOCKS);
        db.execSQL(CREATE_TABLE_USER_STOCKS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop old tables if they exist and create new ones
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STOCKS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_STOCKS);
        onCreate(db);
    }

    public boolean validateUser(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        String[] columns = {USER_ID}; // We only need the user ID to check if the user exists
        String selection = USERNAME + " = ? AND " + PASSWORD + " = ?";
        String[] selectionArgs = {username, password};

        Cursor cursor = db.query(
                TABLE_USERS,        // The table to query
                columns,            // The columns to return
                selection,          // The columns for the WHERE clause
                selectionArgs,      // The values for the WHERE clause
                null,               // Group by rows
                null,               // Filter by row groups
                null);              // Sort order

        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();

        return exists;  // Returns true if a matching user was found, otherwise false
    }

    public void createUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USERNAME, username);
        values.put(PASSWORD, password);
        values.put(BALANCE, 0.0);

        db.insert(TABLE_USERS, null, values);
        db.close();
    }

    @SuppressLint("Range")
    public double getUserBalance(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + BALANCE + " FROM " + TABLE_USERS + " WHERE " + USER_ID + " = ?", new String[]{String.valueOf(id)});
        Log.d("BuyStock", "User ID: " + id);
        Log.d("DatabaseHelper", "Query: SELECT " + BALANCE + " FROM " + TABLE_USERS + " WHERE " + USER_ID + " = " + id);


        double balance = 0;
        if (cursor.moveToFirst()) {
            balance = cursor.getDouble(cursor.getColumnIndexOrThrow(BALANCE));
        }
        cursor.close();
        db.close();
        return balance;
    }

    @SuppressLint("Range")
    public int getUserIdByUsername(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + USER_ID + " FROM " + TABLE_USERS + " WHERE " + USERNAME + " = ?", new String[]{username});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(cursor.getColumnIndex(USER_ID));
        }
        cursor.close();
        db.close();
        return userId;
    }

    public void updateUserBalance(int userId, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(BALANCE, newBalance);
        db.update(TABLE_USERS, values, USER_ID + " = ?", new String[]{String.valueOf(userId)});
        db.close();
    }

    public void addStockToUser(int userId, int stockId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if the user already owns some quantity of this stock
        String query = "SELECT quantity FROM user_stocks WHERE user_id = ? AND stock_id = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), String.valueOf(stockId)});

        if (cursor.moveToFirst()) {
            // User already owns this stock, update the quantity
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow("quantity"));
            int newQuantity = existingQuantity + quantity;

            ContentValues contentValues = new ContentValues();
            contentValues.put("quantity", newQuantity);

            db.update("user_stocks", contentValues, "user_id = ? AND stock_id = ?", new String[]{String.valueOf(userId), String.valueOf(stockId)});
        } else {
            // User doesn't own this stock, insert a new entry
            ContentValues contentValues = new ContentValues();
            contentValues.put("user_id", userId);
            contentValues.put("stock_id", stockId);
            contentValues.put("quantity", quantity);

            db.insert("user_stocks", null, contentValues);
        }

        cursor.close();
        db.close();
    }

    public int getStockIdBySymbol(String stockSymbol) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + STOCK_ID + " FROM " + TABLE_STOCKS + " WHERE " + STOCK_SYMBOL + " = ?", new String[]{stockSymbol});
        int stockId = -1;
        if (cursor.moveToFirst()) {
            stockId = cursor.getInt(cursor.getColumnIndexOrThrow(STOCK_ID));
        }
        cursor.close();
        db.close();
        return stockId;
    }

    public int addStock(String stockSymbol, double price) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(STOCK_SYMBOL, stockSymbol);
        values.put(PRICE, price);

        long stockId = db.insert(TABLE_STOCKS, null, values);
        db.close();
        return (int) stockId;
    }

    public Cursor getStocksForUser(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT " + TABLE_STOCKS + "." + STOCK_SYMBOL + ", " + TABLE_USER_STOCKS + "." + QUANTITYPOSSESED +
                        " FROM " + TABLE_USER_STOCKS +
                        " JOIN " + TABLE_STOCKS + " ON " + TABLE_USER_STOCKS + "." + STOCKTOUSER_ID + " = " + TABLE_STOCKS + "." + STOCK_ID +
                        " WHERE " + TABLE_USER_STOCKS + "." + USERTOSTOCK_ID + " = ?",
                new String[]{String.valueOf(userId)}
        );
    }

    public void removeStockFromUser(int userId, int stockId, int quantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + QUANTITYPOSSESED + " FROM " + TABLE_USER_STOCKS + " WHERE " + USERTOSTOCK_ID + " = ? AND " + STOCKTOUSER_ID + " = ?",
                new String[]{String.valueOf(userId), String.valueOf(stockId)});
        if (cursor.moveToFirst()) {
            int existingQuantity = cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITYPOSSESED));
            if (existingQuantity > quantity) {
                ContentValues values = new ContentValues();
                values.put(QUANTITYPOSSESED, existingQuantity - quantity);
                db.update(TABLE_USER_STOCKS, values, USERTOSTOCK_ID + " = ? AND " + STOCKTOUSER_ID + " = ?", new String[]{String.valueOf(userId), String.valueOf(stockId)});
            } else if (existingQuantity == quantity) {
                db.delete(TABLE_USER_STOCKS, USERTOSTOCK_ID + " = ? AND " + STOCKTOUSER_ID + " = ?", new String[]{String.valueOf(userId), String.valueOf(stockId)});
            }
        }
        cursor.close();
        db.close();
    }

    public List<User_Stock> getUserStocks(int userId) {
        List<User_Stock> userStocks = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Define the SQL query to fetch stock symbols and quantities for the given user_id
        String query = "SELECT " + TABLE_STOCKS + "." + STOCK_SYMBOL + ", " + TABLE_USER_STOCKS + "." + QUANTITYPOSSESED +
                " FROM " + TABLE_USER_STOCKS +
                " INNER JOIN " + TABLE_STOCKS + " ON " + TABLE_USER_STOCKS + "." + STOCKTOUSER_ID + " = " + TABLE_STOCKS + "." + STOCK_ID +
                " WHERE " + TABLE_USER_STOCKS + "." + USERTOSTOCK_ID + " = ?";

        // Log the query being executed for debugging
        Log.d("Database", "Executing query: " + query + " with user ID: " + userId);

        // Execute the query
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId)});

        if (cursor != null) {
            Log.d("Database", "Number of records found: " + cursor.getCount());

            while (cursor.moveToNext()) {
                // Extracting the stock symbol and quantity
                String stockSymbol = cursor.getString(cursor.getColumnIndexOrThrow(STOCK_SYMBOL));
                int quantity = cursor.getInt(cursor.getColumnIndexOrThrow(QUANTITYPOSSESED));

                // Log the extracted data
                Log.d("Database", "Stock Symbol: " + stockSymbol + ", Quantity: " + quantity);

                // Creating User_Stock object and adding it to the list
                User_Stock userStock = new User_Stock(stockSymbol, quantity);
                userStocks.add(userStock);
            }
            cursor.close(); // Close the cursor to free resources
        } else {
            Log.d("Database", "Cursor is null.");
        }

        db.close(); // Close the database connection

        return userStocks;
    }


}

