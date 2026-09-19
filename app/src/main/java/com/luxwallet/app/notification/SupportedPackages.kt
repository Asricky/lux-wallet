package com.luxwallet.app.notification

import com.luxwallet.app.core.model.SourceApp

/**
 * The hard allowlist gate (PRD §8): Lux Wallet only ever processes notifications whose package
 * name is in this map, or in the user's own explicit mapping. Everything else — Gmail, SMS,
 * WhatsApp, any other app — is dropped before it ever reaches a parser, regardless of what its
 * text looks like.
 *
 * Play Store package names change between app releases and differ per variant (myBCA vs BCA
 * mobile, Gojek vs standalone GoPay, Shopee vs standalone ShopeePay), so each source lists every
 * identifier it is publicly known to ship under. When an app still isn't recognised, the user
 * maps its real package name themselves from the Notification Lab (PRD §39) — that mapping is a
 * deliberate, explicit allowlist edit by the user, never a silent expansion of what gets processed.
 */
object SupportedPackages {

    val PACKAGE_TO_SOURCE: Map<String, SourceApp> = mapOf(
        // BCA — myBCA (the "omni" super app), BCA mobile, and the older/alternate ids.
        "com.bca.mybca.omni.android" to SourceApp.MYBCA,
        "com.bca.mybca" to SourceApp.MYBCA,
        "com.bca" to SourceApp.MYBCA,
        "com.bca.android" to SourceApp.MYBCA,

        // SeaBank Indonesia.
        "id.co.bankbkemobile.digitalbank" to SourceApp.SEABANK,
        "com.seabank.mobile" to SourceApp.SEABANK,
        "id.co.seabank.android" to SourceApp.SEABANK,
        "com.sea.seabank" to SourceApp.SEABANK,
        "com.seagroup.seabank" to SourceApp.SEABANK,

        // ShopeePay — inside the Shopee app and as the standalone wallet.
        "com.shopee.id" to SourceApp.SHOPEEPAY,
        "com.shopeepay.id" to SourceApp.SHOPEEPAY,
        "com.shopee.pay" to SourceApp.SHOPEEPAY,
        "com.airpay.id" to SourceApp.SHOPEEPAY,

        // GoPay — inside the Gojek app and as the standalone wallet.
        "com.gojek.app" to SourceApp.GOPAY,
        "com.gojek.gopay" to SourceApp.GOPAY,
        "com.gojek.gopay.app" to SourceApp.GOPAY
    )

    /** Built-in allowlist only. Callers that also honour user mappings use [resolve]. */
    fun sourceForPackage(packageName: String): SourceApp? = PACKAGE_TO_SOURCE[packageName]

    /**
     * Built-in allowlist plus the user's own package → source mappings from the Notification Lab.
     * A user mapping wins, so a mis-sorted package can be corrected without an app update.
     */
    fun resolve(packageName: String, userMappings: Map<String, SourceApp>): SourceApp? =
        userMappings[packageName] ?: PACKAGE_TO_SOURCE[packageName]

    /** Packages this build recognises for [source], for display in Settings / the Notification Lab. */
    fun packagesFor(source: SourceApp): List<String> =
        PACKAGE_TO_SOURCE.filterValues { it == source }.keys.sorted()
}
