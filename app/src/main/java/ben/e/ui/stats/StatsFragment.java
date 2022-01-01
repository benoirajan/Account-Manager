package ben.e.ui.stats;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleEventObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.preference.PreferenceManager;

import com.anychart.AnyChart;
import com.anychart.AnyChartView;
import com.anychart.chart.common.dataentry.DataEntry;
import com.anychart.chart.common.dataentry.ValueDataEntry;
import com.anychart.charts.Pie;
import com.anychart.enums.Align;
import com.anychart.enums.LegendLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import ben.e.R;
import ben.e.TransactionAddActivity;
import ben.e.database.DbUtil;
import ben.e.database.tables.Category;
import ben.e.database.tables.Transactions;
import ben.e.view.PieChart;

public class StatsFragment extends Fragment {

    private int i;
    private ListView list;
    private TextView totalText, dateFrom, dateTo;
    private Button incomeBtn, expenseBtn;
    private SimpleDateFormat df;
    private PieChart chart;
    private AnyChartView charT;
    private Pie pie;
    private String currency;
    private LifecycleEventObserver observer = new LifecycleEventObserver() {
        @Override
        public void onStateChanged(@NonNull LifecycleOwner source, @NonNull Lifecycle.Event event) {
            if (event.equals(Lifecycle.Event.ON_DESTROY))
                setUpViews();
        }
    };

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.stats, container, false);
        totalText = root.findViewById(R.id.stat_total);
        list = root.findViewById(R.id.stat_list);
        incomeBtn = root.findViewById(R.id.stat_income);
        expenseBtn = root.findViewById(R.id.stat_expense);
        expenseBtn.setSelected(true);
        dateFrom = root.findViewById(R.id.date_from);
        dateTo = root.findViewById(R.id.date_to);
        chart = root.findViewById(R.id.chart);
        charT = root.findViewById(R.id.chart2);
        pie = AnyChart.pie();
        charT.setChart(pie);
        pie.labels().position("outside");
        pie.legend()
                .position("center-bottom")
                .itemsLayout(LegendLayout.HORIZONTAL)
                .align(Align.CENTER);

        currency = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("currency","");
        Calendar calendar = Calendar.getInstance();
        df = new SimpleDateFormat("yyyy-MM-dd");

        dateTo.setText(df.format(calendar.getTime()));
        calendar.set(Calendar.DATE, 1);
        dateFrom.setText(df.format(calendar.getTime()));
        setUpViews();

        totalText.setOnClickListener(v -> setUpViews());
        dateFrom.setOnClickListener(v -> {
            final DialogFragment newFragment = new TransactionAddActivity.DatePickerFragment(dateFrom);
            newFragment.show(requireActivity().getSupportFragmentManager(), "datePicker");
             newFragment.getLifecycle().addObserver(observer);
        });

        dateTo.setOnClickListener(v -> {
            final DialogFragment newFragment = new TransactionAddActivity.DatePickerFragment(dateTo);
            newFragment.show(requireActivity().getSupportFragmentManager(), "datePicker");
            newFragment.getLifecycle().addObserver(observer);
        });

        expenseBtn.setOnClickListener(view -> {
            if (incomeBtn.isSelected()) {
                expenseBtn.setSelected(true);
                incomeBtn.setSelected(false);
                setUpViews();
            }
        });

        incomeBtn.setOnClickListener(view -> {
            if (expenseBtn.isSelected()) {
                expenseBtn.setSelected(false);
                incomeBtn.setSelected(true);
                setUpViews();
            }
        });
        //statsViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        Toast.makeText(requireContext(),"Touch chart to view amount values",Toast.LENGTH_LONG).show();
        return root;
    }


    private void setUpViews() {
        String type = "'" + (expenseBtn.isSelected() ? DbUtil.TYPE_EXPENSE : DbUtil.TYPE_INCOME) + "'";
        String uid = "'" + requireActivity().getIntent().getStringExtra(Category.UID) + "'";
        String date1 = dateFrom.getText().toString();
        String date2 = dateTo.getText().toString();

        //getting category name
        Cursor c = new DbUtil(getContext()).getReadableDatabase()
                .query(Category.TABLE_NAME, null, Category.TYPE + "=" + type + " AND " +
                                Category.UID + "=" + uid, null,
                        null, null, null);

        ArrayMap<Integer, String> cat = new ArrayMap<>();
        while (c.moveToNext()) {
            int id = c.getInt(c.getColumnIndexOrThrow(Category.C_ID));
            String name = c.getString(c.getColumnIndexOrThrow(Category.C_NAME));
            cat.put(id, name);
        }
        c.close();

        //getting Total
        String[] cols = {"sum(" + Transactions.AMOUNT + ") " + Transactions.AMOUNT};
        c = new DbUtil(getContext()).getWritableDatabase()
                .query(Transactions.TABLE_NAME, cols,
                        Transactions.TYPE + "=" + type + " AND " +
                                Transactions.DATE + ">='" + date1 + "' AND " +
                                Transactions.DATE + "<='" + date2 + "' AND " +
                                Category.UID + "=" + uid, null, null, null, null);
        double total = 0;
        if (c.moveToNext())
            total = c.getDouble(c.getColumnIndexOrThrow(Transactions.AMOUNT));
        totalText.setText(String.format("Total %.1f", total));
        c.close();

        //getting data
        cols = new String[]{"sum(" + Transactions.AMOUNT + ") " + Transactions.AMOUNT,
                Transactions.C_ID};
        c = new DbUtil(getContext()).getWritableDatabase()
                .query(Transactions.TABLE_NAME, cols,
                        Transactions.TYPE + "=" + type + " AND " +
                                Transactions.UID + "=" + uid + " AND " +
                                Transactions.DATE + ">='" + date1 + "' AND " +
                                Transactions.DATE + "<='" + date2 + "'"
                        , null, "" + Transactions.C_ID, null, Transactions.AMOUNT+" DESC");
        if(c.getCount()==0){
            c.close();
            return;
        }

        String[][] data = new String[c.getCount()][3];
        double[] values = new double[c.getCount()];
        int[] colors = new int[c.getCount()];
        List<DataEntry> chartData = new ArrayList<>();
        while (c.moveToNext()) {
            int cid = c.getInt(c.getColumnIndexOrThrow(Transactions.C_ID));
            double amt = c.getDouble(c.getColumnIndexOrThrow(Transactions.AMOUNT));
            values[c.getPosition()] = amt;
            colors[c.getPosition()] = Color.HSVToColor(new float[]{c.getPosition() * 30, 0.57f, 0.83f});
            data[c.getPosition()][0] = amt + "";
            data[c.getPosition()][1] = cat.get(cid);
            data[c.getPosition()][2] = String.format(Locale.getDefault(), "%.1f", amt * 100 / total) + "";
            chartData.add(new ValueDataEntry(cat.get(cid), amt));
        }
        c.close();
        list.setAdapter(new StatsAdapter(data,colors));
        chart.setData(total, values, colors);

        pie.data(chartData);
        pie.title(String.format("Total %.0f "+currency, total));
    }
}