package ben.e.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;

import ben.e.data.model.LoggedInUser;
import ben.e.database.tables.Accounts;
import ben.e.database.tables.Category;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;

public class DbUtil extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "expenseManager";
    private static final int DATABASE_VERSION = 1;

    public static final String TYPE_INCOME = "in";
    public static final String TYPE_EXPENSE = "ex";

    public static final String CREATE_USERS = "CREATE TABLE " + Users.TABLE_NAME +
            " (" + Users.UID + " TEXT PRIMARY KEY NOT NULL, " +
            Users.NAME + " TEXT NOT NULL, " +
            Users.PASS + " TEXT NOT NULL)";

    public static final String CREATE_ACCOUNTS = "CREATE TABLE " + Accounts.TABLE_NAME +
            " (" + Accounts.AC_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            Accounts.UID + " TEXT REFERENCES " + Users.TABLE_NAME + "(" + Users.UID + ") NOT NULL, " +
            Accounts.AC_NAME + " TEXT NOT NULL, " +
            Accounts.BALANCE + " REAL NOT NULL)";

    public static final String CREATE_CATEGORY = "CREATE TABLE " + Category.TABLE_NAME +
            " (" + Category.C_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            Category.UID + " TEXT REFERENCES " + Users.TABLE_NAME + "(" + Users.UID + ") NOT NULL, " +
            Category.TYPE + " TEXT NOT NULL, " +
            Category.C_NAME + " TEXT NOT NULL)";

    public static final String CREATE_TRANSACTIONS = "CREATE TABLE " + Transactions.TABLE_NAME +
            " (" + Transactions.T_ID + " INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
            Transactions.UID + " TEXT REFERENCES " + Users.TABLE_NAME + "(" + Users.UID + ") NOT NULL, " +
            Transactions.DATE + " TEXT NOT NULL, " +
            Transactions.TIME + " TEXT NOT NULL, " +
            Transactions.AC_ID + " INTEGER REFERENCES " + Accounts.TABLE_NAME + "(" + Accounts.AC_ID + ") NOT NULL, " +
            Transactions.AMOUNT + " REAL NOT NULL, " +
            Transactions.C_ID + " INTEGER REFERENCES " + Category.TABLE_NAME + "(" + Category.C_ID + ") NOT NULL, " +
            Transactions.NOTE + " TEXT, " +
            Transactions.TYPE + " TEXT NOT NULL)";

    public DbUtil(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS);
        db.execSQL(CREATE_ACCOUNTS);
        db.execSQL(CREATE_CATEGORY);
        db.execSQL(CREATE_TRANSACTIONS);
        ContentValues values = new ContentValues();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public static LoggedInUser login(SQLiteDatabase db, String password, String username) {
        String sql = "SELECT " + Users.NAME + " FROM " + Users.TABLE_NAME +
                " WHERE " + Users.PASS + "='" + password + "' AND " + Users.UID + " = '" + username + "'";
        Cursor cursor = db.rawQuery(sql,
                null);
        if (cursor.getCount() > 0) {
            cursor.moveToNext();
            return new LoggedInUser(username,
                    cursor.getString(0));
        } else throw new SQLiteException("Not registered " + sql);
    }

    public static LoggedInUser register(SQLiteDatabase db, String password, String username, String name) {
        String sql = "SELECT " + Users.NAME + " FROM " + Users.TABLE_NAME +
                " WHERE " + Users.UID + " = '" + username + "'";
        Cursor cursor = db.rawQuery(sql,
                null);
        if (cursor.getCount() == 0) {
            ContentValues values = new ContentValues();
            values.put(Users.UID, username);
            values.put(Users.NAME, name);
            values.put(Users.PASS, password);
            if (db.insert(Users.TABLE_NAME, null, values) == -1)
                throw new SQLiteException("Error on query : " + values.toString());
            sql = "INSERT INTO " + Accounts.TABLE_NAME + "(" + Accounts.UID + "," + Accounts.BALANCE + "," + Accounts.AC_NAME + ")" +
                    " VALUES('" + username + "'," + "0,'Cash')," +
                    " ('" + username + "'," + "0,'Account')," +
                    " ('" + username + "'," + "0,'Card')";
            db.execSQL(sql);

            sql = "INSERT INTO " + Category.TABLE_NAME + "(" + Category.UID + "," + Category.TYPE + "," + Category.C_NAME + ")" +
                    " VALUES('" + username + "','" + TYPE_INCOME + "','Allowance')," +
                    " ('" + username + "','" + TYPE_INCOME + "','Salary')," +
                    " ('" + username + "','" + TYPE_INCOME + "','Petty cash')," +
                    " ('" + username + "','" + TYPE_INCOME + "','Bonus')," +
                    " ('" + username + "','" + TYPE_INCOME + "','Others')," +

                    " ('" + username + "','" + TYPE_EXPENSE + "','Food')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Social Life')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Self-development')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Transportation')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Culture')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Household')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Apparel')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Beauty')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Health')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Education')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Gift')," +
                    " ('" + username + "','" + TYPE_EXPENSE + "','Other')";
            db.execSQL(sql);
            return new LoggedInUser(username, name);
        }

        cursor.close();
        throw new SQLiteException("Already registered !");
    }
}
