package com.terralink.ui.client.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.terralink.R;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.databinding.ActivityClientHomepageBinding;
import com.terralink.ui.client.profile.ProfileActivity;
import java.util.Locale;
import java.util.List;
import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class ClientHomepageActivity extends AppCompatActivity {

    private HomeViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityClientHomepageBinding homepageBinding = ActivityClientHomepageBinding
                .inflate(getLayoutInflater());

        setContentView(homepageBinding.getRoot());

        viewModel = new ViewModelProvider(this)
                .get(HomeViewModel.class);

        // get active user info
        viewModel.getActiveUser().observe(this,
                result -> {
                    switch (result.getStatus()){
                        case LOADING:

                            // Show loading UI.
                            homepageBinding.tvBorrowerName.setText("Loading...");

                            break;

                        case SUCCESS:

                            UserProfileResponse client =
                                    result.getData();

                            if (client != null && client.getFullName() != null) {
                                homepageBinding.tvBorrowerName.setText(client.getFullName());

                                //get client loan info including loanId
                                viewModel.getClientLoans(client.getClientId()).observe(this,
                                        loanResult->{
                                                switch (result.getStatus()){
                                                    case LOADING:

                                                        homepageBinding.tvLoanBalance.setText(
                                                                "Loading..."
                                                        );

                                                        break;

                                                    case SUCCESS:

                                                        List<ClientLoansResponse> loans = loanResult.getData();

                                                        if (loans != null && !loans.isEmpty()) {

                                                            ClientLoansResponse loan = loans.get(0);
                                                            //get loan detail using the obtained loan ID
                                                            viewModel.getClientDetails(loan.getLoanNo()).observe(
                                                                    this,
                                                                    loanDetailsResults ->{
                                                                        switch (loanDetailsResults.getStatus()){
                                                                            case LOADING:

                                                                                homepageBinding.tvLoanBalance.setText(
                                                                                        "Loading..."
                                                                                );

                                                                                break;

                                                                            case SUCCESS:
                                                                                homepageBinding.tvLoanBalance.setText(
                                                                                        String.format(
                                                                                                Locale.getDefault(),
                                                                                                "KES %,.2f",
                                                                                                loan.getBalance()
                                                                                        )
                                                                                );

                                                                                homepageBinding.cardNewLoanApp.setOnClickListener(v -> {
                                                                                    startActivity();
                                                                                });
                                                                            case ERROR:
                                                                                Toast.makeText(
                                                                                        this,
                                                                                        result.getMessage(),
                                                                                        Toast.LENGTH_LONG
                                                                                ).show();

                                                                                Log.e("HomeActivity", "onCreate: "+ result.getMessage());
                                                                        }
                                                                    }
                                                            );
                                                        }

                                                        break;

                                                    case ERROR:

                                                        homepageBinding.tvLoanBalance.setText(
                                                                "Unable to load"
                                                        );

                                                        break;
                                                }
                                        }
                                );

                                homepageBinding.bottomNavigationView.setOnItemSelectedListener(item->{

                                    int itemId = item.getItemId();

                                    if(itemId == R.id.nav_home)
                                        return true;

                                    else if (itemId == R.id.nav_loans)
                                        return true;

                                    else if(itemId == R.id.nav_history)
                                        return true;

                                    else if(itemId == R.id.nav_profile) {
                                        Intent intent = new Intent(
                                                ClientHomepageActivity.this,
                                                ProfileActivity.class
                                        );

                                        startActivity(intent);
                                    }

                                    return true;
                                });
                            }

                            break;

                        case ERROR:

                            // Show an error message.
                            homepageBinding.tvBorrowerName.setText("Error loading profile");
                            Toast.makeText(
                                this,
                                    result.getMessage(),
                                    Toast.LENGTH_LONG
                            ).show();

                            Log.e("HomeActivity", "onCreate: "+ result.getMessage());

                            break;
                    }
                }
        );

    }
}