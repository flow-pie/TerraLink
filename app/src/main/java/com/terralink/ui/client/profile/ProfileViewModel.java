package com.terralink.ui.client.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.AuthRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.auth.LoginStatus;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final MediatorLiveData<Resource<UserProfileResponse>> userProfile = new MediatorLiveData<>();
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Inject
    public ProfileViewModel(UserRepository userRepository, AuthRepository authRepository){
        this.userRepository = userRepository;
        this.authRepository = authRepository;
        refreshProfile();
    }

    public void refreshProfile() {
        LiveData<Resource<UserProfileResponse>> source = userRepository.getMe();
        userProfile.addSource(source, resource -> {
            userProfile.setValue(resource);
            if (resource.getStatus() != LoginStatus.LOADING) {
                userProfile.removeSource(source);
            }
        });
    }

    public LiveData<Resource< UserProfileResponse>> getActiveUser(){
        return userProfile;
    }

    public LiveData<Resource<Void>> logout(){
        return authRepository.logout();
    }
}
