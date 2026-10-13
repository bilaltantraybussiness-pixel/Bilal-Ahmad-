package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val phoneNumber: String, // STRICTLY PRIVATE: Only visible in admin dashboard
    val passwordHash: String,
    val district: String,
    val area: String = "",
    val email: String? = null,
    val roles: String = "WORKER,EMPLOYER,SELLER,BUYER", // Multi-role support
    val isActive: Boolean = true,
    val isVerified: Boolean = false,
    val isBanned: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long = System.currentTimeMillis(),
    val securityQuestion: String = "What is your birthplace or favorite Kashmir valley?",
    val securityAnswer: String = "Kashmir"
)

@Entity(tableName = "worker_profiles")
data class WorkerProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val name: String,
    val photoUrl: String = "",
    val district: String,
    val area: String,
    val profession: String, // e.g. Carpenter, Apple Picker, Calligrapher, Plumber, Doctor, etc.
    val occupation: String,
    val skills: String, // Comma-separated, unlimited custom & standard
    val education: String = "", // Optional - NOT mandatory
    val experienceYears: Int = 1,
    val languages: String = "Kashmiri, Urdu",
    val bio: String = "",
    val previousWork: String = "",
    val services: String = "",
    val portfolioPhotos: String = "",
    val availability: String = "Available Now", // "Available Now", "Available From Date", "Seasonal Worker", "Looking for Daily Work"
    val workType: String = "Daily wage", // "Full-time", "Part-time", "Daily wage", "Seasonal", "Contract", "Freelance", "Temporary"
    val workPreference: String = "On-site", // "On-site", "Remote", "Hybrid"
    val expectedPayment: String = "₹800/day",
    val paymentFrequency: String = "Daily", // "Hourly", "Daily", "Weekly", "Monthly", "Per Project"
    val isSeasonalWorker: Boolean = false,
    val seasonalSpecialties: String = "", // Apple Picking, Orchard Pruning, Fruit Packing, Harvesting, Farm Work, Tourism Jobs, Snow Removal
    val isVerified: Boolean = false, // Blue Verified Profile Badge
    val rating: Float = 4.8f,
    val reviewCount: Int = 12,
    val voiceIntroAudioPath: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "jobs")
data class JobEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val employerUserId: Long,
    val employerName: String,
    val title: String,
    val profession: String,
    val description: String,
    val requiredSkills: String,
    val workersNeeded: Int = 1,
    val district: String,
    val area: String,
    val workLocation: String = "On-site",
    val workPreference: String = "On-site", // Remote, Hybrid, On-site
    val workType: String = "Daily wage", // Full-time, Part-time, Contract, Temporary, Seasonal, Freelance, Daily wage, Remote, Hybrid, On-site
    val startDate: String = "Immediate",
    val endDate: String = "",
    val hours: String = "8 AM - 5 PM",
    val paymentAmount: String = "₹900",
    val paymentPeriod: String = "Per Day", // Per Hour, Per Day, Per Week, Per Month, Per Project, Fixed
    val experienceRequired: String = "1+ years",
    val requirements: String = "Hardworking, reliable",
    val isSeasonal: Boolean = false,
    val seasonalCategory: String = "None", // Apple Picking, Fruit Picking, Orchard Work, Fruit Packing, Harvesting, Farm Work, Tourism Jobs, Snow Removal
    val status: String = "ACTIVE", // ACTIVE, CLOSED, MODERATED
    val voiceAudioPath: String = "",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "job_applications")
