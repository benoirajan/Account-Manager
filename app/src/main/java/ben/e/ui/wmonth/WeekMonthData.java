package ben.e.ui.wmonth;

import java.util.Comparator;

public class WeekMonthData {
    public static final Comparator<? super WeekMonthData> DATE = new Comparator<WeekMonthData>() {
        @Override
        public int compare(WeekMonthData o1, WeekMonthData o2) {
            return o2.date.compareTo(o1.date);
        }
    };
    String date;
    float income,
            expense;

    public WeekMonthData setDate(String date) {
        this.date = date;
        return this;
    }

    public void setExpense(float expense) {
        this.expense = expense;
    }

    public void setIncome(float income) {
        this.income = income;
    }
}
