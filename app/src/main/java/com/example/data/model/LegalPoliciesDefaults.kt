package com.example.data.model

object LegalPoliciesDefaults {
    fun getDefaultPolicies(): List<LegalPolicyEntity> = listOf(
        LegalPolicyEntity(
            policyKey = "privacy_policy",
            title = "Privacy Policy",
            category = "LEGAL",
            contentMarkdown = """
                # Rozgar Kashmir Privacy Policy
                **Last Updated:** October 2026 | **Effective Date:** Immediate
                
                ### 1. Introduction & Overview
                Rozgar Kashmir ("we", "us", or "our") is dedicated to protecting the privacy, security, and digital dignity of citizens across the Jammu & Kashmir valley. This Privacy Policy details how we collect, store, safeguard, and process your personal and transactional information across our marketplace and employment applications.

                ### 2. Information We Collect
                * **Account & Profile Data:** Full name, district, locality, email address, password hash, and user roles (Buyer, Seller, Worker, Employer).
                * **Strictly Protected Contact Data:** Your mobile phone number is collected solely for two-factor authentication, account security, and authorized administrative compliance. **We do not display your raw telephone number publicly to other users.**
                * **Delivery & Address Information:** Formatted addresses, house/building names, landmarks, geographic coordinates (latitude/longitude), and PIN codes for physical order and logistics fulfillment.
                * **Financial & Settlement Data:** For sellers, artisans, and contributors requesting payouts, we store bank settlement details and UPI Virtual Payment Addresses (VPAs). Account numbers and IFSC codes are securely encrypted and masked (e.g. •••• 8910) in client views.
                * **Communication:** In-app encrypted textual messages and inquiries. Note: Voice, audio, or video calling features are intentionally omitted to safeguard personal privacy and prevent harassment.

                ### 3. Purpose of Processing
                We process your information to:
                1. Authenticate users and facilitate multi-role commerce and job connections.
                2. Calculate, snapshot, and settle platform commissions and seller payables.
                3. Dispatch physical shipments via verified logistics partners.
                4. Moderate content against hate speech, fraudulent schemes, or prohibited products.
                5. Comply with applicable local and national digital consumer protection statutes.

                ### 4. Data Retention & Account Deletion
                You maintain complete ownership of your personal data. You may request irrevocable account deletion directly via the Settings panel. Non-transactional profile records will be expunged within 30 days, while verified accounting ledgers and tax records are retained strictly as required by statutory accounting standards.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "terms_and_conditions",
            title = "Terms & Conditions",
            category = "LEGAL",
            contentMarkdown = """
                # Rozgar Kashmir Terms & Conditions
                **Last Updated:** October 2026
                
                ### 1. Acceptance of Terms
                By creating an account or accessing Rozgar Kashmir, you agree to be bound by these Terms & Conditions. If you do not agree, you must cease using the platform immediately.
                
                ### 2. User Eligibility & Account Integrity
                * Users must provide accurate, verifiable identity details.
                * Impersonation of artisans, business establishments, or government entities is strictly forbidden and subject to instant blacklisting.
                * Account holders are responsible for maintaining the confidentiality of their login credentials.
                
                ### 3. Marketplace Transactions & Snapshotted Commissions
                * Rozgar Kashmir operates as a multi-vendor marketplace connecting Kashmiri artisans, farmers, restaurants, employers, and consumers.
                * Platform commissions (e.g. 5% on general retail, 1% on food, 2% on literature) are snapshotted at the exact moment of order placement. Subsequent administrative fee revisions do not affect past contracts.
                * Sellers receive settlement payouts after delivery confirmation and expiry of the return window.
                
                ### 4. Code of Conduct & Prohibition of Harassment
                * Harassment, fraudulent recruitment, solicitation of illegal labor, or non-consensual contact is strictly prohibited.
                * The platform utilizes messaging-only communication. No voice or video calling is supported or permitted.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "refund_cancellation",
            title = "Refund & Cancellation Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Refund & Cancellation Policy
                **Last Updated:** October 2026
                
                ### 1. Buyer Cancellation Windows
                * **General Marketplace Orders:** May be cancelled by the buyer before the seller dispatches the item (`PROCESSING` status). Once marked `DISPATCHED` or `OUT_FOR_DELIVERY`, cancellations are not permitted without return initiation.
                * **Food & Restaurant Orders:** Due to perishable preparation by culinary kitchens and Wazas, food orders may only be cancelled within 5 minutes of kitchen acceptance.
                
                ### 2. Eligible Returns & Damaged Goods
                * Buyers may claim a refund within 5 days of delivery for damaged goods, mismatched variants (e.g. incorrect pheran size), or counterfeit claims.
                * Photographic evidence must be submitted via the in-app reporting flow.
                
                ### 3. Refund Settlements
                * Approved online payment refunds are credited to the original payment source (UPI/Card/NetBanking) within 5–7 business days via payment gateway reverse settlement.
                * Cash on Delivery (COD) refunds are routed to the buyer's verified UPI ID or nodal bank account upon item collection by the courier.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "shipping_delivery",
            title = "Shipping & Delivery Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Shipping & Delivery Policy
                **Last Updated:** October 2026
                
                ### 1. Delivery Coverage
                Rozgar Kashmir serves all 10 districts of the Kashmir Valley: Srinagar, Baramulla, Anantnag, Budgam, Pulwama, Kupwara, Shopian, Ganderbal, Kulgam, and Bandipora.
                
                ### 2. Standard Timelines
                * **Local Intradistrict Deliveries:** 1–2 business days.
                * **Interdistrict Valley Shipping:** 2–4 business days.
                * **Weather Advisories:** Winter snowfall and mountain pass closures (e.g. Mughal Road, Jawahar Tunnel) may occasionally introduce unavoidable courier transit delays. Real-time tracking notes are maintained on every order.
                
                ### 3. Delivery Addresses
                * Buyers must provide a complete, verified delivery address with district, pin code, and recipient contact number before order placement.
                * COD orders without a confirmed address and valid landmark will be rejected.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "seller_policy",
            title = "Seller & Artisan Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Seller & Artisan Marketplace Policy
                **Last Updated:** October 2026
                
                ### 1. Genuine Craftsmanship Standards
                * All items categorized under Kashmiri Handicrafts, Pashmina, Kani, Paper Mache, or Walnut Wood must adhere to authentic craftsmanship benchmarks. Counterfeit synthetic items passed as handmade cashmere are banned.
                * Geographical Indication (GI) tagged goods must be verifiably sourced.
                
                ### 2. Fulfillment Obligations
                * Sellers must accept or reject incoming orders within 24 hours.
                * Items must be packaged securely in climate-resistant wrapping.
                
                ### 3. Payout Settlement & Banking Confidentiality
                * Sellers receive settlement payouts directly into their registered bank account or UPI VPA.
                * No seller may view or access the private financial accounts, bank details, or order ledgers of another merchant.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "restaurant_food_policy",
            title = "Restaurant & Food Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Restaurant & Food Safety Policy
                **Last Updated:** October 2026
                
                ### 1. Food Hygiene & Preparation
                * All affiliated restaurants, Kandurs (traditional bakeries), and Wazas must adhere to FSSAI sanitary standards and maintain clean commercial kitchens.
                * Halal slaughter compliance is standard across all non-vegetarian Kashmiri dishes.
                
                ### 2. Freshness & Packaging
                * Traditional Wazwan dishes (Rogan Josh, Gushtaba, Rista) and fresh breads (Girda, Lavasa, Sheermal) must be packaged in food-grade insulated containers to maintain temperature during valley transit.
                
                ### 3. Accurate Preparation Times
                * Kitchens must declare realistic prep times (e.g. 20–35 mins). Orders must not be marked `READY` prematurely.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "property_listing_policy",
            title = "Property Listing Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Real Estate & Property Listing Policy
                **Last Updated:** October 2026
                
                ### 1. Legitimate Ownership & Authorization
                * Listings for flats, rooms, student accommodations, houses, orchards, and commercial spaces may only be submitted by genuine property owners or authorized legal agents.
                * Misrepresenting residential property for commercial zoning or listing properties under active civil disputes is prohibited.
                
                ### 2. Disclosed Pricing & Deposits
                * Monthly rents, security deposits, and amenities (hamam, heating, geyser, power backup) must be explicitly disclosed without hidden surprise surcharges.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "community_guidelines",
            title = "Community Guidelines",
            category = "SAFETY",
            contentMarkdown = """
                # Rozgar Kashmir Community Guidelines
                **Last Updated:** October 2026
                
                ### 1. Respect & Mutual Dignity
                Rozgar Kashmir is built upon the time-honored Kashmiri ethic of **Kashmiriyat** — mutual respect, communal harmony, hospitality, and brotherhood.
                
                ### 2. Prohibited Content
                * No abusive language, sectarian bias, or discrimination based on caste, religion, or gender.
                * No fraudulent job offers or demanding upfront interview cash fees from vulnerable job seekers.
                * No unauthorized dissemination of private contact credentials.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "user_safety_policy",
            title = "User Safety Policy",
            category = "SAFETY",
            contentMarkdown = """
                # User Safety & Worker Protection Policy
                **Last Updated:** October 2026
                
                ### 1. On-Site Work Safety
                * Employers hiring seasonal orchard workers, construction masons, electricians, or plumbers must provide safe working conditions and protective gear for hazardous tasks.
                * Winter work (such as roof snow clearing) requires proper harness security.
                
                ### 2. In-App Reporting
                * Users can report suspicious job posters, fraudulent listings, or abusive conduct with one tap.
                * Our moderation desk reviews safety reports within 4 hours.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "account_deletion_policy",
            title = "Account Deletion Policy",
            category = "USER",
            contentMarkdown = """
                # Account Deletion & Right to Be Forgotten
                **Last Updated:** October 2026
                
                ### 1. How to Delete Your Account
                You have the absolute right to terminate your account at any time. Navigate to **Profile > Settings > Privacy & Account > Delete Account**.
                
                ### 2. What Gets Deleted
                * Your public profile, saved items, job applications, address records, and personal profile photos are permanently deleted.
                * Your login credentials and authentication tokens are immediately revoked.
                
                ### 3. What Is Retained
                * Completed financial transaction records and tax invoices are retained in anonymized format to comply with legal auditing and GST requirements.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "payment_policy",
            title = "Payment & Settlement Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Payment & Financial Settlement Policy
                **Last Updated:** October 2026
                
                ### 1. Multi-Option Payment Gateway
                * Rozgar Kashmir supports Cash on Delivery (COD), UPI (Google Pay, PhonePe, Paytm, BHIM), RuPay, Visa/Mastercard Debit and Credit Cards, and Net Banking.
                * Online transactions are encrypted via industry-standard TLS 1.3 cryptographic protocols through PCI-DSS Level 1 certified payment gateways.
                
                ### 2. No Raw Credential Storage
                * We do NOT store complete debit/credit card numbers or CVV codes on our servers.
                * All online payments undergo server-side cryptographic signature verification before an order is marked `PAID`.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "marketplace_rules",
            title = "Marketplace Rules",
            category = "COMMERCE",
            contentMarkdown = """
                # Marketplace Operational Rules
                **Last Updated:** October 2026
                
                ### 1. Listing Integrity
                * Products must have authentic, high-resolution photographs representing the actual item for sale.
                * Stock quantities must be kept up to date. Selling out-of-stock items will incur seller rating penalties.
                
                ### 2. Fair Pricing
                * Exorbitant price gouging on essential seasonal goods (such as Kangris or winter heaters) during heavy snowfall emergencies is strictly prohibited.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "reseller_policy",
            title = "Reseller & Dropshipping Policy",
            category = "COMMERCE",
            contentMarkdown = """
                # Reseller Policy
                **Last Updated:** October 2026
                
                ### 1. Reseller Program
                * Registered Kashmiri youths and micro-entrepreneurs can resell catalog items directly across digital social circles using unique referral codes.
                * The reseller margin (the difference between the supplier wholesale base and customer price) is credited to the reseller's wallet upon verified delivery.
                
                ### 2. Quality Guarantee
                * Suppliers remain responsible for authentic craftsmanship and warranty.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "contact_support",
            title = "Contact & Grievance Support",
            category = "USER",
            contentMarkdown = """
                # Contact & Grievance Redressal
                **Last Updated:** October 2026
                
                ### 1. Support Desk
                * **Email:** support@rozgarkashmir.in
                * **Administrative Office:** Chinar Commercial Complex, Residency Road, Lal Chowk, Srinagar, Jammu & Kashmir 190001
                * **Hours:** Monday to Saturday, 9:30 AM to 6:00 PM IST
                
                ### 2. Grievance Redressal Officer
                In accordance with Indian Digital Commerce Rules, our designated Grievance Officer investigates consumer and merchant concerns within 48 business hours.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        ),
        LegalPolicyEntity(
            policyKey = "data_privacy_settings",
            title = "Data & Privacy Controls",
            category = "USER",
            contentMarkdown = """
                # Data & Privacy Controls
                **Last Updated:** October 2026
                
                ### 1. In-App Privacy Controls
                * **Phone Number Masking:** Your phone number is never shown publicly to buyers or visitors.
                * **Address Visibility:** Only the seller and delivery rider assigned to your specific order can view your delivery coordinates.
                * **Activity Tracking:** We do not sell user behavioral data to third-party ad networks.
                
                ### 2. Permission Controls
                You can toggle location access and notification permissions at any time from your device's system settings.
            """.trimIndent(),
            lastUpdated = "October 2026",
            version = "2.4"
        )
    )
}
