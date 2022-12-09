/*
 * Copyright (C) 2019 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.devbyteviewer.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.android.devbyteviewer.database.getDatabase
import com.example.android.devbyteviewer.domain.DevByteVideo
import com.example.android.devbyteviewer.network.DevByteNetwork
import com.example.android.devbyteviewer.network.asDomainModel
import com.example.android.devbyteviewer.repository.VideosRepository
import kotlinx.coroutines.*
import java.io.IOException

/**
 * DevByteViewModel didesain untuk menyimpan dan mengatur UI-related data pada lifecycle conscious.
 * memperbolehkan data untuk bertahan dari perubahan configurasi.
 */
class DevByteViewModel(application: Application) : AndroidViewModel(application) {

    /**
     * sumber data pada ViewModel diambil dari results from.
     */
    private val videosRepository = VideosRepository(getDatabase(application))

    /**
     * playlist videos ditampilkan pada layar.
     */
    val playlist = videosRepository.videos

    /**
     * Event terjadi ketika network error. dibuat private untuk menghindari pembeberan
     * caranya dengan mengatur value dengan observers.
     */
    private var _eventNetworkError = MutableLiveData<Boolean>(false)

    /**
     * Event terjadi ketika network error. Views seharusnya menggunakan ini untuk mengakses data.
     */
    val eventNetworkError: LiveData<Boolean>
        get() = _eventNetworkError

    /**
     * tanda untuk tampilan error message. dibuat private untuk menghindari pembeberan
     * caranya dengan mengatur value dengan observers.
     */
    private var _isNetworkErrorShown = MutableLiveData<Boolean>(false)

    /**
     * tanda untuk tampilan the error message. Views seharusnya menggunakan ini untuk mengakses data.
     */
    val isNetworkErrorShown: LiveData<Boolean>
        get() = _isNetworkErrorShown

    /**
     * init{} dipanggil langsung ketika ViewModel terbuat.
     */
    init {
        refreshDataFromRepository()
    }

    /**
     * memperbarui data dari repository. mengguankan coroutine launch untuk menjalankannya pada
     * background thread.
     */
    private fun refreshDataFromRepository() {
        viewModelScope.launch {
            try {
                videosRepository.refreshVideos()
                _eventNetworkError.value = false
                _isNetworkErrorShown.value = false

            } catch (networkError: IOException) {
                // Show a Toast error message and hide the progress bar.
                if(playlist.value.isNullOrEmpty())
                    _eventNetworkError.value = true
            }
        }
    }

    /**
     * Resets tanda network error.
     */
    fun onNetworkErrorShown() {
        _isNetworkErrorShown.value = true
    }

    /**
     * Factory untuk membangun DevByteViewModel dengan parameter
     */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(DevByteViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return DevByteViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}
