package com.helloandroid.codeloading

interface LoadableEntry {
    fun entryName(): String
    fun run(): String
}

class ReflectionTarget : LoadableEntry {
    override fun entryName(): String = "ReflectionTarget"

    override fun run(): String {
        return "direct call 和 reflection call 都命中了同一个业务入口。"
    }
}

class GeneratedRouteTable {
    fun routes(): List<String> {
        return listOf("/home", "/profile", "/diagnosis")
    }
}
