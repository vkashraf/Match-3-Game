package com.example.game.economy

data class EconomyTransaction(
    val transactionId: String,
    val type: String,
    val source: TransactionSource,
    val amount: Long,
    val timestamp: Long = System.currentTimeMillis(),
    val referenceId: String? = null
)
