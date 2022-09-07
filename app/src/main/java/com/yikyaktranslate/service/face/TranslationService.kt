package com.yikyaktranslate.service.face

import com.yikyaktranslate.model.Language
import okhttp3.MultipartBody
import retrofit2.Response

class TranslationService(private val api: TranslationApi) {

    suspend fun fetchLanguages(): ApiResult<List<Language>> {
        return api.getLanguages().toApiResult()
    }

    suspend fun translate(sourceLanguage: String, targetLanguage: String, text: String): ApiResult<Translation> {
        return api.translateString(
            MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("source", sourceLanguage)
                .addFormDataPart("target", targetLanguage)
                .addFormDataPart("q", text)
                .addFormDataPart("format", "text")
                .build()
        ).toApiResult()
    }
}

/**
 * Below is a copy past of API sealed classes. Makes it easy to consume and understand responses from retrofit.
 *
 * When it comes to Compose and managing UI state, I haven't really come to a decision on what's best. I feel that this ApiResult pattern that I've
 * come accustom too is really great - but it's not valuable for knowing when it's actually performing the fetch. This results in two classes.
 *
 * API Result, and the UIState that is defined in the same file as the TranslateViewModel Class.
 */
fun <T : Any> Response<T>.toApiResult(): ApiResult<T> {
    var result: ApiResult<T> = ApiResult.Failure.General(this.code(), this.message())
    when {
        this.isSuccessful -> {
            val body = this.body()
            if (body != null) {
                result = ApiResult.Success(body) // toModel method produces our OUTPUT
            }
        }
        this.code() == 404 -> result = ApiResult.Failure.NotFound
        this.code() == 429 -> result = ApiResult.Failure.ExcessiveRequests

    }
    return result
}

sealed class ApiResult<out SUCCESS_TYPE : Any?> {
    /**
     * Additionally - if we have multiple success codes, I could track them here
     * Or even follow the nested sealed class pattern.
     */
    data class Success<out SUCCESS_TYPE : Any>(val data: SUCCESS_TYPE) : ApiResult<SUCCESS_TYPE>()

    /**
     * We could get a lot more specific with these errors
     */
    sealed class Failure : ApiResult<Nothing>() {

        // 429 error "Too many requests"
        object ExcessiveRequests : Failure()

        // 404 error "Example of additional error catching"
        object NotFound : Failure()

        // General error "Catch all"
        data class General(val errorCode: Int, val message: String?) : Failure()
    }
}
