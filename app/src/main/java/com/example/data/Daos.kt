package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>

    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun getUserByUsername(username: String): User?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User)

    @Query("DELETE FROM users WHERE username = :username")
    suspend fun deleteUser(username: String)
}

@Dao
interface ProductDao {
    @Query("SELECT * FROM products")
    fun getAllProducts(): Flow<List<Product>>

    @Query("SELECT * FROM products WHERE id = :id LIMIT 1")
    suspend fun getProductById(id: Int): Product?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProduct(product: Product)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: Int)
}

@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getCartItems(): Flow<List<CartItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCartItem(cartItem: CartItem)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: Int)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY orderDate DESC")
    fun getAllOrders(): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE customerUsername = :username ORDER BY orderDate DESC")
    fun getOrdersByCustomer(username: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE assignedDriverUsername = :username ORDER BY orderDate DESC")
    fun getOrdersByDriver(username: String): Flow<List<Order>>

    @Query("SELECT * FROM orders WHERE id = :id LIMIT 1")
    suspend fun getOrderById(id: Int): Order?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: Order)

    @Query("DELETE FROM orders WHERE id = :id")
    suspend fun deleteOrder(id: Int)
}

@Dao
interface LocalizationDao {
    @Query("SELECT * FROM localizations")
    fun getAllLocalizationsFlow(): Flow<List<Localization>>

    @Query("SELECT * FROM localizations")
    suspend fun getAllLocalizations(): List<Localization>

    @Query("SELECT * FROM localizations WHERE stringKey = :key LIMIT 1")
    suspend fun getLocalizationByKey(key: String): Localization?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocalization(localization: Localization)
}

@Dao
interface LivestockDao {
    @Query("SELECT * FROM livestock ORDER BY lastUpdated DESC")
    fun getAllLivestock(): Flow<List<Livestock>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLivestock(livestock: Livestock)

    @Query("DELETE FROM livestock WHERE animalId = :id")
    suspend fun deleteLivestock(id: String)
}

@Dao
interface ProductionRecordDao {
    @Query("SELECT * FROM production_records ORDER BY date DESC")
    fun getAllProductionRecords(): Flow<List<ProductionRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProductionRecord(record: ProductionRecord)

    @Query("DELETE FROM production_records WHERE id = :id")
    suspend fun deleteProductionRecord(id: Int)
}
