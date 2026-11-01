package com.yunusemre.m3ustream.domain.repository

import com.yunusemre.m3ustream.data.local.entity.DownloadEntity
import com.yunusemre.m3ustream.data.local.entity.DownloadState
import kotlinx.coroutines.flow.Flow

interface DownloadRepository {
    fun getAllDownloads(): Flow<List<DownloadEntity>>
    fun getDownloadFlow(contentId: String): Flow<DownloadEntity?>
    suspend fun getDownload(contentId: String): DownloadEntity?
    suspend fun getDownloadsByState(vararg states: DownloadState): List<DownloadEntity>
    suspend fun addOrUpdateDownload(download: DownloadEntity)
    suspend fun updateDownloadState(contentId: String, state: DownloadState, errorMessage: String? = null)
    suspend fun updateDownloadProgress(contentId: String, state: DownloadState, progress: Float, downloadedBytes: Long, totalBytes: Long)
    suspend fun updateLocalPath(contentId: String, localPath: String?)
    suspend fun deleteDownload(contentId: String)
}
