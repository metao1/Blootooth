package com.metao.app.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.metao.app.R;
import com.metao.app.model.BLEService;

public class DetailsActivity extends AppCompatActivity {

    private BLEService bleService;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details_layout);
        Intent intent = getIntent();
        if (intent != null) {
            BLEService bleService = intent.getParcelableExtra("parcelable_extra");
            if (bleService != null) {
                this.bleService = bleService;
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Toast.makeText(this, bleService.getUuid(), Toast.LENGTH_SHORT).show();
    }
}
