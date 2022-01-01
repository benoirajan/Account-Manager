package ben.e.ui.acc;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.List;

import ben.e.AccountEditorActivity;
import ben.e.R;
import ben.e.database.tables.Accounts;

/**
 * {@link RecyclerView.Adapter} that can display a {@link AccItem}.
 */
public class AccountAdapter extends RecyclerView.Adapter<AccountAdapter.ViewHolder> {

    private final List<AccItem> mValues;
    private String currency;

    public AccountAdapter(List<AccItem> items, String currency) {
        mValues = items;
        this.currency = currency;
    }

    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.accounts_list_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        holder.accName.setText(mValues.get(position).name);
        final double balance = mValues.get(position).balance;
        holder.balance.setText(balance + this.currency);
        int color = holder.balance.getResources().getColor(balance<0?android.R.color.holo_orange_dark: R.color.blue
                ,null);
        holder.balance.setTextColor(color);
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView accName;
        public final TextView balance;
        public AccItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            accName = view.findViewById(R.id.item_name);
            balance = view.findViewById(R.id.item_amount);
            view.setOnClickListener(tview->{
                Intent acc = new Intent(view.getContext(), AccountEditorActivity.class);
                acc.putExtra(Accounts.AC_ID,mItem.id);
                view.getContext().startActivity(acc);
            });
        }

        @NotNull
        @Override
        public String toString() {
            return super.toString() + " '" + balance.getText() + "'";
        }
    }
}