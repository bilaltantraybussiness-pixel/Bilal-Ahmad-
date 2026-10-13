package com.example.data.repository

import com.example.data.local.AppDao
import com.example.data.model.*
import kotlinx.coroutines.flow.Flow
import java.security.MessageDigest

class AppRepository(private val dao: AppDao) {

    companion object {
        fun hashPassword(password: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
            val hash = digest.digest(password.toByteArray(Charsets.UTF_8))
            return hash.joinToString("") { "%02x".format(it) }
        }
    }

    // --- Users & Auth ---
    suspend fun getUserByPhone(phone: String): UserEntity? = dao.getUserByPhone(phone)
    suspend fun getUserByEmail(email: String): UserEntity? = dao.getUserByEmail(email)
    suspend fun getUserByIdentifier(identifier: String): UserEntity? = dao.getUserByIdentifier(identifier)
    suspend fun getUserById(id: Long): UserEntity? = dao.getUserById(id)
    fun getAllUsersFlow(): Flow<List<UserEntity>> = dao.getAllUsersFlow()
    suspend fun insertUser(user: UserEntity): Long = dao.insertUser(user)
    suspend fun updateUser(user: UserEntity) = dao.updateUser(user)
    suspend fun setUserBanned(id: Long, banned: Boolean) = dao.setUserBanned(id, banned)
    suspend fun setUserVerified(id: Long, verified: Boolean) = dao.setUserVerified(id, verified)
    suspend fun deleteUser(id: Long) = dao.deleteUser(id)
    fun getUserCountFlow(): Flow<Int> = dao.getUserCountFlow()

    // --- Worker Profiles ---
    fun getAllWorkersFlow(): Flow<List<WorkerProfileEntity>> = dao.getAllWorkersFlow()
    fun getSeasonalWorkersFlow(): Flow<List<WorkerProfileEntity>> = dao.getSeasonalWorkersFlow()
    suspend fun getWorkerProfileByUserId(userId: Long): WorkerProfileEntity? = dao.getWorkerProfileByUserId(userId)
    suspend fun getWorkerProfileById(id: Long): WorkerProfileEntity? = dao.getWorkerProfileById(id)
    suspend fun insertWorkerProfile(profile: WorkerProfileEntity): Long = dao.insertWorkerProfile(profile)
    suspend fun updateWorkerProfile(profile: WorkerProfileEntity) = dao.updateWorkerProfile(profile)
    suspend fun deleteWorkerProfile(id: Long) = dao.deleteWorkerProfile(id)
    fun getWorkerCountFlow(): Flow<Int> = dao.getWorkerCountFlow()

    val jobRepository: JobRepository = RoomFirestoreJobRepository(dao)

    // --- Jobs ---
    fun getActiveJobsFlow(): Flow<List<JobEntity>> = jobRepository.getActiveJobsStream()
    fun getAllJobsFlow(): Flow<List<JobEntity>> = jobRepository.getAllJobsStream()
    fun getSeasonalJobsFlow(): Flow<List<JobEntity>> = jobRepository.getSeasonalJobsStream()
    fun getJobsByEmployerFlow(employerId: Long): Flow<List<JobEntity>> = jobRepository.getJobsByEmployerStream(employerId)
    suspend fun getJobById(id: Long): JobEntity? = jobRepository.getJobById(id)
    suspend fun insertJob(job: JobEntity): Long = jobRepository.postJob(job)
    suspend fun updateJob(job: JobEntity) = jobRepository.updateJob(job)
    suspend fun setJobStatus(id: Long, status: String) = jobRepository.setJobStatus(id, status)
    suspend fun deleteJob(id: Long) = jobRepository.deleteJob(id)
    suspend fun syncJobsWithFirestore(): Result<Int> = jobRepository.syncWithFirestore()
    fun getActiveJobCountFlow(): Flow<Int> = dao.getActiveJobCountFlow()
    fun getTotalJobCountFlow(): Flow<Int> = dao.getTotalJobCountFlow()

    // --- Job Applications ---
    fun getApplicationsForJobFlow(jobId: Long): Flow<List<JobApplicationEntity>> = dao.getApplicationsForJobFlow(jobId)
    fun getApplicationsForUserFlow(userId: Long): Flow<List<JobApplicationEntity>> = dao.getApplicationsForUserFlow(userId)
    fun getApplicationsForEmployerFlow(employerId: Long): Flow<List<JobApplicationEntity>> = dao.getApplicationsForEmployerFlow(employerId)
    fun getAllApplicationsFlow(): Flow<List<JobApplicationEntity>> = dao.getAllApplicationsFlow()
    suspend fun getExistingApplication(jobId: Long, userId: Long): JobApplicationEntity? = dao.getExistingApplication(jobId, userId)
    suspend fun insertApplication(application: JobApplicationEntity): Long = dao.insertApplication(application)
    suspend fun updateApplicationStatus(id: Long, status: String) = dao.updateApplicationStatus(id, status)
    fun getTotalApplicationsCountFlow(): Flow<Int> = dao.getTotalApplicationsCountFlow()

    // --- Products ---
    fun getActiveProductsFlow(): Flow<List<ProductEntity>> = dao.getActiveProductsFlow()
    fun getAllProductsFlow(): Flow<List<ProductEntity>> = dao.getAllProductsFlow()
    fun getSecondHandProductsFlow(): Flow<List<ProductEntity>> = dao.getSecondHandProductsFlow()
    fun getProductsBySellerFlow(sellerId: Long): Flow<List<ProductEntity>> = dao.getProductsBySellerFlow(sellerId)
    suspend fun getProductById(id: Long): ProductEntity? = dao.getProductById(id)
    suspend fun insertProduct(product: ProductEntity): Long = dao.insertProduct(product)
    suspend fun updateProduct(product: ProductEntity) = dao.updateProduct(product)
    suspend fun updateProductStock(productId: Long, newStock: Int) = dao.updateProductStock(productId, newStock)
    suspend fun setProductStatus(id: Long, status: String) = dao.setProductStatus(id, status)
    suspend fun deleteProduct(id: Long) = dao.deleteProduct(id)
    fun getActiveProductCountFlow(): Flow<Int> = dao.getActiveProductCountFlow()

    // --- Conversations & Messages ---
    fun getConversationsForUserFlow(userId: Long): Flow<List<ConversationEntity>> = dao.getConversationsForUserFlow(userId)
    suspend fun getConversationById(id: Long): ConversationEntity? = dao.getConversationById(id)
    suspend fun findConversationBetween(u1: Long, u2: Long): ConversationEntity? = dao.findConversationBetween(u1, u2)
    suspend fun insertConversation(conv: ConversationEntity): Long = dao.insertConversation(conv)
    suspend fun updateConversation(conv: ConversationEntity) = dao.updateConversation(conv)
    fun getMessagesForConversationFlow(convId: Long): Flow<List<MessageEntity>> = dao.getMessagesForConversationFlow(convId)
    suspend fun insertMessage(msg: MessageEntity): Long = dao.insertMessage(msg)
    suspend fun markMessagesAsRead(convId: Long, userId: Long) = dao.markMessagesAsRead(convId, userId)
    fun getTotalConversationsCountFlow(): Flow<Int> = dao.getTotalConversationsCountFlow()

    // --- Reports ---
    fun getAllReportsFlow(): Flow<List<ReportEntity>> = dao.getAllReportsFlow()
    suspend fun insertReport(report: ReportEntity): Long = dao.insertReport(report)
    suspend fun updateReportStatus(id: Long, status: String, adminNotes: String) = dao.updateReportStatus(id, status, adminNotes)
    fun getPendingReportsCountFlow(): Flow<Int> = dao.getPendingReportsCountFlow()

    // --- Notifications ---
    fun getNotificationsForUserFlow(userId: Long): Flow<List<NotificationEntity>> = dao.getNotificationsForUserFlow(userId)
    fun getUnreadNotificationsCountFlow(userId: Long): Flow<Int> = dao.getUnreadNotificationsCountFlow(userId)
    suspend fun insertNotification(notification: NotificationEntity): Long = dao.insertNotification(notification)
    suspend fun markAllNotificationsRead(userId: Long) = dao.markAllNotificationsRead(userId)

    // --- Donations ---
    fun getAllDonationsFlow(): Flow<List<DonationEntity>> = dao.getAllDonationsFlow()
    fun getDonationsByUserFlow(userId: Long): Flow<List<DonationEntity>> = dao.getDonationsByUserFlow(userId)
    suspend fun insertDonation(donation: DonationEntity): Long = dao.insertDonation(donation)
    suspend fun updateDonationStatus(id: Long, status: String) = dao.updateDonationStatus(id, status)
    fun getTotalDonationsCountFlow(): Flow<Int> = dao.getTotalDonationsCountFlow()

    // --- Login Activity ---
    fun getAllLoginActivitiesFlow(): Flow<List<LoginActivityEntity>> = dao.getAllLoginActivitiesFlow()
    suspend fun insertLoginActivity(activity: LoginActivityEntity): Long = dao.insertLoginActivity(activity)

    // --- Saved Items ---
    fun getSavedItemsForUserFlow(userId: Long): Flow<List<SavedItemEntity>> = dao.getSavedItemsForUserFlow(userId)
    suspend fun isItemSaved(userId: Long, itemType: String, itemId: Long): Boolean {
        return dao.getSavedItem(userId, itemType, itemId) != null
    }
    suspend fun toggleSaveItem(userId: Long, itemType: String, itemId: Long, title: String, subtitle: String): Boolean {
        val existing = dao.getSavedItem(userId, itemType, itemId)
        return if (existing != null) {
            dao.deleteSavedItem(userId, itemType, itemId)
            false
        } else {
            dao.insertSavedItem(SavedItemEntity(userId = userId, itemType = itemType, itemId = itemId, title = title, subtitle = subtitle))
            true
        }
    }

    // --- Verification Requests ---
    fun getAllVerificationRequestsFlow(): Flow<List<VerificationRequestEntity>> = dao.getAllVerificationRequestsFlow()
    fun getLatestVerificationRequestForUserFlow(userId: Long): Flow<VerificationRequestEntity?> = dao.getLatestVerificationRequestForUserFlow(userId)
    suspend fun insertVerificationRequest(request: VerificationRequestEntity): Long = dao.insertVerificationRequest(request)
    suspend fun approveVerification(requestId: Long, targetUserId: Long, notes: String) {
        dao.updateVerificationRequestStatus(requestId, "APPROVED", notes)
        dao.setUserVerified(targetUserId, true)
        dao.setWorkerProfileVerified(targetUserId, true)
    }
    suspend fun rejectVerification(requestId: Long, notes: String) {
        dao.updateVerificationRequestStatus(requestId, "REJECTED", notes)
    }

    // --- Reviews ---
    fun getReviewsForTargetFlow(targetType: String, targetId: Long): Flow<List<ReviewEntity>> = dao.getReviewsForTargetFlow(targetType, targetId)
    suspend fun submitReview(review: ReviewEntity): Result<Long> {
        val existing = dao.getExistingReview(review.authorUserId, review.targetType, review.targetId)
        if (existing != null) {
            return Result.failure(IllegalStateException("You have already reviewed this service/listing."))
        }
        val id = dao.insertReview(review)
        if (review.targetType == "WORKER") {
            val worker = dao.getWorkerProfileById(review.targetId)
            if (worker != null) {
                val newCount = worker.reviewCount + 1
                val newRating = ((worker.rating * worker.reviewCount) + review.rating) / newCount
                dao.updateWorkerRating(worker.id, (newRating * 10).toInt() / 10.0f, newCount)
            }
        }
        return Result.success(id)
    }
    suspend fun reportReview(id: Long, reason: String) = dao.reportReview(id, reason)
    fun getReportedReviewsFlow(): Flow<List<ReviewEntity>> = dao.getReportedReviewsFlow()
    suspend fun updateReviewStatus(id: Long, status: String) = dao.updateReviewStatus(id, status)

