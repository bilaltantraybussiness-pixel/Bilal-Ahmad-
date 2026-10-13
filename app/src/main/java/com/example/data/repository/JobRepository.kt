package com.example.data.repository

import com.example.data.model.JobEntity
import kotlinx.coroutines.flow.Flow

/**
 * JobRepository: Unified Room-to-Firestore repository interface managing local and remote
 * storage for job postings across Rozgar Kashmir.
 */
interface JobRepository {

    /**
     * Observe active job postings from the local Room database as an offline-first reactive Flow.
     */
    fun getActiveJobsStream(): Flow<List<JobEntity>>

    /**
     * Observe all job postings from the local Room database as an offline-first reactive Flow.
     */
    fun getAllJobsStream(): Flow<List<JobEntity>>

    /**
     * Observe seasonal job postings (e.g., apple harvest, saffron, winter pruning) from Room.
     */
    fun getSeasonalJobsStream(): Flow<List<JobEntity>>

    /**
     * Observe jobs posted by a specific employer from local Room database.
     */
    fun getJobsByEmployerStream(employerId: Long): Flow<List<JobEntity>>

    /**
     * Fetch a job posting by its unique local identifier.
     */
    suspend fun getJobById(id: Long): JobEntity?

    /**
     * Create and store a new job posting:
     * 1. Persists locally in the Room database for instantaneous offline availability.
     * 2. Asynchronously pushes the posting to the remote Firebase Firestore collection.
     * Returns the local Room row ID.
     */
    suspend fun postJob(job: JobEntity): Long

    /**
     * Update a job posting locally in Room and synchronize updates to remote Firestore.
     */
    suspend fun updateJob(job: JobEntity)

    /**
     * Delete a job posting locally from Room and remove from remote Firestore.
     */
    suspend fun deleteJob(id: Long)

    /**
     * Update job status (e.g., "ACTIVE", "CLOSED", "FILLED") across local and remote storage.
     */
    suspend fun setJobStatus(id: Long, status: String)

    /**
     * Bidirectional Synchronization:
     * 1. Fetches remote jobs from Firestore collection "job_postings" and caches them in Room.
     * 2. Uploads local jobs to Firestore that have not yet been published remotely.
     * Returns Result with the count of successfully synchronized jobs.
     */
    suspend fun syncWithFirestore(): Result<Int>

    /**
     * Real-time remote listener:
     * Subscribes to Firestore collection changes and automatically reflects them in the Room database.
     */
    fun startRemoteJobSyncStream(): Flow<List<JobEntity>>
}
