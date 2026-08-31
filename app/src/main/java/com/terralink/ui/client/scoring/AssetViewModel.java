package com.terralink.ui.client.scoring;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.CreateAssetRequest;
import com.terralink.data.model.CreateIncomeAssessmentRequest;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.UserProfileResponse;
import com.terralink.data.repository.ClientRepository;
import com.terralink.data.repository.UserRepository;
import com.terralink.ui.common.Resource;

import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.lifecycle.HiltViewModel;

@HiltViewModel
public class AssetViewModel extends ViewModel {

    private final ClientRepository clientRepository;
    private final UserRepository userRepository;

    @Inject
    public AssetViewModel(ClientRepository clientRepository, UserRepository userRepository) {
        this.clientRepository = clientRepository;
        this.userRepository = userRepository;
    }

    public LiveData<Resource<UserProfileResponse>> getActiveUser() {
        return userRepository.getMe();
    }

    public LiveData<Resource<List<AssetResponse>>> getClientAssets(long clientId) {
        return clientRepository.getClientAssets(clientId);
    }

    public LiveData<Resource<AssetResponse>> createAsset(long clientId, CreateAssetRequest request) {
        return clientRepository.createAsset(clientId, request);
    }
    
    public LiveData<Resource<Void>> verifyAsset(long clientId, long assetId, double estimatedValue) {
        return clientRepository.verifyAsset(clientId, assetId, estimatedValue);
    }

    public LiveData<Resource<Void>> rejectAsset(long clientId, long assetId) {
        return clientRepository.rejectAsset(clientId, assetId);
    }

    public LiveData<Resource<List<IncomeAssessmentResponse>>> getIncomeAssessments(long clientId) {
        return clientRepository.getIncomeAssessments(clientId);
    }

    public LiveData<Resource<IncomeAssessmentResponse>> createIncomeAssessment(long clientId, CreateIncomeAssessmentRequest request) {
        return clientRepository.createIncomeAssessment(clientId, request);
    }

    public LiveData<Resource<Void>> verifyIncomeAssessment(long clientId, long assessmentId) {
        return clientRepository.verifyIncomeAssessment(clientId, assessmentId);
    }

    public LiveData<Resource<Void>> rejectIncomeAssessment(long clientId, long assessmentId) {
        return clientRepository.rejectIncomeAssessment(clientId, assessmentId);
    }
}
