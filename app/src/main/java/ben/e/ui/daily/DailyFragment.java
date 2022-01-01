package ben.e.ui.daily;

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
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.util.Date;

import ben.e.R;
import ben.e.database.DbUtil;
import ben.e.database.tables.Accounts;
import ben.e.database.tables.Category;
import ben.e.database.tables.Transactions;
import ben.e.database.tables.Users;
import ben.e.ui.tabs.TViewModel;
import ben.e.ui.util.DailyUtil;

import static ben.e.ui.util.DailyUtil.ACCOUNT;
import static ben.e.ui.util.DailyUtil.CATEGORY;
import static ben.e.ui.util.DailyUtil.DATE;
import static ben.e.ui.util.DailyUtil.NOTE;
import static ben.e.ui.util.DailyUtil.TYPE;

/**
 * A placeholder fragment containing a simple view.
 */
public class DailyFragment extends Fragment {

    private static final String ARG_SECTION_NUMBER = "section_number";
    private static String UID;
    private RecyclerView recyclerView;
    private Calendar calendar;
    private String currency;

    public static DailyFragment newInstance(int index) {
        DailyFragment fragment = new DailyFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(ARG_SECTION_NUMBER, index);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (calendar == null)
            calendar = Calendar.getInstance();
        getTransactions(calendar);
    }

    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_tab, container, false);
        recyclerView = root.findViewById(R.id.rView);
        UID = requireActivity().getIntent().getStringExtra(Users.UID);
        currency = PreferenceManager.getDefaultSharedPreferences(getContext()).getString("currency", "");
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));


        TViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(TViewModel.class);
        dashboardViewModel.getText().observe(getViewLifecycleOwner(), new Observer<Calendar>() {
            @Override
            public void onChanged(Calendar calendar) {
                getTransactions(calendar);
                DailyFragment.this.calendar = calendar;
            }
        });
        return root;
    }

    private void getTransactions(Calendar calendar) {
        new Query().execute(calendar);
    }

    class Query extends AsyncTask<Calendar, Void, Void> {

        private String[][] data;
        private double[] amount;
        private long[] ids;

        @Override
        protected Void doInBackground(Calendar... calendars) {
            Calendar calendar = calendars[0];
            SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
            SimpleDateFormat view = new SimpleDateFormat("dd yyyy.MM\n EEE ");
            String cal = sf.format(calendar);
            SQLiteDatabase db = new DbUtil(getContext()).getReadableDatabase();
            String sql = "SELECT t.*, " + Accounts.AC_NAME + ", " + Category.C_NAME + " FROM " +
                    Transactions.TABLE_NAME + " t," + Category.TABLE_NAME +
                    " c," + Accounts.TABLE_NAME + " a " +
                    "WHERE t.uId = '" + UID + "' AND t.acId = a.acId AND t.cId = c.cId " +
                    "AND t." + Transactions.DATE +
                    " < date('" + cal + "','start of month','+1 month') AND " + Transactions.DATE + " >= date('" + cal + "','start of month') " +
                    "ORDER BY t." + Transactions.DATE + " DESC";
            Cursor cursor = db.rawQuery(sql, null);

            data = new String[cursor.getCount()][DailyUtil.COUNT];
            amount = new double[cursor.getCount()];
            ids = new long[cursor.getCount()];
            while (cursor.moveToNext()) {
                long id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(Transactions.T_ID));
                String cName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Category.C_NAME));
                String acName = cursor.getString(
                        cursor.getColumnIndexOrThrow(Accounts.AC_NAME));
                String type = cursor.getString(
                        cursor.getColumnIndexOrThrow(Transactions.TYPE));
                String date = cursor.getString(
                        cursor.getColumnIndexOrThrow(Transactions.DATE));
                String note = cursor.getString(
                        cursor.getColumnIndexOrThrow(Transactions.NOTE));
                double amt = cursor.getDouble(
                        cursor.getColumnIndexOrThrow(Transactions.AMOUNT));
                Date d = null;
                try {
                    d = sf.parse(date);
                } catch (ParseException e) {
                    e.printStackTrace();
                }

                int p = cursor.getPosition();
                data[p][DATE] = view.format(d);
                data[p][CATEGORY] = cName;
                data[p][ACCOUNT] = acName;
                data[p][NOTE] = note.length() > 0 ? note : null;
                data[p][TYPE] = type;
                amount[p] = amt;
                ids[p] = id;
            }
            cursor.close();
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            recyclerView.setAdapter(new CardAdapterDaily(data, amount, ids, UID, currency));
        }
    }
}