package com.example.plex.viewModel;

import android.app.Application;
import android.content.Intent;

import androidx.annotation.NonNull;

import com.example.plex.navigators.FillDetailsNavigator;
import com.example.plex.util.ImportantMethods;

public class FillDetailsViewModel extends BaseViewModel<FillDetailsNavigator> {

    public FillDetailsViewModel(@NonNull Application application) {
        super(application);
    }

    public void openViewModelGalary() {
        if(ImportantMethods.hasAllPermissions(getApplication().getApplicationContext())){getNavigator().openGalary();}
    }
    public void openViewModelCamera() {
        if(ImportantMethods.hasAllPermissions(getApplication().getApplicationContext())){getNavigator().openCamera();}
    }


}
