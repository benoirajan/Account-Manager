package ben.e.data;

import android.database.sqlite.SQLiteDatabase;

import ben.e.data.model.LoggedInUser;
import ben.e.database.DbUtil;

import java.io.IOException;

/**
 * Class that handles authentication w/ login credentials and retrieves user information.
 */
public class LoginDataSource {

    public Result<LoggedInUser> login(String username, String password, DbUtil dbUtil) {

        try {
            // TODO: handle loggedInUser authentication
            SQLiteDatabase db = dbUtil.getWritableDatabase();
            LoggedInUser fakeUser = DbUtil.login(db, password, username);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(new IOException("Error logging in", e));
        }
    }

    public Result<LoggedInUser> register(String username, String name, String password, DbUtil dbUtil) {

        try {
            // TODO: handle loggedInUser authentication
            SQLiteDatabase db = dbUtil.getWritableDatabase();
            LoggedInUser fakeUser = DbUtil.register(db, password, username, name);
            return new Result.Success<>(fakeUser);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result.Error(new IOException("Error in register", e));
        }
    }
}