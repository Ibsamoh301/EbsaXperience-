package com.example.data

import android.content.Context
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class AppLanguage {
    ENGLISH,
    AMHARIC,
    AFAAN_OROMOO
}

class BeysaRepository(private val db: AppDatabase) {

    private val userDao = db.userDao()
    private val productDao = db.productDao()
    private val cartDao = db.cartDao()
    private val orderDao = db.orderDao()
    private val localizationDao = db.localizationDao()
    private val livestockDao = db.livestockDao()
    private val productionRecordDao = db.productionRecordDao()

    // Active session states
    private val _currentLanguage = MutableStateFlow(AppLanguage.ENGLISH)
    val currentLanguage: StateFlow<AppLanguage> = _currentLanguage

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser

    // Exposing Flows
    val allProducts: Flow<List<Product>> = productDao.getAllProducts()
    val cartItems: Flow<List<CartItem>> = cartDao.getCartItems()
    val allOrders: Flow<List<Order>> = orderDao.getAllOrders()
    val allUsers: Flow<List<User>> = userDao.getAllUsers()
    val allLivestock: Flow<List<Livestock>> = livestockDao.getAllLivestock()
    val allProductionRecords: Flow<List<ProductionRecord>> = productionRecordDao.getAllProductionRecords()
    val localizationsFlow: Flow<List<Localization>> = localizationDao.getAllLocalizationsFlow()

    // Local in-memory caching of generic localizations for super-fast lookup
    private var localizationCache = mapOf<String, Localization>()

    init {
        // Initialize caching and default data asynchronously
        CoroutineScope(Dispatchers.IO).launch {
            preloadDefaultDataIfNeeded()
            refreshLocalizationCache()
        }
    }

    fun setLanguage(language: AppLanguage) {
        _currentLanguage.value = language
    }

    suspend fun refreshLocalizationCache() {
        val list = localizationDao.getAllLocalizations()
        localizationCache = list.associateBy { it.stringKey }
    }

    fun getString(key: String): String {
        val loc = localizationCache[key] ?: return key
        return when (_currentLanguage.value) {
            AppLanguage.ENGLISH -> loc.englishValue
            AppLanguage.AMHARIC -> loc.amharicValue
            AppLanguage.AFAAN_OROMOO -> loc.afaanOromooValue
        }
    }

    // Authentication Actions
    suspend fun authenticateUser(phoneNumber: String): User? {
        // Find user by phone number
        val users = userDao.getAllUsers().first()
        val match = users.find { it.phoneNumber == phoneNumber }
        if (match != null) {
            _currentUser.value = match
        }
        return match
    }

    suspend fun registerCustomer(username: String, name: String, phoneNumber: String): User {
        val newUser = User(
            username = username.ifEmpty { "user_" + System.currentTimeMillis() },
            name = name,
            phoneNumber = phoneNumber,
            role = UserRole.CUSTOMER,
            isVerified = true
        )
        userDao.insertUser(newUser)
        _currentUser.value = newUser
        return newUser
    }

    fun logout() {
        _currentUser.value = null
    }

    // Cart Actions
    suspend fun addToCart(productId: Int, quantity: Int) {
        val existing = cartItems.first().find { it.productId == productId }
        if (existing != null) {
            cartDao.insertCartItem(CartItem(productId, existing.quantity + quantity))
        } else {
            cartDao.insertCartItem(CartItem(productId, quantity))
        }
    }

    suspend fun updateCartQuantity(productId: Int, quantity: Int) {
        if (quantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.insertCartItem(CartItem(productId, quantity))
        }
    }

