package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.R
import com.example.data.local.AppDao
import com.example.data.model.OrderEntity
import com.example.data.model.ProductEntity
import com.example.data.model.ShopEntity
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * FirestoreMarketplaceRepository: Handles two-way cloud persistence and real-time synchronization
 * for Rozgar Kashmir Multi-Vendor Marketplace between Room (SQLite) and Firebase Firestore.
 * Supports Seller Dashboard product uploads, stock management, and live order status workflows.
 */
class FirestoreMarketplaceRepository(
    private val context: Context,
    private val dao: AppDao
) {
    private val tag = "FirestoreMarketplaceRepo"
    private val productsCollection = "products"
    private val ordersCollection = "orders"
    private val shopsCollection = "shops"

    val firestore: FirebaseFirestore? by lazy {
        try {
            val dbId = context.getString(R.string.firestore_database_id)
            if (dbId.isNotBlank() && dbId != "(default)") {
                FirebaseFirestore.getInstance(dbId)
            } else {
                FirebaseFirestore.getInstance()
            }
        } catch (e: Exception) {
            try {
                FirebaseFirestore.getInstance()
            } catch (e2: Exception) {
                Log.w(tag, "Could not initialize Firestore instance: ${e2.message}")
                null
            }
        }
    }

    /**
     * Pushes a newly created or updated product to Firestore.
     */
    suspend fun pushProductToFirestore(product: ProductEntity): Boolean {
        return try {
            val db = firestore ?: return false
            val docRef = db.collection(productsCollection).document("product_${product.id}")
            val data = hashMapOf(
                "id" to product.id,
                "sellerUserId" to product.sellerUserId,
                "sellerName" to product.sellerName,
                "shopId" to product.shopId,
                "title" to product.title,
                "description" to product.description,
                "category" to product.category,
                "culturalProductType" to product.culturalProductType,
                "condition" to product.condition,
                "price" to product.price,
                "stockQuantity" to product.stockQuantity,
                "isSecondHand" to product.isSecondHand,
                "isNegotiable" to product.isNegotiable,
                "availableSizes" to product.availableSizes,
                "availableColors" to product.availableColors,
                "genderTarget" to product.genderTarget,
                "district" to product.district,
                "area" to product.area,
                "imageUri" to product.imageUri,
                "imageUris" to product.imageUris,
                "isResellable" to product.isResellable,
                "supplierPrice" to product.supplierPrice,
                "maxResellerMargin" to product.maxResellerMargin,
                "status" to product.status,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
            Log.d(tag, "Product ${product.id} successfully pushed to Firestore")
            true
        } catch (e: Exception) {
            Log.w(tag, "Error pushing product ${product.id} to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Updates product stock quantity both locally and in Firestore.
     */
    suspend fun updateProductStock(productId: Long, newStock: Int): Boolean {
        // 1. Update local database immediately
        dao.updateProductStock(productId, newStock)

        // 2. Synchronize new stock to Firebase Firestore
        return try {
            val db = firestore ?: return true
            val docRef = db.collection(productsCollection).document("product_$productId")
            docRef.update(
                mapOf(
                    "stockQuantity" to newStock,
                    "updatedAt" to System.currentTimeMillis()
                )
            ).awaitTask()
            Log.d(tag, "Product $productId stock updated to $newStock in Firestore")
            true
        } catch (e: Exception) {
            Log.w(tag, "Stock sync to Firestore failed for product $productId: ${e.message}")
            true // Local write already succeeded
        }
    }

    /**
     * Pushes a new order to Firestore.
     */
    suspend fun pushOrderToFirestore(order: OrderEntity): Boolean {
        return try {
            val db = firestore ?: return false
            val docRef = db.collection(ordersCollection).document("order_${order.id}")
            val data = hashMapOf(
                "id" to order.id,
                "orderNumber" to order.orderNumber,
                "customerId" to order.customerId,
                "customerName" to order.customerName,
                "customerPhone" to order.customerPhone,
                "customerAddress" to order.customerAddress,
                "customerDistrict" to order.customerDistrict,
                "customerArea" to order.customerArea,
                "productId" to order.productId,
                "productTitle" to order.productTitle,
                "productImageUri" to order.productImageUri,
                "shopId" to order.shopId,
                "supplierUserId" to order.supplierUserId,
                "supplierName" to order.supplierName,
                "isResoldOrder" to order.isResoldOrder,
                "resellerUserId" to order.resellerUserId,
                "resellerName" to order.resellerName,
                "resellerMargin" to order.resellerMargin,
                "selectedSize" to order.selectedSize,
                "selectedColor" to order.selectedColor,
                "quantity" to order.quantity,
                "unitPrice" to order.unitPrice,
                "totalAmount" to order.totalAmount,
                "paymentMethod" to order.paymentMethod,
                "paymentStatus" to order.paymentStatus,
                "orderStatus" to order.orderStatus,
                "trackingNotes" to order.trackingNotes,
                "orderedAt" to order.orderedAt,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
            Log.d(tag, "Order ${order.orderNumber} successfully pushed to Firestore")
            true
        } catch (e: Exception) {
            Log.w(tag, "Error pushing order ${order.orderNumber} to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Updates order fulfillment status (Pending -> Confirmed -> Packed -> Dispatched -> Out for Delivery -> Delivered)
     * both in Room SQLite and in Firebase Firestore.
     */
    suspend fun updateOrderStatus(
        orderId: Long,
        newStatus: String,
        notes: String
    ): Boolean {
        // 1. Update Room DB first for immediate offline-safe responsiveness
        dao.updateOrderStatus(orderId, newStatus, notes)

        // 2. Synchronize to Firebase Firestore
        return try {
            val db = firestore ?: return true
            val docRef = db.collection(ordersCollection).document("order_$orderId")
            val updates = mutableMapOf<String, Any>(
                "orderStatus" to newStatus,
                "trackingNotes" to notes,
                "updatedAt" to System.currentTimeMillis()
            )
            if (newStatus == "DISPATCHED" || newStatus == "SHIPPED") {
                updates["dispatchedAt"] = System.currentTimeMillis()
            } else if (newStatus == "DELIVERED") {
                updates["deliveredAt"] = System.currentTimeMillis()
                updates["paymentStatus"] = "PAID" // COD marked collected on delivery
            }
            docRef.update(updates).awaitTask()
            Log.d(tag, "Order $orderId status updated to $newStatus in Firestore")
            true
        } catch (e: Exception) {
            Log.w(tag, "Failed to sync order status to Firestore for order $orderId: ${e.message}")
            true
        }
    }

    /**
     * Synchronizes a Shop to Firestore.
     */
    suspend fun pushShopToFirestore(shop: ShopEntity): Boolean {
        return try {
            val db = firestore ?: return false
            val docRef = db.collection(shopsCollection).document("shop_${shop.id}")
            val data = hashMapOf(
                "id" to shop.id,
                "ownerUserId" to shop.ownerUserId,
                "ownerName" to shop.ownerName,
                "shopName" to shop.shopName,
                "shopType" to shop.shopType,
                "logoUrl" to shop.logoUrl,
                "bannerUrl" to shop.bannerUrl,
                "description" to shop.description,
                "district" to shop.district,
                "area" to shop.area,
                "address" to shop.address,
                "contactPhone" to shop.contactPhone,
                "contactWhatsapp" to shop.contactWhatsapp,
                "openingHours" to shop.openingHours,
                "deliveryArea" to shop.deliveryArea,
                "isVerified" to shop.isVerified,
                "membershipType" to shop.membershipType,
                "rating" to shop.rating,
                "reviewCount" to shop.reviewCount,
                "status" to shop.status,
                "updatedAt" to System.currentTimeMillis()
            )
            docRef.set(data, SetOptions.merge()).awaitTask()
            Log.d(tag, "Shop ${shop.shopName} successfully synced to Firestore")
            true
        } catch (e: Exception) {
            Log.w(tag, "Error syncing shop to Firestore: ${e.message}")
            false
        }
    }

    /**
     * Real-time listener for incoming orders for a specific seller/supplier from Firestore.
     */
    fun listenToSellerOrders(supplierUserId: Long): Flow<List<OrderEntity>> = callbackFlow {
        val db = firestore
        if (db == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val registration = db.collection(ordersCollection)
            .whereEqualTo("supplierUserId", supplierUserId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w(tag, "Listen failed for seller orders $supplierUserId", error)
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val ordersList = snapshot.documents.mapNotNull { doc ->
                        try {
                            OrderEntity(
                                id = (doc.get("id") as? Number)?.toLong() ?: 0L,
                                orderNumber = doc.getString("orderNumber") ?: "RK-ORD-${doc.id}",
                                customerId = (doc.get("customerId") as? Number)?.toLong() ?: 0L,
                                customerName = doc.getString("customerName") ?: "Customer",
                                customerPhone = doc.getString("customerPhone") ?: "",
                                customerAddress = doc.getString("customerAddress") ?: "",
                                customerDistrict = doc.getString("customerDistrict") ?: "Srinagar",
                                customerArea = doc.getString("customerArea") ?: "",
                                productId = (doc.get("productId") as? Number)?.toLong() ?: 0L,
                                productTitle = doc.getString("productTitle") ?: "",
                                productImageUri = doc.getString("productImageUri") ?: "",
                                shopId = (doc.get("shopId") as? Number)?.toLong() ?: 0L,
                                supplierUserId = (doc.get("supplierUserId") as? Number)?.toLong() ?: supplierUserId,
                                supplierName = doc.getString("supplierName") ?: "",
                                isResoldOrder = doc.getBoolean("isResoldOrder") ?: false,
                                resellerUserId = (doc.get("resellerUserId") as? Number)?.toLong() ?: 0L,
                                resellerName = doc.getString("resellerName") ?: "",
                                resellerMargin = (doc.get("resellerMargin") as? Number)?.toDouble() ?: 0.0,
                                selectedSize = doc.getString("selectedSize") ?: "",
                                selectedColor = doc.getString("selectedColor") ?: "",
                                quantity = (doc.get("quantity") as? Number)?.toInt() ?: 1,
                                unitPrice = (doc.get("unitPrice") as? Number)?.toDouble() ?: 0.0,
                                totalAmount = (doc.get("totalAmount") as? Number)?.toDouble() ?: 0.0,
                                supplierShareAmount = (doc.get("unitPrice") as? Number)?.toDouble() ?: 0.0,
                                platformFeeAmount = 0.0,
                                paymentMethod = doc.getString("paymentMethod") ?: "CASH_ON_DELIVERY",
                                paymentStatus = doc.getString("paymentStatus") ?: "PENDING",
                                orderStatus = doc.getString("orderStatus") ?: "ORDERED",
                                trackingNotes = doc.getString("trackingNotes") ?: "",
                                orderedAt = (doc.get("orderedAt") as? Number)?.toLong() ?: System.currentTimeMillis()
                            )
                        } catch (e: Exception) {
                            null
                        }
                    }
                    trySend(ordersList)
                }
            }

        awaitClose {
            registration.remove()
        }
    }
}

private suspend fun <T> Task<T>.awaitTask(): T = suspendCancellableCoroutine { continuation ->
    addOnSuccessListener { continuation.resume(it) }
    addOnFailureListener { continuation.resumeWithException(it) }
}