data class JobApplicationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val jobId: Long,
    val jobTitle: String,
    val employerUserId: Long,
    val applicantUserId: Long,
    val applicantName: String,
    val applicantProfession: String,
    val applicantDistrict: String,
    val expectedPay: String = "",
    val availabilityNote: String = "Available immediately",
    val coverNote: String = "",
    val skillsSummary: String = "",
    val experienceYears: Int = 1,
    val portfolioLink: String = "",
    val optionalVoicePath: String = "",
    val status: String = "SUBMITTED", // SUBMITTED, UNDER_REVIEW, ACCEPTED, REJECTED
    val appliedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val sellerUserId: Long,
    val sellerName: String,
    val title: String,
    val description: String,
    val category: String, // Electronics, Mobile Phones, Computers, Furniture, Vehicles, Auto Parts, Clothing, Shoes, Books, Appliances, Tools, Agricultural Equipment, Farm Products, Handicrafts, Kashmiri Handicrafts, Carpets, Traditional Clothing, Art, Calligraphy, Sports Equipment, Musical Instruments, Home & Garden, Other
    val condition: String = "Good", // Brand New, New, Like New, Good, Used, Needs Repair
    val isSecondHand: Boolean = false,
    val price: Double = 0.0,
    val isNegotiable: Boolean = true,
    val quantity: Int = 1,
    val district: String,
    val area: String,
    val imageUri: String = "",
    val imageUris: String = "", // Comma-separated list of up to 10 photos
    val shopId: Long = 0, // Linked Shop if sold by a registered Shop
    val stockQuantity: Int = 10,
    val availableSizes: String = "S, M, L, XL",
    val availableColors: String = "Natural, Maroon, Pine Green",
    val genderTarget: String = "Unisex", // Men, Women, Kids, Unisex
    val culturalProductType: String = "", // Pheran, Kangri, Topi, Kashmiri Handicrafts, Kashmiri Hookah, Copper Utensils, Walnuts, Walnut Seeds, Flowers, Agriculture Products, Clothing, Food
    val isResellable: Boolean = true,
    val supplierPrice: Double = 0.0, // Base supplier wholesale price for resellers
    val maxResellerMargin: Double = 500.0, // Configured maximum margin a reseller can add
    val platformCommissionRate: Double = 3.33, // Platform fee percentage (e.g. ₹10 on ₹300)
    val voiceIntroAudioPath: String = "", // Voice product description/introduction
    val status: String = "ACTIVE", // ACTIVE, SOLD, MODERATED, REPORTED
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "conversations")
data class ConversationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val user1Id: Long,
    val user2Id: Long,
    val user1Name: String,
    val user2Name: String,
    val subject: String = "General Inquiry",
    val hasFirstVoiceNote: Boolean = false, // VOICE FIRST RULE: First message MUST be a voice note
    val lastMessageText: String = "",
    val lastMessageTime: Long = System.currentTimeMillis(),
    val isBlocked: Boolean = false,
    val blockedByUserId: Long = 0
)

@Entity(tableName = "messages")
data class MessageEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val conversationId: Long,
    val senderUserId: Long,
    val senderName: String,
    val messageType: String = "VOICE", // "VOICE", "TEXT", "IMAGE", "CONTACT_SHARE"
    val textContent: String = "",
    val voiceDurationSeconds: Int = 0,
    val voiceAudioPath: String = "",
    val sharedContactInfo: String = "", // Voluntary contact sharing
    val timestamp: Long = System.currentTimeMillis(),
    val isRead: Boolean = false
)

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterUserId: Long,
    val reporterName: String,
    val reportedType: String, // "USER", "JOB", "PRODUCT", "MESSAGE"
    val targetId: Long,
    val targetTitle: String,
    val reason: String,
    val details: String = "",
    val status: String = "PENDING", // PENDING, UNDER_REVIEW, RESOLVED, REJECTED
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "notifications")
data class NotificationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val title: String,
    val message: String,
    val type: String, // "MESSAGE", "VOICE_MESSAGE", "APPLICATION_UPDATE", "NEW_JOB", "JOB_APPLIED", "ADMIN_NOTICE"
    val referenceId: Long = 0,
    val isRead: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "donations")
data class DonationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val donorName: String,
    val amount: Double,
    val referenceId: String,
    val upiId: String = "rozgarkashmir@upi",
    val status: String = "PENDING", // PENDING, VERIFIED, REJECTED
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "login_activities")
data class LoginActivityEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val phoneNumber: String, // PRIVATE: Only admin can view
    val district: String,
    val activityType: String, // "REGISTER", "LOGIN"
    val timestamp: Long = System.currentTimeMillis(),
    val accountStatus: String = "ACTIVE"
)