    suspend fun deleteCartItem(productId: Int) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Order Actions
    suspend fun checkout(deliveryAddress: String): Order? {
        val cartList = cartItems.first()
        if (cartList.isEmpty()) return null
        val user = _currentUser.value ?: return null

        val productList = allProducts.first()
        var total = 0.0
        val itemsStrList = mutableListOf<String>()

        for (item in cartList) {
            val prod = productList.find { it.id == item.productId }
            if (prod != null) {
                total += prod.price * item.quantity
                itemsStrList.add("${item.productId}:${item.quantity}")
                // Deduct stock
                val updatedProd = prod.copy(stockQuantity = (prod.stockQuantity - item.quantity).coerceAtLeast(0))
                productDao.insertProduct(updatedProd)
            }
        }

        val order = Order(
            customerUsername = user.username,
            totalAmount = total,
            deliveryAddress = deliveryAddress,
            itemsJson = itemsStrList.joinToString(","),
            status = OrderStatus.PENDING
        )

        orderDao.insertOrder(order)
        cartDao.clearCart()
        return order
    }

    fun getOrdersForCurrentUser(): Flow<List<Order>> {
        val user = _currentUser.value ?: return MutableStateFlow(emptyList())
        return when (user.role) {
            UserRole.CUSTOMER -> orderDao.getOrdersByCustomer(user.username)
            UserRole.DRIVER -> orderDao.getOrdersByDriver(user.username)
            UserRole.MANAGER, UserRole.SUPER_ADMIN -> orderDao.getAllOrders()
        }
    }

    suspend fun assignOrder(orderId: Int, driverUsername: String) {
        val order = orderDao.getOrderById(orderId)
        if (order != null) {
            orderDao.insertOrder(order.copy(assignedDriverUsername = driverUsername, status = OrderStatus.ASSIGNED))
        }
    }

    suspend fun updateOrderStatus(orderId: Int, status: OrderStatus, proofPhoto: String? = null, proofSig: String? = null, proofPin: String? = null) {
        val order = orderDao.getOrderById(orderId)
        if (order != null) {
            orderDao.insertOrder(order.copy(
                status = status,
                proofPhotoUri = proofPhoto,
                proofSignatureUri = proofSig,
                proofPin = proofPin
            ))
        }
    }

    // Products Management
    suspend fun saveProduct(product: Product) {
        productDao.insertProduct(product)
    }

    suspend fun deleteProduct(id: Int) {
        productDao.deleteProduct(id)
    }

    // Localizations Management
    suspend fun saveLocalization(localization: Localization) {
        localizationDao.insertLocalization(localization)
        refreshLocalizationCache()
    }

    // Livestock Management
    suspend fun saveLivestock(livestock: Livestock) {
        livestockDao.insertLivestock(livestock)
    }

    suspend fun deleteLivestock(id: String) {
        livestockDao.deleteLivestock(id)
    }

    // Production Records Management
    suspend fun saveProductionRecord(record: ProductionRecord) {
        productionRecordDao.insertProductionRecord(record)
    }

    suspend fun deleteProductionRecord(id: Int) {
        productionRecordDao.deleteProductionRecord(id)
    }

    // User/Employee Management
    suspend fun saveUser(user: User) {
        userDao.insertUser(user)
    }

    suspend fun deleteUser(username: String) {
        userDao.deleteUser(username)
    }

