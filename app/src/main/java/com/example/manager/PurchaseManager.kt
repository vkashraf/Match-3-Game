package com.example.manager

/**
 * PurchaseManager abstraction to support Google Play Billing integration in future releases.
 */
interface PurchaseManager {
    fun initialize()
    fun purchaseItem(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit)
}

class MockPurchaseManager : PurchaseManager {
    override fun initialize() {}

    override fun purchaseItem(productId: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        // Fictional purchase simulation for offline / dev build
        onSuccess()
    }
}
