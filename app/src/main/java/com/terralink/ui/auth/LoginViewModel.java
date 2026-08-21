package com.terralink.ui.auth;

import android.widget.Toast;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.LoginRequest;
import com.terralink.data.model.LoginResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.AuthRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;
@HiltViewModel
public class LoginViewModel extends ViewModel {
    private final AuthRepository authRepository;
    private final UserRepository userRepository;

    @Inject
    public LoginViewModel(
            AuthRepository authRepository,
            UserRepository userRepository
    ){
        this.authRepository = authRepository;
        this.userRepository = userRepository;
    }
    //hold current login result
    private final MediatorLiveData<Resource<LoginResponse>> loginResult = new MediatorLiveData<>();

    //expose as immutable live data
    public LiveData<Resource <LoginResponse>> getLoginResult(){
        return loginResult;
    }

    //expose it as live data not mutable data
    public void login(LoginRequest request){
        LiveData<Resource <LoginResponse>> source = authRepository.login(request);

        loginResult.addSource(
                source,
                result -> {
                    loginResult.setValue(result);
                    if (result.getStatus() != LoginStatus.LOADING) {
                        loginResult.removeSource(source);
                    }
                }
        );
    }


    //get Logged in user
    public LiveData<Resource<UserProfileResponse>> getCurrentUser() {
        return userRepository.getMe();
    }


}
