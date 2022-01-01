package ben.e.cat;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import ben.e.R;

public class CategoryAdapter extends BaseAdapter {
    private final String[] names;

    public CategoryAdapter (String[] names){
        this.names = names;
    }

    @Override
    public int getCount() {
        return names.length;
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
        if(convertView == null) {
            TextView tv = new TextView(parent.getContext());
            int padding = (int) parent.getContext().getResources().getDimension(R.dimen.appbar_padding);
            tv.setPadding(padding,padding,padding,padding);
            convertView = tv;
        }
        ((TextView)convertView).setText(names[position]);
        return convertView;
    }
}