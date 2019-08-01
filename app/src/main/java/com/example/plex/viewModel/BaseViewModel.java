package com.example.plex.viewModel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;

import java.lang.ref.WeakReference;

public class BaseViewModel<N> extends AndroidViewModel {

    private  WeakReference<N> mNavigator;
    public BaseViewModel(@NonNull Application application) {
        super(application);
    }
    public N getNavigator() {
        return mNavigator.get();
    }

    public void setNavigator(N mNavigator) {
        this.mNavigator = new WeakReference<>(mNavigator);
    }
}
