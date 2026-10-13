package com.example.data.local

import androidx.room.*
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow

@Dao
interface AppDao {

    // --- Users ---
    @Query("SELECT * FROM users WHERE phoneNumber = :phone LIMIT 1")
    suspend fun getUserByPhone(phone: String): UserEntity?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): UserEntity?

    @Query("SELECT * FROM users WHERE fullName = :identifier OR email = :identifier OR phoneNumber = :identifier LIMIT 1")
    suspend fun getUserByIdentifier(identifier: String): UserEntity?

    @Query("SELECT * FROM users WHERE id = :id LIMIT 1")
    suspend fun getUserById(id: Long): UserEntity?

    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<UserEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET isBanned = :banned WHERE id = :id")
    suspend fun setUserBanned(id: Long, banned: Boolean)

    @Query("UPDATE users SET isVerified = :verified WHERE id = :id")
    suspend fun setUserVerified(id: Long, verified: Boolean)

    @Query("DELETE FROM users WHERE id = :id")
    suspend fun deleteUser(id: Long)

    @Query("SELECT COUNT(*) FROM users")
    fun getUserCountFlow(): Flow<Int>

    // --- Worker Profiles ---
    @Query("SELECT * FROM worker_profiles ORDER BY rating DESC, createdAt DESC")
    fun getAllWorkersFlow(): Flow<List<WorkerProfileEntity>>

    @Query("SELECT * FROM worker_profiles WHERE userId = :userId LIMIT 1")
    suspend fun getWorkerProfileByUserId(userId: Long): WorkerProfileEntity?

    @Query("SELECT * FROM worker_profiles WHERE id = :id LIMIT 1")
    suspend fun getWorkerProfileById(id: Long): WorkerProfileEntity?

    @Query("SELECT * FROM worker_profiles WHERE isSeasonalWorker = 1 ORDER BY createdAt DESC")
    fun getSeasonalWorkersFlow(): Flow<List<WorkerProfileEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWorkerProfile(profile: WorkerProfileEntity): Long

    @Update
    suspend fun updateWorkerProfile(profile: WorkerProfileEntity)

    @Query("DELETE FROM worker_profiles WHERE id = :id")
    suspend fun deleteWorkerProfile(id: Long)

    @Query("SELECT COUNT(*) FROM worker_profiles")
    fun getWorkerCountFlow(): Flow<Int>

    // --- Jobs ---
    @Query("SELECT * FROM jobs WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    fun getAllJobsFlow(): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs ORDER BY createdAt DESC")
    suspend fun getAllJobsList(): List<JobEntity>

    @Query("SELECT * FROM jobs WHERE id = :id LIMIT 1")
    suspend fun getJobById(id: Long): JobEntity?

    @Query("SELECT * FROM jobs WHERE employerUserId = :employerId ORDER BY createdAt DESC")
    fun getJobsByEmployerFlow(employerId: Long): Flow<List<JobEntity>>

    @Query("SELECT * FROM jobs WHERE isSeasonal = 1 AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getSeasonalJobsFlow(): Flow<List<JobEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJob(job: JobEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertJobs(jobs: List<JobEntity>): List<Long>

    @Update
    suspend fun updateJob(job: JobEntity)

    @Query("UPDATE jobs SET status = :status WHERE id = :id")
    suspend fun setJobStatus(id: Long, status: String)

    @Query("DELETE FROM jobs WHERE id = :id")
    suspend fun deleteJob(id: Long)

    @Query("DELETE FROM jobs WHERE employerUserId = 1")
    suspend fun deleteSampleJobs()

    @Query("SELECT COUNT(*) FROM jobs WHERE status = 'ACTIVE'")
    fun getActiveJobCountFlow(): Flow<Int>

    @Query("SELECT COUNT(*) FROM jobs")
    fun getTotalJobCountFlow(): Flow<Int>

    // --- Job Applications ---
    @Query("SELECT * FROM job_applications WHERE jobId = :jobId ORDER BY appliedAt DESC")
    fun getApplicationsForJobFlow(jobId: Long): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE applicantUserId = :userId ORDER BY appliedAt DESC")
    fun getApplicationsForUserFlow(userId: Long): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications WHERE jobId = :jobId AND applicantUserId = :userId LIMIT 1")
    suspend fun getExistingApplication(jobId: Long, userId: Long): JobApplicationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApplication(application: JobApplicationEntity): Long

    @Query("UPDATE job_applications SET status = :status WHERE id = :id")
    suspend fun updateApplicationStatus(id: Long, status: String)

    @Query("SELECT * FROM job_applications WHERE employerUserId = :employerId ORDER BY appliedAt DESC")
    fun getApplicationsForEmployerFlow(employerId: Long): Flow<List<JobApplicationEntity>>

    @Query("SELECT * FROM job_applications ORDER BY appliedAt DESC")
    fun getAllApplicationsFlow(): Flow<List<JobApplicationEntity>>

    @Query("SELECT COUNT(*) FROM job_applications")
    fun getTotalApplicationsCountFlow(): Flow<Int>

    // --- Marketplace Products ---
    @Query("SELECT * FROM products WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActiveProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products ORDER BY createdAt DESC")
    fun getAllProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE isSecondHand = 1 AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getSecondHandProductsFlow(): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE sellerUserId = :sellerId ORDER BY createdAt DESC")
    fun getProductsBySellerFlow(sellerId: Long): Flow<List<ProductEntity>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Long): ProductEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: ProductEntity): Long

    @Update
    suspend fun updateProduct(product: ProductEntity)

    @Query("UPDATE products SET stockQuantity = :newStock WHERE id = :productId")
    suspend fun updateProductStock(productId: Long, newStock: Int)

    @Query("UPDATE products SET status = :status WHERE id = :id")
    suspend fun setProductStatus(id: Long, status: String)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Long)

    @Query("DELETE FROM products WHERE sellerUserId = 1")
    suspend fun deleteSampleProducts()

    @Query("SELECT COUNT(*) FROM products WHERE status = 'ACTIVE'")
    fun getActiveProductCountFlow(): Flow<Int>

    // --- Conversations & Messages ---
    @Query("SELECT * FROM conversations WHERE user1Id = :userId OR user2Id = :userId ORDER BY lastMessageTime DESC")
    fun getConversationsForUserFlow(userId: Long): Flow<List<ConversationEntity>>

    @Query("SELECT * FROM conversations WHERE id = :id LIMIT 1")
    suspend fun getConversationById(id: Long): ConversationEntity?

    @Query("SELECT * FROM conversations WHERE (user1Id = :u1 AND user2Id = :u2) OR (user1Id = :u2 AND user2Id = :u1) LIMIT 1")
    suspend fun findConversationBetween(u1: Long, u2: Long): ConversationEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConversation(conv: ConversationEntity): Long

    @Update
    suspend fun updateConversation(conv: ConversationEntity)

    @Query("SELECT * FROM messages WHERE conversationId = :convId ORDER BY timestamp ASC")
    fun getMessagesForConversationFlow(convId: Long): Flow<List<MessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(msg: MessageEntity): Long

    @Query("UPDATE messages SET isRead = 1 WHERE conversationId = :convId AND senderUserId != :userId")
    suspend fun markMessagesAsRead(convId: Long, userId: Long)

    @Query("SELECT COUNT(*) FROM conversations")
    fun getTotalConversationsCountFlow(): Flow<Int>

    // --- Reports ---
    @Query("SELECT * FROM reports ORDER BY createdAt DESC")
    fun getAllReportsFlow(): Flow<List<ReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReport(report: ReportEntity): Long

    @Query("UPDATE reports SET status = :status, adminNotes = :adminNotes WHERE id = :id")
    suspend fun updateReportStatus(id: Long, status: String, adminNotes: String)

    @Query("SELECT COUNT(*) FROM reports WHERE status = 'PENDING'")
    fun getPendingReportsCountFlow(): Flow<Int>

    // --- Notifications ---
    @Query("SELECT * FROM notifications WHERE userId = :userId ORDER BY createdAt DESC")
    fun getNotificationsForUserFlow(userId: Long): Flow<List<NotificationEntity>>

    @Query("SELECT COUNT(*) FROM notifications WHERE userId = :userId AND isRead = 0")
    fun getUnreadNotificationsCountFlow(userId: Long): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationEntity): Long

    @Query("UPDATE notifications SET isRead = 1 WHERE userId = :userId")
    suspend fun markAllNotificationsRead(userId: Long)

    // --- Donations ---
    @Query("SELECT * FROM donations ORDER BY createdAt DESC")
    fun getAllDonationsFlow(): Flow<List<DonationEntity>>

    @Query("SELECT * FROM donations WHERE userId = :userId ORDER BY createdAt DESC")
    fun getDonationsByUserFlow(userId: Long): Flow<List<DonationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDonation(donation: DonationEntity): Long

    @Query("UPDATE donations SET status = :status WHERE id = :id")
    suspend fun updateDonationStatus(id: Long, status: String)

    @Query("SELECT COUNT(*) FROM donations")
    fun getTotalDonationsCountFlow(): Flow<Int>

    // --- Login Activities ---
    @Query("SELECT * FROM login_activities ORDER BY timestamp DESC LIMIT 200")
    fun getAllLoginActivitiesFlow(): Flow<List<LoginActivityEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLoginActivity(activity: LoginActivityEntity): Long

    // --- Saved Items ---
    @Query("SELECT * FROM saved_items WHERE userId = :userId ORDER BY savedAt DESC")
    fun getSavedItemsForUserFlow(userId: Long): Flow<List<SavedItemEntity>>

    @Query("SELECT * FROM saved_items WHERE userId = :userId AND itemType = :itemType AND itemId = :itemId LIMIT 1")
    suspend fun getSavedItem(userId: Long, itemType: String, itemId: Long): SavedItemEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedItem(item: SavedItemEntity): Long

    @Query("DELETE FROM saved_items WHERE userId = :userId AND itemType = :itemType AND itemId = :itemId")
    suspend fun deleteSavedItem(userId: Long, itemType: String, itemId: Long)

    @Query("DELETE FROM saved_items WHERE userId = :userId")
    suspend fun deleteSavedItemsForUser(userId: Long)

    // --- Verification Requests ---
    @Query("SELECT * FROM verification_requests ORDER BY requestedAt DESC")
    fun getAllVerificationRequestsFlow(): Flow<List<VerificationRequestEntity>>

    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY requestedAt DESC LIMIT 1")
    fun getLatestVerificationRequestForUserFlow(userId: Long): Flow<VerificationRequestEntity?>

    @Query("SELECT * FROM verification_requests WHERE userId = :userId ORDER BY requestedAt DESC LIMIT 1")
    suspend fun getLatestVerificationRequestForUser(userId: Long): VerificationRequestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVerificationRequest(request: VerificationRequestEntity): Long

    @Query("UPDATE verification_requests SET status = :status, adminReviewNotes = :notes, reviewedAt = :reviewedAt WHERE id = :id")
    suspend fun updateVerificationRequestStatus(id: Long, status: String, notes: String, reviewedAt: Long = System.currentTimeMillis())

    @Query("UPDATE worker_profiles SET isVerified = :verified WHERE userId = :userId")
    suspend fun setWorkerProfileVerified(userId: Long, verified: Boolean)

    // --- Reviews ---
    @Query("SELECT * FROM reviews WHERE targetType = :targetType AND targetId = :targetId AND status != 'REMOVED' ORDER BY createdAt DESC")
    fun getReviewsForTargetFlow(targetType: String, targetId: Long): Flow<List<ReviewEntity>>

    @Query("SELECT * FROM reviews WHERE authorUserId = :authorId AND targetType = :targetType AND targetId = :targetId LIMIT 1")
    suspend fun getExistingReview(authorId: Long, targetType: String, targetId: Long): ReviewEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReview(review: ReviewEntity): Long

    @Query("UPDATE reviews SET isReported = 1, reportReason = :reason, status = 'FLAGGED' WHERE id = :id")
    suspend fun reportReview(id: Long, reason: String)

    @Query("SELECT * FROM reviews WHERE isReported = 1 ORDER BY createdAt DESC")
    fun getReportedReviewsFlow(): Flow<List<ReviewEntity>>

    @Query("UPDATE reviews SET status = :status WHERE id = :id")
    suspend fun updateReviewStatus(id: Long, status: String)

    @Query("UPDATE worker_profiles SET rating = :rating, reviewCount = :count WHERE id = :workerId")
    suspend fun updateWorkerRating(workerId: Long, rating: Float, count: Int)

    // --- Kashmiri Contributions & Books ---
    @Query("SELECT * FROM kashmiri_contributions WHERE status = 'APPROVED' OR status = 'FEATURED' ORDER BY createdAt DESC")
    fun getApprovedContributionsFlow(): Flow<List<KashmiriContributionEntity>>

    @Query("SELECT * FROM kashmiri_contributions WHERE isBook = 1 AND (status = 'APPROVED' OR status = 'FEATURED') ORDER BY createdAt DESC")
    fun getApprovedBooksFlow(): Flow<List<KashmiriContributionEntity>>

    @Query("SELECT * FROM kashmiri_contributions WHERE isBook = 0 AND (status = 'APPROVED' OR status = 'FEATURED') ORDER BY createdAt DESC")
    fun getApprovedWritingsFlow(): Flow<List<KashmiriContributionEntity>>

    @Query("SELECT * FROM kashmiri_contributions ORDER BY createdAt DESC")
    fun getAllContributionsFlow(): Flow<List<KashmiriContributionEntity>>

    @Query("SELECT * FROM kashmiri_contributions WHERE id = :id LIMIT 1")
    suspend fun getContributionById(id: Long): KashmiriContributionEntity?

    @Query("SELECT * FROM kashmiri_contributions WHERE authorUserId = :authorId ORDER BY createdAt DESC")
    fun getContributionsByAuthorFlow(authorId: Long): Flow<List<KashmiriContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContribution(contribution: KashmiriContributionEntity): Long

    @Update
    suspend fun updateContribution(contribution: KashmiriContributionEntity)

    @Query("UPDATE kashmiri_contributions SET status = :status WHERE id = :id")
    suspend fun updateContributionStatus(id: Long, status: String)

    @Query("DELETE FROM kashmiri_contributions WHERE id = :id")
    suspend fun deleteContribution(id: Long)

    @Query("UPDATE kashmiri_contributions SET readsCount = readsCount + 1 WHERE id = :id")
    suspend fun incrementContributionReads(id: Long)

    @Query("UPDATE kashmiri_contributions SET purchaseCount = purchaseCount + 1 WHERE id = :id")
    suspend fun incrementBookPurchases(id: Long)

    // --- Book Purchases ---
    @Query("SELECT * FROM book_purchases WHERE buyerUserId = :userId ORDER BY purchaseDate DESC")
    fun getBookPurchasesByUserFlow(userId: Long): Flow<List<BookPurchaseEntity>>

    @Query("SELECT * FROM book_purchases WHERE buyerUserId = :userId AND bookId = :bookId LIMIT 1")
    suspend fun getBookPurchase(userId: Long, bookId: Long): BookPurchaseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBookPurchase(purchase: BookPurchaseEntity): Long

    // --- Shops & Businesses ---
    @Query("SELECT * FROM shops WHERE status = 'ACTIVE' ORDER BY isVerified DESC, rating DESC, totalOrdersCount DESC")
    fun getAllShopsFlow(): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE district = :district AND status = 'ACTIVE' ORDER BY isVerified DESC, rating DESC")
    fun getShopsByDistrictFlow(district: String): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE shopType = :type AND status = 'ACTIVE' ORDER BY isVerified DESC, rating DESC")
    fun getShopsByTypeFlow(type: String): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE ownerUserId = :ownerId ORDER BY createdAt DESC")
    fun getShopsByOwnerFlow(ownerId: Long): Flow<List<ShopEntity>>

    @Query("SELECT * FROM shops WHERE id = :id LIMIT 1")
    suspend fun getShopById(id: Long): ShopEntity?

    @Query("SELECT * FROM shops WHERE status = 'ACTIVE' ORDER BY totalOrdersCount DESC, totalRevenue DESC LIMIT 10")
    fun getTopSellersFlow(): Flow<List<ShopEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShop(shop: ShopEntity): Long

    @Update
    suspend fun updateShop(shop: ShopEntity)

    @Query("DELETE FROM shops WHERE id = :id")
    suspend fun deleteShop(id: Long)

    @Query("SELECT * FROM products WHERE shopId = :shopId AND status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getProductsByShopFlow(shopId: Long): Flow<List<ProductEntity>>

    // --- Reseller Listings ---
    @Query("SELECT * FROM reseller_listings WHERE status = 'ACTIVE' ORDER BY totalOrdersCount DESC, createdAt DESC")
    fun getAllResellerListingsFlow(): Flow<List<ResellerListingEntity>>

    @Query("SELECT * FROM reseller_listings WHERE resellerUserId = :userId ORDER BY createdAt DESC")
    fun getResellerListingsForUserFlow(userId: Long): Flow<List<ResellerListingEntity>>

    @Query("SELECT * FROM reseller_listings WHERE shareableCode = :code LIMIT 1")
    suspend fun getResellerListingByCode(code: String): ResellerListingEntity?

    @Query("SELECT * FROM reseller_listings WHERE id = :id LIMIT 1")
    suspend fun getResellerListingById(id: Long): ResellerListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResellerListing(listing: ResellerListingEntity): Long

    @Update
    suspend fun updateResellerListing(listing: ResellerListingEntity)

    @Query("SELECT * FROM reseller_listings ORDER BY totalOrdersCount DESC, totalEarnedMargin DESC LIMIT 10")
    fun getTopResellersFlow(): Flow<List<ResellerListingEntity>>

    // --- Orders ---
    @Query("SELECT * FROM orders ORDER BY orderedAt DESC")
    fun getAllOrdersFlow(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE customerId = :customerId ORDER BY orderedAt DESC")
    fun getOrdersForCustomerFlow(customerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE supplierUserId = :supplierId ORDER BY orderedAt DESC")
    fun getOrdersForSupplierFlow(supplierId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE shopId = :shopId ORDER BY orderedAt DESC")
    fun getOrdersForShopFlow(shopId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE resellerUserId = :resellerId ORDER BY orderedAt DESC")
    fun getOrdersForResellerFlow(resellerId: Long): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Long): OrderEntity?

    @Query("SELECT * FROM orders WHERE orderNumber = :orderNumber LIMIT 1")
    suspend fun getOrderByNumber(orderNumber: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity): Long

    @Update
    suspend fun updateOrder(order: OrderEntity)

    @Query("UPDATE orders SET orderStatus = :status, trackingNotes = :notes WHERE id = :orderId")
    suspend fun updateOrderStatus(orderId: Long, status: String, notes: String)

    // --- Shop Reviews ---
    @Query("SELECT * FROM shop_reviews WHERE shopId = :shopId ORDER BY createdAt DESC")
    fun getReviewsForShopFlow(shopId: Long): Flow<List<ShopReviewEntity>>

    @Query("SELECT COUNT(*) FROM shop_reviews WHERE shopId = :shopId AND customerUserId = :userId")
    suspend fun countReviewsByCustomerForShop(shopId: Long, userId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertShopReview(review: ShopReviewEntity): Long

    // --- Memberships ---
    @Query("SELECT * FROM membership_subscriptions ORDER BY startDate DESC")
    fun getAllMembershipsFlow(): Flow<List<MembershipSubscriptionEntity>>

    @Query("SELECT * FROM membership_subscriptions WHERE shopId = :shopId AND status = 'ACTIVE' LIMIT 1")
    suspend fun getActiveMembershipForShop(shopId: Long): MembershipSubscriptionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMembership(sub: MembershipSubscriptionEntity): Long

    @Update
    suspend fun updateMembership(sub: MembershipSubscriptionEntity)

    // --- Content Moderation & Reports ---
    @Query("SELECT * FROM content_reports ORDER BY createdAt DESC")
    fun getAllContentReportsFlow(): Flow<List<ContentReportEntity>>

    @Query("SELECT * FROM content_reports WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingContentReportsFlow(): Flow<List<ContentReportEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContentReport(report: ContentReportEntity): Long

    @Update
    suspend fun updateContentReport(report: ContentReportEntity)

    // --- User Blocks ---
    @Query("SELECT * FROM user_blocks WHERE blockerUserId = :userId")
    fun getUserBlocksFlow(userId: Long): Flow<List<UserBlockEntity>>

    @Query("SELECT COUNT(*) FROM user_blocks WHERE blockerUserId = :blockerId AND blockedUserId = :blockedId")
    suspend fun isUserBlocked(blockerId: Long, blockedId: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserBlock(block: UserBlockEntity): Long

    @Query("DELETE FROM user_blocks WHERE blockerUserId = :blockerId AND blockedUserId = :blockedId")
    suspend fun removeUserBlock(blockerId: Long, blockedId: Long)

    // --- Audit Logs ---
    @Query("SELECT * FROM admin_audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogsFlow(): Flow<List<AdminAuditLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AdminAuditLogEntity): Long

    // --- Property Listings ---
    @Query("SELECT * FROM property_listings WHERE status = 'ACTIVE' ORDER BY createdAt DESC")
    fun getActivePropertiesFlow(): Flow<List<PropertyListingEntity>>

    @Query("SELECT * FROM property_listings ORDER BY createdAt DESC")
    fun getAllPropertiesFlow(): Flow<List<PropertyListingEntity>>

    @Query("SELECT * FROM property_listings WHERE ownerUserId = :ownerId ORDER BY createdAt DESC")
    fun getPropertiesByOwnerFlow(ownerId: Long): Flow<List<PropertyListingEntity>>

    @Query("SELECT * FROM property_listings WHERE id = :id LIMIT 1")
    suspend fun getPropertyById(id: Long): PropertyListingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperty(property: PropertyListingEntity): Long

    @Update
    suspend fun updateProperty(property: PropertyListingEntity)

    @Query("DELETE FROM property_listings WHERE id = :id")
    suspend fun deleteProperty(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProperties(properties: List<PropertyListingEntity>)

    // --- Restaurants & Food Menu Items ---
    @Query("SELECT * FROM restaurants WHERE status = 'ACTIVE' ORDER BY rating DESC")
    fun getActiveRestaurantsFlow(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants ORDER BY createdAt DESC")
    fun getAllRestaurantsFlow(): Flow<List<RestaurantEntity>>

    @Query("SELECT * FROM restaurants WHERE id = :id LIMIT 1")
    suspend fun getRestaurantById(id: Long): RestaurantEntity?

    @Query("SELECT * FROM restaurants WHERE ownerUserId = :ownerId LIMIT 1")
    suspend fun getRestaurantByOwnerId(ownerId: Long): RestaurantEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurant(restaurant: RestaurantEntity): Long

    @Update
    suspend fun updateRestaurant(restaurant: RestaurantEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRestaurants(restaurants: List<RestaurantEntity>)

    @Query("SELECT * FROM food_menu_items WHERE restaurantId = :restaurantId AND isAvailable = 1 ORDER BY category, price")
    fun getMenuItemsForRestaurantFlow(restaurantId: Long): Flow<List<FoodMenuItemEntity>>

    @Query("SELECT * FROM food_menu_items WHERE restaurantId = :restaurantId")
    suspend fun getMenuItemsForRestaurantList(restaurantId: Long): List<FoodMenuItemEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(item: FoodMenuItemEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(items: List<FoodMenuItemEntity>)

    @Update
    suspend fun updateMenuItem(item: FoodMenuItemEntity)

    @Query("DELETE FROM food_menu_items WHERE id = :id")
    suspend fun deleteMenuItem(id: Long)

    // --- Kashmiri Word Contributions ---
    @Query("SELECT * FROM kashmiri_word_contributions WHERE status = 'APPROVED' ORDER BY createdAt DESC")
    fun getApprovedWordsFlow(): Flow<List<KashmiriWordContributionEntity>>

    @Query("SELECT * FROM kashmiri_word_contributions ORDER BY createdAt DESC")
    fun getAllWordsFlow(): Flow<List<KashmiriWordContributionEntity>>

    @Query("SELECT * FROM kashmiri_word_contributions WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingWordsFlow(): Flow<List<KashmiriWordContributionEntity>>

    @Query("SELECT * FROM kashmiri_word_contributions WHERE contributorUserId = :userId ORDER BY createdAt DESC")
    fun getWordsByContributorFlow(userId: Long): Flow<List<KashmiriWordContributionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordContribution(word: KashmiriWordContributionEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWordContributions(words: List<KashmiriWordContributionEntity>)

    @Update
    suspend fun updateWordContribution(word: KashmiriWordContributionEntity)

    @Query("SELECT COUNT(*) FROM kashmiri_word_contributions WHERE status = 'APPROVED'")
    fun getApprovedWordCountFlow(): Flow<Int>

    // --- Contributor Rewards ---
    @Query("SELECT * FROM contributor_rewards WHERE userId = :userId LIMIT 1")
    fun getRewardForUserFlow(userId: Long): Flow<ContributorRewardEntity?>

    @Query("SELECT * FROM contributor_rewards WHERE userId = :userId LIMIT 1")
    suspend fun getRewardForUser(userId: Long): ContributorRewardEntity?

    @Query("SELECT * FROM contributor_rewards ORDER BY totalApprovedWords DESC LIMIT 20")
    fun getLeaderboardFlow(): Flow<List<ContributorRewardEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdateReward(reward: ContributorRewardEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRewards(rewards: List<ContributorRewardEntity>)

    // --- Platform Settings ---
    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    fun getPlatformSettingsFlow(): Flow<PlatformSettingsEntity?>

    @Query("SELECT * FROM platform_settings WHERE id = 1 LIMIT 1")
    suspend fun getPlatformSettings(): PlatformSettingsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlatformSettings(settings: PlatformSettingsEntity): Long

    // --- Payout Requests ---
    @Query("SELECT * FROM payout_requests ORDER BY createdAt DESC")
    fun getAllPayoutRequestsFlow(): Flow<List<PayoutRequestEntity>>

    @Query("SELECT * FROM payout_requests WHERE status = 'PENDING' ORDER BY createdAt DESC")
    fun getPendingPayoutRequestsFlow(): Flow<List<PayoutRequestEntity>>

    @Query("SELECT * FROM payout_requests WHERE userId = :userId ORDER BY createdAt DESC")
    fun getPayoutRequestsForUserFlow(userId: Long): Flow<List<PayoutRequestEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPayoutRequest(request: PayoutRequestEntity): Long

    @Update
    suspend fun updatePayoutRequest(request: PayoutRequestEntity)

    // --- Central Transaction & Settlement Ledger ---
    @Query("SELECT * FROM transaction_ledger ORDER BY createdAt DESC")
    fun getAllLedgerEntriesFlow(): Flow<List<TransactionLedgerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntry(entry: TransactionLedgerEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLedgerEntries(entries: List<TransactionLedgerEntity>)

    @Update
    suspend fun updateLedgerEntry(entry: TransactionLedgerEntity)

    @Query("SELECT * FROM transaction_ledger WHERE id = :id LIMIT 1")
    suspend fun getLedgerEntryById(id: Long): TransactionLedgerEntity?

    @Query("SELECT SUM(platformCommission) FROM transaction_ledger WHERE commissionStatus != 'WAIVED'")
    fun getTotalCommissionEarnedFlow(): Flow<Double?>

    // --- Worker Bookings ---
    @Query("SELECT * FROM worker_bookings ORDER BY createdAt DESC")
    fun getAllWorkerBookingsFlow(): Flow<List<WorkerBookingEntity>>

    @Query("SELECT * FROM worker_bookings WHERE customerUserId = :userId ORDER BY createdAt DESC")
    fun getBookingsForCustomerFlow(userId: Long): Flow<List<WorkerBookingEntity>>

    @Query("SELECT * FROM worker_bookings WHERE workerUserId = :workerUserId ORDER BY createdAt DESC")
    fun getBookingsForWorkerFlow(workerUserId: Long): Flow<List<WorkerBookingEntity>>

    @Query("SELECT * FROM worker_bookings WHERE id = :id LIMIT 1")
    suspend fun getBookingById(id: Long): WorkerBookingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: WorkerBookingEntity): Long

    @Update
    suspend fun updateBooking(booking: WorkerBookingEntity)

    @Query("UPDATE worker_bookings SET status = :status WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: String)

    // ==========================================
    // DELIVERY ADDRESSES
    // ==========================================
    @Query("SELECT * FROM delivery_addresses WHERE userId = :userId ORDER BY isDefault DESC, createdAt DESC")
    fun getAddressesForUserFlow(userId: Long): Flow<List<DeliveryAddressEntity>>

    @Query("SELECT * FROM delivery_addresses WHERE id = :id LIMIT 1")
    suspend fun getAddressById(id: Long): DeliveryAddressEntity?

    @Query("SELECT * FROM delivery_addresses WHERE userId = :userId AND isDefault = 1 LIMIT 1")
    suspend fun getDefaultAddressForUser(userId: Long): DeliveryAddressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: DeliveryAddressEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddresses(addresses: List<DeliveryAddressEntity>)

    @Update
    suspend fun updateAddress(address: DeliveryAddressEntity)

    @Query("UPDATE delivery_addresses SET isDefault = 0 WHERE userId = :userId")
    suspend fun clearDefaultAddresses(userId: Long)

    @Query("DELETE FROM delivery_addresses WHERE id = :id")
    suspend fun deleteAddress(id: Long)

    // ==========================================
    // LEGAL POLICIES & TERMS CMS
    // ==========================================
    @Query("SELECT * FROM legal_policies WHERE isPublished = 1 ORDER BY category ASC, title ASC")
    fun getAllPoliciesFlow(): Flow<List<LegalPolicyEntity>>

    @Query("SELECT * FROM legal_policies WHERE policyKey = :key LIMIT 1")
    suspend fun getPolicyByKey(key: String): LegalPolicyEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdatePolicy(policy: LegalPolicyEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPolicies(policies: List<LegalPolicyEntity>)

    // ==========================================
    // DEMO DATA SEPARATION & PRODUCTION PURGE
    // ==========================================
    @Query("DELETE FROM products WHERE isDemo = 1")
    suspend fun deleteDemoProducts()

    @Query("DELETE FROM jobs WHERE isDemo = 1")
    suspend fun deleteDemoJobs()

    @Query("DELETE FROM property_listings WHERE isDemo = 1")
    suspend fun deleteDemoProperties()

    @Query("DELETE FROM food_menu_items WHERE isDemo = 1")
    suspend fun deleteDemoFoodItems()

    @Query("DELETE FROM restaurants WHERE isDemo = 1")
    suspend fun deleteDemoRestaurants()

    @Query("DELETE FROM orders WHERE isDemo = 1")
    suspend fun deleteDemoOrders()

    @Query("DELETE FROM transaction_ledger WHERE isDemo = 1")
    suspend fun deleteDemoLedgerEntries()

    @Query("DELETE FROM delivery_addresses WHERE isDemo = 1")
    suspend fun deleteDemoAddresses()

    @Query("SELECT COUNT(*) FROM products WHERE isDemo = 1")
    suspend fun countDemoProducts(): Int

    @Query("SELECT COUNT(*) FROM jobs WHERE isDemo = 1")
    suspend fun countDemoJobs(): Int
}