@Entity(tableName = "saved_items")
data class SavedItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val itemType: String, // "JOB", "PRODUCT", "WORKER"
    val itemId: Long,
    val title: String,
    val subtitle: String,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "verification_requests")
data class VerificationRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userPhone: String, // Strictly for admin verification verification
    val userProfession: String = "",
    val feeAmount: Double = 49.0, // Fixed ₹49 Verification Fee
    val upiReferenceId: String, // 12-digit UPI UTR / Transaction Reference
    val upiId: String = "rozgarkashmir@upi",
    val idProofType: String = "Aadhaar Card", // "Artisan Card", "Aadhaar Card", "Driving License", "Voter ID", "Business Registration"
    val idProofNumber: String = "",
    val notes: String = "",
    val status: String = "PENDING_VERIFICATION", // "PENDING_VERIFICATION", "APPROVED", "REJECTED"
    val adminReviewNotes: String = "",
    val requestedAt: Long = System.currentTimeMillis(),
    val reviewedAt: Long? = null
)

@Entity(tableName = "reviews")
data class ReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorUserId: Long,
    val authorName: String,
    val targetType: String, // "WORKER", "JOB", "PRODUCT"
    val targetId: Long,
    val targetTitle: String, // Worker name, Job title, or Product title
    val rating: Int, // 1 to 5 Stars
    val reviewText: String,
    val transactionOrJobRef: String = "Completed Job/Service",
    val isReported: Boolean = false,
    val reportReason: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "FLAGGED", "REMOVED"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "kashmiri_contributions")
data class KashmiriContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val authorUserId: Long,
    val authorName: String,
    val authorBio: String = "",
    val type: String = "ARTICLE", // "BOOK", "POETRY", "ARTICLE", "STORY", "FOLKLORE", "RESEARCH"
    val titleKashmiri: String, // Proper Kashmiri / Nastaliq script title
    val titleEnglish: String = "", // Roman / English transliteration or translation
    val content: String, // Full text of article, poem, story, or book sample excerpt
    val summary: String = "",
    val genre: String = "General Literature", // "Poetry (شٲعِری)", "Stories (افسانے / دٔلیلہٕ)", "History (تٲریخ)", "Sufism (تصوّف)", "Language Learning", "Folk Tales (لۄک کَتھ)", "Children", "Research"
    val coverImageUri: String = "",
    val isBook: Boolean = false,
    val isPaid: Boolean = false,
    val price: Double = 0.0, // 0 for free or e.g. 49.0, 99.0, 199.0
    val downloadUrlOrPdf: String = "",
    val pageCount: Int = 1,
    val status: String = "APPROVED", // "PENDING_REVIEW", "APPROVED", "FEATURED", "REJECTED"
    val readsCount: Int = 0,
    val purchaseCount: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "book_purchases")
data class BookPurchaseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val buyerUserId: Long,
    val buyerName: String,
    val bookId: Long,
    val bookTitle: String,
    val authorName: String,
    val amountPaid: Double,
    val paymentRef: String,
    val purchaseDate: Long = System.currentTimeMillis(),
    val downloadAccessGranted: Boolean = true
)

