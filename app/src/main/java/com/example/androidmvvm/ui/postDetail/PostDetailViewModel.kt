package com.example.androidmvvm.ui.postDetail

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.view.View
import com.example.androidmvvm.model.Post
import com.example.androidmvvm.repository.PostsRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers

class PostDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val postTitle = MutableLiveData<String>()
    private val postBody = MutableLiveData<String>()
    private var postsRepository: PostsRepository = PostsRepository(application)

    private lateinit var networkSubscription: Disposable
    private lateinit var dbSubscription: Disposable
    val loadingVisibility: MutableLiveData<Int> = MutableLiveData()
    val post: MutableLiveData<Post> = MutableLiveData()


    public fun loadPostDetail() {
        if (post.value != null) {
            networkSubscription = postsRepository.getPostDetailFromApi(post.value!!.id).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe { showLoading() }
                .doOnTerminate { hideLoading() }
                .subscribe(
                    { postDetail -> onPostDetailSuccess(postDetail) },
                    { onPostDetailError() }
                )
        }

    }

    private fun onPostDetailSuccess(postDetail: Post?) {
        post.value = postDetail
        postTitle.value = postDetail?.title
        postBody.value = postDetail?.body
    }

    private fun onPostDetailError() {

        dbSubscription = postsRepository.getPostDetailFromDb(post.value!!.id)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {}
            .doOnTerminate {}
            .subscribe(
                { postDetail ->
                    onPostDetailFetchedFromDb(postDetail)
                }, {
                    onErrorFetchingFromDb()
                }
            )

    }

    private fun onPostDetailFetchedFromDb(postDetail: Post) {
        post.value = postDetail
        postTitle.value = postDetail?.title
        postBody.value = postDetail?.body
    }


    private fun hideLoading() {
        loadingVisibility.value = View.GONE
    }

    private fun showLoading() {
        loadingVisibility.value = View.VISIBLE
        // errorMessage.value = null
    }


    private fun onErrorFetchingFromDb() {
        //errorMessage.value = R.string.post_error
    }

    fun getPostTitle(): MutableLiveData<String> {
        return postTitle
    }

    fun getPostBody(): MutableLiveData<String> {
        return postBody
    }

    override fun onCleared() {
        super.onCleared()
        if (::networkSubscription.isInitialized)
            networkSubscription.dispose()
        if (::dbSubscription.isInitialized)
            dbSubscription.dispose()
    }
}