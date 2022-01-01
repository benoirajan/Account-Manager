package ben.e.ui.tabs;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;

import java.util.Objects;

import ben.e.AddEditCategory;
import ben.e.MainActivity;
import ben.e.R;
import ben.e.TransactionAddActivity;
import ben.e.database.DbUtil;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;
import ben.e.settings.SettingsActivity;
import ben.e.ui.login.LoginActivity;

public class TransactionActivity extends AppCompatActivity {

    private Calendar calendar;
    ImageButton menu;
    TextView income, expense, total;
    private TextView title;
    private SimpleDateFormat sf;
    private TViewModel dashboardViewModel;
    private String UID;
    private String currency;
    private ViewPager vp;
    private TabLayout tabs;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_root);
        Objects.requireNonNull(getSupportActionBar()).hide();
        dashboardViewModel = new ViewModelProvider(this).get(TViewModel.class);
        vp = findViewById(R.id.view_pager);
        tabs = findViewById(R.id.tabs);
        UID = getSharedPreferences("", 0).getString(Users.UID, "");
        currency = PreferenceManager.getDefaultSharedPreferences(this).getString("currency", "");
        final FloatingActionButton actionButton = findViewById(R.id.fab);
        actionButton.setVisibility(View.VISIBLE);
        actionButton.setOnClickListener(view -> startActivity(new Intent(this, TransactionAddActivity.class)
                .putExtra(Users.UID, UID)));
        title = findViewById(R.id.title);
        menu = findViewById(R.id.menu);
        income = findViewById(R.id.col1);
        expense = findViewById(R.id.col2);
        total = findViewById(R.id.col3);
        sf = new SimpleDateFormat("yyyy MMM");
        ImageButton prev = findViewById(R.id.prev_btn);
        ImageButton forward = findViewById(R.id.forwared_btn);
        tabs.setupWithViewPager(vp);
        vp.setAdapter(new SectionsPagerAdapter(this, getSupportFragmentManager(), " " + currency));

        title.setOnClickListener(v -> {
            picDate();
        });
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                setUpView();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        calendar = Calendar.getInstance();
        calendar.set(Calendar.DATE, 1);
        setUpView();
        final View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = v.getId();

                switch (id) {
                    case R.id.prev_btn:
                        calendar.add(Calendar.MONTH, -1);
                        break;
                    case R.id.forwared_btn:
                        calendar.add(Calendar.MONTH, 1);
                        break;
                    case R.id.menu:
                        openMenu();
                }
                setUpView();
            }
        };
        menu.setOnClickListener(l);
        prev.setOnClickListener(l);
        forward.setOnClickListener(l);

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                MainActivity.NAV = item.getItemId();
                if (item.getItemId() == R.id.stat || item.getItemId() == R.id.accounts) {
                    finish();
                }
                return false;
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        setUpView();
    }


    private void openMenu() {
        PopupMenu pm = new PopupMenu(this, menu);
        pm.getMenuInflater().inflate(R.menu.main, pm.getMenu());
        pm.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.category:
                    startActivity(new Intent(this, AddEditCategory.class)
                            .putExtra(Users.UID, this
                                    .getIntent()
                                    .getStringExtra(Users.UID)));
                    break;
                case R.id.logout:
                    getSharedPreferences("", 0).edit()
                            .putString(Users.UID, "")
                            .apply();
                    startActivity(new Intent(this, LoginActivity.class));
                    finish();
                    break;
                case R.id.settings:
                    startActivity(new Intent(this, SettingsActivity.class));
            }
            return true;
        });
        pm.show();
    }

    private void setUpView() {
        title.setText(sf.format(calendar.getTime()));
        getIntent().putExtra(Users.UID, UID);
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar);
        System.out.println(date);
        String dateName;
        if (tabs.getSelectedTabPosition() == 2) dateName = "Year";
        else dateName = "Month";

        new QueryTask().execute(date, dateName);
        dashboardViewModel.setDate((Calendar) calendar.clone());
    }

    public float getQValues(String date, String type, String dateVal) {
        String sql = "select sum(" + Transactions.AMOUNT + ") sum from " +
                Transactions.TABLE_NAME + " where " + Transactions.TYPE + " = '" + type + "' and " +
                Transactions.UID + " = '" + UID + "' and date between date('" +
                date + "','start of " + dateVal + "','-1 day') and date('" + date + "','start of " + dateVal + "','+1 " + dateVal + "')";
        Cursor c = new DbUtil(this).getReadableDatabase().rawQuery(sql, null);
        if (!c.moveToNext()) {
            c.close();
            return 0;
        }
        float v = c.getFloat(c.getColumnIndexOrThrow("sum"));
        c.close();
        return v;
    }

    class QueryTask extends AsyncTask<String, Float, Void> {
        @Override
        protected Void doInBackground(String... strings) {
            float ex = getQValues(strings[0], DbUtil.TYPE_EXPENSE, strings[1]);
            float in = getQValues(strings[0], DbUtil.TYPE_INCOME, strings[1]);
            publishProgress(in, ex, in - ex);
            return null;
        }

        @Override
        protected void onProgressUpdate(Float... val) {
            super.onProgressUpdate(val);
            String in = String.format(getText(R.string.income_f).toString(), val[0], "");
            String ex = String.format(getText(R.string.expense_f).toString(), val[1], "");
            String t = String.format(getText(R.string.total_f).toString(), val[2], "");
            income.setText(in);
            expense.setText(ex);
            total.setText(t);
        }
    }

    public void picDate() {
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        calendar.set(year, month, 1);
                        setUpView();
                    }
                }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE)).show();
    }
}