    // --- Kashmiri Contributions & Books ---
    fun getApprovedContributionsFlow(): Flow<List<KashmiriContributionEntity>> = dao.getApprovedContributionsFlow()
    fun getApprovedBooksFlow(): Flow<List<KashmiriContributionEntity>> = dao.getApprovedBooksFlow()
    fun getApprovedWritingsFlow(): Flow<List<KashmiriContributionEntity>> = dao.getApprovedWritingsFlow()
    fun getAllContributionsFlow(): Flow<List<KashmiriContributionEntity>> = dao.getAllContributionsFlow()
    suspend fun insertContribution(item: KashmiriContributionEntity): Long = dao.insertContribution(item)
    suspend fun updateContributionStatus(id: Long, status: String) = dao.updateContributionStatus(id, status)
    suspend fun deleteContribution(id: Long) = dao.deleteContribution(id)
    suspend fun incrementContributionReads(id: Long) = dao.incrementContributionReads(id)
    fun getBookPurchasesByUserFlow(userId: Long): Flow<List<BookPurchaseEntity>> = dao.getBookPurchasesByUserFlow(userId)
    suspend fun purchaseBook(purchase: BookPurchaseEntity): Long {
        val id = dao.insertBookPurchase(purchase)
        dao.incrementBookPurchases(purchase.bookId)
        return id
    }

    // --- Shops & Businesses ---
    fun getAllShopsFlow(): Flow<List<ShopEntity>> = dao.getAllShopsFlow()
    fun getShopsByDistrictFlow(district: String): Flow<List<ShopEntity>> = dao.getShopsByDistrictFlow(district)
    fun getShopsByTypeFlow(type: String): Flow<List<ShopEntity>> = dao.getShopsByTypeFlow(type)
    fun getShopsByOwnerFlow(ownerId: Long): Flow<List<ShopEntity>> = dao.getShopsByOwnerFlow(ownerId)
    suspend fun getShopById(id: Long): ShopEntity? = dao.getShopById(id)
    fun getTopSellersFlow(): Flow<List<ShopEntity>> = dao.getTopSellersFlow()
    suspend fun insertShop(shop: ShopEntity): Long = dao.insertShop(shop)
    suspend fun updateShop(shop: ShopEntity) = dao.updateShop(shop)
    suspend fun deleteShop(id: Long) = dao.deleteShop(id)
    fun getProductsByShopFlow(shopId: Long): Flow<List<ProductEntity>> = dao.getProductsByShopFlow(shopId)

    // --- Reseller Listings ---
    fun getAllResellerListingsFlow(): Flow<List<ResellerListingEntity>> = dao.getAllResellerListingsFlow()
    fun getResellerListingsForUserFlow(userId: Long): Flow<List<ResellerListingEntity>> = dao.getResellerListingsForUserFlow(userId)
    suspend fun getResellerListingByCode(code: String): ResellerListingEntity? = dao.getResellerListingByCode(code)
    suspend fun getResellerListingById(id: Long): ResellerListingEntity? = dao.getResellerListingById(id)
    suspend fun insertResellerListing(listing: ResellerListingEntity): Long = dao.insertResellerListing(listing)
    suspend fun updateResellerListing(listing: ResellerListingEntity) = dao.updateResellerListing(listing)
    fun getTopResellersFlow(): Flow<List<ResellerListingEntity>> = dao.getTopResellersFlow()

