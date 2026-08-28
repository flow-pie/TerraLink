package com.terralink.ui.officer.tasks;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.KycDocumentResponse;
import com.terralink.data.repository.ClientRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ClientVerificationViewModel extends ViewModel {
    private final ClientRepository clientRepository;

    @Inject
    public ClientVerificationViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public LiveData<Resource<java.util.List<KycDocumentResponse>>> getKycDocuments(int clientId) {
        return clientRepository.getKycDocuments(clientId);
    }

    public LiveData<Resource<Void>> verifyClient(int clientId) {
        return clientRepository.verifyClient(clientId);
    }

    public LiveData<Resource<Void>> rejectClient(int clientId, String reason) {
        return clientRepository.rejectClient(clientId, reason);
    }
}
