package ben.e;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.Date;

import ben.e.database.DbUtil;
import ben.e.database.tables.Accounts;
import ben.e.database.tables.Category;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;

public class TransactionAddActivity extends AppCompatActivity {

    private Button expenseBtn, incomeBtn, saveBtn, continueBtn, delBtn;
    private EditText amount, note;
    private TextView date, time, account, category;
    private SQLiteDatabase database;
    private DbUtil dbUtil;
    private String UID;
    private int accountId;
    private int categoryId;
    private long tid;
    private static SimpleDateFormat dateFormat, timeFormat;
    /**
     * Old amount in case of updating
     */
    private double oldAmt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);
        //init uid
        UID = getIntent().getStringExtra(Users.UID);
        tid = getIntent().getLongExtra(Transactions.T_ID, -1);
        oldAmt = 0;
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        timeFormat = new SimpleDateFormat("HH:mm");

        dbUtil = new DbUtil(this);
        expenseBtn = findViewById(R.id.expenseBtn);
        incomeBtn = findViewById(R.id.incomeBtn);
        saveBtn = findViewById(R.id.saveBtn);
        continueBtn = findViewById(R.id.continueBtn);
        delBtn = findViewById(R.id.delBtn);

        date = findViewById(R.id.date);
        time = findViewById(R.id.time);
        account = findViewById(R.id.account);
        category = findViewById(R.id.category);
        amount = findViewById(R.id.amount);
        note = findViewById(R.id.note);

        expenseBtn.setOnClickListener(view -> {
            if (incomeBtn.isSelected()) {
                category.setText("");
                expenseBtn.setSelected(true);
                incomeBtn.setSelected(false);
            }
        });

        incomeBtn.setOnClickListener(view -> {
            if (expenseBtn.isSelected()) {
                category.setText("");
                expenseBtn.setSelected(false);
                incomeBtn.setSelected(true);
            }
        });

        saveBtn.setOnClickListener(view -> {
            if (validateInputs()) return;
            addToDataBase();
            finish();
        });

        continueBtn.setOnClickListener(view -> {
            if (validateInputs()) return;
            addToDataBase();
        });

        date.setOnClickListener(view -> {
            final DialogFragment newFragment = new DatePickerFragment(date);
            newFragment.show(getSupportFragmentManager(), "datePicker");
        });

        time.setOnClickListener((view) -> {
            final DialogFragment newFragment = new TimePickerFragment(time);
            newFragment.show(getSupportFragmentManager(), "timePicker");
        });

        account.setOnClickListener((view) -> {
            final PopupMenu menu = new PopupMenu(TransactionAddActivity.this, account);
            final Cursor cursor = getAccounts(UID, dbUtil);
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Accounts.AC_ID));
                String itemName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Accounts.AC_NAME));
                menu.getMenu().add(0, (int) itemId, (int) itemId, itemName);
            }
            cursor.close();
            menu.setOnMenuItemClickListener(item -> {
                account.setText(item.getTitle());
                accountId = item.getItemId();
                menu.dismiss();
                return true;
            });
            menu.show();
        });

        category.setOnClickListener((view) -> {
            final PopupMenu popupMenu = new PopupMenu(TransactionAddActivity.this, category);
            final Cursor cursor = getMenuItemCategory(expenseBtn.isSelected() ?
                    DbUtil.TYPE_EXPENSE :
                    DbUtil.TYPE_INCOME);

            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Category.C_ID));
                String itemName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Category.C_NAME));
                popupMenu.getMenu().add(0, (int) itemId, (int) itemId, itemName);
            }
            cursor.close();
            popupMenu.setOnMenuItemClickListener(item -> {
                category.setText(item.getTitle());
                categoryId = item.getItemId();
                popupMenu.dismiss();
                return true;
            });
            popupMenu.show();
        });

        delBtn.setOnClickListener(v -> {
            delete();
        });

        //database
        database = new DbUtil(this).getWritableDatabase();
        if (tid > -1) {
            fetchData();
            delBtn.setVisibility(View.VISIBLE);
            return;
        }
        //setting default values
        expenseBtn.setSelected(true);
        Calendar c = Calendar.getInstance();
        date.setText(dateFormat.format(c.getTime()));
        time.setText(timeFormat.format(c.getTime()));
    }

    private void delete() {
        database.delete(Transactions.TABLE_NAME,
                Transactions.T_ID + "=" + tid, null);

        String amt = amount.getText().toString();
        amt = expenseBtn.isSelected() ? "+" + amt : "-" + amt;
        String sql = "UPDATE " + Accounts.TABLE_NAME + " SET " + Accounts.BALANCE + "=" + Accounts.BALANCE +
                amt + " WHERE " + Accounts.AC_ID + "=" + accountId;
        database.execSQL(sql);
        finish();
    }

    private void fetchData() {
        String select = Transactions.T_ID + "=" + tid;
        Cursor c = database.query(Transactions.TABLE_NAME, null, select, null,
                null, null, null);
        if (c.moveToNext()) {
            String sDate = c.getString(c.getColumnIndexOrThrow(Transactions.DATE));
            String sTime = c.getString(c.getColumnIndexOrThrow(Transactions.TIME));
            accountId = c.getInt(c.getColumnIndexOrThrow(Transactions.AC_ID));
            categoryId = c.getInt(c.getColumnIndexOrThrow(Transactions.C_ID));
            double amt = c.getDouble(c.getColumnIndexOrThrow(Transactions.AMOUNT));
            String sNote = c.getString(c.getColumnIndexOrThrow(Transactions.NOTE));
            String sType = c.getString(c.getColumnIndexOrThrow(Transactions.TYPE));

            oldAmt = amt;
            date.setText(sDate);
            time.setText(sTime);
            amount.setText(amt + "");
            note.setText(sNote);
            if (sType.equals(DbUtil.TYPE_EXPENSE))
                expenseBtn.setSelected(true);
            else
                incomeBtn.setSelected(true);
            c.close();

            //getting acId
            select = Accounts.AC_ID + "=" + accountId;
            c = database.query(Accounts.TABLE_NAME, null, select, null,
                    null, null, null);
            if (c.moveToNext()) {
                String aName = c.getString(c.getColumnIndexOrThrow(Accounts.AC_NAME));
                account.setText(aName);
            }
            c.close();

            //getting Category
            select = Category.C_ID + "=" + categoryId;
            c = database.query(Category.TABLE_NAME, null, select, null,
                    null, null, null);
            if (c.moveToNext()) {
                String cName = c.getString(c.getColumnIndexOrThrow(Category.C_NAME));
                category.setText(cName);
            }
            c.close();
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        private final TextView editor;

        public DatePickerFragment(TextView date) {
            this.editor = date;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current date as the default date in the picker
            int day, month, year;
            final Calendar c = Calendar.getInstance();
            if (editor.getText() != null) {
                try {
                    Date d = new SimpleDateFormat("yyyy-MM-dd").parse(editor.getText().toString());
                    c.setTime(d);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            year = c.get(Calendar.YEAR);
            month = c.get(Calendar.MONTH);
            day = c.get(Calendar.DAY_OF_MONTH);

            // Create a new instance of DatePickerDialog and return it
            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            Calendar c = Calendar.getInstance();
            c.set(year, month, day);
            editor.setText(new SimpleDateFormat("yyyy-MM-dd").format(c.getTime()));
        }
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        private final TextView editor;

        public TimePickerFragment(TextView view) {
            editor = view;
        }

        @NotNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Use the current time as the default values for the picker
            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            // Create a new instance of TimePickerDialog and return it
            return new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }

        @SuppressLint("SimpleDateFormat")
        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
            Calendar c = Calendar.getInstance();
            c.set(Calendar.HOUR, hourOfDay);
            c.set(Calendar.MINUTE, minute);
            editor.setText(new SimpleDateFormat("HH:mm").format(c.getTime()));
        }
    }

    public static Cursor getAccounts(String uid, DbUtil dbUtil) {
        SQLiteDatabase db = dbUtil.getReadableDatabase();
        String[] projection = {
                Accounts.AC_NAME,
                Accounts.AC_ID
        };

        // Filter results WHERE "title" = 'My Title'
        String selection = Accounts.UID + " = ?";
        String[] selectionArgs = {uid};

        String sortOrder =
                Accounts.AC_NAME + " ASC";

        return db.query(
                Accounts.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
    }

    private Cursor getMenuItemCategory(String typeExpense) {

        SQLiteDatabase db = dbUtil.getReadableDatabase();

        // Filter results WHERE "title" = 'My Title'
        String selection = Category.TYPE + " = ? AND " + Category.UID + " = ?";
        String[] selectionArgs = {typeExpense, UID};

        String sortOrder =
                Category.C_NAME + " ASC";

        return db.query(
                Category.TABLE_NAME,   // The table to query
                null,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
        );
    }

    private void addToDataBase() {
        float amt = Float.parseFloat(amount.getText().toString());
        final String note = this.note.getText().toString();
        final String isEx = expenseBtn.isSelected() ?
                DbUtil.TYPE_EXPENSE : DbUtil.TYPE_INCOME;
        String date = this.date.getText().toString();
        String time = this.time.getText().toString();
        if (tid > -1) {
            ContentValues values = new ContentValues();
            values.put(Transactions.TYPE, isEx);
            values.put(Transactions.AC_ID, accountId);
            values.put(Transactions.AMOUNT, amt);
            values.put(Transactions.DATE, date);
            values.put(Transactions.NOTE, note);
            values.put(Transactions.C_ID, categoryId);
            values.put(Transactions.TIME, time);
            dbUtil.getWritableDatabase()
                    .update(Transactions.TABLE_NAME, values, Transactions.T_ID + "=" + tid, null);
        } else {
            String sql = "insert into " + Transactions.TABLE_NAME + " values(null,'" + UID + "','" + date +
                    "','" + time + "'," + accountId + "," + amt + "," + categoryId + ",'" + note + "','" + isEx + "')";

            dbUtil.getWritableDatabase().execSQL(sql);
        }

        //updating account
        amt = expenseBtn.isSelected() ? -amt : amt;
        String sql = "update " + Accounts.TABLE_NAME + " set " +
                Accounts.BALANCE + "=" + Accounts.BALANCE + "+" + (amt - oldAmt) +
                " where " + Accounts.AC_ID + "=" + accountId;
        dbUtil.getWritableDatabase().execSQL(sql);
        oldAmt = 0;
        reset();
    }

    private boolean validateInputs() {
        final String txt = "This field required";
        if (account.getText().toString().length() == 0) {
            account.setError(txt);
            account.requestFocus();
            return true;
        }
        if (category.getText().toString().length() == 0) {
            category.setError(txt);
            account.requestFocus();
            return true;
        }
        if (amount.getText().toString().length() == 0) {
            amount.setError(txt);
            account.requestFocus();
            return true;
        }
        return false;
    }

    private void reset() {
        tid = -1;
        delBtn.setVisibility(View.GONE);
        account.setText("");
        category.setText("");
        amount.setText("");
        note.setText("");
    }
}