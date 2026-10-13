package com.example.data.repository

import android.util.Log
import com.example.data.local.AppDao
import com.example.data.model.JobEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * RoomFirestoreJobRepository: Production-grade implementation of JobRepository that combines
 * Room (for immediate offline access, local speed, and reactive SQLite queries) and
 * Firebase Firestore (for distributed cloud storage, remote backup, and cross-device sync).
 */
class RoomFirestoreJobRepository(
    private val dao: AppDao,
    private val databaseId: String = "ai-studio-android-rozgarka-911424b6-be7d-4546-ac03-7db2890abea6",
    private val firestoreProvider: () -> FirebaseFirestore? = {
        try {
            FirebaseFirestore.getInstance(databaseId)
        } catch (e: Exception) {
            Log.w("RoomFirestoreJobRepo", "FirebaseFirestore is not initialized with $databaseId: ${e.message}")
            null
        }
    }
) : JobRepository {

    private val tag = "RoomFirestoreJobRepo"
    private val jobsCollectionName = "job_postings"

    private val firestore: FirebaseFirestore?
        get() = firestoreProvider()

    override fun getActiveJobsStream(): Flow<List<JobEntity>> = dao.getActiveJobsFlow()

    override fun getAllJobsStream(): Flow<List<JobEntity>> = dao.getAllJobsFlow()

    override fun getSeasonalJobsStream(): Flow<List<JobEntity>> = dao.getSeasonalJobsFlow()

    override fun getJobsByEmployerStream(employerId: Long): Flow<List<JobEntity>> =
        dao.getJobsByEmployerFlow(employerId)

    override suspend fun getJobById(id: Long): JobEntity? = dao.getJobById(id)

    override suspend fun postJob(job: JobEntity): Long {
        // 1. Write to Room local database first (guarantees offline availability)
        val localId = dao.insertJob(job)
        val persistedJob = job.copy(id = localId)

        // 2. Asynchronously sync to Firebase Firestore
        try {
            firestore?.let { db ->
                val docRef = db.collection(jobsCollectionName).document("job_$localId")
                docRef.set(persistedJob.toFirestoreMap(), SetOptions.merge()).awaitTask()
                Log.d(tag, "Job successfully pushed to Firestore: job_$localId")
            }
        } catch (e: Exception) {
            // Local post succeeds even if device is offline or Firestore network fails
            Log.w(tag, "Firestore push pending / failed (offline mode): ${e.message}")
        }

        return localId
    }

    override suspend fun updateJob(job: JobEntity) {
        // 1. Update in local Room
        dao.updateJob(job)

        // 2. Synchronize to Firestore
        try {
            firestore?.let { db ->
                val docRef = db.collection(jobsCollectionName).document("job_${job.id}")
                docRef.set(job.toFirestoreMap(), SetOptions.merge()).awaitTask()
                Log.d(tag, "Job updated in Firestore: job_${job.id}")
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore update failed (offline mode): ${e.message}")
        }
    }

    override suspend fun deleteJob(id: Long) {
        // 1. Delete locally from Room
        dao.deleteJob(id)

        // 2. Delete remotely from Firestore
        try {
            firestore?.let { db ->
                db.collection(jobsCollectionName).document("job_$id").delete().awaitTask()
                Log.d(tag, "Job deleted from Firestore: job_$id")
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore delete failed: ${e.message}")
        }
    }

    override suspend fun setJobStatus(id: Long, status: String) {
        dao.setJobStatus(id, status)
        try {
            firestore?.let { db ->
                db.collection(jobsCollectionName).document("job_$id")
                    .update("status", status)
                    .awaitTask()
            }
        } catch (e: Exception) {
            Log.w(tag, "Firestore status update failed: ${e.message}")
        }
    }

    override suspend fun syncWithFirestore(): Result<Int> {
        val db = firestore ?: return Result.failure(IllegalStateException("Firebase Firestore is unavailable"))

        return try {
            var syncedCount = 0

            // 1. Pull remote jobs from Firestore and upsert into Room
            val remoteDocs = db.collection(jobsCollectionName).get().awaitTask()
            val remoteJobs = remoteDocs.documents.mapNotNull { doc ->
                doc.data?.let { data ->
                    val docId = doc.id.removePrefix("job_").toLongOrNull() ?: 0L
                    dataToJobEntity(docId, data)
                }
            }

            if (remoteJobs.isNotEmpty()) {
                dao.insertJobs(remoteJobs)
                syncedCount += remoteJobs.size
                Log.d(tag, "Synced ${remoteJobs.size} remote jobs from Firestore into Room")
            }

            // 2. Push any local jobs that might be missing from remote
            val localJobs = dao.getAllJobsList()
            for (localJob in localJobs) {
                val docRef = db.collection(jobsCollectionName).document("job_${localJob.id}")
                docRef.set(localJob.toFirestoreMap(), SetOptions.merge()).awaitTask()
            }
            syncedCount += localJobs.size

            Result.success(syncedCount)
        } catch (e: Exception) {
            Log.e(tag, "Bidirectional sync failed", e)
            Result.failure(e)
        }
    }

    override fun startRemoteJobSyncStream(): Flow<List<JobEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            close()
            return@callbackFlow
        }

        var registration: ListenerRegistration? = null
        try {
            registration = db.collection(jobsCollectionName)
                .whereEqualTo("status", "ACTIVE")
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w(tag, "Firestore listen failed", error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val jobs = snapshot.documents.mapNotNull { doc ->
                            doc.data?.let { data ->
                                val docId = doc.id.removePrefix("job_").toLongOrNull() ?: 0L
                                dataToJobEntity(docId, data)
                            }
                        }
                        trySend(jobs)
                    }
                }
        } catch (e: Exception) {
            Log.e(tag, "Failed to register Firestore snapshot listener", e)
        }

        awaitClose {
            registration?.remove()
            Log.d(tag, "Removed Firestore snapshot listener")
        }
    }

    // --- Entity Serialization / Deserialization Helpers ---

    private fun JobEntity.toFirestoreMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "employerUserId" to employerUserId,
            "employerName" to employerName,
            "title" to title,
            "profession" to profession,
            "description" to description,
            "requiredSkills" to requiredSkills,
            "workersNeeded" to workersNeeded,
            "district" to district,
            "area" to area,
            "workLocation" to workLocation,
            "workType" to workType,
            "startDate" to startDate,
            "endDate" to endDate,
            "hours" to hours,
            "paymentAmount" to paymentAmount,
            "paymentPeriod" to paymentPeriod,
            "experienceRequired" to experienceRequired,
            "requirements" to requirements,
            "isSeasonal" to isSeasonal,
            "seasonalCategory" to seasonalCategory,
            "status" to status,
            "createdAt" to createdAt
        )
    }

    private fun dataToJobEntity(docId: Long, data: Map<String, Any?>): JobEntity {
        return JobEntity(
            id = (data["id"] as? Number)?.toLong() ?: docId,
            employerUserId = (data["employerUserId"] as? Number)?.toLong() ?: 0L,
            employerName = data["employerName"] as? String ?: "",
            title = data["title"] as? String ?: "",
            profession = data["profession"] as? String ?: "General",
            description = data["description"] as? String ?: "",
            requiredSkills = data["requiredSkills"] as? String ?: "",
            workersNeeded = (data["workersNeeded"] as? Number)?.toInt() ?: 1,
            district = data["district"] as? String ?: "Srinagar",
            area = data["area"] as? String ?: "",
            workLocation = data["workLocation"] as? String ?: "On-site",
            workType = data["workType"] as? String ?: "Daily wage",
            startDate = data["startDate"] as? String ?: "Immediate",
            endDate = data["endDate"] as? String ?: "",
            hours = data["hours"] as? String ?: "8 AM - 5 PM",
            paymentAmount = data["paymentAmount"] as? String ?: "₹800",
            paymentPeriod = data["paymentPeriod"] as? String ?: "Per Day",
            experienceRequired = data["experienceRequired"] as? String ?: "",
            requirements = data["requirements"] as? String ?: "",
            isSeasonal = data["isSeasonal"] as? Boolean ?: false,
            seasonalCategory = data["seasonalCategory"] as? String ?: "None",
            status = data["status"] as? String ?: "ACTIVE",
            createdAt = (data["createdAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
        )
    }
}

/**
 * Extension helper to await Google Play Tasks in coroutines cleanly.
 */
private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { result ->
        continuation.resume(result)
    }
    addOnFailureListener { exception ->
        continuation.resumeWithException(exception)
    }
}
