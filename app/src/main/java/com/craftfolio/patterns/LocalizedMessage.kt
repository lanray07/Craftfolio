package com.craftfolio.patterns

import android.content.Context

/** Keep framework exception text out of the UI; resolve app messages in the active locale. */
class UserFacingFailure(val text: Int) : IllegalStateException()

fun Context.userMessage(error: Throwable, fallback: Int = R.string.action_failed): String =
    getString((error as? UserFacingFailure)?.text ?: fallback)

fun requireUser(condition: Boolean, message: Int) {
    if (!condition) throw UserFacingFailure(message)
}
