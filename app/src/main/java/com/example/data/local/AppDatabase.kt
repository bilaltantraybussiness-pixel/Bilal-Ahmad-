package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.model.*

@Database(
    entities = [
        UserEntity::class,
        WorkerProfileEntity::class,
        JobEntity::class,
        JobApplicationEntity::class,
        ProductEntity::class,
        ConversationEntity::class,
        MessageEntity::class,
        ReportEntity::class,
        NotificationEntity::class,
        DonationEntity::class,
        LoginActivityEntity::class,
        SavedItemEntity::class,
        VerificationRequestEntity::class,
        ReviewEntity::class,
        KashmiriContributionEntity::class,
        BookPurchaseEntity::class,
        ShopEntity::class,
        ResellerListingEntity::class,
        OrderEntity::class,
        ShopReviewEntity::class,
        MembershipSubscriptionEntity::class,
        ContentReportEntity::class,
        UserBlockEntity::class,
        AdminAuditLogEntity::class,
        PropertyListingEntity::class,
        RestaurantEntity::class,
        FoodMenuItemEntity::class,
        KashmiriWordContributionEntity::class,
        ContributorRewardEntity::class,
        PlatformSettingsEntity::class,
        PayoutRequestEntity::class,
        TransactionLedgerEntity::class,
        WorkerBookingEntity::class,
        DeliveryAddressEntity::class,
        LegalPolicyEntity::class
    ],
    version = 8,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun appDao(): AppDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "rozgar_kashmir_db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
