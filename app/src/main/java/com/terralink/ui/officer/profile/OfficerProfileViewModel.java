package com.terralink.ui.officer.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class OfficerProfileViewModel extends ViewModel {
    private final UserRepository userRepository;

    @Inject
    public OfficerProfileViewModel(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getProfile() {
        return userRepository.getMe();
    }

    public LiveData<Resource<Void>> logout() {
        return userRepository.logout();
    }
}
