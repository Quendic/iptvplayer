package com.yunusemre.m3ustream.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.yunusemre.m3ustream.data.local.entity.DownloadEntity
import com.yunusemre.m3ustream.data.local.entity.DownloadState
import kotlinx.coroutines.flow.Flow

@Dao
interface DownloadDao {

    @Query("SELECT * FROM downloads ORDER BY createdAt DESC")
    fun getAllDownloads(): Flow<List<DownloadEntity>>

    @Query("SELECT * FROM downloads WHERE contentId = :contentId")
    fun getDownloadFlow(contentId: String): Flow<DownloadEntity?>

    @Query("SELECT * FROM downloads WHERE contentId = :contentId")
    suspend fun getDownload(contentId: String): DownloadEntity?

    @Query("SELECT * FROM downloads WHERE state IN (:states)")
    suspend fun getDownloadsByState(vararg states: String): List<DownloadEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(download: DownloadEntity)

    @Update
    suspend fun update(download: DownloadEntity)

    @Query("UPDATE downloads SET state = :state, progress = :progress, downloadedBytes = :downloadedBytes, totalBytes = :totalBytes WHERE contentId = :contentId")
    suspend fun updateProgress(contentId: String, state: String, progress: Float, downloadedBytes: Long, totalBytes: Long)
    
    @Query("UPDATE downloads SET state = :state, errorMessage = :errorMessage WHERE contentId = :contentId")
    suspend fun updateState(contentId: String, state: String, errorMessage: String? = null)
    
    @Query("UPDATE downloads SET localFilePath = :localPath WHERE contentId = :contentId")
    suspend fun updateLocalPath(contentId: String, localPath: String?)

    @Query("DELETE FROM downloads WHERE contentId = :contentId")
    suspend fun delete(contentId: String)
}