@Entity(tableName = "shops")
data class ShopEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: Long,
    val ownerName: String,
    val shopName: String,
    val shopType: String = "SHOP", // "SHOP", "RESTAURANT", "CAFETERIA", "ARTISAN_STUDIO", "ORCHARD_SUPPLIER"
    val logoUrl: String = "",
    val bannerUrl: String = "",
    val description: String = "",
    val district: String,
    val area: String,
    val address: String = "",
    val contactPhone: String = "", // Protected in backend, displayed when consented
    val contactWhatsapp: String = "",
    val openingHours: String = "9:00 AM - 8:00 PM",
    val deliveryArea: String = "All Valley / District Delivery",
    val isVerified: Boolean = false, // Blue Verified badge
    val membershipType: String = "FREE", // "FREE", "VERIFIED_SHOP" (₹49/mo), "RESTAURANT" (₹99/mo)
    val membershipExpiresAt: Long = 0,
    val rating: Float = 5.0f,
    val reviewCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val totalRevenue: Double = 0.0,
    val status: String = "ACTIVE", // "ACTIVE", "SUSPENDED", "PENDING_VERIFICATION"
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "reseller_listings")
data class ResellerListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val resellerUserId: Long,
    val resellerName: String,
    val resellerPhone: String,
    val originalProductId: Long,
    val productTitle: String,
    val productImageUri: String = "",
    val supplierUserId: Long,
    val supplierName: String,
    val supplierPrice: Double, // Wholesale price
    val resellerPrice: Double, // Customer price
    val resellerMargin: Double, // Profit margin for reseller (resellerPrice - supplierPrice)
    val shareableCode: String, // e.g. "RK-RESELL-10492"
    val totalSharesCount: Int = 0,
    val totalOrdersCount: Int = 0,
    val totalEarnedMargin: Double = 0.0,
    val status: String = "ACTIVE",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val orderNumber: String, // e.g. "RK-ORD-2026-9810"
    val customerId: Long,
    val customerName: String,
    val customerPhone: String, // Private, provided to supplier for fulfillment
    val customerAddress: String,
    val customerDistrict: String,
    val customerArea: String,
    val productId: Long,
    val productTitle: String,
    val productImageUri: String = "",
    val shopId: Long = 0,
    val supplierUserId: Long,
    val supplierName: String,
    val isResoldOrder: Boolean = false,
    val resellerUserId: Long = 0,
    val resellerName: String = "",
    val resellerMargin: Double = 0.0,
    val selectedSize: String = "",
    val selectedColor: String = "",
    val quantity: Int = 1,
    val unitPrice: Double,
    val totalAmount: Double,
    val supplierShareAmount: Double, // Supplier payout (e.g. ₹290 on ₹300)
    val platformFeeAmount: Double, // Admin platform fee (e.g. ₹10 on ₹300)
    val paymentMethod: String = "CASH_ON_DELIVERY", // "CASH_ON_DELIVERY", "ONLINE_UPI", "RAZORPAY"
    val paymentStatus: String = "PENDING", // "PENDING", "PAID", "REFUNDED"
    val paymentTransactionRef: String = "",
    val orderStatus: String = "ORDERED", // "ORDERED", "CONFIRMED", "PREPARING", "DISPATCHED", "OUT_FOR_DELIVERY", "DELIVERED", "CANCELLED"
    val estimatedDeliveryDays: Int = 3,
    val trackingNotes: String = "Order received by Kashmir artisan/supplier",
    val deliveryAddressId: Long = 0,
    val deliveryAddressFormatted: String = "",
    val recipientPhone: String = "",
    val deliveryInstructions: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    val paymentGatewayOrderId: String = "",
    val paymentGatewayPaymentId: String = "",
    val paymentGatewaySignature: String = "",
    val isDemo: Boolean = false,
    val orderedAt: Long = System.currentTimeMillis(),
    val dispatchedAt: Long? = null,
    val deliveredAt: Long? = null
)

@Entity(tableName = "shop_reviews")
data class ShopReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val shopId: Long,
    val orderId: Long = 0, // Linked to completed order to verify legitimate buyer
    val customerUserId: Long,
    val customerName: String,
    val rating: Int = 5, // 1 to 5 Stars
    val reviewText: String,
    val district: String = "",
    val isVerifiedPurchase: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "membership_subscriptions")
