package com.yunusemre.m3ustream.data.repository

import com.yunusemre.m3ustream.data.local.dao.DownloadDao
import com.yunusemre.m3ustream.data.local.entity.DownloadEntity
import com.yunusemre.m3ustream.data.local.entity.DownloadState
import com.yunusemre.m3ustream.domain.repository.DownloadRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DownloadRepositoryImpl @Inject constructor(
    private val downloadDao: DownloadDao
) : DownloadRepository {
    override fun getAllDownloads(): Flow<List<DownloadEntity>> = downloadDao.getAllDownloads()
    
    override fun getDownloadFlow(contentId: String): Flow<DownloadEntity?> = downloadDao.getDownloadFlow(contentId)
    
    override suspend fun getDownload(contentId: String): DownloadEntity? = downloadDao.getDownload(contentId)
    
    override suspend fun getDownloadsByState(vararg states: DownloadState): List<DownloadEntity> {
        val stringStates = states.map { it.name }.toTypedArray()
        return downloadDao.getDownloadsByState(*stringStates)
    }
    
    override suspend fun addOrUpdateDownload(download: DownloadEntity) = downloadDao.insertOrUpdate(download)
    
    override suspend fun updateDownloadState(contentId: String, state: DownloadState, errorMessage: String?) {
        downloadDao.updateState(contentId, state.name, errorMessage)
    }
    
    override suspend fun updateDownloadProgress(contentId: String, state: DownloadState, progress: Float, downloadedBytes: Long, totalBytes: Long) {
        downloadDao.updateProgress(contentId, state.name, progress, downloadedBytes, totalBytes)
    }
    
    override suspend fun updateLocalPath(contentId: String, localPath: String?) {
        downloadDao.updateLocalPath(contentId, localPath)
    }
    
    override suspend fun deleteDownload(contentId: String) = downloadDao.delete(contentId)
}
