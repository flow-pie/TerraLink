package com.terralink.ui.client.profile;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class ProfileViewModel extends ViewModel {

    private final LiveData<Resource<UserProfileResponse>> userProfile;

    @Inject
    public ProfileViewModel(UserRepository userRepository){
        this.userProfile = userRepository.getMe();
    }

    public LiveData<Resource< UserProfileResponse>> getActiveUser(){
        return userProfile;
    }
}
