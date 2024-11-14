package com.example.storyapp.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.example.storyapp.api.ApiService
import com.example.storyapp.data.request.LoginRequest
import com.example.storyapp.data.response.AddNewStoryResponse
import com.example.storyapp.data.response.DetailStoryResponse
import com.example.storyapp.data.response.ErrorResponse
import com.example.storyapp.data.response.StoriesResponse
import com.example.storyapp.preference.UserModel
import com.example.storyapp.preference.UserPreference
import com.example.storyapp.utils.Result
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.HttpException

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {
    suspend fun getStories(): StoriesResponse {
        val token = userPreference.getSession().first().token
       return apiService.getStories("Bearer $token")

    }

    suspend fun getDetailStory(id: String): DetailStoryResponse? {
        return try {
            val token = userPreference.getSession().first().token
            apiService.getDetailStory("Bearer $token", id)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    suspend fun register(name: String, email: String, password: String) = apiService.register(name, email, password)

    suspend fun login(loginRequest: LoginRequest) = apiService.logInUser(loginRequest.email,loginRequest.password)

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }
    suspend fun logout() {
        userPreference.logout()
    }

    fun getSession(): Flow<UserModel> = userPreference.getSession()

    fun addStory(file : MultipartBody.Part,description: RequestBody): LiveData<Result<AddNewStoryResponse>> = liveData{
        emit(Result.Loading)
        try{
            val token = userPreference.getSession().first().token
            val response = apiService.addStory("Bearer $token",file,description)
            emit(Result.Success(response))
        }catch (e: HttpException) {
            val jsonInString = e.response()?.errorBody()?.string()
            val errorBody = Gson().fromJson(jsonInString, ErrorResponse::class.java)
            val errorMessage = errorBody.message
            emit(Result.Error(errorMessage.toString()))
        }
    }

    companion object {
        @Volatile
        private var instance: StoryRepository? = null

        fun getInstance(apiService: ApiService, userPreference: UserPreference): StoryRepository =
            instance ?: synchronized(this) {
                instance ?: StoryRepository(apiService, userPreference)
            }.also { instance = it }
    }
}