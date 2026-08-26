package com.terralink.ui.officer.registration;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.repository.ClientRepository;
import com.terralink.ui.common.Resource;

import java.io.File;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class RegisterClientViewModel extends ViewModel {
    private final ClientRepository clientRepository;

    // Multi-step form data
    public String fullName;
    public String nationalId;
    public String phone;
    public String dateOfBirth;
    public String gender = "MALE";
    public String address;
    public File idFront;
    public File idBack;
    public File passportPhoto;

    @Inject
    public RegisterClientViewModel(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    public LiveData<Resource<Void>> register() {
        return clientRepository.registerClient(
                fullName, nationalId, phone, dateOfBirth, gender, address,
                idFront, idBack, passportPhoto
        );
    }
}