data class MembershipSubscriptionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val shopId: Long,
    val shopName: String,
    val planType: String, // "VERIFIED_SHOP" (₹49), "RESTAURANT" (₹99)
    val amountPaid: Double,
    val paymentRef: String,
    val paymentMethod: String = "UPI", // "UPI", "RAZORPAY", "WALLET"
    val status: String = "ACTIVE", // "ACTIVE", "EXPIRED", "PENDING_VERIFICATION"
    val startDate: Long = System.currentTimeMillis(),
    val endDate: Long = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
)

@Entity(tableName = "content_reports")
data class ContentReportEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val reporterUserId: Long,
    val reporterName: String,
    val reportedContentType: String, // "PRODUCT", "SHOP", "VIDEO", "PHOTO", "PROFILE", "JOB", "MESSAGE"
    val contentId: Long,
    val contentTitle: String,
    val contentOwnerUserId: Long = 0,
    val reportCategory: String, // "HARASSMENT", "BULLYING", "PRIVACY_VIOLATION", "NON_CONSENSUAL", "MINORS", "ABUSE", "COPYRIGHT", "INAPPROPRIATE"
    val reportDescription: String,
    val evidenceMetadata: String = "", // IP/Device hash, timestamp, content snapshot
    val status: String = "PENDING", // "PENDING", "UNDER_REVIEW", "ACTIONED_REMOVED", "DISMISSED"
    val actionTaken: String = "", // "CONTENT_REMOVED", "USER_SUSPENDED", "WARNING_ISSUED", "DISMISSED"
    val adminNotes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val resolvedAt: Long? = null
)

@Entity(tableName = "user_blocks")
data class UserBlockEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val blockerUserId: Long,
    val blockedUserId: Long,
    val reason: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "admin_audit_logs")
data class AdminAuditLogEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val adminUserId: Long,
    val adminName: String = "System Admin",
    val actionType: String, // "CONTENT_REMOVAL", "USER_SUSPENSION", "SHOP_VERIFICATION", "MEMBERSHIP_CONFIRMED", "COMMISSION_UPDATED"
    val targetEntity: String,
    val targetId: Long,
    val details: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "property_listings")
data class PropertyListingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: Long,
    val ownerName: String,
    val title: String,
    val description: String,
    val propertyType: String, // "Room", "Shared Room", "Flat / Apartment", "House / Villa", "Commercial Space / Shop", "Plot / Land"
    val listingType: String, // "Rent", "Sale", "Lease"
    val rentOrPrice: Double,
    val deposit: Double = 0.0,
    val district: String,
    val town: String,
    val address: String,
    val furnishing: String = "Unfurnished", // "Furnished", "Semi-Furnished", "Unfurnished"
    val bedrooms: Int = 1,
    val bathrooms: Int = 1,
    val amenities: String = "Electricity, Water Supply, Road Access",
    val imageUris: String = "",
    val status: String = "ACTIVE", // "ACTIVE", "RENTED", "SOLD", "UNDER_REVIEW"
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "restaurants")
data class RestaurantEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ownerUserId: Long,
    val ownerName: String,
    val name: String,
    val description: String,
    val cuisineType: String = "Authentic Kashmiri Wazwan & Bakery",
    val district: String,
    val area: String,
    val address: String,
    val openingHours: String = "10:00 AM - 10:00 PM",
    val isClosedTemporarily: Boolean = false,
    val bannerImageUri: String = "",
    val logoImageUri: String = "",
    val deliveryFee: Double = 30.0,
    val minimumOrder: Double = 150.0,
    val rating: Double = 4.8,
    val totalReviews: Int = 12,
    val status: String = "ACTIVE",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "food_menu_items")
