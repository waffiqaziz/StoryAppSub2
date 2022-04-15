package com.dicoding.storyapp.ui.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.dicoding.storyapp.data.model.UserModel
import com.dicoding.storyapp.data.remote.response.ApiResponse
import com.dicoding.storyapp.data.remote.retrofit.ApiConfig
import com.dicoding.storyapp.helper.Helper
import okhttp3.MultipartBody
import org.json.JSONObject
import org.json.JSONTokener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddStoryViewModel : ViewModel() {
  private val _isLoading = MutableLiveData<Boolean>()
  val isLoading: LiveData<Boolean> = _isLoading

  fun uploadImage(
    user: UserModel,
    description: String,
    imageMultipart: MultipartBody.Part,
    callback: Helper.ApiCallbackString
  ) {
    _isLoading.value = true
      val service = ApiConfig().getApiService().addStories("Bearer ${user.token}", description, imageMultipart)
      service.enqueue(object : Callback<ApiResponse> {
        override fun onResponse(
          call: Call<ApiResponse>,
          response: Response<ApiResponse>
        ) {
          if (response.isSuccessful) {
            val responseBody = response.body()
            if (responseBody != null && !responseBody.error) {
              callback.onResponse(response.body() != null, SUCCESS)
            }
          } else {
            Log.e(TAG, "onFailure: ${response.message()}")

            // get message error
            val jsonObject = JSONTokener(response.errorBody()!!.string()).nextValue() as JSONObject
            val message = jsonObject.getString("message")
            callback.onResponse(false, message)
          }
        }
        override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
          _isLoading.value = false
          Log.e(TAG, "onFailure: ${t.message}")
          callback.onResponse(false, t.message.toString())
        }
      })

  }

  companion object {
    private const val TAG = "AddStoryViewModel"
    private const val SUCCESS = "success"
  }
}