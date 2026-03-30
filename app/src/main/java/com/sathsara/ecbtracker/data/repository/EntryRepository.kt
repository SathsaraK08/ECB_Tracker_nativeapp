package com.sathsara.ecbtracker.data.repository

import com.sathsara.ecbtracker.data.model.Entry
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Order
import io.github.jan.supabase.storage.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class EntryRepository @Inject constructor(
    private val supabase: SupabaseClient
) : EntryRepositoryContract {
    private val entriesTable = supabase.postgrest["entries"]
    private val storageBucket = supabase.storage["meter_images"]

    @Serializable
    private data class EntryInsertPayload(
        @SerialName("user_id") val userId: String,
        val date: String,
        val time: String,
        val unit: Double,
        val used: Double,
        val note: String? = null,
        val appliances: List<String>? = null,
        @SerialName("img_url") val imgUrl: String? = null
    )

    private fun requireUserId(): String {
        return supabase.auth.currentUserOrNull()?.id ?: throw Exception("User not logged in")
    }

    private fun toMomentKey(date: String, time: String): String = "$date|$time"

    private fun calculateUsed(currentUnit: Double, previousUnit: Double?): Double {
        return if (previousUnit == null) 0.0 else (currentUnit - previousUnit).coerceAtLeast(0.0)
    }

    private suspend fun getOrderedEntries(userId: String): List<Entry> {
        return entriesTable.select {
            filter { eq("user_id", userId) }
            order("date", Order.ASCENDING)
            order("time", Order.ASCENDING)
        }.decodeList<Entry>()
    }

    private suspend fun updateExistingEntry(entry: Entry) {
        entriesTable.upsert(entry)
    }

    override suspend fun getEntries(
        limit: Long = 20,
        isVerified: Boolean? = null
    ): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            val all = entriesTable.select {
                filter { eq("user_id", userId) }
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
                limit(limit)
            }.decodeList<Entry>()

            when (isVerified) {
                true -> all.filter { !it.imgUrl.isNullOrBlank() }
                false -> all.filter { it.imgUrl.isNullOrBlank() }
                null -> all
            }
        }
    }

    override suspend fun getRecentEntries(limit: Long = 3): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            entriesTable.select {
                filter { eq("user_id", userId) }
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
                limit(limit)
            }.decodeList<Entry>()
        }
    }

    override suspend fun getEntriesForMonth(yearMonth: String): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            val startOfMonth = "$yearMonth-01"
            val endOfMonth = "$yearMonth-31"
            
            entriesTable.select {
                filter {
                    eq("user_id", userId)
                    gte("date", startOfMonth)
                    lte("date", endOfMonth)
                }
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
            }.decodeList<Entry>()
        }
    }
    
    override suspend fun getEntriesForRange(startDate: String, endDate: String): Result<List<Entry>> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            entriesTable.select {
                filter {
                    eq("user_id", userId)
                    gte("date", startDate)
                    lte("date", endDate)
                }
                order("date", Order.DESCENDING)
                order("time", Order.DESCENDING)
            }.decodeList<Entry>()
        }
    }

    override suspend fun insertEntry(entry: Entry, photoFile: File? = null): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            val orderedEntries = getOrderedEntries(userId)
            val targetMomentKey = toMomentKey(entry.date, entry.time)

            if (orderedEntries.any { toMomentKey(it.date, it.time) == targetMomentKey }) {
                throw IllegalArgumentException("A reading already exists for this date and time.")
            }

            val insertIndex = orderedEntries.indexOfFirst {
                toMomentKey(it.date, it.time) > targetMomentKey
            }.let { index ->
                if (index == -1) orderedEntries.size else index
            }

            val previousEntry = orderedEntries.getOrNull(insertIndex - 1)
            val nextEntry = orderedEntries.getOrNull(insertIndex)

            if (previousEntry != null && entry.unit < previousEntry.unit) {
                throw IllegalArgumentException("This reading is lower than the previous saved meter value.")
            }

            if (nextEntry != null && entry.unit > nextEntry.unit) {
                throw IllegalArgumentException("This reading is higher than a later saved meter value.")
            }

            var imgUrl: String? = null
            
            if (photoFile != null && photoFile.exists()) {
                val timestamp = System.currentTimeMillis()
                val safeTime = entry.time.replace(":", "-")
                val path = "$userId/${entry.date}_${safeTime}_$timestamp.jpg"
                
                storageBucket.upload(path, photoFile.readBytes())
                imgUrl = storageBucket.publicUrl(path)
            }

            val finalEntry = entry.copy(
                userId = userId,
                used = calculateUsed(entry.unit, previousEntry?.unit),
                imgUrl = imgUrl
            )
            entriesTable.insert(
                EntryInsertPayload(
                    userId = finalEntry.userId,
                    date = finalEntry.date,
                    time = finalEntry.time,
                    unit = finalEntry.unit,
                    used = finalEntry.used,
                    note = finalEntry.note,
                    appliances = finalEntry.appliances,
                    imgUrl = finalEntry.imgUrl
                )
            )

            if (nextEntry != null && nextEntry.id.isNotBlank()) {
                updateExistingEntry(
                    nextEntry.copy(used = calculateUsed(nextEntry.unit, finalEntry.unit))
                )
            }

            Unit
        }
    }

    override suspend fun deleteEntry(id: String): Result<Unit> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            val orderedEntries = getOrderedEntries(userId)
            val entryIndex = orderedEntries.indexOfFirst { it.id == id }

            if (entryIndex == -1) {
                return@runCatching Unit
            }

            val previousEntry = orderedEntries.getOrNull(entryIndex - 1)
            val nextEntry = orderedEntries.getOrNull(entryIndex + 1)

            entriesTable.delete {
                filter {
                    eq("id", id)
                    eq("user_id", userId)
                }
            }

            if (nextEntry != null && nextEntry.id.isNotBlank()) {
                updateExistingEntry(
                    nextEntry.copy(used = calculateUsed(nextEntry.unit, previousEntry?.unit))
                )
            }

            Unit
        }
    }
    
    override suspend fun getLatestEntryBefore(date: String, time: String): Result<Entry?> = withContext(Dispatchers.IO) {
        Result.runCatching {
            val userId = requireUserId()
            val targetMomentKey = toMomentKey(date, time)
            getOrderedEntries(userId).lastOrNull { toMomentKey(it.date, it.time) < targetMomentKey }
        }
    }
}
