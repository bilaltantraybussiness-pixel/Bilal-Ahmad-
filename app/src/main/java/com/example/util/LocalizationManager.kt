package com.example.util

import android.content.Context
import android.content.SharedPreferences
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppLanguage(
    val code: String,
    val displayName: String,
    val nativeName: String,
    val isRtl: Boolean
) {
    ENGLISH("en", "English", "English", false),
    URDU("ur", "Urdu", "اردو", true),
    KASHMIRI("ks", "Kashmiri", "کٲشُر", true)
}

/**
 * LocalizationManager
 * Centralized translation dictionary and state provider for Rozgar Kashmir.
 * Supports:
 * - English (LTR)
 * - Urdu (RTL)
 * - Kashmiri (RTL) with proper Kashmiri script (نستعلیق / کشمیری اعراب)
 * - Automatic persistence in SharedPreferences across app restarts
 */
object LocalizationManager {

    private const val PREFS_NAME = "rozgar_kashmir_prefs"
    private const val KEY_LANGUAGE = "selected_language"

    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage.asStateFlow()

    private var prefs: SharedPreferences? = null

    fun initialize(context: Context) {
        if (prefs == null) {
            prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val savedCode = prefs?.getString(KEY_LANGUAGE, AppLanguage.ENGLISH.code) ?: AppLanguage.ENGLISH.code
            val lang = AppLanguage.entries.find { it.code == savedCode } ?: AppLanguage.ENGLISH
            _currentLanguage.value = lang
        }
    }

    fun setLanguage(context: Context?, language: AppLanguage) {
        _currentLanguage.value = language
        context?.let { ctx ->
            if (prefs == null) {
                prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            }
            prefs?.edit()?.putString(KEY_LANGUAGE, language.code)?.apply()
        }
    }

    fun setLanguage(language: AppLanguage, context: Context? = null) {
        setLanguage(context, language)
    }

