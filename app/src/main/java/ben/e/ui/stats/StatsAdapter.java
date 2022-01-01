package ben.e.ui.stats;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import androidx.annotation.ColorInt;

import ben.e.R;

public class StatsAdapter extends BaseAdapter {
    private final String[][] data;
    private final int[] colors;

    public StatsAdapter(String[][] data, @ColorInt int[] colors) {
        this.data = data;
        this.colors = colors;
    }

    @Override
    public int getCount() {
        return data.length;
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_daily_row, parent, false);
        }
        TextView amt = convertView.findViewById(R.id.category);
        TextView name = convertView.findViewById(R.id.account);
        TextView percentage = convertView.findViewById(R.id.amount);
        amt.setText(data[position][0]);
        name.setText(data[position][1]);
        percentage.setText(data[position][2] + "%");
        amt.setTextAppearance(R.style.IncomeText);
        percentage.setBackgroundColor(colors[position]);
        percentage.setTextColor(0xffffffff);
        return convertView;
    }
}
