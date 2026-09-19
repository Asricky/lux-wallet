package com.luxwallet.app.engine

import org.junit.Assert.assertEquals
import org.junit.Test

class NetWorthEngineTest {

    @Test
    fun calculatesAssetsMinusLiabilities() {
        val result = NetWorthEngine.calculate(
            NetWorthInput(
                accountBalances = listOf(15_000_000, 7_500_000, 25_191, 350_000),
                otherAssetValues = listOf(50_000_000), // e.g. Deposito
                liabilityOutstanding = listOf(1_000_000)
            )
        )
        assertEquals(72_875_191L, result.totalAssets)
        assertEquals(1_000_000L, result.totalLiabilities)
        assertEquals(71_875_191L, result.netWorth)
    }

    @Test
    fun noAssetsOrLiabilities_netWorthIsZero() {
        val result = NetWorthEngine.calculate(NetWorthInput(emptyList(), emptyList(), emptyList()))
        assertEquals(0L, result.netWorth)
    }

    @Test
    fun liabilitiesExceedAssets_netWorthIsNegative() {
        val result = NetWorthEngine.calculate(
            NetWorthInput(accountBalances = listOf(1_000_000), otherAssetValues = emptyList(), liabilityOutstanding = listOf(5_000_000))
        )
        assertEquals(-4_000_000L, result.netWorth)
    }
}
