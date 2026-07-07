package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

sealed class Screen {
    object Login : Screen()
    object Register : Screen()
    object MainHub : Screen() // Directs to the correct role-specific layout
}

class BeysaViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    val repository = BeysaRepository(db)

    // Current screen navigation
    private val _currentScreen = MutableStateFlow<Screen>(Screen.Login)
    val currentScreen: StateFlow<Screen> = _currentScreen

    // User & Language flows
    val currentUser: StateFlow<User?> = repository.currentUser
    val currentLanguage: StateFlow<AppLanguage> = repository.currentLanguage

    // Active sub-screens for each role tab
    private val _customerTab = MutableStateFlow("browse") // "browse", "cart", "orders", "profile", "detail"
    val customerTab: StateFlow<String> = _customerTab

    private val _driverTab = MutableStateFlow("deliveries") // "deliveries", "history", "active_delivery"
    val driverTab: StateFlow<String> = _driverTab

    private val _managerTab = MutableStateFlow("dashboard") // "dashboard", "products", "orders", "translations"
    val managerTab: StateFlow<String> = _managerTab

    private val _adminTab = MutableStateFlow("dashboard") // "dashboard", "livestock", "production", "users", "translations", "analytics"
    val adminTab: StateFlow<String> = _adminTab

    // Search and Filter State for Catalog
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory

    // Detail/Editing Selections
    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct

    private val _selectedOrder = MutableStateFlow<Order?>(null)
    val selectedOrder: StateFlow<Order?> = _selectedOrder

    private val _selectedLivestock = MutableStateFlow<Livestock?>(null)
    val selectedLivestock: StateFlow<Livestock?> = _selectedLivestock

    // Login/OTP verification simulation states
    private val _loginPhoneInput = MutableStateFlow("")
    val loginPhoneInput: StateFlow<String> = _loginPhoneInput

    private val _loginOtpInput = MutableStateFlow("")
    val loginOtpInput: StateFlow<String> = _loginOtpInput

    private val _isOtpRequested = MutableStateFlow(false)
    val isOtpRequested: StateFlow<Boolean> = _isOtpRequested

    private val _otpError = MutableStateFlow<String?>(null)
    val otpError: StateFlow<String?> = _otpError

    // Favorites (in-memory simplified favorites)
    private val _favoriteProductIds = MutableStateFlow<Set<Int>>(emptySet())
    val favoriteProductIds: StateFlow<Set<Int>> = _favoriteProductIds

    // Raw flows from Room DB
    val allProducts: StateFlow<List<Product>> = repository.allProducts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val cartItems: StateFlow<List<CartItem>> = repository.cartItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allOrders: StateFlow<List<Order>> = repository.allOrders
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allUsers: StateFlow<List<User>> = repository.allUsers
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLivestock: StateFlow<List<Livestock>> = repository.allLivestock
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allProductionRecords: StateFlow<List<ProductionRecord>> = repository.allProductionRecords
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allLocalizations: StateFlow<List<Localization>> = repository.localizationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Enriched Cart Items
    val enrichedCartItems: StateFlow<List<Pair<Product, Int>>> = combine(cartItems, allProducts) { items, products ->
        items.mapNotNull { item ->
            val product = products.find { it.id == item.productId }
            if (product != null) Pair(product, item.quantity) else null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Filtered Products
    val filteredProducts: StateFlow<List<Product>> = combine(allProducts, searchQuery, selectedCategory) { products, query, category ->
        products.filter { prod ->
            val matchesCategory = category == "All" || prod.category.equals(category, ignoreCase = true)
            val matchesQuery = query.isEmpty() ||
                    prod.nameEn.contains(query, ignoreCase = true) ||
                    prod.nameAm.contains(query, ignoreCase = true) ||
                    prod.nameAf.contains(query, ignoreCase = true) ||
                    prod.descriptionEn.contains(query, ignoreCase = true)
            matchesCategory && matchesQuery
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Helpers to get localized string from UI
    fun getString(key: String): String {
        return repository.getString(key)
    }

    // Setters
    fun setPhoneInput(phone: String) { _loginPhoneInput.value = phone }
    fun setOtpInput(otp: String) { _loginOtpInput.value = otp }
    fun setSearchQuery(query: String) { _searchQuery.value = query }
    fun setSelectedCategory(cat: String) { _selectedCategory.value = cat }
    fun setCustomerTab(tab: String) { _customerTab.value = tab }
    fun setDriverTab(tab: String) { _driverTab.value = tab }
    fun setManagerTab(tab: String) { _managerTab.value = tab }
    fun setAdminTab(tab: String) { _adminTab.value = tab }
    fun setLanguage(lang: AppLanguage) { repository.setLanguage(lang) }

    fun selectProduct(product: Product?) {
        _selectedProduct.value = product
    }

    fun selectOrder(order: Order?) {
        _selectedOrder.value = order
    }

    fun selectLivestock(livestock: Livestock?) {
        _selectedLivestock.value = livestock
    }

    fun toggleFavorite(productId: Int) {
        val current = _favoriteProductIds.value
        _favoriteProductIds.value = if (current.contains(productId)) {
            current - productId
        } else {
            current + productId
        }
    }

    // AUTH ACTIONS
    fun requestOtp() {
        val phone = _loginPhoneInput.value.trim()
        if (phone.isEmpty()) {
            _otpError.value = "Phone number is required."
            return
        }
        viewModelScope.launch {
            // Check if user exists
            val match = repository.authenticateUser(phone)
            _isOtpRequested.value = true
            _otpError.value = null
            // For testing convenience, we autofill the standard OTP "1234" in state
            _loginOtpInput.value = "1234"
        }
    }

    fun verifyOtpAndLogin() {
        val otp = _loginOtpInput.value.trim()
        if (otp != "1234") {
            _otpError.value = "Invalid verification code. Enter 1234 to log in."
            return
        }
        viewModelScope.launch {
            val phone = _loginPhoneInput.value.trim()
            val user = repository.authenticateUser(phone)
            if (user != null) {
                _currentScreen.value = Screen.MainHub
                _otpError.value = null
                // Reset states
                _isOtpRequested.value = false
                _loginOtpInput.value = ""
                _loginPhoneInput.value = ""
            } else {
                _otpError.value = "Registered account not found. Please click Register Customer."
            }
        }
    }

    fun registerNewCustomer(username: String, name: String, phone: String) {
        viewModelScope.launch {
            val createdUser = repository.registerCustomer(username, name, phone)
            _currentScreen.value = Screen.MainHub
            _otpError.value = null
            _isOtpRequested.value = false
            _loginOtpInput.value = ""
            _loginPhoneInput.value = ""
        }
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
            _currentScreen.value = Screen.Login
            // Reset navigations
            _customerTab.value = "browse"
            _driverTab.value = "deliveries"
            _managerTab.value = "dashboard"
            _adminTab.value = "dashboard"
        }
    }

    // CART ACTIONS
    fun addProductToCart(productId: Int, quantity: Int = 1) {
        viewModelScope.launch {
            repository.addToCart(productId, quantity)
        }
    }

    fun updateCartItemQuantity(productId: Int, quantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(productId, quantity)
        }
    }

    fun deleteCartItem(productId: Int) {
        viewModelScope.launch {
            repository.deleteCartItem(productId)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // CHECKOUT
    fun submitCheckout(address: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            val order = repository.checkout(address)
            if (order != null) {
                onSuccess()
                _customerTab.value = "orders"
            }
        }
    }

    // ORDER MGMT
    fun assignDriverToOrder(orderId: Int, driverUsername: String) {
        viewModelScope.launch {
            repository.assignOrder(orderId, driverUsername)
        }
    }

    fun updateOrderStatus(orderId: Int, status: OrderStatus, photo: String? = null, sig: String? = null, pin: String? = null) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, status, photo, sig, pin)
            // Refresh detailed order if selected
            val active = _selectedOrder.value
            if (active != null && active.id == orderId) {
                _selectedOrder.value = active.copy(
                    status = status,
                    proofPhotoUri = photo,
                    proofSignatureUri = sig,
                    proofPin = pin
                )
            }
        }
    }

    // PRODUCT MGMT
    fun saveProduct(product: Product) {
        viewModelScope.launch {
            repository.saveProduct(product)
        }
    }

    fun deleteProduct(id: Int) {
        viewModelScope.launch {
            repository.deleteProduct(id)
        }
    }

    // LIVESTOCK MGMT
    fun saveLivestock(livestock: Livestock) {
        viewModelScope.launch {
            repository.saveLivestock(livestock)
        }
    }

    fun deleteLivestock(id: String) {
        viewModelScope.launch {
            repository.deleteLivestock(id)
        }
    }

    // PRODUCTION RECORD MGMT
    fun saveProductionRecord(record: ProductionRecord) {
        viewModelScope.launch {
            repository.saveProductionRecord(record)
        }
    }

    fun deleteProductionRecord(id: Int) {
        viewModelScope.launch {
            repository.deleteProductionRecord(id)
        }
    }

    // EMPLOYEE MGMT
    fun saveUser(user: User) {
        viewModelScope.launch {
            repository.saveUser(user)
        }
    }

    fun deleteUser(username: String) {
        viewModelScope.launch {
            repository.deleteUser(username)
        }
    }

    // TRANSLATIONS / LOCALIZATIONS
    fun saveLocalization(localization: Localization) {
        viewModelScope.launch {
            repository.saveLocalization(localization)
        }
    }
}
