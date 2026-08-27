package com.terralink.data.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.terralink.data.api.ClientApi;
import com.terralink.data.model.ClientListItemResponse;
import com.terralink.data.model.PaginatedResponse;
import com.terralink.ui.common.Resource;

import java.io.File;

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

        MultipartBody.Part idFrontPart = prepareFilePart("nationalIdFront", idFront);
        MultipartBody.Part idBackPart = prepareFilePart("nationalIdBack", idBack);
        MultipartBody.Part passportPart = prepareFilePart("passportPhoto", passportPhoto);

        clientApi.registerClient(
                fullNamePart, nationalIdPart, phonePart, dobPart, genderPart, addressPart,
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
