package com.terralink.ui.officer.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.LoanApplicationResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.ClientRepository;
import com.terralink.data.repository.LoanRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.Resource;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerTasksViewModel extends ViewModel {
    private final LoanRepository loanRepository;
    private final UserRepository userRepository;
    private final ClientRepository clientRepository;

    @Inject
    public OfficerTasksViewModel(LoanRepository loanRepository, UserRepository userRepository, ClientRepository clientRepository) {
        this.loanRepository = loanRepository;
        this.userRepository = userRepository;
        this.clientRepository = clientRepository;
    }

    public LiveData<Resource<List<OfficerTask>>> getCombinedTasks() {
        MediatorLiveData<Resource<List<OfficerTask>>> combined = new MediatorLiveData<>();
        combined.setValue(Resource.loading());

        LiveData<Resource<PaginatedResponse<LoanApplicationResponse>>> appraisalsLive = loanRepository.getLoanApplications(null, 1, 50);
        LiveData<Resource<PaginatedResponse<ClientListItemResponse>>> clientsLive = clientRepository.getClients(1, 50, null);

        combined.addSource(appraisalsLive, appraisals -> updateCombined(combined, appraisals, clientsLive.getValue()));
        combined.addSource(clientsLive, clients -> updateCombined(combined, appraisalsLive.getValue(), clients));

        return combined;
    }

    private void updateCombined(MediatorLiveData<Resource<List<OfficerTask>>> combined,
                                Resource<PaginatedResponse<LoanApplicationResponse>> appraisals,
                                Resource<PaginatedResponse<ClientListItemResponse>> clients) {

        if (appraisals == null || clients == null) return;

        if (appraisals.getStatus() == LoginStatus.ERROR) {
            combined.setValue(Resource.error(appraisals.getMessage()));
            return;
        }
        if (clients.getStatus() == LoginStatus.ERROR) {
            combined.setValue(Resource.error(clients.getMessage()));
            return;
        }

        if (appraisals.getStatus() == LoginStatus.SUCCESS && clients.getStatus() == LoginStatus.SUCCESS) {
            List<OfficerTask> tasks = new ArrayList<>();
            
            if (appraisals.getData() != null) {
                for (LoanApplicationResponse app : appraisals.getData().getItems()) {
                    if ("SUBMITTED".equals(app.getStatus()) || "INFO_REQUESTED".equals(app.getStatus())) {
                        tasks.add(new OfficerTask.AppraisalTask(app));
                    }
                }
            }

            if (clients.getData() != null) {
                for (ClientListItemResponse client : clients.getData().getItems()) {
                    boolean needsAttention = !"VERIFIED".equals(client.getStatus()) || "INACTIVE".equals(client.getUserStatus());
                    if (needsAttention) {
                        tasks.add(new OfficerTask.VerificationTask(client));
                    }
                }
            }
            
            combined.setValue(Resource.success(tasks));
        }
    }

    public LiveData<Resource<UserProfileResponse>> getProfile() {
        return userRepository.getMe();
    }

    public LiveData<Resource<Void>> logout() {
        return userRepository.logout();
    }
}