    private val translations: Map<String, Map<AppLanguage, String>> = mapOf(
        // App Identity & Tagline
        "app_title" to mapOf(
            AppLanguage.ENGLISH to "ROZGAR KASHMIR",
            AppLanguage.URDU to "روزگار کشمیر",
            AppLanguage.KASHMIRI to "کٲشُر روزگار"
        ),
        "tagline" to mapOf(
            AppLanguage.ENGLISH to "Connecting Skills With Opportunities",
            AppLanguage.URDU to "ہنر کو باوقار روزگار سے جوڑنا",
            AppLanguage.KASHMIRI to "کٲرِیگَرَن تہٕ مَزدُورَن باوَقار روزگار"
        ),

        // Navigation Bar (9 Items)
        "nav_home" to mapOf(
            AppLanguage.ENGLISH to "Home",
            AppLanguage.URDU to "مرکز",
            AppLanguage.KASHMIRI to "گَرہِ"
        ),
        "nav_jobs" to mapOf(
            AppLanguage.ENGLISH to "Jobs",
            AppLanguage.URDU to "ملازمتیں",
            AppLanguage.KASHMIRI to "کٲم"
        ),
        "nav_workers" to mapOf(
            AppLanguage.ENGLISH to "Workers",
            AppLanguage.URDU to "کاریگر",
            AppLanguage.KASHMIRI to "کٲرِیگَر"
        ),
        "nav_post" to mapOf(
            AppLanguage.ENGLISH to "Post",
            AppLanguage.URDU to "درج کریں",
            AppLanguage.KASHMIRI to "نوٚو کٲم"
        ),
        "nav_market" to mapOf(
            AppLanguage.ENGLISH to "Market",
            AppLanguage.URDU to "بازار",
            AppLanguage.KASHMIRI to "کٲشُر بازار"
        ),
        "nav_food" to mapOf(
            AppLanguage.ENGLISH to "Food",
            AppLanguage.URDU to "کھانا",
            AppLanguage.KASHMIRI to "کھؠن"
        ),
        "nav_property" to mapOf(
            AppLanguage.ENGLISH to "Property",
            AppLanguage.URDU to "جائیداد",
            AppLanguage.KASHMIRI to "جائیداد و کرایہ"
        ),
        "nav_messages" to mapOf(
            AppLanguage.ENGLISH to "Messages",
            AppLanguage.URDU to "پیغامات",
            AppLanguage.KASHMIRI to "پیغام"
        ),
        "nav_profile" to mapOf(
            AppLanguage.ENGLISH to "Profile",
            AppLanguage.URDU to "پروفائل",
            AppLanguage.KASHMIRI to "مےٚون پروفائل"
        ),

        // Common Buttons & Actions
        "btn_search" to mapOf(
            AppLanguage.ENGLISH to "Search",
            AppLanguage.URDU to "تلاش کریں",
            AppLanguage.KASHMIRI to "ژھانٛڈِو"
        ),
        "btn_apply" to mapOf(
            AppLanguage.ENGLISH to "Apply Now",
            AppLanguage.URDU to "درخواست دیں",
            AppLanguage.KASHMIRI to "دَرخواست دِیو"
        ),
        "btn_contact" to mapOf(
            AppLanguage.ENGLISH to "Contact",
            AppLanguage.URDU to "رابطہ کریں",
            AppLanguage.KASHMIRI to "رٲبطہ کٔرِو"
        ),
        "btn_contact_seller" to mapOf(
            AppLanguage.ENGLISH to "Contact Seller",
            AppLanguage.URDU to "دکاندار سے رابطہ",
            AppLanguage.KASHMIRI to "دُکاندارَس سٟتؠ رٲبطہ"
        ),
        "btn_save" to mapOf(
            AppLanguage.ENGLISH to "Save",
            AppLanguage.URDU to "محفوظ کریں",
            AppLanguage.KASHMIRI to "مَحفوٗظ کٔرِو"
        ),
        "btn_cancel" to mapOf(
            AppLanguage.ENGLISH to "Cancel",
            AppLanguage.URDU to "منسوخ",
            AppLanguage.KASHMIRI to "مَنسوٗخ"
        ),
        "btn_submit" to mapOf(
            AppLanguage.ENGLISH to "Submit",
            AppLanguage.URDU to "جمع کرائیں",
            AppLanguage.KASHMIRI to "پیش کٔرِو"
        ),
        "btn_retry" to mapOf(
            AppLanguage.ENGLISH to "Retry",
            AppLanguage.URDU to "دوبارہ کوشش کریں",
            AppLanguage.KASHMIRI to "بیٚیہِ کٔرِو کوٗشِش"
        ),
        "btn_refresh" to mapOf(
            AppLanguage.ENGLISH to "Refresh",
            AppLanguage.URDU to "تازہ کریں",
            AppLanguage.KASHMIRI to "تازٕ کٔرِو"
        ),
        "btn_manage_cloud" to mapOf(
            AppLanguage.ENGLISH to "Manage Cloud Profile",
            AppLanguage.URDU to "کلاؤڈ پروفائل کا انتظام",
            AppLanguage.KASHMIRI to "کلاؤڈ پروفائل اِنتِظام"
        ),
        "btn_sync" to mapOf(
            AppLanguage.ENGLISH to "Sync Profile",
            AppLanguage.URDU to "ہم آہنگ کریں",
            AppLanguage.KASHMIRI to "کلاؤڈَس سٟتؠ مِلاوِو"
        ),
        "btn_view_details" to mapOf(
            AppLanguage.ENGLISH to "View Details",
            AppLanguage.URDU to "تفصیلات دیکھیں",
            AppLanguage.KASHMIRI to "تفصیٖل وُچھِو"
        ),
        "btn_post_job" to mapOf(
            AppLanguage.ENGLISH to "Post a Job",
            AppLanguage.URDU to "ملازمت درج کریں",
            AppLanguage.KASHMIRI to "نوٚو کٲم لؠکھِو"
        ),
        "btn_list_product" to mapOf(
            AppLanguage.ENGLISH to "List Product",
            AppLanguage.URDU to "سامان فروخت کے لیے درج کریں",
            AppLanguage.KASHMIRI to "سامان کٕننہٕ خٲطرٕ لؠکھِو"
        ),
        "btn_order_now" to mapOf(
            AppLanguage.ENGLISH to "Order Now",
            AppLanguage.URDU to "آرڈر کریں",
            AppLanguage.KASHMIRI to "آرڈر کٔرِو"
        ),
        "btn_checkout" to mapOf(
            AppLanguage.ENGLISH to "Proceed to Checkout",
            AppLanguage.URDU to "خریداری مکمل کریں",
            AppLanguage.KASHMIRI to "کھؠن خٔریٖدی پانس تام واتٲوِو"
        ),

        // Search & Filters
        "search_hint" to mapOf(
            AppLanguage.ENGLISH to "Search jobs, skills, artisans, food or products...",
            AppLanguage.URDU to "ملازمتیں، ہنر، کاریگر، کھانا یا مصنوعات تلاش کریں...",
            AppLanguage.KASHMIRI to "کٲم، ہُنَر، کٲرِیگَر، کھؠن یا سامان ژھانٛڈِو..."
        ),
        "all_districts" to mapOf(
            AppLanguage.ENGLISH to "All Districts",
            AppLanguage.URDU to "تمام اضلاع",
            AppLanguage.KASHMIRI to "سٲری اَضلاع"
        ),
        "filter_category" to mapOf(
            AppLanguage.ENGLISH to "Filter by Category",
            AppLanguage.URDU to "شعبہ منتخب کریں",
            AppLanguage.KASHMIRI to "شُعبہٕ ژارِو"
        ),

        // Verified Badge & Membership
        "verified_badge" to mapOf(
            AppLanguage.ENGLISH to "Verified Profile",
            AppLanguage.URDU to "تصدیق شدہ پروفائل",
            AppLanguage.KASHMIRI to "تصدِیق شُدٕ پروفائل"
        ),
        "get_verified_title" to mapOf(
            AppLanguage.ENGLISH to "Get Verified Blue Badge (₹49)",
            AppLanguage.URDU to "بلیو ویری فائیڈ بیج حاصل کریں (₹49)",
            AppLanguage.KASHMIRI to "تصدِیق شُدٕ بِلِیو بیج حٲصِل کٔرِو (₹49)"
        ),
        "membership_banner" to mapOf(
            AppLanguage.ENGLISH to "Rozgar Premium Membership (₹99/month)",
            AppLanguage.URDU to "روزگار پریمیئم رکنیت (₹99 ماہانہ)",
            AppLanguage.KASHMIRI to "روزگار پریمیئم رُکنیَت (₹99 پرٛؠتھ رؠتھ)"
        ),
        "membership_desc" to mapOf(
            AppLanguage.ENGLISH to "Priority placement in search results, verified badge, enhanced shop profile & promotional tools.",
            AppLanguage.URDU to "تلاش میں ترجیح، تصدیق شدہ بیج، دکان کی تشہیر اور خصوصی کاروباری سہولیات۔",
            AppLanguage.KASHMIRI to "ژھانٛڈنہِ مَنٛز ترجیٖح، تصدِیق شُدٕ بیج، دُکانہٕ ہِنٛز تشہیٖر تہٕ خصوٗصی کٲروٗبار."
        ),

        // Food & Restaurants Section
        "food_title" to mapOf(
            AppLanguage.ENGLISH to "Kashmiri Food & Restaurants",
            AppLanguage.URDU to "کشمیری کھانا اور ریستوران",
            AppLanguage.KASHMIRI to "کٲشُر کھؠن تہٕ ریستوران"
        ),
        "food_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Authentic Wazwan, Harissa, traditional bakery (Kandur) & cafe delivery across Kashmir",
            AppLanguage.URDU to "اصلی وازوان، ہریسہ، روایتی قندور بیکری اور کیفے ڈلیوری",
            AppLanguage.KASHMIRI to "اصلی وازوان، ہریٖسہٕ، کٲشُر کانٛدُر بیکری تہٕ ہوٹلَن ہُنٛد کھؠن"
        ),
        "tab_wazwan" to mapOf(
            AppLanguage.ENGLISH to "Wazwan Specialties",
            AppLanguage.URDU to "وازوان کے پکوان",
            AppLanguage.KASHMIRI to "وازوانِک کھؠن"
        ),
        "tab_harissa" to mapOf(
            AppLanguage.ENGLISH to "Kashmiri Harissa",
            AppLanguage.URDU to "کشمیری ہریسہ",
            AppLanguage.KASHMIRI to "کٲشُر ہریٖسہٕ"
        ),
        "tab_bakery" to mapOf(
            AppLanguage.ENGLISH to "Traditional Bakery (Kandur)",
            AppLanguage.URDU to "روایتی بیکری (کاندر چوٹ)",
            AppLanguage.KASHMIRI to "کانٛدُر روٹِہ (گِردا، تِلور)"
        ),
        "cart_title" to mapOf(
            AppLanguage.ENGLISH to "Your Food Order",
            AppLanguage.URDU to "آپ کا کھانے کا آرڈر",
            AppLanguage.KASHMIRI to "تُہُنٛد کھؠنُک آرڈر"
        ),
        "delivery_address" to mapOf(
            AppLanguage.ENGLISH to "Delivery Address & Landmark",
            AppLanguage.URDU to "ڈلیوری کا پتہ اور نشانی",
            AppLanguage.KASHMIRI to "پتہٕ تہٕ نِشانی"
        ),
        "food_commission_note" to mapOf(
            AppLanguage.ENGLISH to "Platform fee is only 1% to empower local food businesses.",
            AppLanguage.URDU to "مقامی ہوٹلوں کی مدد کے لیے پلیٹ فارم فیس صرف 1٪ ہے۔",
            AppLanguage.KASHMIRI to "مقامی ہوٹلَن ہِنٛدِ مددِ خٲطرٕ چھُ صِرف 1٪ فیس."
        ),

