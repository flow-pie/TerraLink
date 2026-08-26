package com.terralink.ui.client.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.AuthRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final LiveData<Resource<UserProfileResponse>> userProfile;
    private final AuthRepository authRepository;

    @Inject
    public ProfileViewModel(UserRepository userRepository, AuthRepository authRepository){
        this.userProfile = userRepository.getMe();
        this.authRepository = authRepository;
    }

    public LiveData<Resource< UserProfileResponse>> getActiveUser(){
        return userProfile;
    }

    public LiveData<Resource<Void>> logout(){
        return authRepository.logout();
    }
}
