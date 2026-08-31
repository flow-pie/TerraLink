package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.ClientApi;
import com.terralink.data.model.AssetResponse;
import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.CreateAssetRequest;
import com.terralink.data.model.CreateIncomeAssessmentRequest;
import com.terralink.data.model.IncomeAssessmentResponse;
import com.terralink.data.model.KycDocumentResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.data.model.VerificationRejectRequest;
import com.terralink.data.model.VerifyAssetRequest;
import com.terralink.ui.common.Resource;

import java.io.File;
import java.util.List;

import javax.inject.Inject;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClientRepository {
    private final ClientApi clientApi;

    @Inject
    public ClientRepository(ClientApi clientApi) {
        this.clientApi = clientApi;
    }

    public LiveData<Resource<Void>> registerClient(
            String fullName,
            String nationalId,
            String phone,
            String dateOfBirth,
            String gender,
            String address,
            String email,
            String password,
            File idFront,
            File idBack,
            File passportPhoto
    ) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        RequestBody fullNamePart = toPlainTextBody(fullName);
        RequestBody nationalIdPart = toPlainTextBody(nationalId);
        RequestBody phonePart = toPlainTextBody(phone);
        RequestBody dobPart = toPlainTextBody(dateOfBirth);
        RequestBody genderPart = toPlainTextBody(gender);
        RequestBody addressPart = toPlainTextBody(address);
        RequestBody emailPart = toPlainTextBody(email);
        RequestBody passwordPart = toPlainTextBody(password);

        MultipartBody.Part idFrontPart = prepareFilePart("nationalIdFront", idFront);
        MultipartBody.Part idBackPart = prepareFilePart("nationalIdBack", idBack);
        MultipartBody.Part passportPart = prepareFilePart("passportPhoto", passportPhoto);

        clientApi.registerClient(
                fullNamePart, nationalIdPart, phonePart, dobPart, genderPart, addressPart,
                emailPart, passwordPart,
                idFrontPart, idBackPart, passportPart
        ).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    String errorMsg = "Registration Failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.postValue(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<PaginatedResponse<ClientListItemResponse>>> getClients(int page, int pageSize, String search) {
        MutableLiveData<Resource<PaginatedResponse<ClientListItemResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());

        clientApi.getClients(page, pageSize, search).enqueue(new Callback<PaginatedResponse<ClientListItemResponse>>() {
            @Override
            public void onResponse(Call<PaginatedResponse<ClientListItemResponse>> call, Response<PaginatedResponse<ClientListItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load clients"));
                }
            }

            @Override
            public void onFailure(Call<PaginatedResponse<ClientListItemResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });

        return result;
    }

    public LiveData<Resource<Void>> verifyClient(int clientId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.verifyClient(clientId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    String errorMsg = "Verification failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.postValue(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Void>> rejectClient(int clientId, String reason) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.rejectClient(clientId, new VerificationRejectRequest(reason)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    String errorMsg = "Rejection failed: " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorMsg += " - " + response.errorBody().string();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    result.postValue(Resource.error(errorMsg));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<List<KycDocumentResponse>>> getKycDocuments(int clientId) {
        MutableLiveData<Resource<List<KycDocumentResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.getKycDocuments(clientId).enqueue(new Callback<List<KycDocumentResponse>>() {
            @Override
            public void onResponse(Call<List<KycDocumentResponse>> call, Response<List<KycDocumentResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load KYC documents"));
                }
            }

            @Override
            public void onFailure(Call<List<KycDocumentResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<List<AssetResponse>>> getClientAssets(long clientId) {
        MutableLiveData<Resource<List<AssetResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.getClientAssets(clientId).enqueue(new Callback<List<AssetResponse>>() {
            @Override
            public void onResponse(Call<List<AssetResponse>> call, Response<List<AssetResponse>> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load assets"));
                }
            }

            @Override
            public void onFailure(Call<List<AssetResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<AssetResponse>> createAsset(long clientId, CreateAssetRequest request) {
        MutableLiveData<Resource<AssetResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.createAsset(clientId, request).enqueue(new Callback<AssetResponse>() {
            @Override
            public void onResponse(Call<AssetResponse> call, Response<AssetResponse> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to create asset"));
                }
            }

            @Override
            public void onFailure(Call<AssetResponse> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Void>> verifyAsset(long clientId, long assetId, double estimatedValue) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.verifyAsset(clientId, assetId, new VerifyAssetRequest(estimatedValue)).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Failed to verify asset"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Void>> rejectAsset(long clientId, long assetId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.rejectAsset(clientId, assetId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Failed to reject asset"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<List<IncomeAssessmentResponse>>> getIncomeAssessments(long clientId) {
        MutableLiveData<Resource<List<IncomeAssessmentResponse>>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.getIncomeAssessments(clientId).enqueue(new Callback<List<IncomeAssessmentResponse>>() {
            @Override
            public void onResponse(Call<List<IncomeAssessmentResponse>> call, Response<List<IncomeAssessmentResponse>> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to load income assessments"));
                }
            }

            @Override
            public void onFailure(Call<List<IncomeAssessmentResponse>> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<IncomeAssessmentResponse>> createIncomeAssessment(long clientId, CreateIncomeAssessmentRequest request) {
        MutableLiveData<Resource<IncomeAssessmentResponse>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.createIncomeAssessment(clientId, request).enqueue(new Callback<IncomeAssessmentResponse>() {
            @Override
            public void onResponse(Call<IncomeAssessmentResponse> call, Response<IncomeAssessmentResponse> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(response.body()));
                } else {
                    result.postValue(Resource.error("Failed to create income assessment"));
                }
            }

            @Override
            public void onFailure(Call<IncomeAssessmentResponse> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Void>> verifyIncomeAssessment(long clientId, long assessmentId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.verifyIncomeAssessment(clientId, assessmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Failed to verify income assessment"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    public LiveData<Resource<Void>> rejectIncomeAssessment(long clientId, long assessmentId) {
        MutableLiveData<Resource<Void>> result = new MutableLiveData<>();
        result.setValue(Resource.loading());
        clientApi.rejectIncomeAssessment(clientId, assessmentId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    result.postValue(Resource.success(null));
                } else {
                    result.postValue(Resource.error("Failed to reject income assessment"));
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                result.postValue(Resource.error("Network Error: " + t.getMessage()));
            }
        });
        return result;
    }

    private MultipartBody.Part prepareFilePart(String partName, File file) {
        if (file == null) return null;
        RequestBody requestFile = RequestBody.create(file, MediaType.parse("image/*"));
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private RequestBody toPlainTextBody(String value) {
        if (value == null) value = "";
        return RequestBody.create(value, MediaType.parse("text/plain"));
    }
}