    // Preloading Default Data
    private suspend fun preloadDefaultDataIfNeeded() {
        val existingLocs = localizationDao.getAllLocalizations()
        if (existingLocs.isEmpty()) {
            val defaults = listOf(
                Localization("app_title", "BEYSA FARM Delivery", "በይሳ እርሻ ማድረሻ", "Geejjiba Qonnaa Beysaa"),
                Localization("login_title", "BEYSA FARM", "በይሳ እርሻ", "Qonna Beysaa"),
                Localization("login_subtitle", "Enter your registered Ethiopia phone number.", "የተመዘገበበትን የኢትዮጵያ ስልክ ቁጥር ያስገቡ።", "Lakk. bilbila kee galchi."),
                Localization("phone_label", "Phone Number", "የስልክ ቁጥር", "Lakkoofsa Bilbilaa"),
                Localization("otp_label", "Verification Code (OTP)", "የማረጋገጫ ኮድ (OTP)", "Koodii Mirkaneessaa (OTP)"),
                Localization("btn_get_otp", "Request OTP", "ኦቲፒ ጠይቅ", "OTP Gaafadhu"),
                Localization("btn_verify_otp", "Verify & Sign In", "አረጋግጥ እና ግባ", "Mirkaneessi & Seeni"),
                Localization("btn_register", "Create Account", "አዲስ መለያ ፍጠር", "Mila Kaayyi Galmaahi"),
                Localization("register_title", "Customer Registration", "የደንበኛ ምዝገባ", "Galmee Maamiltootaa"),
                Localization("name_label", "Full Name", "ሙሉ ስም", "Maqaa Guutuu"),
                Localization("username_label", "Username", "የመለያ ስም", "Maqaa Fayyadamaa"),
                Localization("logout_label", "Sign Out", "ውጣ", "Ba'i"),
                Localization("dashboard", "Dashboard", "ዳሽቦርድ", "Dabaree Hojii"),
                Localization("home_tab", "Browse", "ምርቶች", "Oomishaalee"),
                Localization("cart_tab", "Cart", "ጋሪ", "Guuboo"),
                Localization("orders_tab", "Orders", "ትዕዛዞች", "Ajajawwan"),
                Localization("profile_tab", "Profile", "የኔ ማህደር", "Eenyummeessa"),
                Localization("search_placeholder", "Search for milk, yogurt, honey, livestock...", "ወተት፣ እርጎ፣ ማር፣ እንስሳትን ፈልግ...", "Aanan, ittittu, damma, loon barbaadi..."),
                Localization("stock_available", "In Stock", "አለ", "Jira"),
                Localization("stock_out", "Out of Stock", "አልቋል", "Dhumateera"),
                Localization("add_to_cart", "Add to Cart", "ወደ ጋሪ ጨምር", "Gara Guubootti Dabali"),
                Localization("delivery_info_header", "Delivery Details", "የማድረሻ መረጃ", "Oodeeffannoo Geejjibaa"),
                Localization("item_added", "Added to Cart!", "ጋሪ ውስጥ ተጨምሯል!", "Guuboo keessa seeneera!"),
                Localization("cart_empty", "Your shopping cart is empty.", "የገበያ ጋሪዎ ባዶ ነው።", "Guuboon kee duwwaadha."),
                Localization("btn_checkout", "Place Order", "ትዕዛዝ አስገባ", "Ajajni Mirkaneessi"),
                Localization("checkout_address", "Delivery Address (Dadar, Ethiopia)", "የማድረሻ አድራሻ (ዳዳር፣ ኢትዮጵያ)", "Teessoo Geejjibaa (Dadar, Itoophiyaa)"),
                Localization("checkout_confirm", "Order submitted successfully!", "ትዕዛዙ በተሳካ ሁኔታ ደርሷል!", "Ajajni sirriitti darbeera!"),
                Localization("order_id", "Order ID", "የትዕዛዝ ቁጥር", "Lakk. Ajajaa"),
                Localization("order_date", "Date", "ቀን", "Guyyaa"),
                Localization("order_status_label", "Status", "ሁኔታ", "Haala"),
                Localization("order_total", "Total", "አጠቃላይ", "Ida'ama"),
                Localization("status_pending", "Pending Approval", "የሚጠበቅ", "Eegamaa"),
                Localization("status_assigned", "Driver Out for Delivery", "በመንገድ ላይ", "Konkolaachisaan Karaarra jira"),
                Localization("status_delivered", "Delivered Successfully", "የደረሰ", "Milkiin Ga'eera"),
                Localization("status_cancelled", "Cancelled", "የተሰረዘ", "Haqameera"),
                Localization("driver_dashboard", "Driver Panel", "የሹፌር ሰሌዳ", "Koree Konkolaachistootaa"),
                Localization("assigned_deliveries", "Assigned Deliveries", "የተሰጡህ ማድረሻዎች", "Geejjiba Siif Kenname"),
                Localization("btn_update_status", "Update Delivery Status", "ሁኔታውን ቀይር", "Haala Geejjibaa Jijjiiri"),
                Localization("proof_signature", "Customer Signature", "የደንበኛ ፊርማ", "Mallattoo Maamilaa"),
                Localization("proof_photo", "Take Photo of Delivery", "ፎቶ አንሳ", "Suura Kaasi"),
                Localization("proof_pin", "Enter Security PIN", "የደህንነት ፒን ያስገቡ", "PIN Nageenyaa Galchi"),
                Localization("manager_dashboard", "Manager Panel", "የስራ አስኪያጅ ሰሌዳ", "Dabaree Bulchiinsaa"),
                Localization("admin_dashboard", "Super Admin Panel", "የባለቤት ሰሌዳ (Super Admin)", "Dabaree Bulchiinsaa Ol'aanaa"),
                Localization("livestock_records", "Livestock Registry", "የእንስሳት መዝገብ", "Galmee Loonii"),
                Localization("production_records_label", "Production Output", "የምርት ምርታማነት", "Galmee Oomishaa"),
                Localization("analytics_reports", "Business Analytics", "የንግድ ትንታኔ", "Gabaasa Bu'aa"),
                Localization("manage_users", "User Directory", "የተጠቃሚዎች ዝርዝር", "Fayyadamtota Galmeessi"),
                Localization("manage_products", "Product catalog", "የምርቶች ማውጫ", "Kataaloogii Oomishaa"),
                Localization("language_manager", "Translate & Localization", "ትርጉም እና ጽሑፎች", "Afaan fi Gulaala Jechootaa"),
                Localization("currency", "ETB", "ብር", "ETB")
            )
            for (loc in defaults) {
                localizationDao.insertLocalization(loc)
            }
        }

        // Preload default user accounts if empty
        val existingUsers = userDao.getAllUsers().first()
        if (existingUsers.isEmpty()) {
            val defaults = listOf(
                User("customer", "Ibsan (Customer)", "0911111111", UserRole.CUSTOMER, true),
                User("driver", "Kebede (Driver)", "0922222222", UserRole.DRIVER, true),
                User("manager", "Tolera (Manager)", "0933333333", UserRole.MANAGER, true),
                User("admin", "Chala (Owner)", "0944444444", UserRole.SUPER_ADMIN, true)
            )
            for (u in defaults) {
                userDao.insertUser(u)
            }
        }

        // Preload default products
        val existingProds = productDao.getAllProducts().first()
        if (existingProds.isEmpty()) {
            val defaultProducts = listOf(
                Product(
                    nameEn = "Fresh Dairy Milk",
                    nameAm = "ትኩስ የላም ወተት",
                    nameAf = "Aanan Haaraa",
                    descriptionEn = "100% pure organic raw cow milk, chilled instantly.",
                    descriptionAm = "100% ንጹህ ኦርጋኒክ ጥሬ ወተት በቀጥታ ከእርሻችን።",
                    descriptionAf = "Aanan qulqulluu %100 kallattiin qe'ee qonnaa keenyaa irraa.",
                    category = "Fresh Milk",
                    price = 90.0,
                    stockQuantity = 150,
                    deliveryInfoEn = "Delivered in sanitized steel cans in 2 hours within Dadar Ethiopia.",
                    deliveryInfoAm = "በዳዳር ከተማ በ2 ሰዓት ውስጥ በንጹህ እቃ ይደርሳል።",
                    deliveryInfoAf = "Sa'aatii 2 keessatti konteenara qabbaneessaadhaan geejjibama."
                ),
                Product(
                    nameEn = "Traditional Rich Yogurt",
                    nameAm = "ባህላዊ እርጎ (Ergo)",
                    nameAf = "Ittittu Qulqulluu",
                    descriptionEn = "Creamy thick yogurt prepared naturally without preservatives.",
                    descriptionAm = "ያለ ምንም ኬሚካል በተፈጥሮ የተዘጋጀ ወፍራም እና ጣፋጭ እርጎ።",
                    descriptionAf = "Ittittu furdaa fi mi'aawaa ta'e kan uumamaan qophaaye.",
                    category = "Yogurt",
                    price = 140.0,
                    stockQuantity = 80,
                    deliveryInfoEn = "Transported cold in high-grade insulated coolers.",
                    deliveryInfoAm = "በቅዝቃዜ ተጠብቆ ይደርሳል።",
                    deliveryInfoAf = "Hanga geejjibaatti qabbanaa'ee tura."
                ),
                Product(
                    nameEn = "Highland Forest Honey",
                    nameAm = "የደጋ ጫካ ማር",
                    nameAf = "Damma Bosonaa",
                    descriptionEn = "Raw wild organic honey harvested from modern hives in Dadar highlands.",
                    descriptionAm = "በዳዳር ተራሮች ላይ ከዘመናዊ ቀፎዎቻችን የተሰበሰበ ንጹህ የጫካ ማር።",
                    descriptionAf = "Damma bosonaa qulqulluu gaggeessaa hammayyaa fi aadaa keenya irraa sassaabame.",
                    category = "Honey",
                    price = 520.0,
                    stockQuantity = 120,
                    deliveryInfoEn = "Hermetically sealed in 1kg glass jars.",
                    deliveryInfoAm = "በመስታወት እቃ በጥንቃቄ የታሸገ።",
                    deliveryInfoAf = "Qorqorroo bilillee keessatti qulqullinaan saamsame."
                ),
                Product(
                    nameEn = "Organic Harar Goat",
                    nameAm = "የሃረር ፍየል (በህይወት)",
                    nameAf = "Ree Harar (Lubbuun)",
                    descriptionEn = "Premium fed, active young goat suitable for holidays or breeding.",
                    descriptionAm = "ለስጋ ወይም ለእርባታ የሚሆን በጥንቃቄ የተመገበ ንቁ የሃረር ፍየል።",
                    descriptionAf = "Ree fayyaalessa gabaa foonii ykn horsiisaaf ta'u.",
                    category = "Goats",
                    price = 5800.0,
                    stockQuantity = 25,
                    deliveryInfoEn = "Delivered live via specialized livestock ventilation truck.",
                    deliveryInfoAm = "በእንስሳት መኪናችን በህይወት ያሉ ሆነው ይደርሳሉ።",
                    deliveryInfoAf = "Konkolaataa keenya addaatiin lubbuun geejjibama."
                ),
                Product(
                    nameEn = "Borana Fattened Bull",
                    nameAm = "የቦረና ሰንጋ በሬ",
                    nameAf = "Sangaa Booranaa Coomaa",
                    descriptionEn = "Famous heavyweight Borana cattle, grain-finished and veterinary approved.",
                    descriptionAm = "ታዋቂው የቦረና በሬ ዝርያ፣ በደንብ የተመገበ እና ከባድ ጤናማ በሬ።",
                    descriptionAf = "Kormaa sangaa Booranaa beekamaa fi fayyaalessa ta'e.",
                    category = "Borana Cattle",
                    price = 55000.0,
                    stockQuantity = 10,
                    deliveryInfoEn = "Live delivery with municipal vet certification.",
                    deliveryInfoAm = "ከእንስሳት ጤና ማረጋገጫ ምስክር ወረቀት ጋር ይደርሳል።",
                    deliveryInfoAf = "Waraqaa mirkaneessa fayyaa loonii waliin geejjibama."
                ),
                Product(
                    nameEn = "Domesticated Buffalo (Jaamusii)",
                    nameAm = "ጃሙሲ (ጎሽ)",
                    nameAf = "Jaamusii Qe'ee",
                    descriptionEn = "Local high-yielding dairy buffalo breed.",
                    descriptionAm = "ከፍተኛ የስብ መጠን ያለው ወተት ለሚሰጠው የቤት ጎሽ እርባታ።",
                    descriptionAf = "Horsiisa Jaamusii aanan qabeenya cooma ol'aanaa qabuuf.",
                    category = "Buffalo (Jaamusii)",
                    price = 72000.0,
                    stockQuantity = 5,
                    deliveryInfoEn = "Direct shipping to farm site in Ethiopia.",
                    deliveryInfoAm = "ወደ እርሻ ቦታዎ በቀጥታ ይደርሳል።",
                    deliveryInfoAf = "Kallattiin gara qe'ee qonnaa keessaniitti geejjibama."
                )
            )
            for (prod in defaultProducts) {
                productDao.insertProduct(prod)
            }
        }

        // Preload default livestock registry
        val existingLivestock = livestockDao.getAllLivestock().first()
        if (existingLivestock.isEmpty()) {
            val defaultLivestock = listOf(
                Livestock(
                    animalId = "BF-C01",
                    species = "Dairy Cattle",
                    breed = "Holstein Friesian Cross",
                    dateOfBirth = "2023-04-12",
                    sex = "Female",
                    weight = 420.0,
                    healthStatus = "Healthy",
                    vaccinationHistory = "FMD (Jan 2026), Anthrax (March 2026)",
                    breedingHistory = "Pregnant (Due Dec 2026)",
                    notes = "High daily milk yielding cow."
                ),
                Livestock(
                    animalId = "BF-B01",
                    species = "Buffalo (Jaamusii)",
                    breed = "Murrah Buffalo",
                    dateOfBirth = "2022-08-15",
                    sex = "Female",
                    weight = 510.0,
                    healthStatus = "Healthy",
                    vaccinationHistory = "Anthrax (Feb 2026)",
                    breedingHistory = "None",
                    notes = "Very docile, produces milk with 8% butterfat."
                ),
                Livestock(
                    animalId = "BF-S01",
                    species = "Borana Cattle",
                    breed = "Borana Zebu",
                    dateOfBirth = "2021-11-20",
                    sex = "Male",
                    weight = 620.0,
                    healthStatus = "Healthy",
                    vaccinationHistory = "FMD (Jan 2026), LSD (May 2026)",
                    breedingHistory = "Active stud",
                    notes = "Top-grade breeding bull."
                ),
                Livestock(
                    animalId = "BF-G01",
                    species = "Goats",
                    breed = "Arsi-Bale Goat",
                    dateOfBirth = "2024-02-10",
                    sex = "Female",
                    weight = 35.0,
                    healthStatus = "Under Treatment",
                    vaccinationHistory = "PPR (April 2026)",
                    breedingHistory = "None",
                    notes = "Recovering from minor hoof infection."
                )
            )
            for (l in defaultLivestock) {
                livestockDao.insertLivestock(l)
            }
        }

        // Preload production records
        val existingRecs = productionRecordDao.getAllProductionRecords().first()
        if (existingRecs.isEmpty()) {
            val defaultRecords = listOf(
                ProductionRecord(type = "Milk", quantity = 320.0, date = "2026-07-05", notes = "Morning milk milking output - cattle and buffaloes"),
                ProductionRecord(type = "Honey", quantity = 45.0, date = "2026-07-02", notes = "Hive harvest section C - premium Forest honey"),
                ProductionRecord(type = "Milk", quantity = 310.0, date = "2026-07-04", notes = "Stable evening milking output"),
                ProductionRecord(type = "Honey", quantity = 52.0, date = "2026-06-28", notes = "Summer peak hive yield harvest")
            )
            for (r in defaultRecords) {
                productionRecordDao.insertProductionRecord(r)
            }
        }
    }
}
