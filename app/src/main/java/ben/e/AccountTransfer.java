package ben.e;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import ben.e.data.Validation;
import ben.e.database.DbUtil;
import ben.e.database.tables.Accounts;
import ben.e.database.tables.Users;

public class AccountTransfer extends AppCompatActivity {

    private String uid;
    private EditText amount;
    private TextView from, to;
    private DbUtil dbUtil;
    private int accountFrom, accountTo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_transfer);
        this.uid = getIntent().getStringExtra(Users.UID);
        dbUtil = new DbUtil(this);
        accountFrom = accountTo = -2;

        amount = findViewById(R.id.amount);
        from = findViewById(R.id.account_from);
        to = findViewById(R.id.account_to);

        findViewById(R.id.saveBtn).setOnClickListener(v -> {
            if (validate())
                saveDb();
        });

        from.setOnClickListener(v -> {
            final PopupMenu menu = new PopupMenu(AccountTransfer.this, from);
            final Cursor cursor = TransactionAddActivity.getAccounts(uid, dbUtil);
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Accounts.AC_ID));
                String itemName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Accounts.AC_NAME));
                if (itemId != accountTo)
                    menu.getMenu().add(0, (int) itemId, (int) itemId, itemName);
            }
            cursor.close();
            menu.setOnMenuItemClickListener(item -> {
                from.setText(item.getTitle());
                AccountTransfer.this.accountFrom = item.getItemId();
                menu.dismiss();
                return true;
            });
            menu.show();
        });

        to.setOnClickListener(v -> {
            final PopupMenu menu = new PopupMenu(AccountTransfer.this, to);
            final Cursor cursor = TransactionAddActivity.getAccounts(uid, dbUtil);
            while (cursor.moveToNext()) {
                long itemId = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Accounts.AC_ID));
                String itemName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Accounts.AC_NAME));
                if (itemId != accountFrom)
                    menu.getMenu().add(0, (int) itemId, (int) itemId, itemName);
            }
            cursor.close();
            menu.setOnMenuItemClickListener(item -> {
                to.setText(item.getTitle());
                AccountTransfer.this.accountTo = item.getItemId();
                menu.dismiss();
                return true;
            });
            menu.show();
        });
    }

    private void saveDb() {
        String amt = amount.getText().toString();
        addToDb("-" + amt, accountFrom);
        addToDb("+"+amt, accountTo);
        finish();
    }

    private void addToDb(String amt, int acId) {
        String sql = "update " + Accounts.TABLE_NAME + " set " +
                Accounts.BALANCE + "=" + Accounts.BALANCE + amt +
                " where " + Accounts.AC_ID + "=" + acId;
        dbUtil.getWritableDatabase().execSQL(sql);
    }

    private boolean validate() {
        if (!Validation.validateText(from)) return false;
        if (!Validation.validateText(to)) return false;
        if (!Validation.validateText(amount)) return false;
        return true;
    }
}