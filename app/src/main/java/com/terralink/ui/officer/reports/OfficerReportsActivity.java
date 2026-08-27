package com.terralink.ui.officer.reports;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.terralink.databinding.ActivityOfficerReportsBinding;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class OfficerReportsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityOfficerReportsBinding binding = ActivityOfficerReportsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
