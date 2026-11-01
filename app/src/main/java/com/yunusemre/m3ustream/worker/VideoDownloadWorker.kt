package com.yunusemre.m3ustream.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.yunusemre.m3ustream.data.local.entity.DownloadState
import com.yunusemre.m3ustream.domain.repository.DownloadRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent

class VideoDownloadWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface WorkerEntryPoint {
        fun downloadRepository(): DownloadRepository
    }

    private val downloadRepository by lazy {
        EntryPointAccessors.fromApplication(context, WorkerEntryPoint::class.java).downloadRepository()
    }

    companion object {
        const val KEY_CONTENT_ID = "content_id"
        const val KEY_STREAM_URL = "stream_url"
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val contentId = inputData.getString(KEY_CONTENT_ID) ?: return@withContext Result.failure()
        val streamUrl = inputData.getString(KEY_STREAM_URL) ?: return@withContext Result.failure()

        try {
            downloadRepository.updateDownloadState(contentId, DownloadState.DOWNLOADING)

            // Dosya yolunu belirle
            val fileName = "${contentId}_video.mp4"
            val file = File(context.getExternalFilesDir("downloads"), fileName)
            
            // Eğer klasör yoksa oluştur
            file.parentFile?.mkdirs()

            var downloadedBytes = 0L
            var totalBytes = 0L

            val url = java.net.URL(streamUrl)
            val connection = url.openConnection() as java.net.HttpURLConnection
            connection.connect()

            if (connection.responseCode in 200..299) {
                totalBytes = connection.contentLength.toLong()
                
                connection.inputStream.use { inputStream ->
                    file.outputStream().use { outputStream ->
                        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                        var bytesRead = inputStream.read(buffer)
                        while (bytesRead >= 0) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead

                            val progress = if (totalBytes > 0) (downloadedBytes.toFloat() / totalBytes.toFloat()) * 100f else 0f
                            
                            // Veritabanını güncelle
                            downloadRepository.updateDownloadProgress(
                                contentId = contentId,
                                state = DownloadState.DOWNLOADING,
                                progress = progress,
                                downloadedBytes = downloadedBytes,
                                totalBytes = totalBytes
                            )
                            
                            bytesRead = inputStream.read(buffer)
                        }
                    }
                }
            } else {
                throw Exception("HTTP Error: ${connection.responseCode}")
            }

            // İndirme bitti
            downloadRepository.updateLocalPath(contentId, file.absolutePath)
            downloadRepository.updateDownloadState(contentId, DownloadState.COMPLETED)
            
            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            downloadRepository.updateDownloadState(contentId, DownloadState.ERROR, e.message)
            Result.failure()
        }
    }
}