        // Property & Room Rentals Section
        "property_title" to mapOf(
            AppLanguage.ENGLISH to "Property & Room Rentals",
            AppLanguage.URDU to "جائیداد اور کمروں کے کرائے",
            AppLanguage.KASHMIRI to "جائیداد تہٕ کمرَن ہُنٛد کِرایہِ"
        ),
        "property_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Rooms, shared accommodation, flats, shops & houses for rent across Jammu & Kashmir",
            AppLanguage.URDU to "کمرے، فلیٹس، دکانیں اور مکانات برائے کرایہ و فروخت",
            AppLanguage.KASHMIRI to "کمرٕ، فلیٹ، دُکان تہٕ مَکان کِرایہِ یا کٕننہٕ خٲطرٕ"
        ),
        "btn_post_property" to mapOf(
            AppLanguage.ENGLISH to "List Property / Room",
            AppLanguage.URDU to "جائیداد یا کمرہ درج کریں",
            AppLanguage.KASHMIRI to "جائیداد یا کمرٕ درج کٔرِو"
        ),
        "rent_month" to mapOf(
            AppLanguage.ENGLISH to "Rent / month",
            AppLanguage.URDU to "ماہانہ کرایہ",
            AppLanguage.KASHMIRI to "پرٛؠتھ رؠتھ کِرایہِ"
        ),
        "security_deposit" to mapOf(
            AppLanguage.ENGLISH to "Security Deposit",
            AppLanguage.URDU to "سیکورٹی ڈپازٹ",
            AppLanguage.KASHMIRI to "ضَمانَت رَقَم"
        ),
        "property_type" to mapOf(
            AppLanguage.ENGLISH to "Property Type",
            AppLanguage.URDU to "جائیداد کی قسم",
            AppLanguage.KASHMIRI to "جائیداد قِسٕم"
        ),
        "furnishing_status" to mapOf(
            AppLanguage.ENGLISH to "Furnishing",
            AppLanguage.URDU to "سامان کی حالت",
            AppLanguage.KASHMIRI to "سامانُک حالَت"
        ),

        // Daily Kashmiri Word Contribution Program
        "daily_words_title" to mapOf(
            AppLanguage.ENGLISH to "Daily Kashmiri Word Contribution",
            AppLanguage.URDU to "روزانہ کشمیری الفاظ خدمت پروگرام",
            AppLanguage.KASHMIRI to "دوٚہ دِش کٲشِر لَفظَن ہِنٛز خِدمَت"
        ),
        "daily_words_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Help preserve & build the 100,000 Kashmiri word encyclopedia. Earn contributor recognition & rewards!",
            AppLanguage.URDU to "ایک لاکھ کشمیری الفاظ کا ذخیرہ بنانے میں حصہ لیں اور انعامات و اعزاز حاصل کریں۔",
            AppLanguage.KASHMIRI to "100,000 کٲشِر لَفظَن ہُنٛد ذخیرہٕ بَناوَنَس مَنٛز کٔرِو حِصہٕ تہٕ حٲصِل کٔرِو اِنعام!"
        ),
        "btn_contribute_word" to mapOf(
            AppLanguage.ENGLISH to "Contribute a Kashmiri Word",
            AppLanguage.URDU to "کشمیری لفظ درج کریں",
            AppLanguage.KASHMIRI to "کٲشُر لَفٕظ لؠکھِو"
        ),
        "word_kashmiri" to mapOf(
            AppLanguage.ENGLISH to "Kashmiri Word (in Kashmiri script)",
            AppLanguage.URDU to "کشمیری لفظ (کشمیری رسم الخط میں)",
            AppLanguage.KASHMIRI to "کٲشُر لَفٕظ (کٲشِر خَطَس مَنٛز)"
        ),
        "word_urdu" to mapOf(
            AppLanguage.ENGLISH to "Meaning in Urdu",
            AppLanguage.URDU to "اردو میں معنی",
            AppLanguage.KASHMIRI to "اُردُو مَعنی"
        ),
        "word_english" to mapOf(
            AppLanguage.ENGLISH to "Meaning in English",
            AppLanguage.URDU to "انگریزی میں معنی",
            AppLanguage.KASHMIRI to "اَنٛگریٖزی مَعنی"
        ),
        "word_example" to mapOf(
            AppLanguage.ENGLISH to "Example Sentence in Kashmiri",
            AppLanguage.URDU to "کشمیری میں مثال کا جملہ",
            AppLanguage.KASHMIRI to "کٲشِر زَبانہِ مَنٛز جُملہٕ مِثال"
        ),
        "contributor_leaderboard" to mapOf(
            AppLanguage.ENGLISH to "Top Kashmiri Language Contributors",
            AppLanguage.URDU to "بہترین کشمیری خدمت گزار",
            AppLanguage.KASHMIRI to "کٲشِر زَبانہِ ہٕنٛدی ساروٕے کھۄتہٕ بٔڈ خِدمَت گَار"
        ),

        // Kashmiri Literature & Books
        "literature_title" to mapOf(
            AppLanguage.ENGLISH to "Kashmiri Literature & Publishing",
            AppLanguage.URDU to "کشمیری ادب اور کتابیں",
            AppLanguage.KASHMIRI to "کٲشُر اَدَب تہٕ کِتابہٕ"
        ),
        "literature_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Poetry, stories, folklore, dictionaries & essays in original Kashmiri script",
            AppLanguage.URDU to "شاعری، کہانیاں، لغت، لوک کہانیاں اور تحقیقی مضامین",
            AppLanguage.KASHMIRI to "شٲعِری، دٔلیلہٕ، لُک کَتھ، لُغَت تہٕ تَحقیٖقی مَضموٗن"
        ),
        "read_free" to mapOf(
            AppLanguage.ENGLISH to "Read Free",
            AppLanguage.URDU to "مفت پڑھیں",
            AppLanguage.KASHMIRI to "مُفَت پَرِو"
        ),
        "buy_book" to mapOf(
            AppLanguage.ENGLISH to "Buy Book",
            AppLanguage.URDU to "کتاب خریدیں",
            AppLanguage.KASHMIRI to "کِتاب ہیٚن"
        ),
        "publish_book_title" to mapOf(
            AppLanguage.ENGLISH to "Publish Your Kashmiri Writing",
            AppLanguage.URDU to "اپنی کشمیری تحریر یا کتاب شائع کریں",
            AppLanguage.KASHMIRI to "پَنٕنؠ کٲشِر کِتاب یا تَحریٖر چھاپِو"
        ),

        // Marketplace & Reseller System
        "market_title" to mapOf(
            AppLanguage.ENGLISH to "Kashmir Marketplace & Reselling",
            AppLanguage.URDU to "کشمیری بازار اور ری سیلنگ",
            AppLanguage.KASHMIRI to "کٲشُر بازار تہٕ ری سیلنگ"
        ),
        "market_subtitle" to mapOf(
            AppLanguage.ENGLISH to "Buy, sell & resell authentic handicrafts, pherans, kangris, copperware, second-hand & local goods",
            AppLanguage.URDU to "کشمیری دستکاری، فیرن، کانگڑی، تانبے کے برتن اور پرانا سامان خریدیں اور بیچیں",
            AppLanguage.KASHMIRI to "کٲشِر دَستکٲری، پشمیٖنہٕ، پھیٖرَن، کانٛگٕر، تامِبکِ بانہٕ تہٕ نوٚو یا پُران سامان"
        ),
        "tab_all_items" to mapOf(
            AppLanguage.ENGLISH to "All Products",
            AppLanguage.URDU to "تمام سامان",
            AppLanguage.KASHMIRI to "سٲری سامان"
        ),
        "tab_second_hand" to mapOf(
            AppLanguage.ENGLISH to "Second-Hand Market",
            AppLanguage.URDU to "استعمال شدہ سامان (سیکنڈ ہینڈ)",
            AppLanguage.KASHMIRI to "پُران سامان (سیکَنٛڈ ہینٛڈ)"
        ),
        "resell_and_earn" to mapOf(
            AppLanguage.ENGLISH to "Resell & Earn Margin",
            AppLanguage.URDU to "ری سیل کریں اور منافع کمائیں",
            AppLanguage.KASHMIRI to "ری سیل کٔرِو تہٕ کمٲوِو مَنافَہ"
        ),

        // Profile & Cloud Sync
        "profile_title" to mapOf(
            AppLanguage.ENGLISH to "My Profile & Cloud Card",
            AppLanguage.URDU to "میرا پروفائل اور کلاؤڈ کارڈ",
            AppLanguage.KASHMIRI to "مےٚون پروفائل تہٕ کلاؤڈ کارڈ"
        ),
        "cloud_synced" to mapOf(
            AppLanguage.ENGLISH to "Cloud Synced",
            AppLanguage.URDU to "کلاؤڈ سے منسلک",
            AppLanguage.KASHMIRI to "کلاؤڈَس سٟتؠ مِلاوِتھ"
        ),
        "cloud_syncing" to mapOf(
            AppLanguage.ENGLISH to "Syncing...",
            AppLanguage.URDU to "ہم آہنگ ہو رہا ہے...",
            AppLanguage.KASHMIRI to "مِلاوان چھُ..."
        ),
        "cloud_offline" to mapOf(
            AppLanguage.ENGLISH to "Local Mode (Offline)",
            AppLanguage.URDU to "آف لائن موڈ",
            AppLanguage.KASHMIRI to "آف لائِن موڈ"
        ),
        "verified_skills" to mapOf(
            AppLanguage.ENGLISH to "Verified Skills",
            AppLanguage.URDU to "تصدیق شدہ ہنر",
            AppLanguage.KASHMIRI to "تصدِیق شُدٕ ہُنَر"
        ),
        "service_history_title" to mapOf(
            AppLanguage.ENGLISH to "Service History & Contracts",
            AppLanguage.URDU to "پچھلے کام اور پروجیکٹس",
            AppLanguage.KASHMIRI to "پُرانِ کٲم تہٕ معاہدٕ"
        ),
        "privacy_active_notice" to mapOf(
            AppLanguage.ENGLISH to "Privacy Active: Your phone number is hidden and protected from the public.",
            AppLanguage.URDU to "پرائیویسی فعال ہے: آپ کا فون نمبر پوشیدہ اور محفوظ ہے۔",
            AppLanguage.KASHMIRI to "پرٛائیویسی چھِ چالوٗ: تُہُنٛد فون نمبر چھُ لوٗکَن نِش پَنٛنہِ مَنٛز مَحفوٗظ."
        ),

        // Payouts & Earnings Ledger
        "payouts_title" to mapOf(
            AppLanguage.ENGLISH to "Earnings & Payout Ledger",
            AppLanguage.URDU to "کمائی اور ادائیگی کا ریکارڈ",
            AppLanguage.KASHMIRI to "کَمٲئی تہٕ پؠمینٛٹ ہُنٛد کھاتہٕ"
        ),
        "gross_earnings" to mapOf(
            AppLanguage.ENGLISH to "Gross Sales / Earnings",
            AppLanguage.URDU to "کل کمائی",
            AppLanguage.KASHMIRI to "کُل کَمٲئی"
        ),
        "platform_fee" to mapOf(
            AppLanguage.ENGLISH to "Platform Commission",
            AppLanguage.URDU to "پلیٹ فارم کمیشن",
            AppLanguage.KASHMIRI to "پلیٹ فارم فیس"
        ),
        "net_payable" to mapOf(
            AppLanguage.ENGLISH to "Net Available for Payout",
            AppLanguage.URDU to "قابل ادائیگی خالص رقم",
            AppLanguage.KASHMIRI to "حٲصِل گژھَن واجین رَقَم"
        ),
        "btn_request_payout" to mapOf(
            AppLanguage.ENGLISH to "Request Payout (UPI / Bank)",
            AppLanguage.URDU to "رقم نکالنے کی درخواست (یو پی آئی / بینک)",
            AppLanguage.KASHMIRI to "رَقَم کَڈنٕچ دَرخواست (یوٗ پی آئی / بَینٛک)"
        )
    )

    fun getString(key: String, language: AppLanguage = _currentLanguage.value): String {
        return translations[key]?.get(language)
            ?: translations[key]?.get(AppLanguage.ENGLISH)
            ?: key
    }
}

/**
 * Convenient Composable helper for reactive localized strings reacting to current language.
 */
@Composable
fun localizedString(key: String): String {
    val lang by LocalizationManager.currentLanguage.collectAsState()
    return LocalizationManager.getString(key, lang)
}
