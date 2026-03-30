package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Payment
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PaymentRepository @Inject constructor(
    private val supabase: SupabaseClient
) {
    private val paymentsTable = supabase.postgrest["payments"]

    private fun requireUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
    }

    suspend fun getPayments(): Result<List<Payment>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            paymentsTable.select {
                filter { eq("user_id", userId) }
                order("month", Order.DESCENDING)
            }.decodeList<Payment>()
        }
    }

    suspend fun getPaymentForMonth(yearMonth: String): Result<Payment?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            paymentsTable.select {
                filter {
                    eq("user_id", userId)
                    eq("month", yearMonth)
                }
                limit(1)
            }.decodeSingleOrNull<Payment>()
        }
    }

    suspend fun insertOrUpdatePayment(payment: Payment): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            paymentsTable.upsert(payment.copy(userId = requireUserId())) {
                // Upsert handles both insert and update if the primary key exists
            }
            Unit
        }
    }

    suspend fun getPaymentsForRange(startMonth: String, endMonth: String): Result<List<Payment>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            paymentsTable.select {
                filter {
                    eq("user_id", userId)
                    gte("month", startMonth)
                    lte("month", endMonth)
                }
                order("month", Order.DESCENDING)
            }.decodeList<Payment>()
        }
    }
}
