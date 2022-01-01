package ben.e.ui.acc;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ben.e.AccountEditorActivity;
import ben.e.AccountTransfer;
import ben.e.R;
import ben.e.database.DbUtil;
import ben.e.database.tables.Accounts;
import ben.e.database.tables.Users;

/**
 * A fragment representing a list of Items.
 */
public class AccountsViewFragment extends Fragment {

    TextView tAsset, tLiability, tTotal;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public AccountsViewFragment() {
    }

    public static AccountsViewFragment newInstance(int columnCount) {
        return new AccountsViewFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.accounts_list, container, false);
        tAsset = view.findViewById(R.id.col1);
        tLiability = view.findViewById(R.id.col2);
        tTotal = view.findViewById(R.id.col3);
        view.findViewById(R.id.add)
                .setOnClickListener(v ->
                        startActivity(new Intent(getContext(), AccountEditorActivity.class)
                                .putExtra(Users.UID,
                                        requireActivity().getIntent().getStringExtra(Users.UID))));

        view.findViewById(R.id.transfer)
                .setOnClickListener(v ->
                        startActivity(new Intent(getContext(), AccountTransfer.class)
                                .putExtra(Users.UID,
                                        requireActivity().getIntent().getStringExtra(Users.UID))));
        Context context = view.getContext();
        String currenc = PreferenceManager.getDefaultSharedPreferences(context).getString("currency", "");
        RecyclerView recyclerView = view.findViewById(R.id.list2);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));
        List<AccItem> data = getAccounts();
        recyclerView.setAdapter(new AccountAdapter(data, " " + currenc));
        return view;
    }

    private List<AccItem> getAccounts() {
        final String uid = requireActivity().getIntent().getStringExtra(Users.UID);
        String sql = "select * from " + Accounts.TABLE_NAME +
                " where " + Accounts.UID + "='" + uid + "'";
        Cursor c = new DbUtil(getContext()).getReadableDatabase().rawQuery(sql, null);
        List<AccItem> items = new ArrayList<>();
        float asset = 0, liability = 0;
        while (c.moveToNext()) {
            AccItem item = new AccItem();
            item.id = c.getLong(c.getColumnIndexOrThrow(Accounts.AC_ID));
            item.name = c.getString(c.getColumnIndexOrThrow(Accounts.AC_NAME));
            item.balance = c.getDouble(c.getColumnIndexOrThrow(Accounts.BALANCE));
            if (item.balance < 0)
                liability += item.balance;
            else
                asset += item.balance;
            items.add(item);
        }

        tTotal.setText(String.format("Total\n%.1f",asset+liability));
        tLiability.setText(String.format("Liability\n%.1f",Math.abs(liability)));
        tAsset.setText(String.format("Asset\n%.1f",asset));
        c.close();
        return items;
    }
}