data class FoodMenuItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val restaurantId: Long,
    val name: String,
    val description: String,
    val category: String, // "Wazwan Specialties", "Kashmiri Harissa", "Traditional Bakery (Kandur)", "Rice & Curries", "Beverages & Kehwa", "Fast Food"
    val price: Double,
    val imageUri: String = "",
    val isVegetarian: Boolean = false,
    val isAvailable: Boolean = true,
    val preparationTimeMinutes: Int = 20,
    val portionSizes: String = "Full, Half",
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "kashmiri_word_contributions")
data class KashmiriWordContributionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val contributorUserId: Long,
    val contributorName: String,
    val wordKashmiri: String, // Kashmiri script (e.g. ژھوٗنٛٹ, پۄش, کٲشُر)
    val pronunciation: String = "",
    val meaningUrdu: String,
    val meaningEnglish: String,
    val exampleSentence: String = "",
    val category: String = "Daily Vocabulary", // "Agriculture", "Crafts & Trades", "Flora & Fauna", "Idioms & Proverbs", "Literature", "Daily Vocabulary"
    val dialect: String = "Standard Kashmiri (Maraz / Srinagar)",
    val teamName: String = "Individual",
    val audioUri: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "REJECTED"
    val reviewerNotes: String = "",
    val rewardAmount: Double = 2.0, // Configurable reward credit per approved word
    val rewardPaid: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "contributor_rewards")
data class ContributorRewardEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val totalApprovedWords: Int = 0,
    val streakDays: Int = 1,
    val currentLevel: String = "Level 1: Novice Contributor",
    val totalRewardEarned: Double = 0.0,
    val totalRewardPaid: Double = 0.0,
    val pendingReward: Double = 0.0,
    val upiIdMasked: String = "",
    val bankDetailsMasked: String = "",
    val status: String = "ACTIVE",
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "platform_settings")
data class PlatformSettingsEntity(
    @PrimaryKey val id: Long = 1,
    val membershipPriceMonthly: Double = 99.0, // Configurable paid membership (₹99/month)
    val verifiedBadgeReviewFee: Double = 49.0,
    val productCommissionPercent: Double = 5.0, // 5% for new and second-hand items
    val propertySaleCommissionPercent: Double = 5.0, // 5% of qualifying agreed sale price
    val propertyRentalCommissionPercent: Double = 5.0, // 5% of first month's rent
    val bookCommissionPercent: Double = 2.0, // 2% for authorized books and literature
    val foodCommissionPercent: Double = 1.0, // 1% platform commission on food
    val resellerCommissionPercent: Double = 5.0, // 5% for reseller transactions
    val productCommissionEnabled: Boolean = true,
    val propertySaleCommissionEnabled: Boolean = true,
    val propertyRentalCommissionEnabled: Boolean = true,
    val bookCommissionEnabled: Boolean = true,
    val foodCommissionEnabled: Boolean = true,
    val resellerCommissionEnabled: Boolean = true,
    val includeDeliveryInCommission: Boolean = false,
    val includeTaxesInCommission: Boolean = false,
    val bookMaxOriginalPrice: Double = 500.0,
    val bookMaxAuthorizedEditionPrice: Double = 100.0,
    val wordRewardPoolBudget: Double = 50000.0,
    val rewardPerApprovedWord: Double = 2.0,
    val minimumPayoutAmount: Double = 500.0,
    val effectiveDate: String = "Immediate",
    val announcementBanner: String = "Welcome to Rozgar Kashmir — Valley's Verified Employment & Artisan Marketplace",
    val announcementBannerActive: Boolean = true,
    val demoModeLabelEnabled: Boolean = false, // Production default: NO fake demo badges for customers
    val isProductionMode: Boolean = false, // Set to true for live production mode (suppresses sample records)
    val razorpayKeyId: String = "", // Merchant Key ID from Secrets or Admin
    val googleMapsApiKey: String = "", // Google Maps / Places API Key
    val enableLivePaymentGateway: Boolean = true,
    val customCategoriesCsv: String = "Kashmiri Handicrafts,Pheran & Winter Crafts,Copperware,Walnut Wood,Saffron & Spices,Shawls,Art & Calligraphy,Food & Wazwan,Books,Electronics,Second-Hand",
    val maintenanceMode: Boolean = false,
    val requireVerificationForSelling: Boolean = false,
    val updatedAt: Long = System.currentTimeMillis(),
    val updatedByAdminEmail: String = "bilaltantraypersonal@gmail.com"
)

