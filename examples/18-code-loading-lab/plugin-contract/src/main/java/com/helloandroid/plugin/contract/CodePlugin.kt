package com.helloandroid.plugin.contract

interface CodePlugin {
    val id: String
    val version: String
    fun describe(): String
    fun execute(input: String): String
    fun boundaries(): List<String>
}

interface HotfixPatch {
    val patchId: String
    val targetClass: String
    fun fixedCheckoutTotal(price: Int, discount: Int): Int
    fun patchPlan(): String
}
