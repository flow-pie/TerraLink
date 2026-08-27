package com.terralink.ui.officer.clients;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.repository.ClientRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerClientsViewModel extends ViewModel {
    private final ClientRepository clientRepository;

    @Inject
    public OfficerClientsViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public LiveData<Resource<PaginatedResponse<ClientListItemResponse>>> getClients(int page, int pageSize, String search) {
        return clientRepository.getClients(page, pageSize, search);
    }
}
