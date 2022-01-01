package ben.e.data;

import android.widget.TextView;

public class Validation {

    public static boolean validateText(TextView view) {
        if (view.getText().toString().length() > 0)
            return true;
        view.setError("This field id required");
        view.requestFocus();
        return false;
    }
}
