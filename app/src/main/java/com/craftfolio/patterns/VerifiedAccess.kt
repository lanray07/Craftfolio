package com.craftfolio.patterns

import android.content.SharedPreferences
import org.json.JSONObject

/** Serialises account changes and verified leases so delayed RVS results cannot replace newer state. */
class VerifiedAccess(private val prefs: SharedPreferences, private val environment: String, private val now: () -> Long = System::currentTimeMillis) {
    class Ticket internal constructor(val user: String, val receipt: String, internal val generation: Long, internal val sequence: Long)
    private data class Lease(val until: Long, val checked: Long)
    private var user = if (prefs.getString("environment",null) == environment) prefs.getString("user", null) else null
    private var generation = 0L
    private var sequence = 0L
    private val issued = mutableMapOf<String, Long>()
    private val leases = mutableMapOf<String, Lease>()

    init {
        if (user != null) runCatching {
            val json = prefs.getString("leases", null)
            if (json != null) {
                val stored = JSONObject(json)
                stored.keys().forEach { id -> stored.getJSONObject(id).let { leases[id] = Lease(it.getLong("until"), it.getLong("checked")) } }
            } else {
                // Only migrate legacy leases when their verifier identity is already known to match.
                prefs.getString("receipt", null)?.let { leases[it] = Lease(prefs.getLong("until",0), prefs.getLong("checked",0)) }
            }
        }.onFailure { leases.clear() }
    }

    @Synchronized fun identify(account: String) {
        require(account.isNotBlank())
        if (user != account) {
            user = account; generation++; issued.clear(); leases.clear()
            persist()
        }
    }

    @Synchronized fun begin(account: String, receipt: String): Ticket? {
        if (user != account) return null
        sequence++
        issued[receipt] = sequence
        return Ticket(account, receipt, generation, sequence)
    }

    @Synchronized fun accept(ticket: Ticket, validUntil: Long): Boolean {
        if (ticket.user != user || ticket.generation != generation || issued[ticket.receipt] != ticket.sequence) return false
        val checked = now()
        require(validUntil >= 0 && validUntil <= checked + 72 * 3600000L)
        if (validUntil > checked) leases[ticket.receipt] = Lease(validUntil, checked) else leases.remove(ticket.receipt)
        persist()
        return true
    }

    @Synchronized fun premium(): Boolean = user != null && leases.values.any { Rules.premium(it.until,it.checked,now()) }

    private fun persist() {
        val stored = JSONObject()
        leases.forEach { (id, lease) -> stored.put(id,JSONObject().put("until",lease.until).put("checked",lease.checked)) }
        check(prefs.edit().putString("environment",environment).putString("user",user).putString("leases",stored.toString())
            .remove("receipt").remove("until").remove("checked").commit())
    }
}