    // --- Orders & Fulfillment ---
    fun getAllOrdersFlow(): Flow<List<OrderEntity>> = dao.getAllOrdersFlow()
    fun getOrdersForCustomerFlow(customerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForCustomerFlow(customerId)
    fun getOrdersForSupplierFlow(supplierId: Long): Flow<List<OrderEntity>> = dao.getOrdersForSupplierFlow(supplierId)
    fun getOrdersForShopFlow(shopId: Long): Flow<List<OrderEntity>> = dao.getOrdersForShopFlow(shopId)
    fun getOrdersForResellerFlow(resellerId: Long): Flow<List<OrderEntity>> = dao.getOrdersForResellerFlow(resellerId)
    suspend fun getOrderById(id: Long): OrderEntity? = dao.getOrderById(id)
    suspend fun getOrderByNumber(orderNumber: String): OrderEntity? = dao.getOrderByNumber(orderNumber)
    suspend fun insertOrder(order: OrderEntity): Long = dao.insertOrder(order)
    suspend fun updateOrder(order: OrderEntity) = dao.updateOrder(order)
    suspend fun updateOrderStatus(orderId: Long, status: String, notes: String) = dao.updateOrderStatus(orderId, status, notes)

    // --- Shop Reviews ---
    fun getReviewsForShopFlow(shopId: Long): Flow<List<ShopReviewEntity>> = dao.getReviewsForShopFlow(shopId)
    suspend fun countReviewsByCustomerForShop(shopId: Long, userId: Long): Int = dao.countReviewsByCustomerForShop(shopId, userId)
    suspend fun insertShopReview(review: ShopReviewEntity): Long = dao.insertShopReview(review)

    // --- Memberships ---
    fun getAllMembershipsFlow(): Flow<List<MembershipSubscriptionEntity>> = dao.getAllMembershipsFlow()
    suspend fun getActiveMembershipForShop(shopId: Long): MembershipSubscriptionEntity? = dao.getActiveMembershipForShop(shopId)
    suspend fun insertMembership(sub: MembershipSubscriptionEntity): Long = dao.insertMembership(sub)
    suspend fun updateMembership(sub: MembershipSubscriptionEntity) = dao.updateMembership(sub)

    // --- Content Moderation & Reports ---
    fun getAllContentReportsFlow(): Flow<List<ContentReportEntity>> = dao.getAllContentReportsFlow()
    fun getPendingContentReportsFlow(): Flow<List<ContentReportEntity>> = dao.getPendingContentReportsFlow()
    suspend fun insertContentReport(report: ContentReportEntity): Long = dao.insertContentReport(report)
    suspend fun updateContentReport(report: ContentReportEntity) = dao.updateContentReport(report)

    // --- User Blocks ---
    fun getUserBlocksFlow(userId: Long): Flow<List<UserBlockEntity>> = dao.getUserBlocksFlow(userId)
    suspend fun isUserBlocked(blockerId: Long, blockedId: Long): Boolean = dao.isUserBlocked(blockerId, blockedId) > 0
    suspend fun blockUser(blockerId: Long, blockedId: Long, reason: String = ""): Long = dao.insertUserBlock(UserBlockEntity(blockerUserId = blockerId, blockedUserId = blockedId, reason = reason))
    suspend fun unblockUser(blockerId: Long, blockedId: Long) = dao.removeUserBlock(blockerId, blockedId)

    // --- Admin Audit Logs ---
    fun getAllAuditLogsFlow(): Flow<List<AdminAuditLogEntity>> = dao.getAllAuditLogsFlow()
    suspend fun insertAuditLog(log: AdminAuditLogEntity): Long = dao.insertAuditLog(log)

    // --- Worker Bookings ---
    fun getAllWorkerBookingsFlow(): Flow<List<WorkerBookingEntity>> = dao.getAllWorkerBookingsFlow()
    fun getBookingsForCustomerFlow(userId: Long): Flow<List<WorkerBookingEntity>> = dao.getBookingsForCustomerFlow(userId)
    fun getBookingsForWorkerFlow(workerUserId: Long): Flow<List<WorkerBookingEntity>> = dao.getBookingsForWorkerFlow(workerUserId)
    suspend fun getBookingById(id: Long): WorkerBookingEntity? = dao.getBookingById(id)
    suspend fun insertBooking(booking: WorkerBookingEntity): Long = dao.insertBooking(booking)
    suspend fun updateBooking(booking: WorkerBookingEntity) = dao.updateBooking(booking)
    suspend fun updateBookingStatus(id: Long, status: String) = dao.updateBookingStatus(id, status)

    // --- Database Seeding ---
    suspend fun seedDatabaseIfEmpty() {
        val existingAdmin = dao.getUserByPhone("9906000000")
        if (existingAdmin != null) return

        // 1. Create Admin & Sample Users
        val adminId = dao.insertUser(
            UserEntity(
                fullName = "Admin Rozgar Kashmir",
                phoneNumber = "9906000000",
                passwordHash = hashPassword("Admin@123"),
                district = "Srinagar",
                area = "Lal Chowk",
                email = "admin@rozgarkashmir.in",
                roles = "ADMIN,WORKER,EMPLOYER,SELLER,BUYER",
                isActive = true,
                isVerified = true
            )
        )

        dao.insertUser(
            UserEntity(
                fullName = "Bilal Tantray (Owner)",
                phoneNumber = "9419099999",
                passwordHash = hashPassword("Admin@123"),
                district = "Srinagar",
                area = "Lal Chowk",
                email = "bilaltantraypersonal@gmail.com",
                roles = "ADMIN,WORKER,EMPLOYER,SELLER,BUYER",
                isActive = true,
                isVerified = true
            )
        )

        val user1Id = dao.insertUser(
            UserEntity(
                fullName = "Ghulam Mohammad Rather",
                phoneNumber = "9419011111",
                passwordHash = hashPassword("worker123"),
                district = "Srinagar",
                area = "Downtown / Safa Kadal",
                roles = "WORKER,SELLER",
                isVerified = true
            )
        )

        val user2Id = dao.insertUser(
            UserEntity(
                fullName = "Mohammad Shafi Dar",
                phoneNumber = "9797022222",
                passwordHash = hashPassword("worker123"),
                district = "Baramulla",
                area = "Sopore Fruit Mandi",
                roles = "WORKER,EMPLOYER",
                isVerified = true
            )
        )

        val user3Id = dao.insertUser(
            UserEntity(
                fullName = "Parveena Jan",
                phoneNumber = "9622033333",
                passwordHash = hashPassword("worker123"),
                district = "Budgam",
                area = "Kanihama",
                roles = "WORKER,SELLER",
                isVerified = true
            )
        )

        val user4Id = dao.insertUser(
            UserEntity(
                fullName = "Dr. Adil Hussain",
                phoneNumber = "9596044444",
                passwordHash = hashPassword("worker123"),
                district = "Anantnag",
                area = "KP Road",
                roles = "WORKER",
                isVerified = true
            )
        )

        val user5Id = dao.insertUser(
            UserEntity(
                fullName = "Shabir Ahmad Mir",
                phoneNumber = "9858055555",
                passwordHash = hashPassword("worker123"),
                district = "Pulwama",
                area = "Rajpora",
                roles = "WORKER",
                isVerified = true
            )
        )

        val user6Id = dao.insertUser(
            UserEntity(
                fullName = "Bashir Ahmad Wani",
                phoneNumber = "9906066666",
                passwordHash = hashPassword("employer123"),
                district = "Shopian",
                area = "Zainapora Apple Valley",
                roles = "EMPLOYER,SELLER",
                isVerified = true
            )
        )

        val user7Id = dao.insertUser(
            UserEntity(
                fullName = "Bilal Ahmad Lone",
                phoneNumber = "9419077777",
                passwordHash = hashPassword("worker123"),
                district = "Srinagar",
                area = "Rajbagh",
                roles = "WORKER,BUYER",
                isVerified = true
            )
        )

        // 2. Insert Worker Profiles
        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user1Id,
                name = "Ghulam Mohammad Rather",
                district = "Srinagar",
                area = "Downtown / Safa Kadal",
                profession = "Walnut Wood Carver & Artisan",
                occupation = "Heritage Craftsman",
                skills = "Walnut Carving, Traditional Chinar Motifs, Furniture Restoring, Antique Finishing",
                education = "", // Left blank deliberately: Education is NOT mandatory!
                experienceYears = 28,
                languages = "Kashmiri, Urdu",
                bio = "Third-generation master artisan dedicated to traditional Kashmiri walnut woodcraft. Specializing in intricate lattice work and royal heritage furniture.",
                previousWork = "Carved royal dining sets, decorative jewelry boxes, houseboats interior paneling in Dal Lake.",
                services = "Custom walnut furniture, panel carving, souvenir carving, wood restoration",
                availability = "Available Now",
                workType = "Contract",
                expectedPayment = "₹1,200/day",
                isSeasonalWorker = false,
                rating = 4.9f,
                reviewCount = 34
            )
        )

        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user2Id,
                name = "Mohammad Shafi Dar",
                district = "Baramulla",
                area = "Sopore",
                profession = "Apple Orchardist & Harvest Specialist",
                occupation = "Agricultural Expert",
                skills = "Apple Picking, Tree Pruning, Fruit Grading, Packing & Crating, Orchard Pesticide Spraying",
                education = "Middle School",
                experienceYears = 16,
                languages = "Kashmiri, Urdu",
                bio = "Expert in high-density and traditional apple orchards. Crew lead for seasonal picking operations across North Kashmir.",
                previousWork = "Managed 50-kanal apple orchards in Sopore and Zaingeer belt during peak seasons.",
                services = "Apple harvesting, winter pruning, sorting & grading, packing crew management",
                availability = "Seasonal Worker",
                workType = "Seasonal",
                expectedPayment = "₹900/day",
                isSeasonalWorker = true,
                seasonalSpecialties = "Apple Picking, Orchard Pruning, Fruit Packing, Harvesting",
                rating = 4.8f,
                reviewCount = 28
            )
        )

        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user3Id,
                name = "Parveena Jan",
                district = "Budgam",
                area = "Kanihama",
                profession = "Sozni & Tilla Embroidery Artisan",
                occupation = "Handicraft Master",
                skills = "Fine Sozni Needlework, Gold Tilla Stitching, Pashmina Shawl Bordering, Bridal Phiran Design",
                education = "",
                experienceYears = 14,
                languages = "Kashmiri, Urdu",
                bio = "Master artisan known for delicate needlework on pure Pashmina and Cashmere fabrics. Recipient of State Handicrafts Recognition.",
                previousWork = "Delivered custom bridal tilla sets for over 60 families and international artisan fairs.",
                services = "Custom bridal phiran embroidery, shawl needlework, restore heirloom fabrics",
                availability = "Available Now",
                workType = "Freelance",
                expectedPayment = "₹800/day",
                isSeasonalWorker = false,
                rating = 5.0f,
                reviewCount = 42
            )
        )

        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user4Id,
                name = "Dr. Adil Hussain",
                district = "Anantnag",
                area = "KP Road",
                profession = "General Physician & Home Care Doctor",
                occupation = "Medical Professional",
                skills = "Home Medical Visits, Elderly Care, Hypertension Management, Diabetes Care, Emergency First Aid",
                education = "MBBS",
                experienceYears = 9,
                languages = "Kashmiri, Urdu, English",
                bio = "Compassionate physician providing clinic consultations and scheduled home visits for elderly and mobility-impaired patients in South Kashmir.",
                previousWork = "Former resident medical officer at District Hospital Anantnag, active community health volunteer.",
                services = "Home checkups, chronic illness monitoring, post-op consultation, routine health screenings",
                availability = "Available From Date",
                workType = "Part-time",
                expectedPayment = "₹500/visit",
                isSeasonalWorker = false,
                rating = 4.9f,
                reviewCount = 57
            )
        )

        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user5Id,
                name = "Shabir Ahmad Mir",
                district = "Pulwama",
                area = "Rajpora",
                profession = "Senior Electrician & Solar Technician",
                occupation = "Technical Services",
                skills = "House Wiring, Solar Panel Installation, Inverter Repair, Three-Phase Industrial Power, Fault Finding",
                education = "ITI Diploma",
                experienceYears = 11,
                languages = "Kashmiri, Urdu, Hindi",
                bio = "Certified electrician handling complete domestic and commercial electrical fittings, solar setups for winter backup.",
                previousWork = "Wired over 45 new residential homes, installed rooftop solar systems in Pulwama and Shopian.",
                services = "Residential wiring, inverter battery installation, solar setup, electrical troubleshooting",
                availability = "Looking for Daily Work",
                workType = "Daily wage",
                expectedPayment = "₹950/day",
                isSeasonalWorker = false,
                rating = 4.7f,
                reviewCount = 19
            )
        )

        dao.insertWorkerProfile(
            WorkerProfileEntity(
                userId = user7Id,
                name = "Bilal Ahmad Lone",
                district = "Srinagar",
                area = "Rajbagh",
                profession = "Software Developer & Mobile App Engineer",
                occupation = "Technology Professional",
                skills = "Android Jetpack Compose, Kotlin, Web Development, E-Commerce Setup, Local SEO, API Integration",
                education = "B.Tech Computer Science",
                experienceYears = 5,
                languages = "Kashmiri, Urdu, English",
                bio = "Experienced Kashmiri developer building modern tech solutions for local businesses, handicrafts cooperatives, and logistics startups.",
                previousWork = "Built inventory and billing tools for 12 local handicraft showrooms and fruit traders.",
                services = "Mobile app creation, custom business websites, digital billing setups, tech consulting",
                availability = "Available Now",
                workType = "Project-based",
                expectedPayment = "₹35,000/month",
                isSeasonalWorker = false,
                rating = 4.9f,
                reviewCount = 15
            )
        )

        // 3. Insert Jobs
        dao.insertJob(
            JobEntity(
                employerUserId = user6Id,
                employerName = "Wani Apple Estates",
                title = "Urgent: 10 Apple Pickers & Box Packers Needed",
                profession = "Apple Pickers & Orchard Workers",
                description = "We require 10 energetic workers for our 35-kanal apple orchard in Zainapora, Shopian. Work includes tree picking, sorting Delicious & Kullu varieties, and crating into boxes. Morning tea and afternoon lunch provided on-site.",
                requiredSkills = "Apple Picking, Careful Handling of Fruit, Grading, Box Stacking",
                workersNeeded = 10,
                district = "Shopian",
                area = "Zainapora Apple Belt",
                workLocation = "On-site Orchard",
                workType = "Seasonal",
                startDate = "Immediate",
                endDate = "15 Days Duration",
                hours = "7:30 AM - 5:00 PM",
                paymentAmount = "₹950",
                paymentPeriod = "Per Day",
                experienceRequired = "Prior picking experience preferred",
                requirements = "Punctual, physically fit, careful with apple stalks",
                isSeasonal = true,
                seasonalCategory = "Apple Picking",
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user1Id,
                employerName = "Heritage Woodcrafts Srinagar",
                title = "Walnut Wood Finishers & Sanding Helpers",
                profession = "Carpenters & Wood Polishers",
                description = "Looking for 2 dedicated wood polishers and sanding helpers for a heritage bedroom set export order. Long-term work available for skilled hands.",
                requiredSkills = "Wood Sanding, French Polish, Spirit Lacquer, Careful handling",
                workersNeeded = 2,
                district = "Srinagar",
                area = "Nawa Kadal",
                workLocation = "Workshop",
                workType = "Daily wage",
                startDate = "Tomorrow",
                hours = "9:00 AM - 6:00 PM",
                paymentAmount = "₹850",
                paymentPeriod = "Per Day",
                experienceRequired = "1+ years",
                requirements = "Must understand grain finishing",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user2Id,
                employerName = "Highland Fruit Logistics",
                title = "Fruit Grading & Weight Scale Supervisor",
                profession = "Warehouse & Mandi Workers",
                description = "Immediate opening at Sopore Fruit Mandi for fruit grading supervisor to verify crate weights, check packing quality, and document vehicle dispatches.",
                requiredSkills = "Basic Math, Apple Varieties Knowledge, Dispatch Recording",
                workersNeeded = 1,
                district = "Baramulla",
                area = "Sopore Mandi",
                workLocation = "Mandi Yard",
                workType = "Temporary",
                startDate = "This Week",
                hours = "6:00 AM - 3:00 PM",
                paymentAmount = "₹22,000",
                paymentPeriod = "Per Month",
                experienceRequired = "2 years in Mandi",
                requirements = "Honest, alert, Kashmiri & Urdu fluency",
                isSeasonal = true,
                seasonalCategory = "Fruit Packing",
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user4Id,
                employerName = "Al-Madina Clinic",
                title = "Female Nurse / Clinic Assistant",
                profession = "Nurses & Medical Assistants",
                description = "Day clinic in Anantnag town seeking a certified nurse for vitals check, wound dressing, patient triage, and medication recording.",
                requiredSkills = "BP Check, Blood Sugar Monitoring, Injections, Patient Care",
                workersNeeded = 1,
                district = "Anantnag",
                area = "Khanabal",
                workLocation = "Clinic",
                workType = "Full-time",
                startDate = "1st of next month",
                hours = "10:00 AM - 5:00 PM",
                paymentAmount = "₹18,000",
                paymentPeriod = "Per Month",
                experienceRequired = "Nursing diploma or GNM",
                requirements = "Empathetic, pleasant patient communication",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = adminId,
                employerName = "Gulmarg Adventure Resort",
                title = "Skiing Equipment Helper & Snow Clearing Staff",
                profession = "Tourism & Snow Removal",
                description = "Seasonal staff wanted for winter season in Gulmarg. Assisting tourists with rental gear and clearing hotel approach pathways during snowfall.",
                requiredSkills = "Snow Shoveling, Outdoor Work, Courteous Demeanor",
                workersNeeded = 4,
                district = "Baramulla",
                area = "Gulmarg",
                workLocation = "Resort Area",
                workType = "Seasonal",
                startDate = "November 1st",
                hours = "8:00 AM - 5:00 PM (Rotating)",
                paymentAmount = "₹1,100",
                paymentPeriod = "Per Day",
                experienceRequired = "No prior experience required",
                requirements = "Warm clothing, winter-ready",
                isSeasonal = true,
                seasonalCategory = "Snow Removal",
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user1Id,
                employerName = "Chinar Accounts & Tax Consultancy",
                title = "Accounts Executive & Office Assistant",
                profession = "Office Jobs",
                description = "Corporate office at Lal Chowk seeking accounts assistant for GST filing, ledger maintenance, and client billing using Tally Prime.",
                requiredSkills = "Tally Prime, GST filing, Excel, Office Administration",
                workersNeeded = 1,
                district = "Srinagar",
                area = "Residency Road / Lal Chowk",
                workLocation = "Office",
                workType = "Full-time",
                paymentAmount = "₹18,000",
                paymentPeriod = "Per Month",
                experienceRequired = "1+ years",
                requirements = "B.Com or relevant commerce background",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user3Id,
                employerName = "Kashmir Arts & Handicrafts Emporium",
                title = "Showroom Sales Executive & Customer Care",
                profession = "Shops & Retail",
                description = "Premier handicraft showroom seeking fluent sales representative for Pashmina, walnut wood and saffron sales to tourists and visitors.",
                requiredSkills = "Sales, Customer Service, Fluent English, Kashmiri & Urdu",
                workersNeeded = 2,
                district = "Srinagar",
                area = "Boulevard Road",
                workLocation = "Showroom",
                workType = "Full-time",
                paymentAmount = "₹16,000",
                paymentPeriod = "Per Month",
                experienceRequired = "Sales experience in retail or tourism",
                requirements = "Polite, well-spoken, punctual",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user2Id,
                employerName = "Hotel Grand Boulevard",
                title = "Front Desk Supervisor & Guest Relations",
                profession = "Hotels & Hospitality",
                description = "4-star luxury hotel on Dal Lake Boulevard hiring front office supervisor for guest check-ins, reservation management, and concierge service.",
                requiredSkills = "Hotel Front Desk, Hotel PMS Software, Customer Relations",
                workersNeeded = 1,
                district = "Srinagar",
                area = "Dalgate / Boulevard",
                workLocation = "Hotel",
                workType = "Full-time",
                paymentAmount = "₹20,000",
                paymentPeriod = "Per Month",
                experienceRequired = "2+ years in hospitality",
                requirements = "Hotel management diploma preferred",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user1Id,
                employerName = "Ahdoos Heritage Restaurant",
                title = "Wazwan Chef Assistant & Kitchen Helper",
                profession = "Restaurants & Food Services",
                description = "Iconic Kashmiri restaurant looking for energetic kitchen helpers to assist master Wazas with prep, pounding, and traditional cooking.",
                requiredSkills = "Kitchen Prep, Food Hygiene, Traditional Kashmiri Cooking Basics",
                workersNeeded = 3,
                district = "Srinagar",
                area = "Residency Road",
                workLocation = "Restaurant Kitchen",
                workType = "Full-time",
                paymentAmount = "₹15,000",
                paymentPeriod = "Per Month",
                experienceRequired = "Prior kitchen experience",
                requirements = "Clean habits, hard working",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user5Id,
                employerName = "Kashmir Valley Builders",
                title = "Experienced Stone Mason & Bricklayer (Mistri)",
                profession = "Construction & Civil",
                description = "Residential construction project in Baramulla requires experienced stone mason and bricklayer for heritage Kashmiri brick foundation.",
                requiredSkills = "Stone Masonry, Brickwork, Plastering, Leveling",
                workersNeeded = 4,
                district = "Baramulla",
                area = "Kanispora",
                workLocation = "Construction Site",
                workType = "Daily wage",
                paymentAmount = "₹950",
                paymentPeriod = "Per Day",
                experienceRequired = "3+ years",
                requirements = "Must have own basic hand tools",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user2Id,
                employerName = "Kashmir Holiday Travels",
                title = "Commercial Scorpio / Innova Tourist Driver",
                profession = "Drivers & Transport",
                description = "Licensed commercial driver needed for Gulmarg, Pahalgam, Sonamarg and Doodhpathri tourist circuits. Vehicle provided.",
                requiredSkills = "Mountain Driving, Snow Driving with Chains, Valid Commercial Driving License",
                workersNeeded = 2,
                district = "Srinagar",
                area = "TRC / Lal Chowk",
                workLocation = "Tour Circuit",
                workType = "Full-time",
                paymentAmount = "₹22,000",
                paymentPeriod = "Per Month",
                experienceRequired = "3+ years mountain driving",
                requirements = "Clean driving record, polite demeanor",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user7Id,
                employerName = "Rozgar Express Logistics",
                title = "Fast Delivery Riders with Two-Wheelers",
                profession = "Delivery & Couriers",
                description = "Delivery riders wanted for food, parcel, and marketplace deliveries across Srinagar and Budgam. Bike and smartphone required.",
                requiredSkills = "Local Srinagar Route Knowledge, Bike Riding, Smartphone App Usage",
                workersNeeded = 6,
                district = "Srinagar",
                area = "All Srinagar Zones",
                workLocation = "Field / On-Road",
                workType = "Full-time",
                paymentAmount = "₹16,000",
                paymentPeriod = "Per Month + Petrol Allowance",
                experienceRequired = "Freshers welcome",
                requirements = "Own two-wheeler, valid license, Android phone",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user4Id,
                employerName = "Al-Noor Educational Institute",
                title = "Secondary Science & Mathematics Teacher",
                profession = "Teachers & Tutors",
                description = "Reputed private higher secondary school in Anantnag hiring experienced teacher for Class 9th & 10th Physics and Math.",
                requiredSkills = "Physics, Mathematics, Classroom Management, Board Exam Prep",
                workersNeeded = 1,
                district = "Anantnag",
                area = "KP Road, Anantnag",
                workLocation = "School",
                workType = "Full-time",
                paymentAmount = "₹22,000",
                paymentPeriod = "Per Month",
                experienceRequired = "B.Sc/B.Ed with 2+ years teaching",
                requirements = "Fluent English and Urdu communication",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user5Id,
                employerName = "Srinagar Plumbing Solutions",
                title = "PPR / CPVC Pipe Plumber & Geyser Technician",
                profession = "Plumbers & Sanitary",
                description = "Plumbing service provider hiring master plumber for bathroom sanitary fittings, winter water heating lines, and leak repair.",
                requiredSkills = "PPR Pipe Welding, Geyser Installation, Motor Pump Fitting",
                workersNeeded = 2,
                district = "Srinagar",
                area = "Nowshera / Soura",
                workLocation = "Field / Residential",
                workType = "Daily wage",
                paymentAmount = "₹850",
                paymentPeriod = "Per Day",
                experienceRequired = "2+ years",
                requirements = "Must have plumbing tool kit",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user7Id,
                employerName = "Valley Digital Craft Studios",
                title = "Remote Content Writer & Social Media Specialist",
                profession = "Remote & Online Work",
                description = "Work from home managing Instagram, catalog descriptions, and online marketing for Kashmiri shawls, pherans, and organic walnuts.",
                requiredSkills = "Content Writing, Social Media Marketing, Product Photography, Graphic Design",
                workersNeeded = 1,
                district = "Srinagar",
                area = "Work from Home / Remote",
                workLocation = "Remote",
                workType = "Remote",
                paymentAmount = "₹18,000",
                paymentPeriod = "Per Month",
                experienceRequired = "Portfolio of prior work",
                requirements = "High-speed internet, creative mindset",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user3Id,
                employerName = "Chinar Traditional Tailoring Studio",
                title = "Master Pheran & Suit Tailor (درزی)",
                profession = "Tailors & Garment Masters",
                description = "Experienced tailor required for men's tweed pherans, ladies velvet tilla pherans, and traditional Kashmiri woolen suits. High piece-rate or monthly salary.",
                requiredSkills = "Pattern Cutting, Tweed Stitching, Tilla Attachment, Pheran Lining",
                workersNeeded = 2,
                district = "Srinagar",
                area = "Goni Khan Market",
                workLocation = "Tailoring Workshop",
                workType = "Full-time",
                paymentAmount = "₹24,000",
                paymentPeriod = "Per Month",
                experienceRequired = "3+ years master tailor",
                requirements = "Punctual, skilled in traditional and modern cuts",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user5Id,
                employerName = "Srinagar National Auto Care",
                title = "Experienced Car & Commercial Vehicle Mechanic",
                profession = "Mechanics & Automobile",
                description = "Workshop on Srinagar bypass hiring mechanic for diesel engines, brake servicing, snow chain fitting, and winter coolant prep.",
                requiredSkills = "Engine Overhauling, Brake Inspection, Suspension Repair, Diagnostic Tools",
                workersNeeded = 2,
                district = "Srinagar",
                area = "Pantha Chowk Bypass",
                workLocation = "Automobile Workshop",
                workType = "Full-time",
                paymentAmount = "₹22,000",
                paymentPeriod = "Per Month",
                experienceRequired = "2+ years automobile experience",
                requirements = "Honest, capable of handling commercial fleets",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = adminId,
                employerName = "Chinar Commercial Complex Security",
                title = "Day & Night Security Guards (سیکورٹی)",
                profession = "Security & Protection",
                description = "Security guards needed for commercial market complex and parking area. Uniform, warm winter cabin, and duty tea provided.",
                requiredSkills = "Gate Register Entry, Vigilance, CCTV Monitoring, Polite Visitor Handling",
                workersNeeded = 4,
                district = "Srinagar",
                area = "Residency Road",
                workLocation = "Commercial Complex",
                workType = "Full-time",
                paymentAmount = "₹15,000",
                paymentPeriod = "Per Month",
                experienceRequired = "Ex-servicemen or trained guards preferred",
                requirements = "Physically fit, active, clean record",
                isSeasonal = false,
                status = "ACTIVE"
            )
        )

        dao.insertJob(
            JobEntity(
                employerUserId = user6Id,
                employerName = "Pampore Saffron Cooperative",
                title = "Saffron Flower Plucking & Stigma Separation Crew",
                profession = "Agriculture & Saffron Farming",
                description = "Seasonal harvest workers needed for golden saffron fields in Pampore. Plucking purple crocus flowers early morning and delicate red stigma separation.",
                requiredSkills = "Gentle Flower Picking, Stigma Separation, Careful Drying",
                workersNeeded = 12,
                district = "Pulwama",
                area = "Pampore Saffron Karewas",
                workLocation = "Saffron Fields",
                workType = "Seasonal",
                paymentAmount = "₹1,000",
                paymentPeriod = "Per Day",
                experienceRequired = "No experience required, training given",
                requirements = "Early morning availability, gentle hands",
                isSeasonal = true,
                seasonalCategory = "Saffron Harvesting",
                status = "ACTIVE"
            )
        )

        // 4. Insert Marketplace Products (Buy & Sell)
        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Authentic Kashmiri Pashmina Shawl with Sozni Needlework",
                description = "100% pure Cashmere Pashmina hand-spun on traditional Kashmiri Charkha, featuring exquisite Sozni needle embroidery along borders. Feather-light, warm, certified handloom quality.",
                category = "Handicrafts",
                condition = "Brand New",
                isSecondHand = false,
                price = 18500.0,
                isNegotiable = true,
                quantity = 2,
                stockQuantity = 5,
                culturalProductType = "Pashmina Shawl",
                isResellable = true,
                supplierPrice = 15000.0,
                maxResellerMargin = 3500.0,
                district = "Budgam",
                area = "Kanihama",
                imageUri = "https://images.unsplash.com/photo-1544816155-12df9643f363?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Ghulam Mohammad Rather",
                title = "Hand-Carved Walnut Wood Dry Fruit Bowl & Tray Set",
                description = "Master-crafted solid walnut wood double bowl with traditional Kashmiri Chinar leaf engraving. Finished with natural organic wax. Ideal for weddings and home decor.",
                category = "Walnut Wood & Carvings",
                condition = "New",
                isSecondHand = false,
                price = 3200.0,
                isNegotiable = false,
                quantity = 5,
                stockQuantity = 12,
                culturalProductType = "Walnut Wood",
                isResellable = true,
                supplierPrice = 2500.0,
                maxResellerMargin = 700.0,
                district = "Srinagar",
                area = "Downtown",
                imageUri = "https://images.unsplash.com/photo-1579783900882-c0d3dad7b119?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user6Id,
                sellerName = "Wani Orchards",
                title = "Grade-A Kashmiri Red Delicious Apple Wooden Crates (22kg)",
                description = "Freshly picked from Zainapora Shopian orchards. Extra sweet, crunchy, pesticide-safe. Packed carefully in standard wooden boxes with paper padding.",
                category = "Farm Products",
                condition = "New",
                isSecondHand = false,
                price = 1250.0,
                isNegotiable = true,
                quantity = 40,
                district = "Shopian",
                area = "Zainapora",
                imageUri = "https://images.unsplash.com/photo-1568702846914-96b305d2aaeb?w=500",
                stockQuantity = 40,
                culturalProductType = "Walnut & Fresh Produce",
                isResellable = true,
                supplierPrice = 1000.0,
                maxResellerMargin = 300.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Traditional Hand-Stitched Woolen Kashmiri Pheran with Aari Tilla Work",
                description = "Warm 100% fine woolen Kashmiri Pheran adorned with traditional Aari and Tilla embroidery around collar and cuffs. Comfortable loose winter fit, custom-stitched by Srinagar artisans.",
                category = "Pherans & Traditional Wear",
                condition = "Brand New",
                isSecondHand = false,
                price = 3800.0,
                isNegotiable = true,
                quantity = 15,
                stockQuantity = 15,
                district = "Srinagar",
                area = "Nowhatta",
                imageUri = "https://images.unsplash.com/photo-1590736969955-71cc94801759?w=500",
                availableSizes = "S, M, L, XL, XXL",
                availableColors = "Navy Blue, Dark Charcoal, Maroon, Pine Green",
                genderTarget = "Women",
                culturalProductType = "Pheran",
                isResellable = true,
                supplierPrice = 3000.0,
                maxResellerMargin = 800.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Ghulam Mohammad Rather",
                title = "Charar-i-Sharief Woven Willow Wicker Kangri with Brass Detailing",
                description = "Authentic hand-woven willow wicker Kangri firepot made by master craftsmen of Charar-i-Sharief. Clay Kundal interior with ornate brass trim handles. Winter essential.",
                category = "Kangri & Winter Crafts",
                condition = "Brand New",
                isSecondHand = false,
                price = 750.0,
                isNegotiable = false,
                quantity = 25,
                stockQuantity = 25,
                district = "Budgam",
                area = "Charar-i-Sharief",
                imageUri = "https://images.unsplash.com/photo-1534447677768-be436bb09401?w=500",
                culturalProductType = "Kangri",
                isResellable = true,
                supplierPrice = 550.0,
                maxResellerMargin = 200.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Zadibal Copper Guild",
                title = "Traditional Hammered Copper Kashmiri Samovar with Chinar Engravings",
                description = "Heavy gauge pure copper charcoal tea urn (Samovar) hand-engraved with intricate Kashmiri Chinar motifs. Tin-plated (Kalai) interior for safe Kehwa brewing.",
                category = "Copper Utensils & Samovars",
                condition = "Brand New",
                isSecondHand = false,
                price = 6800.0,
                isNegotiable = true,
                quantity = 8,
                stockQuantity = 8,
                district = "Srinagar",
                area = "Zadibal",
                imageUri = "https://images.unsplash.com/photo-1541689592655-f5f52825a3b8?w=500",
                culturalProductType = "Copperware",
                isResellable = true,
                supplierPrice = 5500.0,
                maxResellerMargin = 1300.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user6Id,
                sellerName = "Pampore Saffron Cooperative",
                title = "Certified Grade-A Pampore Mongra Saffron (5g Sealed Wooden Box)",
                description = "100% pure organic Kashmiri Lacha/Mongra Saffron strands directly harvested from the historic saffron fields of Pampore. Unmatched natural crimson aroma and therapeutic grade.",
                category = "Kashmir Saffron & Spices",
                condition = "Brand New",
                isSecondHand = false,
                price = 1450.0,
                isNegotiable = false,
                quantity = 30,
                stockQuantity = 30,
                district = "Pulwama",
                area = "Pampore",
                imageUri = "https://images.unsplash.com/photo-1596040033229-a9821ebd058d?w=500",
                culturalProductType = "Saffron",
                isResellable = true,
                supplierPrice = 1150.0,
                maxResellerMargin = 300.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user6Id,
                sellerName = "Wani Orchards",
                title = "Organic Kashmiri Walnut Kernels (Kagzi Doon Giri) - 1kg Vacuum Pack",
                description = "Crisp, extra-light organic walnut halves freshly shelled in Shopian. Rich in Omega-3 and natural oils. Vacuum sealed for long-lasting freshness without preservatives.",
                category = "Walnuts, Kernels & Seeds",
                condition = "Brand New",
                isSecondHand = false,
                price = 950.0,
                isNegotiable = false,
                quantity = 50,
                stockQuantity = 50,
                district = "Shopian",
                area = "Zainapora",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500",
                culturalProductType = "Walnut",
                isResellable = true,
                supplierPrice = 750.0,
                maxResellerMargin = 200.0,
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user5Id,
                sellerName = "Shabir Mir",
                title = "Luminous Inverter 1050VA with 150Ah Tubular Battery",
                description = "Used for 1 winter only, excellent backup condition. Perfect for Kashmiri power cuts and winter load shedding. Giving 6-8 hours backup on 4 lights and 2 fans. Comes with heavy stand.",
                category = "Appliances",
                condition = "Used",
                isSecondHand = true, // Second hand section!
                price = 11500.0,
                isNegotiable = true,
                quantity = 1,
                district = "Pulwama",
                area = "Rajpora",
                imageUri = "",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user7Id,
                sellerName = "Bilal Ahmad Lone",
                title = "Second-Hand HP ProBook Core i5 Laptop (16GB RAM, 512GB SSD)",
                description = "Flawless working condition laptop, battery backup 4 hours. Used for software coding. Clean screen, original HP charger included. Tested thoroughly.",
                category = "Computers",
                condition = "Good",
                isSecondHand = true,
                price = 23000.0,
                isNegotiable = true,
                quantity = 1,
                district = "Srinagar",
                area = "Rajbagh",
                imageUri = "",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user2Id,
                sellerName = "Mohammad Shafi Dar",
                title = "Pre-Owned Heavy Duty Apple Orchard Aluminium Ladder (18ft)",
                description = "Sturdy 18-foot lightweight orchard ladder. Easy to transport and position among apple tree branches. No bends or damages.",
                category = "Agricultural Equipment",
                condition = "Like New",
                isSecondHand = true,
                price = 4500.0,
                isNegotiable = false,
                quantity = 2,
                district = "Baramulla",
                area = "Sopore",
                imageUri = "",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Ghulam Mohammad Rather",
                title = "Authentic Astrakhan Karakul Wool Topi (Karakul Cap)",
                description = "Master-crafted traditional Karakul headwear made from genuine soft curled wool. Lightweight, highly insulating winter classic favored across Kashmir.",
                category = "Topi, Caps & Headwear",
                condition = "Brand New",
                isSecondHand = false,
                price = 2800.0,
                quantity = 10,
                stockQuantity = 10,
                culturalProductType = "Karakul Topi",
                isResellable = true,
                supplierPrice = 2200.0,
                maxResellerMargin = 600.0,
                district = "Srinagar",
                area = "Zadibal",
                imageUri = "https://images.unsplash.com/photo-1576871337632-b9aef4c17ab9?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user6Id,
                sellerName = "Pampore Saffron Cooperative",
                title = "Kashmiri Mamra Giri Almonds (100% Organic, 1kg Sealed)",
                description = "Premium grade Kashmiri Mamra almonds grown in South Kashmir orchards. Packed with natural almond oil and nutrients. No chemical bleaching.",
                category = "Walnuts, Kernels & Seeds",
                condition = "Brand New",
                isSecondHand = false,
                price = 1650.0,
                quantity = 25,
                stockQuantity = 25,
                culturalProductType = "Dry Fruit",
                isResellable = true,
                supplierPrice = 1350.0,
                maxResellerMargin = 300.0,
                district = "Pulwama",
                area = "Pampore",
                imageUri = "https://images.unsplash.com/photo-1509440159596-0249088772ff?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Pure Kashmiri Tweed Nehru Bandgala Jacket for Men",
                description = "Handcrafted pure sheep wool Kashmiri tweed waistcoat with mandarin collar, satin lining, and real bone buttons. Perfect for chilly valley winters.",
                category = "Men's Clothing",
                condition = "Brand New",
                isSecondHand = false,
                price = 3200.0,
                quantity = 12,
                stockQuantity = 12,
                availableSizes = "38, 40, 42, 44",
                genderTarget = "Men",
                culturalProductType = "Men Clothing",
                isResellable = true,
                supplierPrice = 2500.0,
                maxResellerMargin = 700.0,
                district = "Srinagar",
                area = "Nowhatta",
                imageUri = "https://images.unsplash.com/photo-1594938298603-c8148c4dae35?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Kashmiri Tilla Embroidered Velvet Kurti Suit & Dupatta",
                description = "Luxurious royal blue velvet suit with intricate gold and silver metallic Tilla embroidery along neckline and sleeves. Paired with pure chinon dupatta.",
                category = "Women's Clothing",
                condition = "Brand New",
                isSecondHand = false,
                price = 4800.0,
                quantity = 8,
                stockQuantity = 8,
                availableSizes = "S, M, L, XL",
                genderTarget = "Women",
                culturalProductType = "Women Clothing",
                isResellable = true,
                supplierPrice = 3900.0,
                maxResellerMargin = 900.0,
                district = "Srinagar",
                area = "Nowhatta",
                imageUri = "https://images.unsplash.com/photo-1610030469983-98e550d6193c?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Warm Woolen Embroidered Pheran for Kids & Toddlers",
                description = "Cute, ultra-cozy kids winter Pheran with traditional Aari floral motifs, attached hood, and front kangaroo pocket. Keeps toddlers snug in sub-zero weather.",
                category = "Kids & Toddlers Wear",
                condition = "Brand New",
                isSecondHand = false,
                price = 1450.0,
                quantity = 15,
                stockQuantity = 15,
                availableSizes = "2-3Y, 4-5Y, 6-7Y, 8-9Y",
                genderTarget = "Kids",
                culturalProductType = "Kids Pheran",
                isResellable = true,
                supplierPrice = 1150.0,
                maxResellerMargin = 300.0,
                district = "Srinagar",
                area = "Nowhatta",
                imageUri = "https://images.unsplash.com/photo-1622290291468-a28f7a7dc6a8?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user7Id,
                sellerName = "Bilal Ahmad Lone",
                title = "10000mAh Cold-Resistant Fast Charging Power Bank",
                description = "Heavy duty power bank engineered to maintain battery capacity even in Kashmiri freezing winters. Dual USB-A and Type-C 20W PD output.",
                category = "Electronics & Mobile Phones",
                condition = "Brand New",
                isSecondHand = false,
                price = 1199.0,
                quantity = 20,
                stockQuantity = 20,
                culturalProductType = "Electronics",
                isResellable = true,
                supplierPrice = 950.0,
                maxResellerMargin = 249.0,
                district = "Srinagar",
                area = "Rajbagh",
                imageUri = "https://images.unsplash.com/photo-1609592424364-77a83d73f089?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Ghulam Mohammad Rather",
                title = "Handmade Kashmiri Pure Wool Felted Namda Rug (3x5 Ft)",
                description = "Traditional hand-felted pure sheep wool rug embroidered with colorful Aari chain stitch in Chinar and paisley motifs. Natural thermal insulation for floors.",
                category = "Household Items & Furniture",
                condition = "Brand New",
                isSecondHand = false,
                price = 1850.0,
                quantity = 8,
                stockQuantity = 8,
                culturalProductType = "Namda Rug",
                isResellable = true,
                supplierPrice = 1450.0,
                maxResellerMargin = 400.0,
                district = "Srinagar",
                area = "Downtown",
                imageUri = "https://images.unsplash.com/photo-1600121848594-d8644e57abab?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user4Id,
                sellerName = "Kashmir Herbal & Flora",
                title = "100% Steam-Distilled Srinagar Damask Rose Water (200ml)",
                description = "Pure organic rose water distilled in copper cauldrons from wild Srinagar Damask roses. Natural toner, eye soother and skin refresher.",
                category = "Beauty & Personal Care",
                condition = "Brand New",
                isSecondHand = false,
                price = 299.0,
                quantity = 30,
                stockQuantity = 30,
                culturalProductType = "Rose Water",
                isResellable = true,
                supplierPrice = 220.0,
                maxResellerMargin = 79.0,
                district = "Srinagar",
                area = "Harwan",
                imageUri = "https://images.unsplash.com/photo-1608248597359-322194cf6f08?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Chinar Book Heritage",
                title = "The Valley of Kashmir by Walter Lawrence (Hardcover Edition)",
                description = "Classic authoritative historical text documenting the culture, arts, crafts, languages, flora, and people of the Kashmir Valley. Beautiful library binding.",
                category = "Books & Kashmir Literature",
                condition = "Brand New",
                isSecondHand = false,
                price = 450.0,
                quantity = 15,
                stockQuantity = 15,
                culturalProductType = "Books",
                isResellable = true,
                supplierPrice = 350.0,
                maxResellerMargin = 100.0,
                district = "Srinagar",
                area = "Lal Chowk",
                imageUri = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user1Id,
                sellerName = "Zadibal Copper Guild",
                title = "Solid Walnut Wood Hand-Carved Folding Book Stand (Rehal)",
                description = "Crafted from seasoned solid walnut wood with open-work Chinar foliage carving. Folds flat smoothly for easy storage. Ideal for Quran, sacred books, or tablets.",
                category = "Walnut Wood & Carvings",
                condition = "Brand New",
                isSecondHand = false,
                price = 1250.0,
                quantity = 10,
                stockQuantity = 10,
                culturalProductType = "Walnut Rehal",
                isResellable = true,
                supplierPrice = 950.0,
                maxResellerMargin = 300.0,
                district = "Srinagar",
                area = "Downtown",
                imageUri = "https://images.unsplash.com/photo-1544716278-ca5e3f4abd8c?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user2Id,
                sellerName = "Mohammad Shafi Dar",
                title = "Heavy-Duty Orchard Apple Pruning Secateurs & Grafting Knife",
                description = "High carbon SK-5 steel bypass shears with ergonomic non-slip handles and spare spring. Effortlessly cuts apple branches up to 25mm thickness.",
                category = "Agricultural Tools & Equipment",
                condition = "Brand New",
                isSecondHand = false,
                price = 850.0,
                quantity = 20,
                stockQuantity = 20,
                culturalProductType = "Orchard Tools",
                isResellable = true,
                supplierPrice = 650.0,
                maxResellerMargin = 200.0,
                district = "Baramulla",
                area = "Sopore",
                imageUri = "https://images.unsplash.com/photo-1416879595882-3373a0480b5b?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user7Id,
                sellerName = "Bilal Ahmad Lone",
                title = "Second-Hand Canon EOS 1500D DSLR Camera with 18-55mm IS II Lens",
                description = "Flawless condition DSLR camera with 24.1MP APS-C sensor, Wi-Fi connectivity, original battery, strap, charger and 64GB SanDisk card. Shutter count under 4,000.",
                category = "Electronics & Mobile Phones",
                condition = "Like New",
                isSecondHand = true,
                price = 24500.0,
                quantity = 1,
                district = "Srinagar",
                area = "Rajbagh",
                imageUri = "https://images.unsplash.com/photo-1516035069371-29a1b244cc32?w=500",
                status = "ACTIVE"
            )
        )

        dao.insertProduct(
            ProductEntity(
                sellerUserId = user3Id,
                sellerName = "Parveena Jan Handcrafts",
                title = "Authentic Kashmiri Bridal Kasaba & Organic Mehendi Artist Service",
                description = "Professional on-location bridal dressing including traditional Kashmiri Kasaba headwear styling, natural Rajasthani-Kashmiri mehendi design, and photography assistance.",
                category = "Local Services",
                condition = "Service",
                isSecondHand = false,
                price = 6500.0,
                quantity = 5,
                district = "Srinagar",
                area = "All Srinagar Districts",
                imageUri = "https://images.unsplash.com/photo-1583391733956-3750e0ff4e8b?w=500",
                status = "ACTIVE"
            )
        )

        // 5. Initial Sample Conversation demonstrating VOICE FIRST
        val convId = dao.insertConversation(
            ConversationEntity(
                user1Id = user6Id,
                user2Id = user2Id,
                user1Name = "Bashir Ahmad Wani",
                user2Name = "Mohammad Shafi Dar",
                subject = "Apple Picking Crew Inquiry",
                hasFirstVoiceNote = true,
                lastMessageText = "🎤 Voice note (18s) - Discussion on orchard crew size",
                lastMessageTime = System.currentTimeMillis() - 3600000
            )
        )

        dao.insertMessage(
            MessageEntity(
                conversationId = convId,
                senderUserId = user6Id,
                senderName = "Bashir Ahmad Wani",
                messageType = "VOICE",
                textContent = "Assalamu Alaikum Shafi Sahab, I saw your orchard harvesting profile. We have 35 kanals in Shopian needing picking next week.",
                voiceDurationSeconds = 24,
                timestamp = System.currentTimeMillis() - 7200000,
                isRead = true
            )
        )

        dao.insertMessage(
            MessageEntity(
                conversationId = convId,
                senderUserId = user2Id,
                senderName = "Mohammad Shafi Dar",
                messageType = "VOICE",
                textContent = "Walaikum Assalam Bashir Sahab, Alhamdulillah my crew of 8 experienced pickers is ready to move after Sunday.",
                voiceDurationSeconds = 18,
                timestamp = System.currentTimeMillis() - 3600000,
                isRead = true
            )
        )

        // 6. Initial Notifications
        dao.insertNotification(
            NotificationEntity(
                userId = adminId,
                title = "Welcome to Rozgar Kashmir",
                message = "The platform is live across all 10 districts of Jammu & Kashmir. Connect skills with opportunities.",
                type = "ADMIN_NOTICE",
                isRead = false
            )
        )

        dao.insertNotification(
            NotificationEntity(
                userId = user2Id,
                title = "New Seasonal Job Alert",
                message = "Wani Apple Estates posted a new picking job in Shopian matching your skills.",
                type = "NEW_JOB",
                isRead = false
            )
        )

        // 7. Initial Login Activity
        dao.insertLoginActivity(
            LoginActivityEntity(
                userId = adminId,
                userName = "Admin Rozgar Kashmir",
                phoneNumber = "9906000000",
                district = "Srinagar",
                activityType = "LOGIN",
                accountStatus = "ACTIVE"
            )
        )

        // 8. Initial Sample Donations
        dao.insertDonation(
            DonationEntity(
                userId = user7Id,
                donorName = "Bilal Ahmad Lone",
                amount = 500.0,
                referenceId = "UPI/428910482910",
                upiId = "rozgarkashmir@upi",
                status = "VERIFIED"
            )
        )

        // 9. Initial Kashmiri Literature & Bookstore Seed Data
        dao.insertContribution(
            KashmiriContributionEntity(
                authorUserId = adminId,
                authorName = "Prof. Ghulam Nabi Firaq & Board",
                authorBio = "Eminent Kashmiri literary scholar and Sahitya Akademi awardee.",
                type = "BOOK",
                titleKashmiri = "لال واکھ: کٲشُر تصوّف تہٕ حِکمت",
                titleEnglish = "Lal Vakh: Kashmiri Wisdom & Mysticism",
                content = """
                    گورَن وونم کُنوی وَژُن
                    نؠبرِ دۄپنم اَندر اَژُن!

                    سو میٛہ لَلِ پیوم وَکھ تہٕ وَژُن
                    تاو مےٚ بٔریوم نَژُن تَن!

                    امی کٲشِر زَبانہِ ہِنٛدی عظمتن ستؠ چُھ کٲشُر ادب سرسبز۔ لل دید ہِنٛز شٲعری چھِ رُوحٲنی رہنمائی تہٕ کٲشِر تمدن ہِچ بنیاد۔ سۄ چھےٚ فرموان زِ خۄداوند کریم چُھ انسنَس پننس اندر موجود، تمس چھنہٕ بنین کُنین منٛز ژھانٛڈنٕچ ضرورت۔ پریتھ اکھ انسان گژھِ پنُن من صاف تھوُن۔
                """.trimIndent(),
                summary = "Master collection of mystic Kashmiri verses with complete philosophical commentary and vocabulary glossary.",
                genre = "Sufi Literature (تصوّف)",
                isBook = true,
                isPaid = true,
                price = 49.0,
                pageCount = 88,
                status = "FEATURED",
                readsCount = 312,
                purchaseCount = 42
            )
        )

        dao.insertContribution(
            KashmiriContributionEntity(
                authorUserId = adminId,
                authorName = "Akhtar Mohiuddin",
                authorBio = "Master short-story writer and pioneering novelist of modern Kashmiri prose.",
                type = "BOOK",
                titleKashmiri = "کٲشِر لۄک کَتھ: وادی ہِنٛز لافانی دٔلیلہٕ",
                titleEnglish = "Kashmiri Folk Tales: Timeless Legends of the Valley",
                content = """
                    اَکھ اوس پادشاہ۔ تمس ٲسی ترےٚ نیٚچوی۔ پادشاہن وۆن تمن زِ یس ژھانٛڈِ ژھانٛڈِ وادی ہُنٛد سارِوٕے کھۄتہٕ نایاب ہُنَر لٔبِتھ انِ، سُے بنیہِ تختس پؠٹھ وارث!

                    بۄڑ نیٚچُو گۆو دۆدھ پَتھر کُن، درمیٲنی نیٚچُو پیوم پَہَلگام کُن، تہٕ لۄکُٹ نیٚچُو پیوم ڈل سَر کُن۔ ڈل سر کِس کنڈس پؠٹھ لٔج تمس اکھ دۄگُر کٲرِیگَر یس پیپر ماشی پیٹھ سونہٕ تِلا ستؠ کٲم اوس کران۔ کٲرِیگرَن وۆنس: 'ہُکمران بننُک اصل راز چُھ پننین کاریگرن تہٕ محنت کشن ہُنٛد احترام کرُن!'
                """.trimIndent(),
                summary = "Captivating folk stories, fables, and traditional bedtime legends passed through generations in Kashmir.",
                genre = "Folk Tales (لۄک کَتھ)",
                isBook = true,
                isPaid = true,
                price = 69.0,
                pageCount = 124,
                status = "APPROVED",
                readsCount = 198,
                purchaseCount = 28
            )
        )

        dao.insertContribution(
            KashmiriContributionEntity(
                authorUserId = adminId,
                authorName = "Ghulam Ahmad Mahjoor (شائرِ کشمیر)",
                authorBio = "National poet of Kashmir known for patriotic and humanist Kashmiri lyrics.",
                type = "POETRY",
                titleKashmiri = "والو ہا باغبانو! نَو بَہارُک شان پٲدا کَر",
                titleEnglish = "Vālo Hā Bāgbāno: O Gardener of the Valley!",
                content = """
                    والو ہا باغبانو! نَو بَہارُک شان پٲدا کَر
                    پھۆلن گُل، گَتھ کَرن بُلبُل، تِتھوی سامان پٲدا کَر!

                    کَرِتھ رُوحَن نَوِو بیدار، بَر چمَنَس جَمال و حُسن
                    مَحَبَت ستؠ، اِتِحادَس ستؠ نَوِو اِنٛسان پٲدا کَر!

                    سۄندر کٔشیٖر چھےٚ ہُنَر منٛدن ہُنٛد گھر۔ کٲرِیگَر، دَہقان تہٕ مَزدُور گژھن خوشحال روزن!
                """.trimIndent(),
                summary = "Iconic classic revolutionary Kashmiri lyric calling for renewal, work ethics, and floral blooming of the Valley.",
                genre = "Poetry (شٲعِری)",
                isBook = false,
                isPaid = false,
                price = 0.0,
                pageCount = 12,
                status = "FEATURED",
                readsCount = 640
            )
        )

        // 10. Sample Verified Reviews
        dao.insertReview(
            ReviewEntity(
                authorUserId = user2Id,
                authorName = "Mohammad Shafi Dar",
                targetType = "WORKER",
                targetId = 1L, // Ghulam Mohammad Rather
                targetTitle = "Ghulam Mohammad Rather (Khatamband Craftsman)",
                rating = 5,
                reviewText = "Ustad Ghulam Mohammad did marvelous Khatamband woodwork for our heritage home in Sopore. Flawless symmetry and genuine deodar timber. Highly respected craftsman!",
                transactionOrJobRef = "Heritage Ceiling Woodwork"
            )
        )

        dao.insertReview(
            ReviewEntity(
                authorUserId = user1Id,
                authorName = "Ghulam Mohammad Rather",
                targetType = "WORKER",
                targetId = 2L, // Mohammad Shafi Dar
                targetTitle = "Mohammad Shafi Dar (Apple Orchardist)",
                rating = 5,
                reviewText = "Hardworking harvest crew manager. Completed picking 40 kanals in Zaingeer on time with zero fruit bruising. Punctual and professional.",
                transactionOrJobRef = "Apple Harvest Season"
            )
        )

        // 11. Initial Platform Settings (Admin Configurable)
        dao.savePlatformSettings(
            PlatformSettingsEntity(
                id = 1,
                membershipPriceMonthly = 99.0, // ₹99/month
                verifiedBadgeReviewFee = 49.0,
                productCommissionPercent = 5.0, // 5% Products & Second-Hand
                propertySaleCommissionPercent = 5.0, // 5% Property Sale
                propertyRentalCommissionPercent = 5.0, // 5% Property Rental (1st month)
                bookCommissionPercent = 2.0, // 2% Kashmiri Books
                foodCommissionPercent = 1.0, // 1% Restaurant / Food Orders
                resellerCommissionPercent = 5.0, // 5% Reseller Transactions
                productCommissionEnabled = true,
                propertySaleCommissionEnabled = true,
                propertyRentalCommissionEnabled = true,
                bookCommissionEnabled = true,
                foodCommissionEnabled = true,
                resellerCommissionEnabled = true,
                includeDeliveryInCommission = false,
                includeTaxesInCommission = false,
                bookMaxOriginalPrice = 500.0,
                bookMaxAuthorizedEditionPrice = 100.0,
                wordRewardPoolBudget = 50000.0,
                rewardPerApprovedWord = 2.0,
                minimumPayoutAmount = 500.0,
                effectiveDate = "Immediate",
                updatedAt = System.currentTimeMillis(),
                updatedByAdminEmail = "bilaltantraypersonal@gmail.com"
            )
        )

        // Seed initial Central Ledger entries
        dao.insertLedgerEntries(
            listOf(
                TransactionLedgerEntity(
                    transactionRef = "TXN-2026-00101",
                    orderRef = "ORD-KASH-901",
                    category = "PRODUCTS",
                    listingTitle = "Hand-woven Pashmina Shawl (Kani Jaal)",
                    buyerId = 3L,
                    buyerName = "Parveena Jan",
                    sellerId = user1Id,
                    sellerName = "Ghulam Mohammad Rather",
                    grossAmount = 6500.0,
                    commissionRatePercent = 5.0,
                    commissionCalculationBasis = "5% of Gross Price",
                    platformCommission = 325.0,
                    paymentProviderFee = 0.0,
                    deliveryCharge = 0.0,
                    sellerPayableAmount = 6175.0,
                    paymentStatus = "PAID",
                    commissionStatus = "DEDUCTED",
                    settlementStatus = "SETTLED"
                ),
                TransactionLedgerEntity(
                    transactionRef = "TXN-2026-00102",
                    orderRef = "ORD-FOOD-802",
                    category = "FOOD",
                    listingTitle = "Traditional Wazwan Meal Box (Rista, Gushtaba, Rogan Josh)",
                    buyerId = 4L,
                    buyerName = "Dr. Adil Hussain",
                    sellerId = user1Id,
                    sellerName = "Ahdoos Wazwan & Traditional Bakery",
                    grossAmount = 850.0,
                    commissionRatePercent = 1.0,
                    commissionCalculationBasis = "1% of Food Order",
                    platformCommission = 8.5,
                    paymentProviderFee = 0.0,
                    deliveryCharge = 50.0,
                    sellerPayableAmount = 891.5,
                    paymentStatus = "PAID",
                    commissionStatus = "DEDUCTED",
                    settlementStatus = "SETTLED"
                ),
                TransactionLedgerEntity(
                    transactionRef = "TXN-2026-00103",
                    orderRef = "PROP-RENT-301",
                    category = "PROPERTY_RENTAL",
                    listingTitle = "Furnished 2BHK Flat near Dal Lake (First Month Rent)",
                    buyerId = 4L,
                    buyerName = "Dr. Adil Hussain",
                    sellerId = user2Id,
                    sellerName = "Mohammad Shafi Dar",
                    grossAmount = 14000.0,
                    commissionRatePercent = 5.0,
                    commissionCalculationBasis = "5% of 1st Month Rent",
                    platformCommission = 700.0,
                    paymentProviderFee = 0.0,
                    deliveryCharge = 0.0,
                    sellerPayableAmount = 13300.0,
                    paymentStatus = "PAID",
                    commissionStatus = "DEDUCTED",
                    settlementStatus = "SETTLED"
                ),
                TransactionLedgerEntity(
                    transactionRef = "TXN-2026-00104",
                    orderRef = "LIT-SALE-401",
                    category = "BOOKS",
                    listingTitle = "Kalaam-e-Mahjoor (Hardcover Poetry)",
                    buyerId = user1Id,
                    buyerName = "Ghulam Mohammad Rather",
                    sellerId = 2L,
                    sellerName = "Kashmir Heritage Publications",
                    grossAmount = 250.0,
                    commissionRatePercent = 2.0,
                    commissionCalculationBasis = "2% of Book Price",
                    platformCommission = 5.0,
                    paymentProviderFee = 0.0,
                    deliveryCharge = 30.0,
                    sellerPayableAmount = 275.0,
                    paymentStatus = "PAID",
                    commissionStatus = "DEDUCTED",
                    settlementStatus = "SETTLED"
                )
            )
        )

        // 12. Sample Kashmiri Restaurants & Wazwan
        val rest1 = dao.insertRestaurant(
            RestaurantEntity(
                ownerUserId = user1Id,
                ownerName = "Ahdoos Heritage Caterers",
                name = "Ahdoos Wazwan & Traditional Bakery",
                description = "Iconic authentic 7-course Kashmiri Wazwan, fresh Girda, Lavasa, Telvor & Sheermal baked hourly.",
                cuisineType = "Authentic Kashmiri Wazwan & Bakery",
                district = "Srinagar",
                area = "Residency Road, Lal Chowk",
                address = "Residency Road, Lal Chowk, Srinagar",
                openingHours = "10:00 AM - 10:30 PM",
                bannerImageUri = "https://images.unsplash.com/photo-1544025162-d76694265947?w=800",
                deliveryFee = 25.0,
                minimumOrder = 150.0,
                rating = 4.9,
                totalReviews = 148
            )
        )

        val rest2 = dao.insertRestaurant(
            RestaurantEntity(
                ownerUserId = user2Id,
                ownerName = "Haji Ghulam Hassan",
                name = "Fateh Kadal Traditional Harissa",
                description = "Legendary winter breakfast delicacy slow-cooked overnight with mutton, rice broth, and aromatic fennel.",
                cuisineType = "Kashmiri Winter Delicacies & Harissa",
                district = "Srinagar",
                area = "Downtown / Fateh Kadal",
                address = "Fateh Kadal, Downtown, Srinagar",
                openingHours = "05:30 AM - 11:30 AM",
                bannerImageUri = "https://images.unsplash.com/photo-1589302168068-964664d93dc0?w=800",
                deliveryFee = 30.0,
                minimumOrder = 200.0,
                rating = 5.0,
                totalReviews = 96
            )
        )

        // Menu Items
        dao.insertMenuItems(
            listOf(
                FoodMenuItemEntity(
                    restaurantId = rest1,
                    name = "Rista (Pounded Meatballs in Saffron Gravy)",
                    description = "Traditional hand-pounded mutton meatballs cooked in crimson Kashmiri red chili broth.",
                    category = "Wazwan Specialties",
                    price = 320.0,
                    isVegetarian = false,
                    portionSizes = "Plate (2 pcs)"
                ),
                FoodMenuItemEntity(
                    restaurantId = rest1,
                    name = "Roghan Josh",
                    description = "Tender mutton simmered with Kashmiri mawal cockscomb flower and fennel spices.",
                    category = "Wazwan Specialties",
                    price = 340.0,
                    isVegetarian = false,
                    portionSizes = "Plate (2 pcs)"
                ),
                FoodMenuItemEntity(
                    restaurantId = rest1,
                    name = "Traditional Kashmiri Kehwa",
                    description = "Freshly brewed green tea leaves infused with saffron strands, crushed almonds, and green cardamom.",
                    category = "Beverages & Kehwa",
                    price = 60.0,
                    isVegetarian = true,
                    portionSizes = "Kup"
                ),
                FoodMenuItemEntity(
                    restaurantId = rest1,
                    name = "Kashmiri Girda & Lavasa",
                    description = "Crisp, warm traditional Kashmiri baker bread freshly baked in clay tandoor.",
                    category = "Traditional Bakery (Kandur)",
                    price = 15.0,
                    isVegetarian = true,
                    portionSizes = "1 piece"
                ),
                FoodMenuItemEntity(
                    restaurantId = rest2,
                    name = "Full Plate Kashmiri Harissa with Kebab & Methi",
                    description = "Rich, velvety shredded mutton seasoned with ghee, served boiling hot with warm Girda.",
                    category = "Kashmiri Harissa",
                    price = 380.0,
                    isVegetarian = false,
                    portionSizes = "Full (500g)"
                )
            )
        )

        // 13. Sample Properties & Room Rentals across Kashmir
        dao.insertProperties(
            listOf(
                PropertyListingEntity(
                    ownerUserId = user2Id,
                    ownerName = "Mohammad Shafi Dar",
                    title = "Furnished 2BHK Flat near Dal Lake & Boulevard",
                    description = "Spacious, sunny upper floor flat with wooden Khatamband ceilings, heating provision, 24-hour water supply, and scenic mountain views.",
                    propertyType = "Flat / Apartment",
                    listingType = "Rent",
                    rentOrPrice = 14000.0,
                    deposit = 28000.0,
                    district = "Srinagar",
                    town = "Dalgate / Boulevard",
                    address = "Near Badyari Chowk, Dalgate, Srinagar",
                    furnishing = "Furnished",
                    bedrooms = 2,
                    bathrooms = 2,
                    amenities = "Geyser, Khatamband, Backup Power, Parking"
                ),
                PropertyListingEntity(
                    ownerUserId = user1Id,
                    ownerName = "Ghulam Mohammad Rather",
                    title = "Single Room for Student / Artisan in Downtown",
                    description = "Clean single room with attached bathroom, peaceful residential lane near Jamia Masjid and bus stop.",
                    propertyType = "Room",
                    listingType = "Rent",
                    rentOrPrice = 4500.0,
                    deposit = 5000.0,
                    district = "Srinagar",
                    town = "Nowhatta / Downtown",
                    address = "Near Malkha Gate, Nowhatta, Srinagar",
                    furnishing = "Semi-Furnished",
                    bedrooms = 1,
                    bathrooms = 1,
                    amenities = "Water Supply, Separate Entrance, Kitchen Corner"
                ),
                PropertyListingEntity(
                    ownerUserId = user2Id,
                    ownerName = "Mohammad Shafi Dar",
                    title = "Commercial Shop on Sopore Fruit Mandi Main Road",
                    description = "Ground floor commercial shutter shop ideal for trade, transport agency, pharmacy, or dry fruit packing.",
                    propertyType = "Commercial Space / Shop",
                    listingType = "Rent",
                    rentOrPrice = 8500.0,
                    deposit = 20000.0,
                    district = "Baramulla",
                    town = "Sopore",
                    address = "Fruit Mandi Link Road, Sopore",
                    furnishing = "Unfurnished",
                    bedrooms = 0,
                    bathrooms = 1,
                    amenities = "Wide Shutter, High Footfall, 3-Phase Electricity"
                )
            )
        )

        // 14. Sample Daily Kashmiri Word Contributions
        dao.insertWordContributions(
            listOf(
                KashmiriWordContributionEntity(
                    contributorUserId = user1Id,
                    contributorName = "Ghulam Mohammad Rather",
                    wordKashmiri = "ژھوٗنٛٹ",
                    pronunciation = "Tshoot",
                    meaningUrdu = "سیب (Apple)",
                    meaningEnglish = "Apple",
                    exampleSentence = "سۄپوٗر چھُ سؠٹھاہ کَمِیوٗنٹی مَنٛز ژھوٗنٛٹَن ہُنٛد مَرکَز۔",
                    category = "Flora & Fauna",
                    dialect = "Maraz / Sopore",
                    status = "APPROVED",
                    rewardAmount = 2.0,
                    rewardPaid = true
                ),
                KashmiriWordContributionEntity(
                    contributorUserId = user1Id,
                    contributorName = "Ghulam Mohammad Rather",
                    wordKashmiri = "خٲتَم بَنٛد",
                    pronunciation = "Khatamband",
                    meaningUrdu = "لکڑی کی روایتی چھت کی دستکاری",
                    meaningEnglish = "Traditional Kashmiri polygonal wooden ceiling craft",
                    exampleSentence = "کٔشیٖرِ ہٕنٛز سۄندر خٲتَم بَنٛد کٲم چھےٚ دُنیَہَس مَنٛز مَشہوٗر۔",
                    category = "Crafts & Trades",
                    dialect = "Srinagar",
                    status = "APPROVED",
                    rewardAmount = 2.0,
                    rewardPaid = true
                ),
                KashmiriWordContributionEntity(
                    contributorUserId = user2Id,
                    contributorName = "Mohammad Shafi Dar",
                    wordKashmiri = "پھؠرَن",
                    pronunciation = "Pheran",
                    meaningUrdu = "کشمیری روایتی اونی لبادہ",
                    meaningEnglish = "Traditional Kashmiri woolen tunic cloak",
                    exampleSentence = "وَنٛدَس مَنٛز چھِ لۄک پھیٖرَن لاگِتھ کانٛگٕر پَنٛنہِ کَنہِ سٟتؠ تھَوان۔",
                    category = "Daily Vocabulary",
                    dialect = "Standard Kashmiri",
                    status = "APPROVED",
                    rewardAmount = 2.0,
                    rewardPaid = true
                )
            )
        )

        dao.insertOrUpdateReward(
            ContributorRewardEntity(
                userId = user1Id,
                userName = "Ghulam Mohammad Rather",
                totalApprovedWords = 24,
                streakDays = 7,
                currentLevel = "Level 2: Active Preserver",
                totalRewardEarned = 48.0,
                totalRewardPaid = 40.0,
                pendingReward = 8.0,
                upiIdMasked = "ghulam***@upi",
                status = "ACTIVE"
            )
        )
    }

    // --- Property Repository Methods ---
    fun getActivePropertiesFlow(): Flow<List<PropertyListingEntity>> = dao.getActivePropertiesFlow()
    fun getAllPropertiesFlow(): Flow<List<PropertyListingEntity>> = dao.getAllPropertiesFlow()
    fun getPropertiesByOwnerFlow(ownerId: Long): Flow<List<PropertyListingEntity>> = dao.getPropertiesByOwnerFlow(ownerId)
    suspend fun getPropertyById(id: Long): PropertyListingEntity? = dao.getPropertyById(id)
    suspend fun insertProperty(property: PropertyListingEntity): Long = dao.insertProperty(property)
    suspend fun updateProperty(property: PropertyListingEntity) = dao.updateProperty(property)
    suspend fun deleteProperty(id: Long) = dao.deleteProperty(id)

    // --- Restaurants & Food Repository Methods ---
    fun getActiveRestaurantsFlow(): Flow<List<RestaurantEntity>> = dao.getActiveRestaurantsFlow()
    fun getAllRestaurantsFlow(): Flow<List<RestaurantEntity>> = dao.getAllRestaurantsFlow()
    suspend fun getRestaurantById(id: Long): RestaurantEntity? = dao.getRestaurantById(id)
    fun getMenuItemsForRestaurantFlow(restaurantId: Long): Flow<List<FoodMenuItemEntity>> = dao.getMenuItemsForRestaurantFlow(restaurantId)
    suspend fun insertRestaurant(restaurant: RestaurantEntity): Long = dao.insertRestaurant(restaurant)
    suspend fun updateRestaurant(restaurant: RestaurantEntity) = dao.updateRestaurant(restaurant)
    suspend fun insertMenuItem(item: FoodMenuItemEntity): Long = dao.insertMenuItem(item)
    suspend fun updateMenuItem(item: FoodMenuItemEntity) = dao.updateMenuItem(item)
    suspend fun deleteMenuItem(id: Long) = dao.deleteMenuItem(id)

    // --- Kashmiri Word Contribution Methods ---
    fun getApprovedWordsFlow(): Flow<List<KashmiriWordContributionEntity>> = dao.getApprovedWordsFlow()
    fun getAllWordsFlow(): Flow<List<KashmiriWordContributionEntity>> = dao.getAllWordsFlow()
    fun getPendingWordsFlow(): Flow<List<KashmiriWordContributionEntity>> = dao.getPendingWordsFlow()
    fun getWordsByContributorFlow(userId: Long): Flow<List<KashmiriWordContributionEntity>> = dao.getWordsByContributorFlow(userId)
    suspend fun insertWordContribution(word: KashmiriWordContributionEntity): Long = dao.insertWordContribution(word)
    suspend fun updateWordContribution(word: KashmiriWordContributionEntity) = dao.updateWordContribution(word)
    fun getApprovedWordCountFlow(): Flow<Int> = dao.getApprovedWordCountFlow()
    fun getRewardForUserFlow(userId: Long): Flow<ContributorRewardEntity?> = dao.getRewardForUserFlow(userId)
    suspend fun getRewardForUser(userId: Long): ContributorRewardEntity? = dao.getRewardForUser(userId)
    fun getLeaderboardFlow(): Flow<List<ContributorRewardEntity>> = dao.getLeaderboardFlow()
    suspend fun insertOrUpdateReward(reward: ContributorRewardEntity): Long = dao.insertOrUpdateReward(reward)

    // --- Platform Settings Methods ---
    fun getPlatformSettingsFlow(): Flow<PlatformSettingsEntity?> = dao.getPlatformSettingsFlow()
    suspend fun getPlatformSettings(): PlatformSettingsEntity? = dao.getPlatformSettings()
    suspend fun savePlatformSettings(settings: PlatformSettingsEntity): Long = dao.savePlatformSettings(settings)

    // --- Payout Requests Methods ---
    fun getAllPayoutRequestsFlow(): Flow<List<PayoutRequestEntity>> = dao.getAllPayoutRequestsFlow()
    fun getPendingPayoutRequestsFlow(): Flow<List<PayoutRequestEntity>> = dao.getPendingPayoutRequestsFlow()
    fun getPayoutRequestsForUserFlow(userId: Long): Flow<List<PayoutRequestEntity>> = dao.getPayoutRequestsForUserFlow(userId)
    suspend fun insertPayoutRequest(request: PayoutRequestEntity): Long = dao.insertPayoutRequest(request)
    suspend fun updatePayoutRequest(request: PayoutRequestEntity) = dao.updatePayoutRequest(request)

    // --- Central Transaction & Settlement Ledger Methods ---
    fun getAllLedgerEntriesFlow(): Flow<List<TransactionLedgerEntity>> = dao.getAllLedgerEntriesFlow()
    suspend fun insertLedgerEntry(entry: TransactionLedgerEntity): Long = dao.insertLedgerEntry(entry)
    suspend fun updateLedgerEntry(entry: TransactionLedgerEntity) = dao.updateLedgerEntry(entry)
    suspend fun getLedgerEntryById(id: Long): TransactionLedgerEntity? = dao.getLedgerEntryById(id)
    fun getTotalCommissionEarnedFlow(): Flow<Double?> = dao.getTotalCommissionEarnedFlow()

    // --- Delivery Addresses Methods ---
    fun getAddressesForUserFlow(userId: Long): Flow<List<DeliveryAddressEntity>> = dao.getAddressesForUserFlow(userId)
    suspend fun getAddressById(id: Long): DeliveryAddressEntity? = dao.getAddressById(id)
    suspend fun getDefaultAddressForUser(userId: Long): DeliveryAddressEntity? = dao.getDefaultAddressForUser(userId)
    suspend fun insertAddress(address: DeliveryAddressEntity): Long {
        if (address.isDefault) {
            dao.clearDefaultAddresses(address.userId)
        }
        return dao.insertAddress(address)
    }
    suspend fun updateAddress(address: DeliveryAddressEntity) {
        if (address.isDefault) {
            dao.clearDefaultAddresses(address.userId)
        }
        dao.updateAddress(address)
    }
    suspend fun deleteAddress(id: Long) = dao.deleteAddress(id)
    suspend fun setDefaultAddress(userId: Long, addressId: Long) {
        dao.clearDefaultAddresses(userId)
        val addr = dao.getAddressById(addressId)
        if (addr != null) {
            dao.updateAddress(addr.copy(isDefault = true))
        }
    }

    // --- Legal Policies & Governance CMS Methods ---
    fun getAllPoliciesFlow(): Flow<List<LegalPolicyEntity>> = dao.getAllPoliciesFlow()
    suspend fun getPolicyByKey(key: String): LegalPolicyEntity? = dao.getPolicyByKey(key)
    suspend fun insertOrUpdatePolicy(policy: LegalPolicyEntity): Long = dao.insertOrUpdatePolicy(policy)
    suspend fun seedDefaultPoliciesIfEmpty() {
        val policies = com.example.data.model.LegalPoliciesDefaults.getDefaultPolicies()
        dao.insertPolicies(policies)
    }

    // --- User Account Deletion ---
    suspend fun deleteUserAccount(userId: Long) {
        dao.deleteWorkerProfile(userId)
        dao.deleteUser(userId)
        dao.deleteSavedItemsForUser(userId)
    }

    // --- Demo & Sample Content Lifecycle ---
    suspend fun reseedRealisticDemoContent() {
        seedDatabaseIfEmpty()
    }

    suspend fun purgeDemoContent() {
        dao.deleteDemoProducts()
        dao.deleteDemoJobs()
        dao.deleteDemoProperties()
        dao.deleteDemoFoodItems()
        dao.deleteDemoRestaurants()
        dao.deleteDemoOrders()
        dao.deleteDemoLedgerEntries()
        dao.deleteDemoAddresses()
        dao.deleteSampleJobs()
        dao.deleteSampleProducts()
    }

    suspend fun countDemoProducts(): Int = dao.countDemoProducts()
    suspend fun countDemoJobs(): Int = dao.countDemoJobs()
}
