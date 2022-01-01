package ben.e;

import android.content.ContentValues;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import org.jetbrains.annotations.NotNull;

import ben.e.cat.CategoryAdapter;
import ben.e.database.DbUtil;
import ben.e.database.tables.Category;
import ben.e.database.tables.Users;

public class AddEditCategory extends AppCompatActivity {

    private EditText cName;
    private ListView list;
    private String uid;
    private Button del, expense, income;
    private long catId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_category);
        list = findViewById(R.id.list);
        cName = findViewById(R.id.name);
        del = findViewById(R.id.add_del);
        expense = findViewById(R.id.c_expense);
        income = findViewById(R.id.c_income);
        uid = getIntent().getStringExtra(Users.UID);

        catId = -1;
        expense.setSelected(true);
        list.setOnItemClickListener((parent, view, position, id) -> {
            cName.setText(dat[position]);
            catId = d[position];
            del.setVisibility(View.VISIBLE);
        });
        del.setOnClickListener(v -> delete(catId));
        findViewById(R.id.add_edit).setOnClickListener(v -> {
            validate();
        });
        final View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.equals(income)) {
                    income.setSelected(true);
                    expense.setSelected(false);
                } else {
                    income.setSelected(false);
                    expense.setSelected(true);
                }
                initViews();
            }
        };
        income.setOnClickListener(l);
        expense.setOnClickListener(l);
        initViews();
    }

    long[] d;
    String[] dat;

    @NotNull
    private void initViews() {
        String type = expense.isSelected() ? DbUtil.TYPE_EXPENSE : DbUtil.TYPE_INCOME;
        final String sql = "select * from " + Category.TABLE_NAME + " where " +
                Category.UID + "='" + uid + "'";

        String selection = Category.UID + "='" + uid + "' AND " + Category.UID +
                "='" + uid + "' and " + Category.TYPE + "='" + type + "'";
        Cursor c = new DbUtil(this).getReadableDatabase().query(Category.TABLE_NAME,
                null,
                selection, null, null, null, null);
        String[] data = new String[c.getCount()];
        d = new long[c.getCount()];
        while (c.moveToNext()) {
            data[c.getPosition()] = c.getString(c.getColumnIndexOrThrow(Category.C_NAME));
            d[c.getPosition()] = c.getLong(c.getColumnIndexOrThrow(Category.C_ID));
        }
        c.close();

        /*Reset all views*/
        del.setVisibility(View.GONE);
        dat = data;
        list.setAdapter(new CategoryAdapter(dat));

        catId = -1;
        cName.setText("");
    }

    /**
     * Delete current category
     *
     * @param id is cId of category table
     * @usage delete
     */
    private void delete(long id) {
        if (id < 0) return;
        String sql = "delete from " +
                Category.TABLE_NAME + " where " + Category.C_ID + "=" + id;
        new DbUtil(this).getWritableDatabase().delete(Category.TABLE_NAME,
                Category.C_ID + "=" + id, null);
        initViews();
    }

    private void validate() {
        if (cName.getText().toString().length() == 0) {
            cName.setError("This field required");
        }
        long id = catId;
        if (id < 0) insert();
        else update((int) id);
        catId = -1;
        initViews();
    }

    private void insert() {
        String type = expense.isSelected() ? DbUtil.TYPE_EXPENSE : DbUtil.TYPE_INCOME;
        ContentValues values = new ContentValues();
        values.put(Category.C_NAME, cName.getText().toString());
        values.put(Category.TYPE, type);
        values.put(Category.UID, uid);
        new DbUtil(this).getWritableDatabase().insert(Category.TABLE_NAME,
                null, values);
    }

    private void update(int id) {
        ContentValues cv = new ContentValues();
        cv.put(Category.C_NAME, cName.getText().toString());
        new DbUtil(this).getWritableDatabase()
                .update(Category.TABLE_NAME,
                cv, Category.C_ID + "=" + id, null);
    }
}