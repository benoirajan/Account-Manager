package ben.e.database.tables;

import android.provider.BaseColumns;

public class Transactions implements BaseColumns {

    //table data
    public static final String TABLE_NAME = "transactions";
    public static final String T_ID = "tId";
    public static final String UID = Users.UID;
    public static final String C_ID = Category.C_ID;
    public static final String AC_ID = Accounts.AC_ID;
    public static final String DATE = "date";
    public static final String TIME = "time";
    public static final String AMOUNT = "amount";
    public static final String NOTE = "note";
    public static final String TYPE = "tType";
}

