package ben.e.ui.tabs;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import android.icu.util.Calendar;

public class TViewModel extends ViewModel {

    private final MutableLiveData<Calendar> mText;

    public TViewModel() {
        mText = new MutableLiveData<>();
    }

    public void setDate(Calendar date) {
        mText.setValue(date);
    }

    public LiveData<Calendar> getText() {
        return mText;
    }
}