package ben.e.ui.wmonth;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import ben.e.R;

public class WeekMonthAdapter extends RecyclerView.Adapter<WeekMonthAdapter.ViewHolder> {
    private final List<WeekMonthData> data;
    private final boolean isMonth;
    private final String currency;

    public WeekMonthAdapter(List<WeekMonthData> datas, boolean isMonth, String currency){
        data = datas;
        this.isMonth = isMonth;
        this.currency = currency;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_week_month, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        View view = holder.itemView;
        String date = data.get(position).date;
        if(isMonth){
            try {
                Date d = new SimpleDateFormat("yyyy-MM-dd").parse(date);
                date = new SimpleDateFormat("MMM").format(d);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        ((TextView)view.findViewById(R.id.date)).setText(date);
        ((TextView)view.findViewById(R.id.income)).setText(data.get(position).income+currency);
        ((TextView)view.findViewById(R.id.expense)).setText(data.get(position).expense+currency);
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ViewHolder(View view) {
            super(view);
        }
    }
}
