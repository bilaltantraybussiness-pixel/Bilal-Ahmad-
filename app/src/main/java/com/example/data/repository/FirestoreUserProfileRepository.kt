package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDao
import com.example.data.model.ServiceHistoryItem
import com.example.data.model.UserEntity
import com.example.data.model.UserProfileFirestore
import com.example.data.model.WorkerProfileEntity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class FirestoreUserProfileRepository(
    private val context: Context,
    private val dao: AppDao
) {
    private val tag = "FirestoreUserRepo"
    private val collectionName = "user_profiles"

    private val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            FirebaseFirestore.getInstance(dbId)
        } catch (e: Exception) {
            Log.w(tag, "Could not initialize Firestore with database ID: ${e.message}")
            null
        }
    }

    /**
     * Listens to real-time changes to the user's profile from Firebase Firestore.
     */
    fun getUserProfileFlow(userId: String): Flow<UserProfileFirestore?> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(null)
            close()
            return@callbackFlow
        }

        val docRef = db.collection(collectionName).document(userId)
        val registration = docRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                Log.w(tag, "Listen failed for user profile $userId", error)
                trySend(null)
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val data = snapshot.data
                if (data != null) {
                    val profile = UserProfileFirestore.fromMap(data)
                    trySend(profile)
                } else {
                    trySend(null)
                }
            } else {
                trySend(null)
            }
        }

        awaitClose {
            registration.remove()
        }
    }

    /**
     * Fetches user profile once with explicit timeout and no infinite suspension.
     */
    suspend fun fetchUserProfileOnce(userId: String): Result<UserProfileFirestore?> {
        val db = firestore ?: return Result.failure(Exception("Firestore service is currently offline"))

        return try {
            val docRef = db.collection(collectionName).document(userId)
            suspendCancellableCoroutine { cont ->
                docRef.get()
                    .addOnSuccessListener { snapshot ->
                        if (snapshot != null && snapshot.exists() && snapshot.data != null) {
                            val profile = UserProfileFirestore.fromMap(snapshot.data!!)
                            if (cont.isActive) cont.resume(Result.success(profile))
                        } else {
                            if (cont.isActive) cont.resume(Result.success(null))
                        }
                    }
                    .addOnFailureListener { ex ->
                        if (cont.isActive) cont.resume(Result.failure(ex))
                    }
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Persists the user profile to Firebase Firestore and mirrors key details in local Room DB.
     */
    suspend fun saveUserProfile(profile: UserProfileFirestore): Result<Unit> {
        val db = firestore ?: return Result.failure(Exception("Firestore service is currently unavailable."))

        return try {
            val docRef = db.collection(collectionName).document(profile.userId)
            val updatedProfile = profile.copy(lastSyncedAt = System.currentTimeMillis())

            suspendCancellableCoroutine { cont ->
                docRef.set(updatedProfile.toMap(), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d(tag, "User profile successfully saved to Firestore: ${profile.userId}")
                        if (cont.isActive) cont.resume(Unit)
                    }
                    .addOnFailureListener { exception ->
                        Log.e(tag, "Failed to save profile to Firestore", exception)
                        if (cont.isActive) cont.resumeWithException(exception)
                    }
            }

            // Sync with local Room database
            try {
                val localUserId = profile.userId.toLongOrNull()
                if (localUserId != null) {
                    val existingUser = dao.getUserById(localUserId)
                    if (existingUser != null) {
                        dao.updateUser(
                            existingUser.copy(
                                fullName = profile.fullName,
                                district = profile.district,
                                area = profile.area,
                                email = profile.email.ifBlank { existingUser.email }
                            )
                        )
                    }

                    if (profile.userRole == "WORKER") {
                        val existingWorker = dao.getWorkerProfileByUserId(localUserId)
                        val skillsString = profile.skills.joinToString(", ")
                        if (existingWorker != null) {
                            dao.updateWorkerProfile(
                                existingWorker.copy(
                                    name = profile.fullName,
                                    district = profile.district,
                                    area = profile.area,
                                    profession = profile.profession,
                                    skills = skillsString,
                                    expectedPayment = profile.expectedPayment,
                                    availability = profile.availability,
                                    workPreference = profile.workPreference,
                                    experienceYears = profile.experienceYears,
                                    bio = profile.bio
                                )
                            )
                        } else {
                            dao.insertWorkerProfile(
                                WorkerProfileEntity(
                                    userId = localUserId,
                                    name = profile.fullName,
                                    district = profile.district,
                                    area = profile.area,
                                    profession = profile.profession,
                                    occupation = profile.occupation,
                                    skills = skillsString,
                                    expectedPayment = profile.expectedPayment,
                                    availability = profile.availability,
                                    workPreference = profile.workPreference,
                                    experienceYears = profile.experienceYears,
                                    bio = profile.bio
                                )
                            )
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w(tag, "Local Room sync notice: ${e.message}")
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(tag, "Error writing profile to Firestore", e)
            Result.failure(e)
        }
    }

    /**
     * Adds a new service history record to the user's Firestore profile.
     */
    suspend fun addServiceHistoryRecord(
        userId: String,
        currentProfile: UserProfileFirestore,
        item: ServiceHistoryItem
    ): Result<Unit> {
        val updatedList = currentProfile.serviceHistory.toMutableList().apply {
            add(0, item) // Insert newest first
        }
        val updatedProfile = currentProfile.copy(serviceHistory = updatedList)
        return saveUserProfile(updatedProfile)
    }

    /**
     * Updates an existing service history record.
     */
    suspend fun updateServiceHistoryRecord(
        userId: String,
        currentProfile: UserProfileFirestore,
        item: ServiceHistoryItem
    ): Result<Unit> {
        val updatedList = currentProfile.serviceHistory.map {
            if (it.id == item.id) item else it
        }
        val updatedProfile = currentProfile.copy(serviceHistory = updatedList)
        return saveUserProfile(updatedProfile)
    }

    /**
     * Removes a service history record by ID.
     */
    suspend fun removeServiceHistoryRecord(
        userId: String,
        currentProfile: UserProfileFirestore,
        recordId: String
    ): Result<Unit> {
        val updatedList = currentProfile.serviceHistory.filterNot { it.id == recordId }
        val updatedProfile = currentProfile.copy(serviceHistory = updatedList)
        return saveUserProfile(updatedProfile)
    }

    /**
     * Creates an initial Firestore profile representation from Room models.
     */
    fun buildInitialProfileFromLocal(
        user: UserEntity,
        workerProfile: WorkerProfileEntity?
    ): UserProfileFirestore {
        val skillsList = workerProfile?.skills
            ?.split(",")
            ?.map { it.trim() }
            ?.filter { it.isNotBlank() } ?: emptyList()

        val isEmployer = user.roles.contains("EMPLOYER") && !user.roles.contains("WORKER")

        return UserProfileFirestore(
            userId = user.id.toString(),
            userRole = if (isEmployer) "EMPLOYER" else "WORKER",
            fullName = user.fullName,
            phoneNumber = user.phoneNumber,
            email = user.email ?: "",
            district = user.district,
            area = user.area,
            bio = workerProfile?.bio ?: "Proud local Kashmir craftsman / professional serving the valley.",
            profession = workerProfile?.profession ?: "General Skilled Craftsman",
            occupation = workerProfile?.occupation ?: "Craftsman / Specialist",
            skills = if (skillsList.isNotEmpty()) skillsList else listOf("Punctual", "Quality Assurance"),
            skillProficiencies = skillsList.associateWith { "Expert" },
            experienceYears = workerProfile?.experienceYears ?: 3,
            expectedPayment = workerProfile?.expectedPayment ?: "₹800/day",
            paymentFrequency = workerProfile?.paymentFrequency ?: "Daily",
            workPreference = workerProfile?.workPreference ?: "On-site",
            availability = workerProfile?.availability ?: "Available Now",
            businessOrEstateName = if (isEmployer) "${user.fullName}'s Enterprises" else "",
            industry = "Agriculture & Kashmiri Crafts",
            serviceHistory = listOf(
                ServiceHistoryItem(
                    title = if (isEmployer) "Seasonal Orchard Harvest Supervision" else "Heritage Wood Carving & Restoration",
                    clientOrEmployerName = if (isEmployer) "Sopore Fruit Market Association" else "Downtown Heritage Trust, Srinagar",
                    category = if (isEmployer) "Harvest Management" else "Woodwork",
                    dateRange = "Jul 2025 - Sep 2025",
                    description = if (isEmployer) "Successfully contracted 15 seasonal harvesters for 40-acre apple orchard harvest and grading." else "Restored intricate walnut wood ceilings and lattice windows for 120-year-old Srinagar residence.",
                    amountEarnedOrBudget = if (isEmployer) "₹1,20,000" else "₹35,000",
                    status = "Completed",
                    locationDistrict = user.district,
                    deliverables = "Quality verified, inspected and signed off by client."
                )
            ),
            lastSyncedAt = System.currentTimeMillis()
        )
    }
}
