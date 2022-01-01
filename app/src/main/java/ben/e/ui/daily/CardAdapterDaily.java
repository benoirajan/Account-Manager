package ben.e.ui.daily;

import android.content.Intent;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.BackgroundColorSpan;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

import ben.e.R;
import ben.e.TransactionAddActivity;
import ben.e.database.DbUtil;
import ben.e.database.tables.Transactions;

import static ben.e.ui.util.DailyUtil.ACCOUNT;
import static ben.e.ui.util.DailyUtil.CATEGORY;
import static ben.e.ui.util.DailyUtil.DATE;
import static ben.e.ui.util.DailyUtil.NOTE;
import static ben.e.ui.util.DailyUtil.TYPE;

public class CardAdapterDaily extends RecyclerView.Adapter<CardAdapterDaily.ViewHolder> {

    private final ArrayList<Integer> pos;
    private final double[] amount;
    private final long[] ids;
    private final String uId;
    private final String currency;
    private String[][] localDataSet;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder).
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final View v;

        public ViewHolder(View view) {
            super(view);
            v = view;
        }

        public View getView() {
            return v;
        }
    }

    public CardAdapterDaily(String[][] dataSet, double[] amt, long[] id, String uid, String currency) {
        localDataSet = dataSet;
        this.currency = " " + currency;
        amount = amt;
        uId = uid;
        ids = id;
        String date = "";
        pos = new ArrayList<Integer>();
        for (int i = 0; i < dataSet.length; i++)
            if (!date.equals(dataSet[i][0])) {
                date = dataSet[i][0];
                pos.add(i);
            }
    }

    // Create new views (invoked by the layout manager)
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.card_daily, viewGroup, false);
        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NotNull ViewHolder viewHolder, final int position) {
        int fir = pos.get(position);
        int sec;
        try {
            sec = pos.get(position + 1);
        } catch (Exception e) {
            sec = localDataSet.length;
            e.printStackTrace();
        }
        View view = viewHolder.getView();
        SpannableString ss = new SpannableString(localDataSet[fir][DATE]);
        if (localDataSet[fir][DATE].contains("Sun"))
            ss.setSpan(new BackgroundColorSpan(0xFFF44336), 11, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        else if (localDataSet[fir][DATE].contains("Sat"))
            ss.setSpan(new BackgroundColorSpan(0xFF03A9F4), 11, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        else
            ss.setSpan(new BackgroundColorSpan(0xA0303030), 11, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ss.setSpan(new ForegroundColorSpan(Color.WHITE), 11, ss.length(), Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        ((TextView) view.findViewById(R.id.date)).setText(ss.subSequence(0, 2));
        ((TextView) view.findViewById(R.id.month)).setText(ss.subSequence(3, ss.length()));
        TableLayout tableLayout = view.findViewById(R.id.table);
        float ex = 0, in = 0;
        for (int i = fir; i < sec; i++) {
            boolean isExpense = localDataSet[i][TYPE].equals(DbUtil.TYPE_EXPENSE);
            if (isExpense) ex += amount[i];
            else in += amount[i];

            View tRow = LayoutInflater.from(tableLayout.getContext()).inflate(R.layout.card_daily_row, tableLayout, false);
            ((TextView) tRow.findViewById(R.id.category)).setText(localDataSet[i][CATEGORY]);
            ((TextView)tRow.findViewById(R.id.account)).setText(localDataSet[i][ACCOUNT]);
            final TextView note = (TextView) tRow.findViewById(R.id.note);
            if (localDataSet[i][NOTE] != null) {
                note.setVisibility(View.VISIBLE);
                note.setText(localDataSet[i][NOTE]);
            }
            TextView amt = tRow.findViewById(R.id.amount);
            amt.setTextAppearance(isExpense ? R.style.ExpenseText : R.style.IncomeText);
            amt.setText(amount[i] + currency);
            tableLayout.addView(tRow);
            int finalI = i;
            tRow.setOnClickListener(v -> {
                view.getContext().startActivity(new Intent(view.getContext(), TransactionAddActivity.class)
                        .putExtra(Transactions.UID, uId)
                        .putExtra(Transactions.T_ID, ids[finalI]));
            });
        }
        ((TextView) view.findViewById(R.id.income)).setText(in + currency);
        ((TextView) view.findViewById(R.id.expense)).setText(ex + currency);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return pos.size();
    }
}