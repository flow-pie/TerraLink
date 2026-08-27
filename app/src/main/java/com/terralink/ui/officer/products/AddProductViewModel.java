package com.terralink.ui.officer.products;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoanProductRequest;
import com.terralink.data.repository.LoanRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AddProductViewModel extends ViewModel {
    private final LoanRepository loanRepository;

    @Inject
    public AddProductViewModel(LoanRepository loanRepository) {
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<Void>> createProduct(LoanProductRequest request) {
        return loanRepository.createLoanProduct(request);
    }
}
