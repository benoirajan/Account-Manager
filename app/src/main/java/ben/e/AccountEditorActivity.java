package ben.e;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.*;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import java.util.Calendar;

import ben.e.database.DbUtil;
import ben.e.database.tables.Accounts;

public class AccountEditorActivity extends AppCompatActivity {
    private EditText amount, name;
    /**
     * Account Id
     * init in onCreate
     */
    private long id;
    private Button del;
    private String uid;
    private String currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_editor);
        amount = findViewById(R.id.ac_amount);
        name = findViewById(R.id.ac_name);
        del = findViewById(R.id.ac_del);
        uid = getIntent().getStringExtra(Accounts.UID);
        id = getIntent().getLongExtra(Accounts.AC_ID, -2);
        currency = PreferenceManager.getDefaultSharedPreferences(this).getString("currency", "");
        if (id != -2)
            initEdit(id);
        findViewById(R.id.ac_add).setOnClickListener(v -> validate());
        del.setOnClickListener(v -> delete());
    }

    /**
     * Init with selected account
     *
     * @param id It is Account Id
     * @link Accounts.AC_ID
     */
    private void initEdit(long id) {
        String sql = "select * from " + Accounts.TABLE_NAME + " where " +
                Accounts.AC_ID + "=" + id;
        Cursor c = new DbUtil(this).getReadableDatabase().rawQuery(sql, null);
        if (c.moveToNext()) {
            double balance = c.getDouble(c.getColumnIndexOrThrow(Accounts.BALANCE));
            String acName = c.getString(c.getColumnIndexOrThrow(Accounts.AC_NAME));
            amount.setText("" + balance);
            name.setText(acName);
        }
        del.setVisibility(View.VISIBLE);
    }

    private void validate() {
        final String txt = "This field is required";
        if (name.getText().length() == 0)
            name.setError(txt);
        else if (amount.getText().length() == 0)
            amount.setError(txt);
        else store();
    }

    private void store() {
        if (id == -2)
            insert();
        else update();
        finish();
    }

    private void update() {
        String sql = "update " + Accounts.TABLE_NAME + " set " + Accounts.AC_NAME + "='" + name.getText().toString() + "', "
                + Accounts.BALANCE + " = " + amount.getText().toString() +
                " where " + Accounts.AC_ID + "=" + id;
        new DbUtil(this).getWritableDatabase().execSQL(sql);
    }

    private void insert() {
        ContentValues cv = new ContentValues();
        cv.put(Accounts.UID, uid);
        cv.put(Accounts.AC_NAME, name.getText().toString());
        cv.put(Accounts.BALANCE, amount.getText().toString());
        SQLiteDatabase db = new DbUtil(this).getWritableDatabase();
        db.insert(Accounts.TABLE_NAME, null, cv);
    }

    public void setRemainder(View v) {
        Intent intent = new Intent(Intent.ACTION_INSERT)
                .setData(Events.CONTENT_URI)
                .putExtra(Events.TITLE,"Account Manager")
                .putExtra(Events.DESCRIPTION,String.format("Account: %s\nbalance: %s"+currency,
                        name.getText().toString(),
                        amount.getText().toString()));
        startActivity(intent);
    }

    private void delete() {
        SQLiteDatabase db = new DbUtil(this).getWritableDatabase();
        db.delete(Accounts.TABLE_NAME, Accounts.AC_ID + "=" + id, null);
        finish();
    }
}