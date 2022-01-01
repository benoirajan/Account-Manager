package ben.e.ui.wmonth;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.icu.text.SimpleDateFormat;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import ben.e.R;
import ben.e.database.DbUtil;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;
import ben.e.ui.tabs.SectionsPagerAdapter;
import ben.e.ui.tabs.TViewModel;

public class WeekMonthFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String UID;
    private static String curren;
    private int index;
    private Calendar cal;
    private RecyclerView recyclerView;

    public static WeekMonthFragment newInstance(int index,String currency) {
        WeekMonthFragment fragment = new WeekMonthFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        curren = currency;
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UID = requireActivity().getIntent().getStringExtra(Users.UID);
        index = 1;
        if (getArguments() != null) {
            index = getArguments().getInt(ARG_SECTION_NUMBER);
        }
    }


    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {

        TViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(TViewModel.class);
        View root = inflater.inflate(R.layout.fragment_tab, container, false);
        recyclerView = root.findViewById(R.id.rView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));

        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<Calendar>() {
            @Override
            public void onChanged(Calendar calendar) {
                cal = calendar;
                updateList(calendar);
            }
        });
        if (cal == null)
            cal = Calendar.getInstance();
        updateList(cal);
        return root;
    }

    private void updateList(Calendar calendar) {
        new QueryTask().execute(calendar);
    }

    class QueryTask extends AsyncTask<Calendar, Void, Void> {
        List<WeekMonthData> data;
        boolean isMonth = false;
        @Override
        protected Void doInBackground(Calendar... calendar) {
            if (index == SectionsPagerAdapter.MONTHLY){
                isMonth = true;
                data = getMonthlyTransactions(calendar[0]);
            }
            else data = getTransactions(calendar[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.setAdapter(new WeekMonthAdapter(data,isMonth,curren));
        }
    }
    private List<WeekMonthData> getMonthlyTransactions(Calendar calendar) {
        List<WeekMonthData> ex = getMonthData(calendar, DbUtil.TYPE_EXPENSE);
        List<WeekMonthData> in = getMonthData(calendar, DbUtil.TYPE_INCOME);
        ex = JoinData(in, ex);

        return ex;
    }

    @NotNull
    private List<WeekMonthData> getMonthData(Calendar calendar, String type) {
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
        String cal = parser.format(calendar);
        SQLiteDatabase db = new DbUtil(getContext()).getReadableDatabase();
        String sql = "SELECT DATE(" + Transactions.DATE + ",'START OF MONTH') start,\n" +
                "SUM(" + Transactions.AMOUNT + ") " + Transactions.AMOUNT + "\n" +
                "FROM " + Transactions.TABLE_NAME + "\n" +
                "WHERE " + Transactions.TYPE + " = '" + type + "' AND \n" +
                Transactions.DATE + " < DATE('" + cal + "','START OF YEAR','+1 YEAR') AND \n" +
                Transactions.DATE + " >= DATE('" + cal + "','START OF YEAR')\n " +
                "AND " + Transactions.UID + " = '" + UID + "'\n" +
                "GROUP BY start\n" +
                "ORDER BY start DESC";
        Cursor cursor = db.rawQuery(sql, null);

        List<WeekMonthData> table = new ArrayList<>();
        while (cursor.moveToNext()) {
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow("start"));
            double amt = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(Transactions.AMOUNT));

            WeekMonthData data = new WeekMonthData();
            data.date = date;
            if (type.equals(DbUtil.TYPE_EXPENSE))
                data.expense = (float) amt;
            else data.income = (float) amt;
            table.add(data);
        }
        cursor.close();
        return table;
    }

    private List<WeekMonthData> getTransactions(Calendar calendar) {
        List<WeekMonthData> table = getWeekType(calendar, DbUtil.TYPE_INCOME);
        List<WeekMonthData> table2 = getWeekType(calendar, DbUtil.TYPE_EXPENSE);
        return JoinData(table, table2);
    }

    private List<WeekMonthData> JoinData(List<WeekMonthData> income, List<WeekMonthData> expense) {
        for (int i = 0; i < income.size(); i++) {
            final WeekMonthData d1 = income.get(i);
            for (int j = 0; j < expense.size(); j++) {
                final WeekMonthData d2 = expense.get(j);
                if (d1.date.equals(d2.date)) {
                    d1.setExpense(d2.expense);
                    expense.remove(d2);
                }
            }
        }
        income.addAll(expense);
        income.sort(WeekMonthData.DATE);
        return income;
    }

    private List<WeekMonthData> getWeekType(Calendar calendar, String type) {
        List<WeekMonthData> table = new ArrayList<>();
        SimpleDateFormat tf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat view = new SimpleDateFormat("MM.dd");
        String cal = tf.format(calendar);
        SQLiteDatabase db = new DbUtil(getContext()).getReadableDatabase();
        String sql = "SELECT DATE(" + Transactions.DATE + ",'WEEKDAY 0','-7 DAY' ) start,\n" +
                "DATE(" + Transactions.DATE + ",'WEEKDAY 0','-1 DAY' ) endDate,\n" +
                "SUM(" + Transactions.AMOUNT + ") " + Transactions.AMOUNT + "\n" +
                "FROM " + Transactions.TABLE_NAME + "\n" +
                "WHERE " + Transactions.TYPE + " = '" + type + "' AND\n" +
                Transactions.DATE + " < DATE('" + cal + "','START OF MONTH','+1 MONTH') AND\n" +
                Transactions.DATE + " >= DATE('" + cal + "','START OF MONTH')\n" +
                "AND " + Transactions.UID + " = '" + UID + "'\n" +
                "GROUP BY start\n" +
                "ORDER BY start DESC";
        System.out.println(sql);
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            String date = cursor.getString(
                    cursor.getColumnIndexOrThrow("start"));
            String dateEnd = cursor.getString(
                    cursor.getColumnIndexOrThrow("endDate"));
            double amt = cursor.getDouble(
                    cursor.getColumnIndexOrThrow(Transactions.AMOUNT));
            Date d = null, d2 = null;
            try {
                d = tf.parse(date);
                d2 = tf.parse(dateEnd);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            WeekMonthData data = new WeekMonthData()
                    .setDate(view.format(d) + " ~ " + view.format(d2));
            if (type.equals(DbUtil.TYPE_EXPENSE)) data.setExpense((float) amt);
            else data.setIncome((float) amt);
            table.add(data);
        }
        cursor.close();
        return table;
    }
}