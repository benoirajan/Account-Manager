package ben.e.ui.tabs;

import android.database.Cursor;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import ben.e.R;
import ben.e.database.DbUtil;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;

public class BudgetFragment extends Fragment {
    private static String UID;
    private final String currency;
    private ProgressBar pExpense, pToday;
    private TextView tExpense, tToday, tTotal, tBudget, tPercent, tDate;
    private Calendar calendar;
    private View layout;

    public BudgetFragment(String currency) {
        this.currency = currency;
    }

    public static BudgetFragment newInstance(String c) {
        return new BudgetFragment(c);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        UID = requireActivity().getIntent().getStringExtra(Users.UID);
        View v = inflater.inflate(R.layout.fragment_budget, container, false);
        pExpense = v.findViewById(R.id.progress_ex);
        pToday = v.findViewById(R.id.progress_today);
        layout = v.findViewById(R.id.layout);
        tExpense = v.findViewById(R.id.col2);
        tToday = v.findViewById(R.id.today);
        tBudget = v.findViewById(R.id.tBudget);
        tTotal = v.findViewById(R.id.total);
        tPercent = v.findViewById(R.id.percent);
        tDate = v.findViewById(R.id.date);
        TViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(TViewModel.class);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), calendar -> {
            BudgetFragment.this.calendar = calendar;
            setup();
        });
        return v;
    }

    private void setup() {
        if (calendar == null)
            calendar = Calendar.getInstance();
        String date = new SimpleDateFormat("yyyy-MM-dd").format(calendar);
        try {
            float budget = Float.parseFloat(
                    PreferenceManager
                            .getDefaultSharedPreferences(getContext())
                            .getString("budget", ""));
            pExpense.setMax((int) budget);
            pToday.setMax((int) budget);
            float ex = getQValues(date, DbUtil.TYPE_EXPENSE, "month");
            final Calendar todayCalendar = Calendar.getInstance();
            float today = todayCalendar.get(Calendar.DATE) * budget /
                    todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH);
            tDate.setText(String.format("%d/%d",
                    todayCalendar.get(Calendar.DATE),
                    todayCalendar.getActualMaximum(Calendar.DAY_OF_MONTH)));
            tBudget.setText("Budget " + budget + currency);
            String b = String.format("Expense %.1f %s", ex, currency);
            tExpense.setText(b);
            b = String.format("Today %.1f %s", today, currency);
            tToday.setText(b);
            tPercent.setText((int) (ex * 100 / budget) + "%");
            pToday.setProgress((int) today);
            pExpense.setProgress((int) ex);
            b = String.format(getString(R.string.total_f), budget - ex, currency);
            tTotal.setText(b);
            layout.setVisibility(View.VISIBLE);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            layout.setVisibility(View.GONE);
        }

    }

    public float getQValues(String date, String type, String dateVal) {
        String sql = "select sum(" + Transactions.AMOUNT + ") sum from " +
                Transactions.TABLE_NAME + " where " + Transactions.TYPE + " = '" + type + "' and " +
                Transactions.UID + " = '" + UID + "' and date between date('" +
                date + "','start of " + dateVal + "','-1 day') and date('" + date + "','start of " + dateVal + "','+1 " + dateVal + "')";
        Cursor c = new DbUtil(getContext()).getReadableDatabase().rawQuery(sql, null);
        if (!c.moveToNext()) {
            c.close();
            return 0;
        }
        float v = c.getFloat(c.getColumnIndexOrThrow("sum"));
        c.close();
        return v;
    }
}