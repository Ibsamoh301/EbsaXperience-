package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    CUSTOMER,
    DRIVER,
    MANAGER,
    SUPER_ADMIN
}

enum class OrderStatus {
    PENDING,
    ASSIGNED,
    DELIVERED,
    CANCELLED
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val username: String, // phone or username
    val name: String,
    val phoneNumber: String,
    val role: UserRole,
    val isVerified: Boolean = false,
    val profilePhotoUri: String? = null
)

@Entity(tableName = "products")
data class Product(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nameEn: String,
    val nameAm: String,
    val nameAf: String,
    val descriptionEn: String,
    val descriptionAm: String,
    val descriptionAf: String,
    val category: String, // "Dairy", "Honey", "Livestock"
    val price: Double,
    val stockQuantity: Int,
    val isAvailable: Boolean = true,
    val deliveryInfoEn: String,
    val deliveryInfoAm: String,
    val deliveryInfoAf: String,
    val imageUrl: String = ""
)

@Entity(tableName = "cart_items")
data class CartItem(
    @PrimaryKey val productId: Int,
    val quantity: Int
)

@Entity(tableName = "orders")
data class Order(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val customerUsername: String,
    val orderDate: Long = System.currentTimeMillis(),
    val status: OrderStatus = OrderStatus.PENDING,
    val totalAmount: Double,
    val deliveryAddress: String,
    val assignedDriverUsername: String? = null,
    val proofPhotoUri: String? = null,
    val proofSignatureUri: String? = null,
    val proofPin: String? = null,
    val itemsJson: String // Format: "productId:quantity,productId:quantity"
)

@Entity(tableName = "localizations")
data class Localization(
    @PrimaryKey val stringKey: String,
    val englishValue: String,
    val amharicValue: String,
    val afaanOromooValue: String
)

@Entity(tableName = "livestock")
data class Livestock(
    @PrimaryKey val animalId: String, // Tag ID e.g. "BF-001"
    val species: String, // "Dairy Cattle", "Buffalo (Jaamusii)", "Borana Cattle", "Goats", "Beehives"
    val breed: String,
    val dateOfBirth: String,
    val sex: String,
    val weight: Double,
    val healthStatus: String, // "Healthy", "Under Treatment", "Sick"
    val vaccinationHistory: String,
    val breedingHistory: String,
    val photoUrl: String = "",
    val notes: String = "",
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "production_records")
data class ProductionRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val type: String, // "Milk" or "Honey"
    val quantity: Double, // Liters or Kg
    val date: String,
    val notes: String = ""
)
