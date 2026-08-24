package com.terralink.ui.client.loan;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.terralink.databinding.ActivityApplyLoanBinding;

public class ApplyLoanActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstances ){
        super.onCreate(savedInstances);

        ActivityApplyLoanBinding loanBinding = ActivityApplyLoanBinding.inflate(getLayoutInflater());
        setContentView(loanBinding.getRoot());


    }
}
