package ben.e.ui.tabs;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import org.jetbrains.annotations.NotNull;

import ben.e.ui.daily.DailyFragment;
import ben.e.ui.wmonth.WeekMonthFragment;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {
    public static final int DAILY = 0, WEEKLY = 1, MONTHLY = 2, BUDGET = 3;
    private static final CharSequence[] TAB_TITLES = {"Daily", "Weekly", "Monthly", "Budget"};
    private final Context mContext;
    private final String currency;

    public SectionsPagerAdapter(Context context, FragmentManager fm, String... strings) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
        this.currency = strings[0];
    }

    @NotNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case DAILY:
                return DailyFragment.newInstance(position);
            case WEEKLY:
            case MONTHLY:
                return WeekMonthFragment.newInstance(position,currency);
            case BUDGET:
                return BudgetFragment.newInstance(currency);
        }
        return null;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return TAB_TITLES[position];
    }

    @Override
    public int getCount() {
        return TAB_TITLES.length;
    }
}