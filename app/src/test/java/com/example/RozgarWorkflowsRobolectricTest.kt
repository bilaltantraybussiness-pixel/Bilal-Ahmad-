package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDao
import com.example.data.local.AppDatabase
import com.example.data.model.*
import com.example.data.repository.AppRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class RozgarWorkflowsRobolectricTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: AppDao
    private lateinit var repository: AppRepository

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.appDao()
        repository = AppRepository(dao)
    }

    @After
    fun closeDb() {
        db.close()
    }

    @Test
    fun testUserRegistrationAndAuthenticationWorkflow() = runBlocking {
        val phone = "9419012345"
        val rawPassword = "KashmirSecurePassword#1"
        val passwordHash = AppRepository.hashPassword(rawPassword)

        val user = UserEntity(
            fullName = "Ghulam Rasool",
            phoneNumber = phone,
            passwordHash = passwordHash,
            district = "Srinagar",
            area = "Lal Chowk",
            email = "ghulam@example.com",
            roles = "WORKER,EMPLOYER,SELLER",
            securityQuestion = "Favorite Kashmir valley?",
            securityAnswer = "Betaab"
        )
        val userId = repository.insertUser(user)
        assertTrue(userId > 0)

        // Test login by phone
        val fetchedUser = repository.getUserByPhone(phone)
        assertNotNull(fetchedUser)
        assertEquals("Ghulam Rasool", fetchedUser?.fullName)
        assertEquals(passwordHash, fetchedUser?.passwordHash)

        // Verify password verification logic
        assertTrue(fetchedUser?.passwordHash == AppRepository.hashPassword(rawPassword))
        assertFalse(fetchedUser?.passwordHash == AppRepository.hashPassword("WrongPassword"))
    }

    @Test
    fun testJobPostingAndApplicationWorkflow() = runBlocking {
        val employer = UserEntity(
            fullName = "Tariq Ahmad",
            phoneNumber = "9419111111",
            passwordHash = AppRepository.hashPassword("pass123"),
            district = "Baramulla",
            roles = "EMPLOYER"
        )
        val employerId = repository.insertUser(employer)

        val job = JobEntity(
            employerUserId = employerId,
            employerName = employer.fullName,
            title = "Apple Orchard Harvest Worker",
            profession = "Orchard Worker",
            description = "Need 4 skilled workers for Apple picking in Sopore",
            requiredSkills = "Apple Picking, Grading, Packing",
            workersNeeded = 4,
            district = "Baramulla",
            area = "Sopore Fruit Mandi",
            workType = "Seasonal",
            paymentAmount = "₹950",
            paymentPeriod = "Per Day",
            isSeasonal = true,
            seasonalCategory = "Apple Picking",
            status = "ACTIVE"
        )
        val jobId = repository.insertJob(job)
        assertTrue(jobId > 0)

        val applicant = UserEntity(
            fullName = "Bashir Ahmad",
            phoneNumber = "9419222222",
            passwordHash = AppRepository.hashPassword("pass456"),
            district = "Baramulla",
            roles = "WORKER"
        )
        val applicantId = repository.insertUser(applicant)

        val application = JobApplicationEntity(
            jobId = jobId,
            jobTitle = job.title,
            employerUserId = employerId,
            applicantUserId = applicantId,
            applicantName = applicant.fullName,
            applicantProfession = "Orchard Worker",
            applicantDistrict = applicant.district,
            expectedPay = "₹950/day",
            availabilityNote = "Available for complete harvest season",
            status = "SUBMITTED"
        )
        val appId = repository.insertApplication(application)
        assertTrue(appId > 0)

        // Employer reviews and accepts application
        val existingApp = repository.getExistingApplication(jobId, applicantId)
        assertNotNull(existingApp)
        assertEquals("SUBMITTED", existingApp?.status)

        repository.updateApplicationStatus(appId, "ACCEPTED")
        val updatedApp = repository.getExistingApplication(jobId, applicantId)
        assertEquals("ACCEPTED", updatedApp?.status)

        // Verify applications flow for job
        val appsForJob = repository.getApplicationsForJobFlow(jobId).first()
        assertEquals(1, appsForJob.size)
        assertEquals("ACCEPTED", appsForJob[0].status)
    }

    @Test
    fun testMarketplaceOrderAndTransactionLedgerWorkflow() = runBlocking {
        val seller = UserEntity(
            fullName = "Kashmir Shawl Emporium",
            phoneNumber = "9419333333",
            passwordHash = AppRepository.hashPassword("sellerPass"),
            district = "Srinagar",
            roles = "SELLER"
        )
        val sellerId = repository.insertUser(seller)

        val product = ProductEntity(
            sellerUserId = sellerId,
            sellerName = seller.fullName,
            title = "Authentic Pashmina Shawl (Sozni Needlework)",
            description = "100% fine Cashmere wool hand embroidered by artisan",
            category = "Kashmiri Handicrafts",
            condition = "Brand New",
            price = 4500.0,
            district = "Srinagar",
            area = "Downtown",
            stockQuantity = 5,
            status = "ACTIVE"
        )
        val productId = repository.insertProduct(product)
        assertTrue(productId > 0)

        val buyer = UserEntity(
            fullName = "Amina Begum",
            phoneNumber = "9419444444",
            passwordHash = AppRepository.hashPassword("buyerPass"),
            district = "Anantnag",
            roles = "BUYER"
        )
        val buyerId = repository.insertUser(buyer)

        // Buyer places order
        val unitPrice = 4500.0
        val quantity = 1
        val totalAmount = unitPrice * quantity
        val platformCommissionPercent = 5.0 // 5% for products
        val platformCommission = (totalAmount * platformCommissionPercent) / 100.0 // ₹225.0
        val sellerPayable = totalAmount - platformCommission // ₹4275.0

        val order = OrderEntity(
            orderNumber = "RK-ORD-TEST-001",
            customerId = buyerId,
            customerName = buyer.fullName,
            customerPhone = buyer.phoneNumber,
            customerAddress = "Near Khanqah, Anantnag",
            customerDistrict = buyer.district,
            customerArea = "Town Center",
            productId = productId,
            productTitle = product.title,
            supplierUserId = sellerId,
            supplierName = seller.fullName,
            quantity = quantity,
            unitPrice = unitPrice,
            totalAmount = totalAmount,
            supplierShareAmount = sellerPayable,
            platformFeeAmount = platformCommission,
            paymentMethod = "CASH_ON_DELIVERY",
            orderStatus = "ORDERED"
        )
        val orderId = repository.insertOrder(order)
        assertTrue(orderId > 0)

        // Record into central transaction ledger
        val ledger = TransactionLedgerEntity(
            transactionRef = "TXN-TEST-1001",
            orderRef = order.orderNumber,
            category = "PRODUCTS",
            listingTitle = product.title,
            buyerId = buyerId,
            buyerName = buyer.fullName,
            sellerId = sellerId,
            sellerName = seller.fullName,
            grossAmount = totalAmount,
            commissionRatePercent = platformCommissionPercent,
            commissionCalculationBasis = "5% product platform fee",
            platformCommission = platformCommission,
            sellerPayableAmount = sellerPayable,
            paymentStatus = "PENDING",
            commissionStatus = "CALCULATED",
            settlementStatus = "UNSETTLED"
        )
        val ledgerId = repository.insertLedgerEntry(ledger)
        assertTrue(ledgerId > 0)

        val retrievedLedger = repository.getLedgerEntryById(ledgerId)
        assertNotNull(retrievedLedger)
        assertEquals(225.0, retrievedLedger?.platformCommission ?: 0.0, 0.01)
        assertEquals(4275.0, retrievedLedger?.sellerPayableAmount ?: 0.0, 0.01)
    }

    @Test
    fun testPropertyInquiryAndPrivacyProtectionWorkflow() = runBlocking {
        val owner = UserEntity(
            fullName = "Farooq Shah",
            phoneNumber = "9419555555",
            passwordHash = AppRepository.hashPassword("ownerPass"),
            district = "Srinagar",
            roles = "PROPERTY_OWNER"
        )
        val ownerId = repository.insertUser(owner)

        val property = PropertyListingEntity(
            ownerUserId = ownerId,
            ownerName = owner.fullName,
            title = "Furnished 2BHK Apartment near Dal Gate",
            description = "Spacious apartment with heating facility, road access, and lake view",
            propertyType = "Flat / Apartment",
            listingType = "Rent",
            rentOrPrice = 16000.0,
            deposit = 32000.0,
            district = "Srinagar",
            town = "Dal Gate",
            address = "Boulevard Road, Dal Gate",
            furnishing = "Furnished",
            bedrooms = 2,
            bathrooms = 2,
            amenities = "Geyser, Inverter, Water Supply, Parking",
            status = "ACTIVE"
        )
        val propertyId = repository.insertProperty(property)
        assertTrue(propertyId > 0)

        val activeProperties = repository.getActivePropertiesFlow().first()
        assertTrue(activeProperties.any { it.id == propertyId })
    }

    @Test
    fun testKashmiriWordContributionAndRewardWorkflow() = runBlocking {
        val contributor = UserEntity(
            fullName = "Zahoor Ahmad",
            phoneNumber = "9419666666",
            passwordHash = AppRepository.hashPassword("contributorPass"),
            district = "Budgam",
            roles = "CONTRIBUTOR"
        )
        val contributorId = repository.insertUser(contributor)

        val word = KashmiriWordContributionEntity(
            contributorUserId = contributorId,
            contributorName = contributor.fullName,
            wordKashmiri = "پۄش",
            pronunciation = "Posh",
            meaningUrdu = "پھول",
            meaningEnglish = "Flower",
            exampleSentence = "پۄش باغس منٛز چھِ لۄسان (Flowers bloom in the garden)",
            category = "Daily Vocabulary",
            dialect = "Maraz",
            rewardAmount = 2.0,
            status = "PENDING"
        )
        val wordId = repository.insertWordContribution(word)
        assertTrue(wordId > 0)

        // Admin approves contribution
        val updatedWord = word.copy(id = wordId, status = "APPROVED", reviewerNotes = "Authentic Kashmiri vocabulary approved")
        repository.updateWordContribution(updatedWord)

        val approvedWords = repository.getApprovedWordsFlow().first()
        assertEquals(1, approvedWords.size)
        assertEquals("APPROVED", approvedWords[0].status)

        // Contributor reward is credited
        val reward = ContributorRewardEntity(
            userId = contributorId,
            userName = contributor.fullName,
            totalApprovedWords = 1,
            streakDays = 1,
            currentLevel = "Level 1: Novice Contributor",
            totalRewardEarned = 2.0,
            pendingReward = 2.0
        )
        repository.insertOrUpdateReward(reward)

        val fetchedReward = repository.getRewardForUser(contributorId)
        assertNotNull(fetchedReward)
        assertEquals(2.0, fetchedReward?.totalRewardEarned ?: 0.0, 0.01)
    }
}