@Entity(tableName = "transaction_ledger")
data class TransactionLedgerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val transactionRef: String, // e.g. "TXN-2026-98102"
    val orderRef: String, // e.g. "ORD-8921" or "PROP-VIEW-12"
    val category: String, // "PRODUCTS", "SECOND_HAND", "RESELLER", "PROPERTY_SALE", "PROPERTY_RENTAL", "BOOKS", "FOOD", "MEMBERSHIP"
    val listingTitle: String,
    val buyerId: Long,
    val buyerName: String,
    val sellerId: Long,
    val sellerName: String,
    val grossAmount: Double,
    val commissionRatePercent: Double,
    val commissionCalculationBasis: String,
    val platformCommission: Double,
    val paymentProviderFee: Double = 0.0,
    val deliveryCharge: Double = 0.0,
    val discountAmount: Double = 0.0,
    val sellerPayableAmount: Double,
    val paymentStatus: String = "PENDING", // "PENDING", "PAID", "REFUNDED", "FAILED"
    val commissionStatus: String = "CALCULATED", // "CALCULATED", "DEDUCTED", "WAIVED"
    val settlementStatus: String = "UNSETTLED", // "UNSETTLED", "SETTLED", "HELD_DISPUTE"
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val completedAt: Long? = null,
    val disputeNotes: String = ""
)

@Entity(tableName = "payout_requests")
data class PayoutRequestEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val userName: String,
    val userRole: String, // "SELLER", "RESELLER", "AUTHOR", "CONTRIBUTOR"
    val amount: Double,
    val upiIdMasked: String,
    val bankAccountMasked: String = "",
    val ifscMasked: String = "",
    val status: String = "PENDING", // "PENDING", "APPROVED", "PAID", "REJECTED"
    val adminNotes: String = "",
    val transactionRef: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val processedAt: Long? = null
)

@Entity(tableName = "worker_bookings")
data class WorkerBookingEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val bookingReference: String, // e.g. "WB-2026-9812"
    val customerUserId: Long,
    val customerName: String,
    val customerPhone: String,
    val customerDistrict: String,
    val customerArea: String,
    val customerAddress: String,
    val workerUserId: Long,
    val workerName: String,
    val workerProfession: String,
    val serviceRequired: String,
    val scheduledDate: String, // e.g. "2026-10-15"
    val scheduledTime: String = "Morning (9:00 AM - 1:00 PM)",
    val bookingType: String = "DAILY", // "HOURLY", "DAILY", "PROJECT"
    val agreedRate: Double = 0.0,
    val totalEstimatedCost: Double = 0.0,
    val status: String = "REQUESTED", // "REQUESTED", "ACCEPTED", "IN_PROGRESS", "COMPLETED", "REJECTED", "CANCELLED"
    val customerNotes: String = "",
    val workerNotes: String = "",
    val completionNotes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "delivery_addresses")
data class DeliveryAddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: Long,
    val label: String = "HOME", // "HOME", "WORK", "OTHER"
    val recipientName: String,
    val recipientPhone: String,
    val houseFlatFloor: String = "",
    val buildingName: String = "",
    val street: String = "",
    val locality: String = "",
    val landmark: String = "",
    val district: String = "Srinagar",
    val state: String = "Jammu & Kashmir",
    val pincode: String = "190001",
    val latitude: Double = 34.0837,
    val longitude: Double = 74.7973,
    val formattedAddress: String,
    val deliveryInstructions: String = "",
    val isDefault: Boolean = false,
    val isDemo: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "legal_policies")
data class LegalPolicyEntity(
    @PrimaryKey val policyKey: String, // "privacy_policy", "terms_and_conditions", "refund_cancellation", etc.
    val title: String,
    val category: String = "LEGAL", // "LEGAL", "COMMERCE", "SAFETY", "USER"
    val contentMarkdown: String,
    val lastUpdated: String = "October 2026",
    val version: String = "2.4",
    val isPublished: Boolean = true,
    val updatedAt: Long = System.currentTimeMillis()
)
