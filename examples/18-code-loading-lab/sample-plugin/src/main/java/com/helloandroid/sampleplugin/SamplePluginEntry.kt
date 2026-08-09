package com.helloandroid.sampleplugin

import com.helloandroid.plugin.contract.CodePlugin
import com.helloandroid.plugin.contract.HotfixPatch

class SamplePluginEntry : CodePlugin {
    override val id: String = "sample-dynamic-plugin"
    override val version: String = "1.0.0"

    override fun describe(): String {
        return "我是一个被宿主从 APK asset 复制出来，再通过 DexClassLoader 加载的插件入口。"
    }

    override fun execute(input: String): String {
        return "plugin[$version] received: $input"
    }

    override fun boundaries(): List<String> {
        return listOf(
            "contract: 通过 plugin-contract 与宿主共享接口",
            "code: 插件代码来自 sample-plugin-debug.apk",
            "resource: 插件资源不能直接等同于宿主 R，需要独立资源加载策略",
            "native: 插件 so 需要 ABI、解压目录和加载顺序",
            "lifecycle: 插件 Activity 需要宿主代理或系统注册",
            "safety: 插件要校验签名、版本、灰度和回滚"
        )
    }
}

class CheckoutHotfixPatch : HotfixPatch {
    override val patchId: String = "checkout-discount-fix-001"
    override val targetClass: String = "com.helloandroid.codeloading.HostCheckoutCalculator"

    override fun fixedCheckoutTotal(price: Int, discount: Int): Int {
        return price - discount
    }

    override fun patchPlan(): String {
        return "真实热修复常把补丁 dex 放到 dexElements 前面；本 demo 用插件补丁入口替换业务策略，保留同样的核心思想：先命中补丁，再回退原实现。"
    }
}
