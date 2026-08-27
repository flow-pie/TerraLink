package com.terralink.ui.officer.clients;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.ClientLoansResponse;
import com.terralink.data.model.LoanDetailsResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.RepaymentInstallments;
import com.terralink.data.repository.ClientRepository;
import com.terralink.data.repository.LoanRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerClientsViewModel extends ViewModel {
    private final ClientRepository clientRepository;
    private final LoanRepository loanRepository;

    @Inject
    public OfficerClientsViewModel(ClientRepository clientRepository, LoanRepository loanRepository) {
        this.clientRepository = clientRepository;
        this.loanRepository = loanRepository;
    }

    public LiveData<Resource<PaginatedResponse<ClientListItemResponse>>> getClients(int page, int pageSize, String search) {
        return clientRepository.getClients(page, pageSize, search);
    }

    public LiveData<Resource<List<ClientLoansResponse>>> getClientLoans(String clientId) {
        return loanRepository.getClientLoans(clientId);
    }

    public LiveData<Resource<LoanDetailsResponse>> getLoanDetails(String loanId) {
        return loanRepository.getClientLoanDetails(loanId);
    }

    public LiveData<Resource<List<RepaymentInstallments>>> getRepaymentSchedule(String loanId) {
        return loanRepository.getRepaymentSchedule(loanId);
    }
}
