package com.example.data.model

import java.util.UUID

data class ServiceHistoryItem(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val clientOrEmployerName: String = "",
    val category: String = "General Services",
    val dateRange: String = "",
    val description: String = "",
    val amountEarnedOrBudget: String = "",
    val status: String = "Completed", // "Completed", "In Progress", "Verified by Client"
    val locationDistrict: String = "Srinagar",
    val deliverables: String = "",
    val rating: Float = 5.0f,
    val completedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "id" to id,
        "title" to title,
        "clientOrEmployerName" to clientOrEmployerName,
        "category" to category,
        "dateRange" to dateRange,
        "description" to description,
        "amountEarnedOrBudget" to amountEarnedOrBudget,
        "status" to status,
        "locationDistrict" to locationDistrict,
        "deliverables" to deliverables,
        "rating" to rating.toDouble(),
        "completedAt" to completedAt
    )

    companion object {
        fun fromMap(map: Map<String, Any?>): ServiceHistoryItem {
            return ServiceHistoryItem(
                id = map["id"] as? String ?: UUID.randomUUID().toString(),
                title = map["title"] as? String ?: "",
                clientOrEmployerName = map["clientOrEmployerName"] as? String ?: "",
                category = map["category"] as? String ?: "General Services",
                dateRange = map["dateRange"] as? String ?: "",
                description = map["description"] as? String ?: "",
                amountEarnedOrBudget = map["amountEarnedOrBudget"] as? String ?: "",
                status = map["status"] as? String ?: "Completed",
                locationDistrict = map["locationDistrict"] as? String ?: "Srinagar",
                deliverables = map["deliverables"] as? String ?: "",
                rating = (map["rating"] as? Number)?.toFloat() ?: 5.0f,
                completedAt = (map["completedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}

data class UserProfileFirestore(
    val userId: String = "",
    val userRole: String = "WORKER", // "WORKER" or "EMPLOYER"
    val fullName: String = "",
    val phoneNumber: String = "",
    val email: String = "",
    val district: String = "Srinagar",
    val area: String = "",
    val bio: String = "",
    val profilePhotoUrl: String = "",
    val languages: List<String> = listOf("Kashmiri", "Urdu"),

    // Worker-specific
    val profession: String = "Artisan / Craftsman",
    val occupation: String = "Specialist",
    val skills: List<String> = emptyList(),
    val skillProficiencies: Map<String, String> = emptyMap(), // skillName -> "Beginner" | "Intermediate" | "Expert" | "Master"
    val experienceYears: Int = 3,
    val expectedPayment: String = "₹800/day",
    val paymentFrequency: String = "Daily",
    val workPreference: String = "On-site", // "On-site", "Remote", "Hybrid"
    val availability: String = "Available Now",
    val toolsOwned: String = "",

    // Employer-specific
    val businessOrEstateName: String = "",
    val industry: String = "Agriculture & Handicrafts",
    val businessAddress: String = "",
    val totalWorkersEngaged: Int = 0,
    val paymentTerms: String = "Weekly Settlement",

    // Service History
    val serviceHistory: List<ServiceHistoryItem> = emptyList(),
    val lastSyncedAt: Long = System.currentTimeMillis()
) {
    fun toMap(): Map<String, Any?> = mapOf(
        "userId" to userId,
        "userRole" to userRole,
        "fullName" to fullName,
        "phoneNumber" to phoneNumber,
        "email" to email,
        "district" to district,
        "area" to area,
        "bio" to bio,
        "profilePhotoUrl" to profilePhotoUrl,
        "languages" to languages,
        "profession" to profession,
        "occupation" to occupation,
        "skills" to skills,
        "skillProficiencies" to skillProficiencies,
        "experienceYears" to experienceYears,
        "expectedPayment" to expectedPayment,
        "paymentFrequency" to paymentFrequency,
        "workPreference" to workPreference,
        "availability" to availability,
        "toolsOwned" to toolsOwned,
        "businessOrEstateName" to businessOrEstateName,
        "industry" to industry,
        "businessAddress" to businessAddress,
        "totalWorkersEngaged" to totalWorkersEngaged,
        "paymentTerms" to paymentTerms,
        "serviceHistory" to serviceHistory.map { it.toMap() },
        "lastSyncedAt" to lastSyncedAt
    )

    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromMap(map: Map<String, Any?>): UserProfileFirestore {
            val rawHistory = map["serviceHistory"] as? List<Map<String, Any?>> ?: emptyList()
            val parsedHistory = rawHistory.mapNotNull {
                try { ServiceHistoryItem.fromMap(it) } catch (_: Exception) { null }
            }
            return UserProfileFirestore(
                userId = map["userId"] as? String ?: "",
                userRole = map["userRole"] as? String ?: "WORKER",
                fullName = map["fullName"] as? String ?: "",
                phoneNumber = map["phoneNumber"] as? String ?: "",
                email = map["email"] as? String ?: "",
                district = map["district"] as? String ?: "Srinagar",
                area = map["area"] as? String ?: "",
                bio = map["bio"] as? String ?: "",
                profilePhotoUrl = map["profilePhotoUrl"] as? String ?: "",
                languages = (map["languages"] as? List<String>) ?: listOf("Kashmiri", "Urdu"),
                profession = map["profession"] as? String ?: "Artisan / Craftsman",
                occupation = map["occupation"] as? String ?: "Specialist",
                skills = (map["skills"] as? List<String>) ?: emptyList(),
                skillProficiencies = (map["skillProficiencies"] as? Map<String, String>) ?: emptyMap(),
                experienceYears = (map["experienceYears"] as? Number)?.toInt() ?: 3,
                expectedPayment = map["expectedPayment"] as? String ?: "₹800/day",
                paymentFrequency = map["paymentFrequency"] as? String ?: "Daily",
                workPreference = map["workPreference"] as? String ?: "On-site",
                availability = map["availability"] as? String ?: "Available Now",
                toolsOwned = map["toolsOwned"] as? String ?: "",
                businessOrEstateName = map["businessOrEstateName"] as? String ?: "",
                industry = map["industry"] as? String ?: "Agriculture & Handicrafts",
                businessAddress = map["businessAddress"] as? String ?: "",
                totalWorkersEngaged = (map["totalWorkersEngaged"] as? Number)?.toInt() ?: 0,
                paymentTerms = map["paymentTerms"] as? String ?: "Weekly Settlement",
                serviceHistory = parsedHistory,
                lastSyncedAt = (map["lastSyncedAt"] as? Number)?.toLong() ?: System.currentTimeMillis()
            )
        }
    }
}
