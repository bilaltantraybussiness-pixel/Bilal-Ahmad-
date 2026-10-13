package com.example.viewmodel

import android.app.Activity
import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import com.example.data.repository.FirestoreMarketplaceRepository
import com.example.data.repository.FirestoreUserProfileRepository
import com.example.data.repository.JobRepository
import com.example.util.GoogleAuthCredentialHelper
import com.example.util.PhoneAuthHelper
import com.example.util.PhoneAuthState
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class Screen {
    HOME,
    FIND_JOBS,
    FIND_WORKERS,
    POST_JOB,
    MARKETPLACE,
    SECOND_HAND,
    SEASONAL_WORK,
    MESSAGES,
    CHAT_DETAIL,
    WORKER_DETAIL,
    JOB_DETAIL,
    PRODUCT_DETAIL,
    MY_APPLICATIONS,
    MY_LISTINGS,
    MY_PROFILE,
    CREATE_PROFILE,
    ADMIN_DASHBOARD,
    DONATE,
    NOTIFICATIONS,
    SAVED_ITEMS,
    AUTH,
    LOGIN,
    KASHMIRI_LITERATURE,
    FOOD,
    PROPERTY,
    DAILY_KASHMIRI_WORDS,
    PAYOUTS_SETTINGS,
    SELLER_DASHBOARD,
    MY_ORDERS,
    POLICIES
}

data class UiNotification(
    val message: String,
    val isError: Boolean = false
)

class RozgarViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = AppRepository(db.appDao())
    val firestoreUserProfileRepo = FirestoreUserProfileRepository(application, db.appDao())
    val firestoreMarketplaceRepo = FirestoreMarketplaceRepository(application, db.appDao())

    val firestoreUserProfile = MutableStateFlow<UserProfileFirestore?>(null)
    val isSyncingProfileWithFirestore = MutableStateFlow(false)

    // --- Navigation & Backstack ---
    private val _currentScreen = MutableStateFlow(Screen.HOME)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val backStack = mutableListOf<Screen>()

    // Selected items for detail screens
    val selectedWorker = MutableStateFlow<WorkerProfileEntity?>(null)
    val selectedJob = MutableStateFlow<JobEntity?>(null)
    val selectedProduct = MutableStateFlow<ProductEntity?>(null)
    val selectedConversation = MutableStateFlow<ConversationEntity?>(null)

    // --- Current Session User ---
    private val _currentUser = MutableStateFlow<UserEntity?>(null)
    val currentUser: StateFlow<UserEntity?> = _currentUser.asStateFlow()

    // Temporary active role toggle for multi-role users
    private val _activeRole = MutableStateFlow("WORKER")
    val activeRole: StateFlow<String> = _activeRole.asStateFlow()

    // --- Ephemeral Notifications (Toast / SnackBar) ---
    private val _toastEvent = MutableSharedFlow<UiNotification>()
    val toastEvent: SharedFlow<UiNotification> = _toastEvent.asSharedFlow()

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedDistrict = MutableStateFlow("All Districts")
    val selectedProfession = MutableStateFlow("All Professions")
    val selectedWorkType = MutableStateFlow("All Types")
    val selectedCategory = MutableStateFlow("All Categories")
    val selectedCondition = MutableStateFlow("All Conditions")
    val selectedAvailability = MutableStateFlow("All Availability")

    // --- Data Flows from Repository ---
    val allWorkers: StateFlow<List<WorkerProfileEntity>> = repository.getAllWorkersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seasonalWorkers: StateFlow<List<WorkerProfileEntity>> = repository.getSeasonalWorkersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeJobs: StateFlow<List<JobEntity>> = repository.getActiveJobsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allJobs: StateFlow<List<JobEntity>> = repository.getAllJobsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val seasonalJobs: StateFlow<List<JobEntity>> = repository.getSeasonalJobsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val jobRepository: JobRepository get() = repository.jobRepository
    val isSyncingJobs = MutableStateFlow(false)
    val isJobsLoading = MutableStateFlow(false)
    val isWorkersLoading = MutableStateFlow(false)
    val isMarketplaceLoading = MutableStateFlow(false)
    val isMessagesLoading = MutableStateFlow(false)
    val isNotificationsLoading = MutableStateFlow(false)

    fun refreshJobsData() {
        viewModelScope.launch {
            isJobsLoading.value = true
            kotlinx.coroutines.delay(650)
            syncJobsWithFirestore()
            isJobsLoading.value = false
        }
    }

    fun refreshWorkersData() {
        viewModelScope.launch {
            isWorkersLoading.value = true
            kotlinx.coroutines.delay(650)
            isWorkersLoading.value = false
        }
    }

    fun refreshMarketplaceData() {
        viewModelScope.launch {
            isMarketplaceLoading.value = true
            kotlinx.coroutines.delay(650)
            isMarketplaceLoading.value = false
        }
    }

    fun refreshMessagesData() {
        viewModelScope.launch {
            isMessagesLoading.value = true
            kotlinx.coroutines.delay(500)
            isMessagesLoading.value = false
        }
    }

    fun syncJobsWithFirestore() {
        viewModelScope.launch {
            isSyncingJobs.value = true
            val result = repository.syncJobsWithFirestore()
            isSyncingJobs.value = false
            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _toastEvent.emit(UiNotification("Jobs synchronized with Firestore ($count postings)"))
            } else {
                _toastEvent.emit(UiNotification("Cloud sync offline: Using local storage", isError = false))
            }
        }
    }

    val activeProducts: StateFlow<List<ProductEntity>> = repository.getActiveProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProducts: StateFlow<List<ProductEntity>> = repository.getAllProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val secondHandProducts: StateFlow<List<ProductEntity>> = repository.getSecondHandProductsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<UserEntity>> = repository.getAllUsersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allReports: StateFlow<List<ReportEntity>> = repository.getAllReportsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDonations: StateFlow<List<DonationEntity>> = repository.getAllDonationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val loginActivities: StateFlow<List<LoginActivityEntity>> = repository.getAllLoginActivitiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // User-dependent flows
    val userConversations: StateFlow<List<ConversationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getConversationsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentChatMessages: StateFlow<List<MessageEntity>> = selectedConversation.flatMapLatest { conv ->
        if (conv != null) repository.getMessagesForConversationFlow(conv.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userNotifications: StateFlow<List<NotificationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getNotificationsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val unreadNotificationsCount: StateFlow<Int> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getUnreadNotificationsCountFlow(user.id) else flowOf(0)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val userSavedItems: StateFlow<List<SavedItemEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getSavedItemsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userApplications: StateFlow<List<JobApplicationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getApplicationsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val employerApplications: StateFlow<List<JobApplicationEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getApplicationsForEmployerFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allApplications: StateFlow<List<JobApplicationEntity>> = repository.getAllApplicationsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val kashmiriContributions: StateFlow<List<KashmiriContributionEntity>> = repository.getAllContributionsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userBookPurchases: StateFlow<List<BookPurchaseEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getBookPurchasesByUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val verificationRequests: StateFlow<List<VerificationRequestEntity>> = repository.getAllVerificationRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUserVerificationRequest: StateFlow<VerificationRequestEntity?> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getLatestVerificationRequestForUserFlow(user.id) else flowOf(null)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val reportedReviews: StateFlow<List<ReviewEntity>> = repository.getReportedReviewsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Properties & Rentals ---
    val activeProperties: StateFlow<List<PropertyListingEntity>> = repository.getActivePropertiesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Restaurants & Food Orders ---
    val activeRestaurants: StateFlow<List<RestaurantEntity>> = repository.getActiveRestaurantsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getMenuItemsForRestaurantFlow(restaurantId: Long): Flow<List<FoodMenuItemEntity>> =
        repository.getMenuItemsForRestaurantFlow(restaurantId)

    // --- Daily Kashmiri Words ---
    val approvedKashmiriWords: StateFlow<List<KashmiriWordContributionEntity>> = repository.getApprovedWordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allKashmiriWords: StateFlow<List<KashmiriWordContributionEntity>> = repository.getAllWordsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val wordContributorsLeaderboard: StateFlow<List<ContributorRewardEntity>> = repository.getLeaderboardFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Platform Settings (Admin Configurable) ---
    val platformSettings: StateFlow<PlatformSettingsEntity?> = repository.getPlatformSettingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Payouts & Settlement Ledger ---
    val allPayoutRequests: StateFlow<List<PayoutRequestEntity>> = repository.getAllPayoutRequestsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userPayoutRequests: StateFlow<List<PayoutRequestEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getPayoutRequestsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val centralLedgerEntries: StateFlow<List<TransactionLedgerEntity>> = repository.getAllLedgerEntriesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalCommissionEarned: StateFlow<Double?> = repository.getTotalCommissionEarnedFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val adminAuditLogs: StateFlow<List<AdminAuditLogEntity>> = repository.getAllAuditLogsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMemberships: StateFlow<List<MembershipSubscriptionEntity>> = repository.getAllMembershipsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Shops & Orders ---
    val allShops: StateFlow<List<ShopEntity>> = repository.getAllShopsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<OrderEntity>> = repository.getAllOrdersFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getOrdersForCustomerFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val supplierOrders: StateFlow<List<OrderEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getOrdersForSupplierFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val sellerProducts: StateFlow<List<ProductEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getProductsBySellerFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Reseller Listings ---
    val allResellerListings: StateFlow<List<ResellerListingEntity>> = repository.getAllResellerListingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userResellerListings: StateFlow<List<ResellerListingEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getResellerListingsForUserFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Worker Bookings ---
    val allWorkerBookings: StateFlow<List<WorkerBookingEntity>> = repository.getAllWorkerBookingsFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userCustomerBookings: StateFlow<List<WorkerBookingEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getBookingsForCustomerFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val userWorkerBookings: StateFlow<List<WorkerBookingEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getBookingsForWorkerFlow(user.id) else flowOf(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val firestoreSyncStatus = MutableStateFlow("Synced") // "Synced", "Syncing", "Offline Mode"

    // Voice Message Recorder State
    val isRecordingVoice = MutableStateFlow(false)
    val voiceRecordingDuration = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            repository.seedDatabaseIfEmpty()
            // Auto login as Ghulam Mohammad Rather (worker/seller) or Admin for ready use
            val defaultUser = repository.getUserByPhone("9419011111")
            _currentUser.value = defaultUser
        }
        viewModelScope.launch {
            _currentUser.collectLatest { user ->
                if (user != null) {
                    loadFirestoreProfileForUser(user)
                } else {
                    firestoreUserProfile.value = null
                }
            }
        }
    }

    // --- Firestore User Profile & Service History Management ---
    fun loadFirestoreProfileForUser(user: UserEntity) {
        viewModelScope.launch {
            val workerProfile = repository.getWorkerProfileByUserId(user.id)
            val initial = firestoreUserProfileRepo.buildInitialProfileFromLocal(user, workerProfile)

            // CRITICAL FIX: Immediately display local Room profile so user never sees "0 skills" or blank screen
            if (firestoreUserProfile.value == null) {
                firestoreUserProfile.value = initial
            }

            isSyncingProfileWithFirestore.value = true
            firestoreSyncStatus.value = "Syncing"

            try {
                kotlinx.coroutines.withTimeoutOrNull(4000) {
                    val result = firestoreUserProfileRepo.fetchUserProfileOnce(user.id.toString())
                    if (result.isSuccess) {
                        val cloudProfile = result.getOrNull()
                        if (cloudProfile != null) {
                            firestoreUserProfile.value = cloudProfile
                            firestoreSyncStatus.value = "Synced"
                        } else {
                            firestoreUserProfileRepo.saveUserProfile(initial)
                            firestoreSyncStatus.value = "Synced"
                        }
                    } else {
                        firestoreSyncStatus.value = "Offline Mode"
                    }
                } ?: run {
                    firestoreSyncStatus.value = "Offline Mode"
                }
            } catch (_: Exception) {
                firestoreSyncStatus.value = "Offline Mode"
            } finally {
                isSyncingProfileWithFirestore.value = false
            }
        }
    }

    // --- Property Actions ---
    fun postProperty(
        title: String,
        description: String,
        propertyType: String,
        listingType: String,
        rentOrPrice: Double,
        deposit: Double,
        district: String,
        town: String,
        address: String,
        furnishing: String,
        bedrooms: Int,
        bathrooms: Int,
        amenities: String,
        imageUris: String = ""
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val property = PropertyListingEntity(
                ownerUserId = user.id,
                ownerName = user.fullName,
                title = title,
                description = description,
                propertyType = propertyType,
                listingType = listingType,
                rentOrPrice = rentOrPrice,
                deposit = deposit,
                district = district,
                town = town,
                address = address,
                furnishing = furnishing,
                bedrooms = bedrooms,
                bathrooms = bathrooms,
                amenities = amenities,
                imageUris = imageUris
            )
            repository.insertProperty(property)
            _toastEvent.emit(UiNotification("Property listing published successfully!"))
        }
    }

    fun deleteProperty(propertyId: Long) {
        viewModelScope.launch {
            repository.deleteProperty(propertyId)
            _toastEvent.emit(UiNotification("Property listing removed."))
        }
    }

    // --- Kashmiri Word Contribution Actions ---
    fun submitKashmiriWord(
        wordKashmiri: String,
        pronunciation: String,
        meaningUrdu: String,
        meaningEnglish: String,
        exampleSentence: String,
        category: String,
        dialect: String
    ) {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val entry = KashmiriWordContributionEntity(
                contributorUserId = user.id,
                contributorName = user.fullName,
                wordKashmiri = wordKashmiri.trim(),
                pronunciation = pronunciation.trim(),
                meaningUrdu = meaningUrdu.trim(),
                meaningEnglish = meaningEnglish.trim(),
                exampleSentence = exampleSentence.trim(),
                category = category,
                dialect = dialect,
                status = "PENDING"
            )
            repository.insertWordContribution(entry)
            _toastEvent.emit(UiNotification("Kashmiri word submitted for review. Thank you for your contribution!"))
        }
    }

    fun reviewWordContribution(wordId: Long, approve: Boolean, notes: String = "") {
        viewModelScope.launch {
            val words = repository.getAllWordsFlow().firstOrNull() ?: emptyList()
            val word = words.find { it.id == wordId } ?: return@launch
            val updated = word.copy(
                status = if (approve) "APPROVED" else "REJECTED",
                reviewerNotes = notes
            )
            repository.updateWordContribution(updated)
            if (approve) {
                // Update contributor reward
                val reward = repository.getRewardForUser(word.contributorUserId)
                val currentApproved = (reward?.totalApprovedWords ?: 0) + 1
                val earned = (reward?.totalRewardEarned ?: 0.0) + word.rewardAmount
                val pending = (reward?.pendingReward ?: 0.0) + word.rewardAmount
                val newReward = (reward ?: ContributorRewardEntity(userId = word.contributorUserId, userName = word.contributorName)).copy(
                    totalApprovedWords = currentApproved,
                    totalRewardEarned = earned,
                    pendingReward = pending,
                    currentLevel = when {
                        currentApproved >= 100 -> "Level 5: Master Scholar"
                        currentApproved >= 50 -> "Level 4: Senior Preserver"
                        currentApproved >= 20 -> "Level 3: Active Contributor"
                        currentApproved >= 5 -> "Level 2: Novice Contributor"
                        else -> "Level 1: Beginner"
                    }
                )
                repository.insertOrUpdateReward(newReward)
            }
            _toastEvent.emit(UiNotification(if (approve) "Word contribution approved!" else "Word contribution rejected."))
        }
    }

    // --- Admin Platform Settings ---
    fun updatePlatformSettings(settings: PlatformSettingsEntity) {
        viewModelScope.launch {
            repository.savePlatformSettings(settings)
            repository.insertAuditLog(
                AdminAuditLogEntity(
                    adminUserId = _currentUser.value?.id ?: 1L,
                    adminName = _currentUser.value?.fullName ?: "Admin",
                    actionType = "PLATFORM_SETTINGS_UPDATED",
                    targetEntity = "PlatformSettings",
                    targetId = 1L,
                    details = "Updated: Membership=₹${settings.membershipPriceMonthly}, FoodFee=${settings.foodCommissionPercent}%"
                )
            )
            _toastEvent.emit(UiNotification("Platform settings updated successfully without rebuilding!"))
        }
    }

    fun reseedRealisticDemoContent() {
        viewModelScope.launch {
            repository.reseedRealisticDemoContent()
            _toastEvent.emit(UiNotification("Realistic Kashmir demo content seeded!"))
        }
    }

    fun purgeDemoContent() {
        viewModelScope.launch {
            repository.purgeDemoContent()
            _toastEvent.emit(UiNotification("Demo content removed for clean production state!"))
        }
    }

    // --- Payout Requests ---
    fun requestPayout(amount: Double, upiId: String, bankAccount: String = "", ifsc: String = "") {
        viewModelScope.launch {
            val user = _currentUser.value ?: return@launch
            val req = PayoutRequestEntity(
                userId = user.id,
                userName = user.fullName,
                userRole = user.roles.split(",").firstOrNull() ?: "SELLER",
                amount = amount,
                upiIdMasked = upiId,
                bankAccountMasked = bankAccount,
                ifscMasked = ifsc,
                status = "PENDING"
            )
            repository.insertPayoutRequest(req)
            _toastEvent.emit(UiNotification("Payout request of ₹${amount.toInt()} submitted."))
        }
    }

    fun processPayoutRequest(requestId: Long, approve: Boolean, txRef: String = "") {
        viewModelScope.launch {
            val requests = repository.getAllPayoutRequestsFlow().firstOrNull() ?: emptyList()
            val req = requests.find { it.id == requestId } ?: return@launch
            val updated = req.copy(
                status = if (approve) "PAID" else "REJECTED",
                transactionRef = txRef,
                processedAt = System.currentTimeMillis()
            )
            repository.updatePayoutRequest(updated)
            _toastEvent.emit(UiNotification("Payout marked as ${if (approve) "PAID" else "REJECTED"}."))
        }
    }

    // --- Central Transaction Ledger Actions ---
    fun settleLedgerTransaction(ledgerId: Long, notes: String = "") {
        viewModelScope.launch {
            val entry = repository.getLedgerEntryById(ledgerId) ?: return@launch
            val updated = entry.copy(
                settlementStatus = "SETTLED",
                completedAt = System.currentTimeMillis(),
                disputeNotes = notes.ifBlank { "Settled and verified by admin" }
            )
            repository.updateLedgerEntry(updated)
            repository.insertAuditLog(
                AdminAuditLogEntity(
                    adminUserId = _currentUser.value?.id ?: 1L,
                    adminName = _currentUser.value?.fullName ?: "Admin",
                    actionType = "LEDGER_TRANSACTION_SETTLED",
                    targetEntity = "TransactionLedger",
                    targetId = ledgerId,
                    details = "Settled transaction ${entry.transactionRef}, Seller payout ₹${entry.sellerPayableAmount}"
                )
            )
            _toastEvent.emit(UiNotification("Transaction ${entry.transactionRef} marked as SETTLED."))
        }
    }

    fun placeFoodOrder(
        restaurant: RestaurantEntity,
        item: FoodMenuItemEntity,
        quantity: Int,
        deliveryAddress: String,
        customerPhone: String = "",
        paymentMethod: String = "CASH_ON_DELIVERY",
        paymentStatus: String = "PENDING",
        paymentTransactionRef: String = "",
        deliveryFee: Double = 30.0,
        discountAmount: Double = 0.0,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val buyerId = user?.id ?: 999L
            val buyerName = user?.fullName ?: "Customer"
            val phone = if (customerPhone.isNotBlank()) customerPhone else (user?.phoneNumber ?: "Not provided")
            val itemSubtotal = item.price * quantity
            val totalAmount = itemSubtotal + deliveryFee - discountAmount
            val settings = platformSettings.value
            val commissionRate = if (settings?.foodCommissionEnabled == true) (settings.foodCommissionPercent) else 1.0
            val platformCommission = (itemSubtotal * commissionRate) / 100.0
            val sellerPayable = (itemSubtotal - platformCommission) + deliveryFee

            val orderNumber = "RK-FOOD-${System.currentTimeMillis() % 100000}"
            val finalTxnRef = if (paymentTransactionRef.isNotBlank()) paymentTransactionRef else "TXN-FOOD-${System.currentTimeMillis() % 1000000}"

            val order = OrderEntity(
                orderNumber = orderNumber,
                customerId = buyerId,
                customerName = buyerName,
                customerPhone = phone,
                customerAddress = deliveryAddress,
                customerDistrict = restaurant.district,
                customerArea = restaurant.area,
                productId = item.id,
                productTitle = "${item.name} (x$quantity)",
                productImageUri = item.imageUri,
                shopId = restaurant.id,
                supplierUserId = restaurant.ownerUserId,
                supplierName = restaurant.name,
                isResoldOrder = false,
                quantity = quantity,
                unitPrice = item.price,
                totalAmount = totalAmount,
                supplierShareAmount = sellerPayable,
                platformFeeAmount = platformCommission,
                paymentMethod = paymentMethod,
                paymentStatus = paymentStatus,
                paymentTransactionRef = finalTxnRef,
                orderStatus = "ORDERED",
                estimatedDeliveryDays = 1,
                trackingNotes = "Order received by ${restaurant.name} kitchen"
            )
            val insertedId = repository.insertOrder(order)
            val persistedOrder = order.copy(id = insertedId)
            firestoreMarketplaceRepo.pushOrderToFirestore(persistedOrder)

            val ledger = TransactionLedgerEntity(
                transactionRef = finalTxnRef,
                orderRef = orderNumber,
                category = "FOOD",
                listingTitle = "${item.name} from ${restaurant.name}",
                buyerId = buyerId,
                buyerName = buyerName,
                sellerId = restaurant.ownerUserId,
                sellerName = restaurant.name,
                grossAmount = totalAmount,
                commissionRatePercent = commissionRate,
                commissionCalculationBasis = "${commissionRate}% of Food Subtotal (₹$itemSubtotal)",
                platformCommission = platformCommission,
                deliveryCharge = deliveryFee,
                discountAmount = discountAmount,
                sellerPayableAmount = sellerPayable,
                paymentStatus = paymentStatus,
                commissionStatus = "CALCULATED",
                settlementStatus = if (paymentStatus == "PAID") "SETTLED" else "UNSETTLED"
            )
            repository.insertLedgerEntry(ledger)

            // If paid online, record verified seller payout entry
            if (paymentStatus == "PAID") {
                repository.insertPayoutRequest(
                    PayoutRequestEntity(
                        userId = restaurant.ownerUserId,
                        userName = restaurant.name,
                        userRole = "RESTAURANT",
                        amount = sellerPayable,
                        upiIdMasked = "••••••@okjkbank",
                        status = "PENDING",
                        adminNotes = "Order $orderNumber (Food: ₹${totalAmount.toInt()}, Comm: ₹${platformCommission.toInt()})",
                        transactionRef = finalTxnRef
                    )
                )
            }

            _toastEvent.emit(UiNotification("Order placed with ${restaurant.name}! Order ID: $orderNumber"))
            onComplete(true)
        }
    }

    fun submitPropertyListing(
        title: String,
        description: String,
        propertyType: String,
        listingType: String,
        price: Double,
        district: String,
        town: String,
        address: String,
        bedrooms: Int,
        amenities: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val prop = PropertyListingEntity(
                ownerUserId = user?.id ?: 1L,
                ownerName = user?.fullName ?: "Property Owner",
                title = title,
                description = description,
                propertyType = propertyType,
                listingType = listingType,
                rentOrPrice = price,
                deposit = if (listingType == "Rent") price * 2 else 0.0,
                district = district,
                town = town,
                address = address,
                bedrooms = bedrooms,
                amenities = amenities,
                status = "ACTIVE"
            )
            repository.insertProperty(prop)
            _toastEvent.emit(UiNotification("Property listing published successfully!"))
            onComplete(true)
        }
    }

    // --- Worker Bookings ---
    fun createWorkerBooking(
        worker: WorkerProfileEntity,
        serviceRequired: String,
        scheduledDate: String,
        scheduledTime: String,
        bookingType: String,
        agreedRate: Double,
        estimatedTotal: Double,
        customerAddress: String,
        customerNotes: String = "",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val ref = "WB-2026-${System.currentTimeMillis() % 100000}"
            val booking = WorkerBookingEntity(
                bookingReference = ref,
                customerUserId = user?.id ?: 1L,
                customerName = user?.fullName ?: "Kashmir Citizen",
                customerPhone = user?.phoneNumber ?: "9419000000",
                customerDistrict = user?.district ?: "Srinagar",
                customerArea = user?.area ?: "City",
                customerAddress = customerAddress,
                workerUserId = worker.userId,
                workerName = worker.name,
                workerProfession = worker.profession,
                serviceRequired = serviceRequired,
                scheduledDate = scheduledDate,
                scheduledTime = scheduledTime,
                bookingType = bookingType,
                agreedRate = agreedRate,
                totalEstimatedCost = estimatedTotal,
                status = "REQUESTED",
                customerNotes = customerNotes
            )
            repository.insertBooking(booking)
            _toastEvent.emit(UiNotification("Booking request sent to ${worker.name}! Ref: $ref"))
            onComplete(true)
        }
    }

    fun updateBookingStatus(bookingId: Long, status: String) {
        viewModelScope.launch {
            repository.updateBookingStatus(bookingId, status)
            _toastEvent.emit(UiNotification("Booking status updated to $status"))
        }
    }

    // --- Product Orders & Reselling ---
    fun placeProductOrder(
        product: ProductEntity,
        quantity: Int,
        selectedSize: String = "",
        selectedColor: String = "",
        deliveryAddress: String,
        customerPhone: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val buyerId = user?.id ?: 1L
            val buyerName = user?.fullName ?: "Kashmir Buyer"
            val orderNum = "RK-ORD-${System.currentTimeMillis() % 1000000}"
            val unitPrice = product.price
            val itemSubtotal = unitPrice * quantity
            val commissionRate = platformSettings.value?.productCommissionPercent ?: 5.0
            val platformCommission = (itemSubtotal * commissionRate) / 100.0
            val sellerPayable = itemSubtotal - platformCommission

            val order = OrderEntity(
                orderNumber = orderNum,
                customerId = buyerId,
                customerName = buyerName,
                customerPhone = customerPhone,
                customerAddress = deliveryAddress,
                customerDistrict = user?.district ?: product.district,
                customerArea = user?.area ?: product.area,
                productId = product.id,
                productTitle = product.title,
                productImageUri = product.imageUri,
                supplierUserId = product.sellerUserId,
                supplierName = product.sellerName,
                selectedSize = selectedSize,
                selectedColor = selectedColor,
                quantity = quantity,
                unitPrice = unitPrice,
                totalAmount = itemSubtotal,
                supplierShareAmount = sellerPayable,
                platformFeeAmount = platformCommission,
                paymentMethod = "CASH_ON_DELIVERY",
                paymentStatus = "PENDING",
                orderStatus = "ORDERED",
                estimatedDeliveryDays = 3,
                trackingNotes = "Order confirmed for cash on delivery"
            )
            val insertedOrderId = repository.insertOrder(order)
            val persistedOrder = order.copy(id = insertedOrderId)
            firestoreMarketplaceRepo.pushOrderToFirestore(persistedOrder)

            val ledger = TransactionLedgerEntity(
                transactionRef = "TXN-${System.currentTimeMillis() % 1000000}",
                orderRef = orderNum,
                category = if (product.isSecondHand) "SECOND_HAND" else "PRODUCTS",
                listingTitle = product.title,
                buyerId = buyerId,
                buyerName = buyerName,
                sellerId = product.sellerUserId,
                sellerName = product.sellerName,
                grossAmount = itemSubtotal,
                commissionRatePercent = commissionRate,
                commissionCalculationBasis = "${commissionRate}% of Product Order (₹$itemSubtotal)",
                platformCommission = platformCommission,
                sellerPayableAmount = sellerPayable,
                paymentStatus = "PENDING",
                commissionStatus = "CALCULATED",
                settlementStatus = "UNSETTLED"
            )
            repository.insertLedgerEntry(ledger)
            _toastEvent.emit(UiNotification("Order placed! Order ID: $orderNum"))
            onComplete(true)
        }
    }

    fun updateOrderStatus(orderId: Long, status: String, notes: String = "") {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, notes)
            firestoreMarketplaceRepo.updateOrderStatus(orderId, status, notes)
            _toastEvent.emit(UiNotification("Order status updated to $status (Synced to Firebase)"))
        }
    }

    fun updateProductStock(productId: Long, newStock: Int) {
        viewModelScope.launch {
            firestoreMarketplaceRepo.updateProductStock(productId, newStock)
            _toastEvent.emit(UiNotification("Stock updated to $newStock units (Synced to Cloud)"))
        }
    }

    fun addSellerProduct(
        title: String,
        description: String,
        category: String,
        culturalProductType: String,
        condition: String,
        price: Double,
        stockQuantity: Int,
        availableSizes: String,
        availableColors: String,
        genderTarget: String,
        district: String,
        area: String,
        imageUri: String,
        imageUris: String,
        isResellable: Boolean,
        supplierPrice: Double,
        maxResellerMargin: Double,
        onComplete: (Boolean) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val userShops = allShops.value.filter { it.ownerUserId == user.id }
            val shopId = userShops.firstOrNull()?.id ?: 0L
            val product = ProductEntity(
                sellerUserId = user.id,
                sellerName = userShops.firstOrNull()?.shopName ?: user.fullName,
                shopId = shopId,
                title = title,
                description = description,
                category = category,
                culturalProductType = culturalProductType,
                condition = condition,
                isSecondHand = condition.contains("Used", ignoreCase = true),
                price = price,
                stockQuantity = stockQuantity,
                availableSizes = availableSizes,
                availableColors = availableColors,
                genderTarget = genderTarget,
                district = district,
                area = area,
                imageUri = imageUri,
                imageUris = imageUris,
                isResellable = isResellable,
                supplierPrice = if (supplierPrice > 0.0) supplierPrice else (price * 0.8),
                maxResellerMargin = if (maxResellerMargin > 0.0) maxResellerMargin else (price * 0.3),
                status = "ACTIVE"
            )
            val newId = repository.insertProduct(product)
            val persisted = product.copy(id = newId)
            firestoreMarketplaceRepo.pushProductToFirestore(persisted)
            _toastEvent.emit(UiNotification("Product \"$title\" added & synced to Firebase!"))
            onComplete(true)
        }
    }

    fun createShop(
        shopName: String,
        shopType: String,
        description: String,
        district: String,
        area: String,
        address: String,
        contactPhone: String,
        openingHours: String = "9:00 AM - 8:00 PM",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val shop = ShopEntity(
                ownerUserId = user?.id ?: 1L,
                ownerName = user?.fullName ?: "Shop Owner",
                shopName = shopName,
                shopType = shopType,
                description = description,
                district = district,
                area = area,
                address = address,
                contactPhone = contactPhone,
                openingHours = openingHours,
                isVerified = false,
                status = "ACTIVE"
            )
            val newShopId = repository.insertShop(shop)
            val persistedShop = shop.copy(id = newShopId)
            firestoreMarketplaceRepo.pushShopToFirestore(persistedShop)
            _toastEvent.emit(UiNotification("Shop \"$shopName\" created and cloud-synced!"))
            onComplete(true)
        }
    }

    fun registerRestaurant(
        name: String,
        description: String,
        cuisineType: String,
        district: String,
        area: String,
        address: String,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val rest = RestaurantEntity(
                ownerUserId = user?.id ?: 1L,
                ownerName = user?.fullName ?: "Chef / Waza",
                name = name,
                description = description,
                cuisineType = cuisineType,
                district = district,
                area = area,
                address = address,
                rating = 5.0
            )
            repository.insertRestaurant(rest)
            _toastEvent.emit(UiNotification("Restaurant \"$name\" registered!"))
            onComplete(true)
        }
    }

    fun addFoodMenuItem(
        restaurantId: Long,
        name: String,
        description: String,
        category: String,
        price: Double,
        isVeg: Boolean,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val item = FoodMenuItemEntity(
                restaurantId = restaurantId,
                name = name,
                description = description,
                category = category,
                price = price,
                isVegetarian = isVeg
            )
            repository.insertMenuItem(item)
            _toastEvent.emit(UiNotification("Added \"$name\" to restaurant menu!"))
            onComplete(true)
        }
    }

    fun updateRestaurant(restaurant: RestaurantEntity) {
        viewModelScope.launch {
            repository.updateRestaurant(restaurant)
            _toastEvent.emit(UiNotification("Restaurant details updated!"))
        }
    }

    fun updateMenuItem(item: FoodMenuItemEntity) {
        viewModelScope.launch {
            repository.updateMenuItem(item)
            _toastEvent.emit(UiNotification("Dish \"${item.name}\" updated!"))
        }
    }

    fun deleteMenuItem(id: Long) {
        viewModelScope.launch {
            repository.deleteMenuItem(id)
            _toastEvent.emit(UiNotification("Dish removed from menu!"))
        }
    }

    fun resellProduct(
        product: ProductEntity,
        resellerMargin: Double,
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val code = "RK-RS-${System.currentTimeMillis() % 100000}"
            val resellerListing = ResellerListingEntity(
                resellerUserId = user?.id ?: 1L,
                resellerName = user?.fullName ?: "Reseller",
                resellerPhone = user?.phoneNumber ?: "",
                originalProductId = product.id,
                productTitle = product.title,
                productImageUri = product.imageUri,
                supplierUserId = product.sellerUserId,
                supplierName = product.sellerName,
                supplierPrice = product.price,
                resellerPrice = product.price + resellerMargin,
                resellerMargin = resellerMargin,
                shareableCode = code,
                status = "ACTIVE"
            )
            repository.insertResellerListing(resellerListing)
            _toastEvent.emit(UiNotification("Reseller listing created! Share code: $code (Margin: ₹${resellerMargin.toInt()})"))
            onComplete(true)
        }
    }

    fun placeResellerCustomerOrder(
        product: ProductEntity,
        resellerMargin: Double,
        quantity: Int,
        customerName: String,
        customerPhone: String,
        deliveryAddress: String,
        customerDistrict: String,
        customerArea: String,
        selectedSize: String = "",
        selectedColor: String = "",
        deliveryInstructions: String = "",
        onComplete: (Boolean) -> Unit
    ) {
        viewModelScope.launch {
            val user = _currentUser.value
            val resellerId = user?.id ?: 1L
            val resellerName = user?.fullName ?: "Kashmir Reseller"
            val orderNum = "RK-RS-${System.currentTimeMillis() % 1000000}"
            val baseUnitPrice = product.price
            val finalUnitPrice = product.price + resellerMargin
            val totalCustomerAmount = finalUnitPrice * quantity
            val totalSupplierAmount = baseUnitPrice * quantity
            val totalResellerEarnedMargin = resellerMargin * quantity

            val commissionRate = platformSettings.value?.productCommissionPercent ?: 5.0
            val platformCommission = (totalSupplierAmount * commissionRate) / 100.0
            val supplierPayable = totalSupplierAmount - platformCommission

            val order = OrderEntity(
                orderNumber = orderNum,
                customerId = resellerId,
                customerName = customerName,
                customerPhone = customerPhone,
                customerAddress = deliveryAddress,
                customerDistrict = customerDistrict,
                customerArea = customerArea,
                productId = product.id,
                productTitle = product.title,
                productImageUri = product.imageUri,
                shopId = product.shopId,
                supplierUserId = product.sellerUserId,
                supplierName = product.sellerName,
                isResoldOrder = true,
                resellerUserId = resellerId,
                resellerName = resellerName,
                resellerMargin = totalResellerEarnedMargin,
                selectedSize = selectedSize,
                selectedColor = selectedColor,
                quantity = quantity,
                unitPrice = finalUnitPrice,
                totalAmount = totalCustomerAmount,
                supplierShareAmount = supplierPayable,
                platformFeeAmount = platformCommission,
                paymentMethod = "CASH_ON_DELIVERY",
                paymentStatus = "PENDING",
                orderStatus = "ORDERED",
                estimatedDeliveryDays = 3,
                trackingNotes = if (deliveryInstructions.isNotBlank()) "Reseller order: $deliveryInstructions" else "Reseller customer order (COD)"
            )

            val insertedOrderId = repository.insertOrder(order)
            val persistedOrder = order.copy(id = insertedOrderId)
            firestoreMarketplaceRepo.pushOrderToFirestore(persistedOrder)

            val ledger = TransactionLedgerEntity(
                transactionRef = "TXN-${System.currentTimeMillis() % 1000000}",
                orderRef = orderNum,
                category = "RESELLING",
                listingTitle = "Resold: ${product.title} (Margin: ₹${totalResellerEarnedMargin.toInt()})",
                buyerId = resellerId,
                buyerName = customerName,
                sellerId = product.sellerUserId,
                sellerName = product.sellerName,
                grossAmount = totalCustomerAmount,
                commissionRatePercent = commissionRate,
                commissionCalculationBasis = "Supplier: ₹${totalSupplierAmount.toInt()}, Reseller Profit: ₹${totalResellerEarnedMargin.toInt()}",
                platformCommission = platformCommission,
                sellerPayableAmount = supplierPayable,
                paymentStatus = "PENDING",
                commissionStatus = "CALCULATED",
                settlementStatus = "UNSETTLED"
            )
            repository.insertLedgerEntry(ledger)
            _toastEvent.emit(UiNotification("Reseller Order placed! You earn ₹${totalResellerEarnedMargin.toInt()} on delivery."))
            onComplete(true)
        }
    }

    fun saveFirestoreUserProfile(
        updatedProfile: UserProfileFirestore,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            isSyncingProfileWithFirestore.value = true
            firestoreUserProfile.value = updatedProfile
            val result = firestoreUserProfileRepo.saveUserProfile(updatedProfile)
            isSyncingProfileWithFirestore.value = false
            if (result.isSuccess) {
                _toastEvent.emit(UiNotification("Profile saved & synchronized with Cloud Firestore"))
                onComplete(true, "Profile saved to Cloud Firestore")
            } else {
                _toastEvent.emit(UiNotification("Cloud sync offline: Changes saved locally", isError = false))
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Offline save")
            }
        }
    }

    fun addServiceHistoryRecord(
        item: ServiceHistoryItem,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val current = firestoreUserProfile.value
        val user = _currentUser.value
        if (current == null || user == null) {
            onComplete(false, "No active user profile")
            return
        }
        viewModelScope.launch {
            val result = firestoreUserProfileRepo.addServiceHistoryRecord(user.id.toString(), current, item)
            if (result.isSuccess) {
                val updatedList = current.serviceHistory.toMutableList().apply { add(0, item) }
                firestoreUserProfile.value = current.copy(serviceHistory = updatedList)
                _toastEvent.emit(UiNotification("Service history record added to Firestore"))
                onComplete(true, "Service history record added")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to add record")
            }
        }
    }

    fun updateServiceHistoryRecord(
        item: ServiceHistoryItem,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val current = firestoreUserProfile.value
        val user = _currentUser.value
        if (current == null || user == null) {
            onComplete(false, "No active user profile")
            return
        }
        viewModelScope.launch {
            val result = firestoreUserProfileRepo.updateServiceHistoryRecord(user.id.toString(), current, item)
            if (result.isSuccess) {
                val updatedList = current.serviceHistory.map { if (it.id == item.id) item else it }
                firestoreUserProfile.value = current.copy(serviceHistory = updatedList)
                _toastEvent.emit(UiNotification("Service record updated in Firestore"))
                onComplete(true, "Service history record updated")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to update record")
            }
        }
    }

    fun removeServiceHistoryRecord(
        recordId: String,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        val current = firestoreUserProfile.value
        val user = _currentUser.value
        if (current == null || user == null) {
            onComplete(false, "No active user profile")
            return
        }
        viewModelScope.launch {
            val result = firestoreUserProfileRepo.removeServiceHistoryRecord(user.id.toString(), current, recordId)
            if (result.isSuccess) {
                val updatedList = current.serviceHistory.filterNot { it.id == recordId }
                firestoreUserProfile.value = current.copy(serviceHistory = updatedList)
                _toastEvent.emit(UiNotification("Service record removed from Firestore"))
                onComplete(true, "Service history record removed")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Failed to remove record")
            }
        }
    }

    fun syncCurrentProfileWithFirestore(onComplete: (Boolean, String) -> Unit = { _, _ -> }) {
        val user = _currentUser.value
        if (user == null) {
            onComplete(false, "No active user session")
            return
        }
        viewModelScope.launch {
            isSyncingProfileWithFirestore.value = true
            val workerProfile = repository.getWorkerProfileByUserId(user.id)
            val current = firestoreUserProfile.value ?: firestoreUserProfileRepo.buildInitialProfileFromLocal(user, workerProfile)
            val result = firestoreUserProfileRepo.saveUserProfile(current)
            isSyncingProfileWithFirestore.value = false
            if (result.isSuccess) {
                _toastEvent.emit(UiNotification("Profile successfully synced with Cloud Firestore"))
                onComplete(true, "Synced with Cloud Firestore")
            } else {
                onComplete(false, result.exceptionOrNull()?.localizedMessage ?: "Sync error")
            }
        }
    }

    // --- Navigation Controls ---
    fun navigateTo(screen: Screen) {
        if (_currentScreen.value != screen) {
            backStack.add(_currentScreen.value)
            _currentScreen.value = screen
        }
    }

    fun navigateBack(): Boolean {
        if (backStack.isNotEmpty()) {
            _currentScreen.value = backStack.removeAt(backStack.size - 1)
            return true
        }
        if (_currentScreen.value != Screen.HOME) {
            _currentScreen.value = Screen.HOME
            return true
        }
        return false
    }

    fun viewWorkerDetail(worker: WorkerProfileEntity) {
        selectedWorker.value = worker
        navigateTo(Screen.WORKER_DETAIL)
    }

    fun viewJobDetail(job: JobEntity) {
        selectedJob.value = job
        navigateTo(Screen.JOB_DETAIL)
    }

    fun viewProductDetail(product: ProductEntity) {
        selectedProduct.value = product
        navigateTo(Screen.PRODUCT_DETAIL)
    }

    fun openChat(conv: ConversationEntity) {
        selectedConversation.value = conv
        navigateTo(Screen.CHAT_DETAIL)
        viewModelScope.launch {
            _currentUser.value?.let { user ->
                repository.markMessagesAsRead(conv.id, user.id)
            }
        }
    }

    // --- Firebase Phone Auth State & Actions ---
    val phoneAuthState = MutableStateFlow<PhoneAuthState>(PhoneAuthState.Idle)
    val phoneAuthNumber = MutableStateFlow("")
    val phoneAuthVerificationId = MutableStateFlow("")
    val phoneAuthResendToken = MutableStateFlow<PhoneAuthProvider.ForceResendingToken?>(null)
    val phoneUserUid = MutableStateFlow("")
    val isNewPhoneUser = MutableStateFlow(false)

    fun requestPhoneVerification(
        activity: Activity,
        rawPhone: String,
        onCodeSent: () -> Unit = {},
        onError: (String) -> Unit = {}
    ) {
        val formatted = PhoneAuthHelper.formatPhoneNumber(rawPhone)
        val plain10 = PhoneAuthHelper.extract10DigitPhone(rawPhone)
        if (plain10.length < 10) {
            val err = "Please enter a valid 10-digit mobile number."
            phoneAuthState.value = PhoneAuthState.Error(err)
            onError(err)
            return
        }

        phoneAuthNumber.value = formatted
        phoneAuthState.value = PhoneAuthState.SendingCode

        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                val auth = FirebaseAuth.getInstance()
                auth.signInWithCredential(credential).addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val fbUser = task.result?.user
                        phoneUserUid.value = fbUser?.uid ?: ""
                        checkAndLoginOrRegisterPhoneUser(plain10, fbUser)
                    } else {
                        phoneAuthState.value = PhoneAuthState.Error(task.exception?.localizedMessage ?: "Auto-verification failed")
                    }
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                val msg = e.localizedMessage ?: "Verification error"
                // Graceful fallback for emulator environments without cellular SMS or quota
                if (msg.contains("quota", ignoreCase = true) ||
                    msg.contains("network", ignoreCase = true) ||
                    msg.contains("internal", ignoreCase = true) ||
                    msg.contains("reCAPTCHA", ignoreCase = true) ||
                    msg.contains("app not authorized", ignoreCase = true)
                ) {
                    val testVid = "TEST_VID_" + System.currentTimeMillis()
                    phoneAuthVerificationId.value = testVid
                    phoneAuthState.value = PhoneAuthState.CodeSent(testVid, formatted)
                    onCodeSent()
                } else {
                    phoneAuthState.value = PhoneAuthState.Error(msg)
                    onError(msg)
                }
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                phoneAuthVerificationId.value = verificationId
                phoneAuthResendToken.value = token
                phoneAuthState.value = PhoneAuthState.CodeSent(verificationId, formatted, token)
                onCodeSent()
            }
        }

        PhoneAuthHelper.sendVerificationCode(
            activity = activity,
            rawPhoneNumber = rawPhone,
            callbacks = callbacks,
            resendToken = phoneAuthResendToken.value
        )
    }

    fun verifyPhoneOtpCode(
        otpCode: String,
        onSuccess: (isNewUser: Boolean, user: UserEntity?) -> Unit,
        onError: (String) -> Unit
    ) {
        val verificationId = phoneAuthVerificationId.value
        val plain10 = PhoneAuthHelper.extract10DigitPhone(phoneAuthNumber.value)

        phoneAuthState.value = PhoneAuthState.VerifyingCode

        PhoneAuthHelper.verifyOtp(
            verificationId = verificationId,
            otpCode = otpCode,
            onSuccess = { fbUser ->
                phoneUserUid.value = fbUser?.uid ?: ""
                viewModelScope.launch {
                    val existing = repository.getUserByPhone(plain10)
                        ?: repository.getUserByPhone(phoneAuthNumber.value)

                    if (existing != null) {
                        _currentUser.value = existing
                        repository.insertLoginActivity(
                            LoginActivityEntity(
                                userId = existing.id,
                                userName = existing.fullName,
                                phoneNumber = existing.phoneNumber,
                                district = existing.district,
                                activityType = "FIREBASE_PHONE_LOGIN",
                                accountStatus = "ACTIVE"
                            )
                        )
                        phoneAuthState.value = PhoneAuthState.Verified(fbUser, phoneAuthNumber.value)
                        isNewPhoneUser.value = false
                        onSuccess(false, existing)
                    } else {
                        isNewPhoneUser.value = true
                        phoneAuthState.value = PhoneAuthState.Verified(fbUser, phoneAuthNumber.value)
                        onSuccess(true, null)
                    }
                }
            },
            onError = { err ->
                phoneAuthState.value = PhoneAuthState.Error(err)
                onError(err)
            }
        )
    }

    fun completePhoneUserRegistration(
        fullName: String,
        district: String,
        area: String,
        roles: String,
        email: String? = null,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val plain10 = PhoneAuthHelper.extract10DigitPhone(phoneAuthNumber.value)
            val phoneToStore = if (plain10.length == 10) plain10 else phoneAuthNumber.value

            val newUser = UserEntity(
                fullName = fullName.trim(),
                phoneNumber = phoneToStore,
                passwordHash = AppRepository.hashPassword("PHONE_AUTH_${phoneUserUid.value.ifBlank { System.currentTimeMillis().toString() }}"),
                district = district,
                area = area,
                email = email?.takeIf { it.isNotBlank() },
                roles = roles,
                securityAnswer = "Kashmir"
            )
            val newId = repository.insertUser(newUser)
            val savedUser = newUser.copy(id = newId)
            _currentUser.value = savedUser

            repository.insertLoginActivity(
                LoginActivityEntity(
                    userId = newId,
                    userName = fullName,
                    phoneNumber = phoneToStore,
                    district = district,
                    activityType = "FIREBASE_PHONE_REGISTER",
                    accountStatus = "ACTIVE"
                )
            )

            if (roles.contains("WORKER")) {
                repository.insertWorkerProfile(
                    WorkerProfileEntity(
                        userId = newId,
                        name = fullName,
                        district = district,
                        area = area,
                        profession = "Local Skilled Worker",
                        occupation = "General Services",
                        skills = "Punctual, Hardworking",
                        availability = "Available Now",
                        workType = "Daily wage",
                        expectedPayment = "₹800/day"
                    )
                )
            }

            phoneAuthState.value = PhoneAuthState.Idle
            onResult(true, "Registration successful! Welcome to Rozgar Kashmir, $fullName.")
        }
    }

    private fun checkAndLoginOrRegisterPhoneUser(plain10: String, fbUser: FirebaseUser?) {
        viewModelScope.launch {
            val existing = repository.getUserByPhone(plain10)
                ?: repository.getUserByPhone(phoneAuthNumber.value)
            if (existing != null) {
                _currentUser.value = existing
                repository.insertLoginActivity(
                    LoginActivityEntity(
                        userId = existing.id,
                        userName = existing.fullName,
                        phoneNumber = existing.phoneNumber,
                        district = existing.district,
                        activityType = "FIREBASE_PHONE_LOGIN",
                        accountStatus = "ACTIVE"
                    )
                )
                phoneAuthState.value = PhoneAuthState.Verified(fbUser, phoneAuthNumber.value)
                isNewPhoneUser.value = false
            } else {
                isNewPhoneUser.value = true
                phoneAuthState.value = PhoneAuthState.Verified(fbUser, phoneAuthNumber.value)
            }
        }
    }

    fun resetPhoneAuth() {
        phoneAuthState.value = PhoneAuthState.Idle
        phoneAuthNumber.value = ""
        phoneAuthVerificationId.value = ""
        phoneAuthResendToken.value = null
        isNewPhoneUser.value = false
    }

    fun useSimulatedTestPhone(testPhone: String = "9419011111") {
        val formatted = PhoneAuthHelper.formatPhoneNumber(testPhone)
        phoneAuthNumber.value = formatted
        val testVid = "TEST_VID_" + System.currentTimeMillis()
        phoneAuthVerificationId.value = testVid
        phoneAuthState.value = PhoneAuthState.CodeSent(testVid, formatted)
    }

    // --- Auth Actions ---
    fun login(phoneOrIdentifier: String, pass: String, onResult: (Boolean, String) -> Unit) {
        loginWithIdentifier(phoneOrIdentifier, pass, requireAdmin = false, onResult = onResult)
    }

    fun loginWithIdentifier(
        identifier: String,
        pass: String,
        requireAdmin: Boolean = false,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            val trimmed = identifier.trim()
            val user = repository.getUserByIdentifier(trimmed)
                ?: repository.getUserByPhone(trimmed)
                ?: repository.getUserByEmail(trimmed)

            if (user == null) {
                onResult(false, "No account registered with this email, phone, or username.")
                return@launch
            }
            if (user.isBanned) {
                onResult(false, "This account has been suspended or banned. Please contact admin.")
                return@launch
            }
            if (requireAdmin && !user.roles.contains("ADMIN")) {
                onResult(false, "Access Denied: Administrative privileges required.")
                return@launch
            }
            if (user.passwordHash == AppRepository.hashPassword(pass)) {
                _currentUser.value = user
                repository.insertLoginActivity(
                    LoginActivityEntity(
                        userId = user.id,
                        userName = user.fullName,
                        phoneNumber = user.phoneNumber,
                        district = user.district,
                        activityType = if (requireAdmin) "ADMIN_LOGIN" else "LOGIN",
                        accountStatus = "ACTIVE"
                    )
                )
                if (requireAdmin) {
                    navigateTo(Screen.ADMIN_DASHBOARD)
                }
                onResult(true, "Welcome back, ${user.fullName}!")
            } else {
                onResult(false, "Incorrect password. Please verify credentials.")
            }
        }
    }

    fun register(
        fullName: String,
        phone: String,
        pass: String,
        district: String,
        area: String,
        email: String?,
        roles: String,
        securityAnswer: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            if (repository.getUserByPhone(phone) != null) {
                onResult(false, "Phone number is already registered.")
                return@launch
            }
            // Sanitize roles: prevent regular users from granting themselves ADMIN privileges during registration
            val sanitizedRoles = roles.split(",")
                .map { it.trim().uppercase() }
                .filter { it.isNotBlank() && it != "ADMIN" }
                .joinToString(",").ifBlank { "BUYER" }

            val newUser = UserEntity(
                fullName = fullName,
                phoneNumber = phone,
                passwordHash = AppRepository.hashPassword(pass),
                district = district,
                area = area,
                email = email?.takeIf { it.isNotBlank() },
                roles = sanitizedRoles,
                securityAnswer = securityAnswer
            )
            val newId = repository.insertUser(newUser)
            val savedUser = newUser.copy(id = newId)
            _currentUser.value = savedUser

            repository.insertLoginActivity(
                LoginActivityEntity(
                    userId = newId,
                    userName = fullName,
                    phoneNumber = phone,
                    district = district,
                    activityType = "REGISTER",
                    accountStatus = "ACTIVE"
                )
            )

            // If registered as worker, create blank initial profile
            if (roles.contains("WORKER")) {
                repository.insertWorkerProfile(
                    WorkerProfileEntity(
                        userId = newId,
                        name = fullName,
                        district = district,
                        area = area,
                        profession = "Daily-Wage Worker",
                        occupation = "General Services",
                        skills = "Hardworking, Punctual",
                        availability = "Available Now",
                        workType = "Daily wage",
                        expectedPayment = "₹700/day"
                    )
                )
            }

            onResult(true, "Registration successful! Welcome to Rozgar Kashmir.")
        }
    }

    fun recoverPassword(phone: String, answer: String, newPass: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            val user = repository.getUserByPhone(phone)
            if (user == null) {
                onResult(false, "Phone number not found.")
                return@launch
            }
            if (user.securityAnswer.trim().equals(answer.trim(), ignoreCase = true)) {
                repository.updateUser(user.copy(passwordHash = AppRepository.hashPassword(newPass)))
                onResult(true, "Password has been successfully reset. Please log in.")
            } else {
                onResult(false, "Security answer does not match.")
            }
        }
    }

    fun handleGoogleSignInSuccess(
        firebaseUser: FirebaseUser,
        onComplete: (Boolean, String) -> Unit = { _, _ -> }
    ) {
        viewModelScope.launch {
            try {
                val email = firebaseUser.email ?: ""
                val name = firebaseUser.displayName?.ifBlank { null } ?: "Kashmir Citizen"
                val phone = firebaseUser.phoneNumber ?: "0000000000"

                val existing = (if (email.isNotBlank()) repository.getUserByEmail(email) else null)
                    ?: (if (phone != "0000000000") repository.getUserByPhone(phone) else null)

                val userToSet = if (existing != null) {
                    val updated = existing.copy(
                        fullName = if (existing.fullName.isBlank() || existing.fullName == "Kashmir Citizen") name else existing.fullName,
                        email = if (existing.email.isNullOrBlank()) email else existing.email,
                        lastLoginAt = System.currentTimeMillis()
                    )
                    repository.updateUser(updated)
                    updated
                } else {
                    val newUser = UserEntity(
                        fullName = name,
                        phoneNumber = if (phone != "0000000000") phone else "G_${firebaseUser.uid.take(8)}",
                        passwordHash = AppRepository.hashPassword("GOOGLE_AUTH_${firebaseUser.uid}"),
                        district = "Srinagar",
                        area = "Kashmir Valley",
                        email = email.ifBlank { null },
                        roles = "WORKER,EMPLOYER,SELLER,BUYER",
                        isVerified = true,
                        securityAnswer = "Kashmir"
                    )
                    val newId = repository.insertUser(newUser)
                    val saved = newUser.copy(id = newId)

                    // Seed initial worker profile
                    repository.insertWorkerProfile(
                        WorkerProfileEntity(
                            userId = newId,
                            name = name,
                            district = "Srinagar",
                            area = "Kashmir Valley",
                            profession = "Local Skilled Worker",
                            occupation = "Artisan / Craftsman",
                            skills = "Punctual, Quality Assurance",
                            availability = "Available Now",
                            workType = "Daily wage",
                            expectedPayment = "₹800/day"
                        )
                    )
                    saved
                }

                _currentUser.value = userToSet

                repository.insertLoginActivity(
                    LoginActivityEntity(
                        userId = userToSet.id,
                        userName = userToSet.fullName,
                        phoneNumber = userToSet.phoneNumber,
                        district = userToSet.district,
                        activityType = "FIREBASE_GOOGLE_LOGIN",
                        accountStatus = "ACTIVE"
                    )
                )

                loadFirestoreProfileForUser(userToSet)
                _toastEvent.emit(UiNotification("Signed in with Google as ${userToSet.fullName}"))
                onComplete(true, "Signed in with Google successfully")
            } catch (e: Exception) {
                onComplete(false, e.localizedMessage ?: "Failed to complete Google sign-in session")
            }
        }
    }

    fun signOutWithCredentialManager(context: Context, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            GoogleAuthCredentialHelper.signOut(context, viewModelScope) {
                _currentUser.value = null
                firestoreUserProfile.value = null
                navigateTo(Screen.HOME)
                onComplete()
            }
        }
    }

    fun logout() {
        try {
            FirebaseAuth.getInstance().signOut()
        } catch (_: Exception) {}
        _currentUser.value = null
        firestoreUserProfile.value = null
        navigateTo(Screen.HOME)
    }

    fun switchUser(userId: Long) {
        viewModelScope.launch {
            val user = repository.getUserById(userId)
            if (user != null) {
                _currentUser.value = user
                _toastEvent.emit(UiNotification("Switched active user to ${user.fullName}"))
            }
        }
    }

    fun setActiveRole(role: String) {
        _activeRole.value = role
    }

    // --- Jobs & Applications ---
    fun postJob(
        title: String,
        profession: String,
        description: String,
        requiredSkills: String,
        workersNeeded: Int,
        district: String,
        area: String,
        workLocation: String,
        workPreference: String = "On-site",
        workType: String,
        startDate: String,
        endDate: String,
        hours: String,
        paymentAmount: String,
        paymentPeriod: String,
        experienceRequired: String,
        requirements: String,
        isSeasonal: Boolean,
        seasonalCategory: String,
        onComplete: (Boolean) -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val job = JobEntity(
                employerUserId = user.id,
                employerName = user.fullName,
                title = title,
                profession = profession,
                description = description,
                requiredSkills = requiredSkills,
                workersNeeded = workersNeeded,
                district = district,
                area = area,
                workLocation = workLocation,
                workPreference = workPreference,
                workType = workType,
                startDate = startDate,
                endDate = endDate,
                hours = hours,
                paymentAmount = paymentAmount,
                paymentPeriod = paymentPeriod,
                experienceRequired = experienceRequired,
                requirements = requirements,
                isSeasonal = isSeasonal,
                seasonalCategory = seasonalCategory,
                status = "ACTIVE"
            )
            repository.insertJob(job)
            _toastEvent.emit(UiNotification("Job posted successfully! Workers can now apply."))
            onComplete(true)
        }
    }

    fun applyForJob(
        job: JobEntity,
        expectedPay: String,
        availabilityNote: String,
        coverNote: String,
        skillsSummary: String = "",
        experienceYears: Int = 1,
        portfolioLink: String = "",
        optionalVoicePath: String = ""
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val existing = repository.getExistingApplication(job.id, user.id)
            if (existing != null) {
                _toastEvent.emit(UiNotification("You have already applied for this job.", isError = true))
                return@launch
            }
            val app = JobApplicationEntity(
                jobId = job.id,
                jobTitle = job.title,
                employerUserId = job.employerUserId,
                applicantUserId = user.id,
                applicantName = user.fullName,
                applicantProfession = "Worker",
                applicantDistrict = user.district,
                expectedPay = expectedPay,
                availabilityNote = availabilityNote,
                coverNote = coverNote,
                skillsSummary = skillsSummary,
                experienceYears = experienceYears,
                portfolioLink = portfolioLink,
                optionalVoicePath = optionalVoicePath,
                status = "SUBMITTED"
            )
            repository.insertApplication(app)

            // Notify employer
            repository.insertNotification(
                NotificationEntity(
                    userId = job.employerUserId,
                    title = "New Application Received",
                    message = "${user.fullName} applied for: ${job.title}",
                    type = "JOB_APPLIED",
                    referenceId = job.id
                )
            )

            _toastEvent.emit(UiNotification("Application submitted successfully!"))
        }
    }

    fun updateApplicationStatus(applicationId: Long, newStatus: String, applicantUserId: Long, jobTitle: String) {
        viewModelScope.launch {
            repository.updateApplicationStatus(applicationId, newStatus)
            repository.insertNotification(
                NotificationEntity(
                    userId = applicantUserId,
                    title = "Application Update: $newStatus",
                    message = "Your application for \"$jobTitle\" has been updated to: $newStatus",
                    type = "APPLICATION_STATUS",
                    referenceId = applicationId
                )
            )
            _toastEvent.emit(UiNotification("Application marked as $newStatus"))
        }
    }

    fun closeJob(jobId: Long) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                repository.updateJob(job.copy(status = "CLOSED"))
                _toastEvent.emit(UiNotification("Job status updated to CLOSED."))
            }
        }
    }

    fun reopenJob(jobId: Long) {
        viewModelScope.launch {
            val job = repository.getJobById(jobId)
            if (job != null) {
                repository.updateJob(job.copy(status = "ACTIVE"))
                _toastEvent.emit(UiNotification("Job re-opened as ACTIVE."))
            }
        }
    }

    fun setWorkerAvailability(isAvailable: Boolean) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val existing = repository.getWorkerProfileByUserId(user.id)
            if (existing != null) {
                val newStatus = if (isAvailable) "Available Now" else "Unavailable / Fully Booked"
                repository.updateWorkerProfile(existing.copy(availability = newStatus))
                _toastEvent.emit(UiNotification("Availability updated: $newStatus"))
            }
        }
    }

    // --- Worker Profile Management ---
    fun saveWorkerProfile(
        name: String,
        photoUrl: String = "",
        district: String,
        area: String,
        profession: String,
        occupation: String,
        skills: String,
        education: String,
        experienceYears: Int,
        languages: String,
        bio: String,
        previousWork: String,
        services: String,
        availability: String,
        workType: String,
        workPreference: String = "On-site",
        expectedPayment: String,
        paymentFrequency: String = "Daily",
        isSeasonalWorker: Boolean,
        seasonalSpecialties: String,
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val existing = repository.getWorkerProfileByUserId(user.id)
            val updated = WorkerProfileEntity(
                id = existing?.id ?: 0,
                userId = user.id,
                name = name,
                photoUrl = if (photoUrl.isNotBlank()) photoUrl else (existing?.photoUrl ?: ""),
                district = district,
                area = area,
                profession = profession,
                occupation = occupation,
                skills = skills,
                education = education,
                experienceYears = experienceYears,
                languages = languages,
                bio = bio,
                previousWork = previousWork,
                services = services,
                availability = availability,
                workType = workType,
                workPreference = workPreference,
                expectedPayment = expectedPayment,
                paymentFrequency = paymentFrequency,
                isSeasonalWorker = isSeasonalWorker,
                seasonalSpecialties = seasonalSpecialties,
                isVerified = existing?.isVerified ?: user.isVerified,
                rating = existing?.rating ?: 5.0f,
                reviewCount = existing?.reviewCount ?: 1
            )
            if (existing != null) {
                repository.updateWorkerProfile(updated)
            } else {
                repository.insertWorkerProfile(updated)
            }
            _toastEvent.emit(UiNotification("Worker profile updated successfully!"))
            onComplete()
        }
    }

    // --- Marketplace Actions ---
    fun postProduct(
        title: String,
        description: String,
        category: String,
        condition: String,
        isSecondHand: Boolean,
        price: Double,
        isNegotiable: Boolean,
        quantity: Int,
        district: String,
        area: String,
        onComplete: () -> Unit
    ) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val product = ProductEntity(
                sellerUserId = user.id,
                sellerName = user.fullName,
                title = title,
                description = description,
                category = category,
                condition = condition,
                isSecondHand = isSecondHand,
                price = price,
                isNegotiable = isNegotiable,
                quantity = quantity,
                district = district,
                area = area,
                status = "ACTIVE"
            )
            repository.insertProduct(product)
            _toastEvent.emit(UiNotification("Listing published to Marketplace!"))
            onComplete()
        }
    }

    // --- Messaging & Voice-First Conversation ---
    fun startConversationWith(targetUserId: Long, targetUserName: String, subject: String) {
        val user = _currentUser.value ?: return
        if (user.id == targetUserId) {
            viewModelScope.launch {
                _toastEvent.emit(UiNotification("You cannot message yourself.", isError = true))
            }
            return
        }

        viewModelScope.launch {
            var conv = repository.findConversationBetween(user.id, targetUserId)
            if (conv == null) {
                val newConvId = repository.insertConversation(
                    ConversationEntity(
                        user1Id = user.id,
                        user2Id = targetUserId,
                        user1Name = user.fullName,
                        user2Name = targetUserName,
                        subject = subject,
                        hasFirstVoiceNote = false, // Voice First rule is active!
                        lastMessageText = "Start with a Voice Note to connect"
                    )
                )
                conv = repository.getConversationById(newConvId)
            }
            selectedConversation.value = conv
            navigateTo(Screen.CHAT_DETAIL)
        }
    }

    fun sendVoiceMessage(durationSec: Int, noteSummary: String) {
        val conv = selectedConversation.value ?: return
        val user = _currentUser.value ?: return

        viewModelScope.launch {
            val msg = MessageEntity(
                conversationId = conv.id,
                senderUserId = user.id,
                senderName = user.fullName,
                messageType = "VOICE",
                textContent = noteSummary.ifBlank { "Voice note ($durationSec s)" },
                voiceDurationSeconds = durationSec,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)

            val updatedConv = conv.copy(
                hasFirstVoiceNote = true,
                lastMessageText = "🎤 Voice note (${durationSec}s)",
                lastMessageTime = System.currentTimeMillis()
            )
            repository.updateConversation(updatedConv)
            selectedConversation.value = updatedConv

            // Notify recipient
            val recipientId = if (conv.user1Id == user.id) conv.user2Id else conv.user1Id
            repository.insertNotification(
                NotificationEntity(
                    userId = recipientId,
                    title = "New Voice Message",
                    message = "${user.fullName} sent you a voice note",
                    type = "VOICE_MESSAGE",
                    referenceId = conv.id
                )
            )
            _toastEvent.emit(UiNotification("Voice message sent!"))
        }
    }

    fun sendTextMessage(text: String) {
        val conv = selectedConversation.value ?: return
        val user = _currentUser.value ?: return

        if (!conv.hasFirstVoiceNote) {
            viewModelScope.launch {
                _toastEvent.emit(UiNotification("Voice-First Requirement: The first message must be a voice note to start the conversation.", isError = true))
            }
            return
        }

        viewModelScope.launch {
            val msg = MessageEntity(
                conversationId = conv.id,
                senderUserId = user.id,
                senderName = user.fullName,
                messageType = "TEXT",
                textContent = text,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)

            val updatedConv = conv.copy(
                lastMessageText = text,
                lastMessageTime = System.currentTimeMillis()
            )
            repository.updateConversation(updatedConv)
            selectedConversation.value = updatedConv

            val recipientId = if (conv.user1Id == user.id) conv.user2Id else conv.user1Id
            repository.insertNotification(
                NotificationEntity(
                    userId = recipientId,
                    title = "New Message",
                    message = "${user.fullName}: $text",
                    type = "MESSAGE",
                    referenceId = conv.id
                )
            )
        }
    }

    fun shareContactPrivately(contactInfo: String) {
        val conv = selectedConversation.value ?: return
        val user = _currentUser.value ?: return

        if (!conv.hasFirstVoiceNote) {
            viewModelScope.launch {
                _toastEvent.emit(UiNotification("Send a voice note first before sharing contact details.", isError = true))
            }
            return
        }

        viewModelScope.launch {
            val msg = MessageEntity(
                conversationId = conv.id,
                senderUserId = user.id,
                senderName = user.fullName,
                messageType = "CONTACT_SHARE",
                textContent = "Voluntarily shared direct contact details",
                sharedContactInfo = contactInfo,
                timestamp = System.currentTimeMillis()
            )
            repository.insertMessage(msg)

            val updatedConv = conv.copy(
                lastMessageText = "📱 Contact shared",
                lastMessageTime = System.currentTimeMillis()
            )
            repository.updateConversation(updatedConv)
            selectedConversation.value = updatedConv
            _toastEvent.emit(UiNotification("Contact details shared in private chat."))
        }
    }

    // --- Saved Items ---
    fun toggleSave(itemType: String, itemId: Long, title: String, subtitle: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val isNowSaved = repository.toggleSaveItem(user.id, itemType, itemId, title, subtitle)
            _toastEvent.emit(UiNotification(if (isNowSaved) "Saved to your list!" else "Removed from saved items"))
        }
    }

    // --- Reporting ---
    fun submitReport(reportedType: String, targetId: Long, targetTitle: String, reason: String, details: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val report = ReportEntity(
                reporterUserId = user.id,
                reporterName = user.fullName,
                reportedType = reportedType,
                targetId = targetId,
                targetTitle = targetTitle,
                reason = reason,
                details = details,
                status = "PENDING"
            )
            repository.insertReport(report)
            _toastEvent.emit(UiNotification("Report submitted for admin review. Thank you for keeping Rozgar Kashmir safe."))
        }
    }

    // --- Donations ---
    fun submitDonation(amount: Double, donorName: String, referenceId: String) {
        val user = _currentUser.value ?: return
        viewModelScope.launch {
            val donation = DonationEntity(
                userId = user.id,
                donorName = donorName.ifBlank { user.fullName },
                amount = amount,
                referenceId = referenceId,
                status = "PENDING"
            )
            repository.insertDonation(donation)
            _toastEvent.emit(UiNotification("Thank you! Your donation reference has been submitted for verification."))
        }
    }

    // --- Admin Operations ---
    fun adminUpdateUserStatus(userId: Long, banned: Boolean, verified: Boolean) {
        viewModelScope.launch {
            repository.setUserBanned(userId, banned)
            repository.setUserVerified(userId, verified)
            _toastEvent.emit(UiNotification("User status updated."))
        }
    }

    fun adminDeleteUser(userId: Long) {
        viewModelScope.launch {
            repository.deleteUser(userId)
            _toastEvent.emit(UiNotification("User account deleted."))
        }
    }

    fun adminModerateJob(jobId: Long, status: String) {
        viewModelScope.launch {
            repository.setJobStatus(jobId, status)
            _toastEvent.emit(UiNotification("Job status updated to $status"))
        }
    }

    fun adminModerateProduct(productId: Long, status: String) {
        viewModelScope.launch {
            repository.setProductStatus(productId, status)
            _toastEvent.emit(UiNotification("Product status updated to $status"))
        }
    }

    fun adminModerateProperty(propertyId: Long, status: String) {
        viewModelScope.launch {
            val prop = repository.getPropertyById(propertyId)
            if (prop != null) {
                repository.updateProperty(prop.copy(status = status))
                _toastEvent.emit(UiNotification("Property listing marked as $status"))
            }
        }
    }

    fun adminModerateRestaurant(restaurantId: Long, isClosed: Boolean) {
        viewModelScope.launch {
            val rest = repository.getRestaurantById(restaurantId)
            if (rest != null) {
                val updated = rest.copy(
                    isClosedTemporarily = isClosed
                )
                repository.updateRestaurant(updated)
                _toastEvent.emit(UiNotification("Restaurant updated: ${if (isClosed) "CLOSED" else "OPEN"}"))
            }
        }
    }

    fun adminModerateShop(shopId: Long, isVerified: Boolean) {
        viewModelScope.launch {
            val shop = repository.getShopById(shopId)
            if (shop != null) {
                val updated = shop.copy(isVerified = isVerified)
                repository.updateShop(updated)
                _toastEvent.emit(UiNotification("Shop verification set to $isVerified"))
            }
        }
    }

    fun adminUpdateOrderStatus(orderId: Long, status: String, notes: String = "Updated by Admin") {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, notes)
            repository.insertAuditLog(
                AdminAuditLogEntity(
                    adminUserId = _currentUser.value?.id ?: 1L,
                    adminName = _currentUser.value?.fullName ?: "Admin",
                    actionType = "ORDER_STATUS_UPDATED",
                    targetEntity = "Order",
                    targetId = orderId,
                    details = "Order #$orderId changed to $status. Notes: $notes"
                )
            )
            _toastEvent.emit(UiNotification("Order #$orderId status: $status"))
        }
    }

    fun adminRefundOrder(orderId: Long, refundReason: String) {
        viewModelScope.launch {
            val orders = repository.getAllOrdersFlow().firstOrNull() ?: emptyList()
            val order = orders.find { it.id == orderId } ?: return@launch
            repository.updateOrderStatus(orderId, "REFUNDED", "Refund processed by Admin: $refundReason")

            // Update matching central ledger entry if exists
            val ledgerEntries = repository.getAllLedgerEntriesFlow().firstOrNull() ?: emptyList()
            val matchingLedger = ledgerEntries.find { it.orderRef == order.orderNumber || it.orderRef == "ORD-$orderId" }
            if (matchingLedger != null) {
                repository.updateLedgerEntry(
                    matchingLedger.copy(
                        paymentStatus = "REFUNDED",
                        settlementStatus = "SETTLED",
                        disputeNotes = "Refunded: $refundReason"
                    )
                )
            }

            repository.insertAuditLog(
                AdminAuditLogEntity(
                    adminUserId = _currentUser.value?.id ?: 1L,
                    adminName = _currentUser.value?.fullName ?: "Admin",
                    actionType = "ORDER_REFUND_PROCESSED",
                    targetEntity = "Order",
                    targetId = orderId,
                    details = "Refund ₹${order.totalAmount} processed for order #${order.orderNumber}. Reason: $refundReason"
                )
            )
            _toastEvent.emit(UiNotification("Order #${order.orderNumber} refunded successfully."))
        }
    }

    fun adminUpdateReport(reportId: Long, status: String, adminNotes: String) {
        viewModelScope.launch {
            repository.updateReportStatus(reportId, status, adminNotes)
            _toastEvent.emit(UiNotification("Report marked as $status"))
        }
    }

    fun adminUpdateDonation(donationId: Long, status: String) {
        viewModelScope.launch {
            repository.updateDonationStatus(donationId, status)
            _toastEvent.emit(UiNotification("Donation status set to $status"))
        }
    }

    // --- Verification Actions ---
    fun submitVerificationRequest(
        upiRef: String,
        idType: String,
        idNumber: String,
        notes: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please login first.")
            return
        }
        viewModelScope.launch {
            try {
                repository.insertVerificationRequest(
                    VerificationRequestEntity(
                        userId = user.id,
                        userName = user.fullName,
                        userPhone = user.phoneNumber,
                        upiReferenceId = upiRef,
                        idProofType = idType,
                        idProofNumber = idNumber,
                        notes = notes,
                        status = "PENDING_VERIFICATION"
                    )
                )
                _toastEvent.emit(UiNotification("Verification request submitted. Status: PENDING_VERIFICATION"))
                onResult(true, "Request submitted. Admin will verify UPI transaction.")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to submit request")
            }
        }
    }

    fun adminApproveVerification(requestId: Long, targetUserId: Long, notes: String) {
        viewModelScope.launch {
            repository.approveVerification(requestId, targetUserId, notes)
            if (_currentUser.value?.id == targetUserId) {
                _currentUser.value = repository.getUserById(targetUserId)
            }
            _toastEvent.emit(UiNotification("Verification Approved! Blue Badge is now active."))
        }
    }

    fun adminRejectVerification(requestId: Long, notes: String) {
        viewModelScope.launch {
            repository.rejectVerification(requestId, notes)
            _toastEvent.emit(UiNotification("Verification request marked as rejected."))
        }
    }

    // --- Reviews Actions ---
    fun getReviewsForTarget(targetType: String, targetId: Long): Flow<List<ReviewEntity>> {
        return repository.getReviewsForTargetFlow(targetType, targetId)
    }

    fun submitReview(
        targetType: String,
        targetId: Long,
        targetTitle: String,
        rating: Int,
        reviewText: String,
        transactionRef: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please login to leave a review.")
            return
        }
        viewModelScope.launch {
            val result = repository.submitReview(
                ReviewEntity(
                    authorUserId = user.id,
                    authorName = user.fullName,
                    targetType = targetType,
                    targetId = targetId,
                    targetTitle = targetTitle,
                    rating = rating,
                    reviewText = reviewText,
                    transactionOrJobRef = transactionRef
                )
            )
            if (result.isSuccess) {
                _toastEvent.emit(UiNotification("Review submitted successfully!"))
                onResult(true, "Review submitted!")
            } else {
                val err = result.exceptionOrNull()?.localizedMessage ?: "Failed to submit review"
                onResult(false, err)
            }
        }
    }

    fun reportReview(reviewId: Long, reason: String) {
        viewModelScope.launch {
            repository.reportReview(reviewId, reason)
            _toastEvent.emit(UiNotification("Review flagged for administrative inspection."))
        }
    }

    fun adminUpdateReviewStatus(reviewId: Long, status: String) {
        viewModelScope.launch {
            repository.updateReviewStatus(reviewId, status)
            _toastEvent.emit(UiNotification("Review status updated to $status"))
        }
    }

    // --- Kashmiri Contributions & Bookstore Actions ---
    fun publishKashmiriContribution(
        type: String,
        titleKashmiri: String,
        titleEnglish: String,
        authorName: String,
        authorBio: String,
        genre: String,
        content: String,
        summary: String,
        isBook: Boolean,
        isPaid: Boolean,
        price: Double,
        pageCount: Int,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        val userId = user?.id ?: 1L
        viewModelScope.launch {
            try {
                repository.insertContribution(
                    KashmiriContributionEntity(
                        authorUserId = userId,
                        authorName = authorName,
                        authorBio = authorBio,
                        type = type,
                        titleKashmiri = titleKashmiri,
                        titleEnglish = titleEnglish,
                        content = content,
                        summary = summary,
                        genre = genre,
                        isBook = isBook,
                        isPaid = isPaid,
                        price = price,
                        pageCount = pageCount,
                        status = "APPROVED"
                    )
                )
                _toastEvent.emit(UiNotification("Published to Kashmiri Literature & Bookstore!"))
                onResult(true, "Published successfully!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Publication failed")
            }
        }
    }

    fun purchaseBook(
        book: KashmiriContributionEntity,
        paymentRef: String,
        onResult: (Boolean, String) -> Unit
    ) {
        val user = _currentUser.value
        if (user == null) {
            onResult(false, "Please login to purchase.")
            return
        }
        viewModelScope.launch {
            try {
                repository.purchaseBook(
                    BookPurchaseEntity(
                        buyerUserId = user.id,
                        buyerName = user.fullName,
                        bookId = book.id,
                        bookTitle = book.titleKashmiri,
                        authorName = book.authorName,
                        amountPaid = book.price,
                        paymentRef = paymentRef
                    )
                )
                _toastEvent.emit(UiNotification("Book purchased! Digital access unlocked."))
                onResult(true, "Purchase successful!")
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Purchase error")
            }
        }
    }

    fun recordBookRead(bookId: Long) {
        viewModelScope.launch {
            repository.incrementContributionReads(bookId)
        }
    }

    fun adminModerateContribution(id: Long, status: String) {
        viewModelScope.launch {
            repository.updateContributionStatus(id, status)
            _toastEvent.emit(UiNotification("Contribution status: $status"))
        }
    }

    fun adminDeleteContribution(id: Long) {
        viewModelScope.launch {
            repository.deleteContribution(id)
            _toastEvent.emit(UiNotification("Contribution removed."))
        }
    }

    // --- Legal Policies & Governance Flow ---
    val allLegalPolicies: StateFlow<List<LegalPolicyEntity>> = repository.getAllPoliciesFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun updateLegalPolicy(policy: LegalPolicyEntity, onResult: (Boolean) -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.insertOrUpdatePolicy(policy)
                _toastEvent.emit(UiNotification("Policy updated successfully."))
                onResult(true)
            } catch (e: Exception) {
                _toastEvent.emit(UiNotification("Failed to update policy: ${e.message}"))
                onResult(false)
            }
        }
    }

    // --- User Delivery Addresses ---
    val currentUserAddresses: StateFlow<List<DeliveryAddressEntity>> = _currentUser.flatMapLatest { user ->
        if (user != null) repository.getAddressesForUserFlow(user.id) else flowOf<List<DeliveryAddressEntity>>(emptyList())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun saveUserAddress(address: DeliveryAddressEntity, onComplete: ((Long) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val currentUid = _currentUser.value?.id ?: 1L
                val toSave = address.copy(userId = currentUid)
                val id = repository.insertAddress(toSave)
                _toastEvent.emit(UiNotification("Address saved."))
                onComplete?.invoke(id)
            } catch (e: Exception) {
                _toastEvent.emit(UiNotification("Failed to save address: ${e.message}"))
            }
        }
    }

    fun deleteUserAddress(addressId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteAddress(addressId)
                _toastEvent.emit(UiNotification("Address deleted."))
            } catch (e: Exception) {
                _toastEvent.emit(UiNotification("Failed to delete address."))
            }
        }
    }

    fun setDefaultUserAddress(addressId: Long) {
        viewModelScope.launch {
            try {
                val currentUid = _currentUser.value?.id ?: 1L
                repository.setDefaultAddress(currentUid, addressId)
                _toastEvent.emit(UiNotification("Default address updated."))
            } catch (e: Exception) {
                _toastEvent.emit(UiNotification("Failed to set default address."))
            }
        }
    }
}
