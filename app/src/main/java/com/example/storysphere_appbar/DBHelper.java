package com.example.storysphere_appbar;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log; // Import Log

import java.util.ArrayList;
import java.util.List;

public class DBHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "StorysphereDatabase.db";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_WRITINGS = "writings";
    public static final String TABLE_CURRENT_SESSION = "current_session";

    private static final int DATABASE_VERSION = 6;

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_USERS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "email TEXT UNIQUE, " +
                "username TEXT, " +
                "password TEXT, " +
                "image_uri TEXT, " +
                "role TEXT DEFAULT 'user')");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_WRITINGS + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "title TEXT, " +
                "tagline TEXT, " +
                "tag TEXT, " +
                "category TEXT, " +
                "image_path TEXT, " +
                "content TEXT)");

        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_CURRENT_SESSION + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "user_email TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE " + TABLE_WRITINGS + " ADD COLUMN content TEXT");
        }
        if (oldVersion < 4) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN image_uri TEXT");
        }
        if (oldVersion < 5) {
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_CURRENT_SESSION);
            onCreate(db);
        }
        if (oldVersion < 6) {
            db.execSQL("ALTER TABLE " + TABLE_USERS + " ADD COLUMN role TEXT DEFAULT 'user'");
        }
    }

    public boolean saveLoginSession(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_email", email);
        db.delete(TABLE_CURRENT_SESSION, null, null);
        long result = db.insert(TABLE_CURRENT_SESSION, null, values);
        return result != -1;
    }

    public String getLoggedInUserEmail() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String email = null;
        try {
            cursor = db.rawQuery("SELECT user_email FROM " + TABLE_CURRENT_SESSION + " LIMIT 1", null);
            if (cursor != null && cursor.moveToFirst()) {
                int emailIndex = cursor.getColumnIndex("user_email");
                if (emailIndex != -1) {
                    email = cursor.getString(emailIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return email;
    }

    public boolean clearLoginSession() {
        SQLiteDatabase db = this.getWritableDatabase();
        int rowsAffected = db.delete(TABLE_CURRENT_SESSION, null, null);
        return rowsAffected > 0;
    }

    public boolean insertUser(String username, String email, String password, String role) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("email", email);
        values.put("password", password);
        values.put("role", role);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public Boolean checkUserCredentials(String email, String password){
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            Log.d("DBHelper", "Checking credentials for Email: " + email + ", Password: " + password);
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email = ? AND password = ?", new String[] {email, password});
            exists = (cursor != null && cursor.getCount() > 0);
            Log.d("DBHelper", "Credentials check result: " + exists);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    public boolean checkEmailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
            exists = (cursor != null && cursor.getCount() > 0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    /**
     * Retrieves a user's role based on their email.
     * @param email The email of the user.
     * @return The role string (e.g., "user", "admin") or null if user not found.
     */
    public String getUserRole(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String role = null;
        try {
            cursor = db.rawQuery("SELECT role FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                int roleIndex = cursor.getColumnIndex("role");
                if (roleIndex != -1) {
                    role = cursor.getString(roleIndex);
                }
            }
            Log.d("DBHelper", "getUserRole for " + email + ": " + (role != null ? role : "null")); // <-- Add this Log
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return role;
    }

    public String getUserImageUri(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        String imageUri = null;
        try {
            cursor = db.rawQuery("SELECT image_uri FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
            if (cursor != null && cursor.moveToFirst()) {
                int imageUriIndex = cursor.getColumnIndex("image_uri");
                if (imageUriIndex != -1) {
                    imageUri = cursor.getString(imageUriIndex);
                }
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return imageUri;
    }

    public Cursor getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE email = ?", new String[]{email});
    }

    public Boolean checkusername(String username) {
        SQLiteDatabase MyDB = this.getReadableDatabase();
        Cursor cursor = null;
        boolean exists = false;
        try {
            cursor = MyDB.rawQuery("Select * from " + TABLE_USERS + " where username = ?", new String[]{username});
            exists = (cursor != null && cursor.getCount() > 0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return exists;
    }

    public boolean updateUser(String email, String newUsername, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("username", newUsername);
        values.put("password", newPassword);
        int rows = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        return rows > 0;
    }

    public boolean updateUser(String email, String newUsername, String newPassword, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (newUsername != null) {
            values.put("username", newUsername);
        }
        if (newPassword != null) {
            values.put("password", newPassword);
        }
        if (imageUri != null) {
            values.put("image_uri", imageUri);
        }
        if (values.size() == 0) {
            return false;
        }
        int rows = db.update(TABLE_USERS, values, "email = ?", new String[]{email});
        return rows > 0;
    }

    public boolean deleteUser(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_USERS, "email = ?", new String[]{email}) > 0;
    }

    public long insertWriting(String title, String tagline, String tag, String category, String imagePath, String content) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("title", title);
        cv.put("tagline", tagline);
        cv.put("tag", tag);
        cv.put("category", category);
        cv.put("image_path", imagePath);
        cv.put("content", content);
        return db.insert(TABLE_WRITINGS, null, cv);
    }

    public Cursor getAllWritings() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " ORDER BY id DESC", null);
    }

    public boolean deleteWriting (int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_WRITINGS, "id = ?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean updateWriting(int id, String title, String tagline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("tagline", tagline);
        int result = db.update(TABLE_WRITINGS, values, "id = ?", new String[]{String.valueOf(id)});
        return result > 0;
    }

    public List<WritingItem> getAllWritingItems() {
        List<WritingItem> writingList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " ORDER BY id DESC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    String tagline = cursor.getString(cursor.getColumnIndexOrThrow("tagline"));
                    String tag = cursor.getString(cursor.getColumnIndexOrThrow("tag"));
                    String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                    String imagePath = cursor.getString(cursor.getColumnIndexOrThrow("image_path"));
                    String content = cursor.getString(cursor.getColumnIndexOrThrow("content"));

                    WritingItem item = new WritingItem(id, title, tagline, tag, category, imagePath);
                    writingList.add(item);
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return writingList;
    }

    public Cursor getWritingById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_WRITINGS + " WHERE id = ?", new String[]{String.valueOf(id)});
    }

    public boolean insertBook(String title, String imageUri) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("title", title);
        values.put("image_path", imageUri);
        long result = db.insert(TABLE_WRITINGS, null, values);
        return result != -1;
    }

    public Boolean insertData(String username, String password){
        SQLiteDatabase MyDB = this.getWritableDatabase();
        ContentValues contentValues= new ContentValues();
        contentValues.put("username", username);
        contentValues.put("password", password);
        long result = MyDB.insert(TABLE_USERS, null, contentValues);
        return result != -1;
    }
}
