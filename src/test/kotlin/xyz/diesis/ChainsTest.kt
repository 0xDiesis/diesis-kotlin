package xyz.diesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ChainsTest {
    @Test
    fun `chain ids`() {
        assertEquals(1980L, DiesisChains.diesis.id)
        assertEquals(19803L, DiesisChains.diesisTestnet.id)
        assertEquals(listOf(DiesisChains.diesis, DiesisChains.diesisTestnet), DiesisChains.ALL)
    }

    @Test
    fun `chains match canonical json`() {
        val chains = TestData.canonical["chains"]
        for ((key, chain) in listOf("mainnet" to DiesisChains.diesis, "testnet" to DiesisChains.diesisTestnet)) {
            val expected = chains[key]
            assertEquals(expected["id"].asLong(), chain.id)
            assertEquals(expected["name"].asString(), chain.name)
            assertEquals(expected["rpcUrl"].asString(), chain.rpcUrl)
            assertEquals(expected["explorerUrl"].asString(), chain.explorerUrl)
            assertEquals(expected["testnet"].asBoolean(), chain.testnet)
            assertEquals(expected["nativeCurrency"]["name"].asString(), chain.nativeCurrency.name)
            assertEquals(expected["nativeCurrency"]["symbol"].asString(), chain.nativeCurrency.symbol)
            assertEquals(expected["nativeCurrency"]["decimals"].asInt(), chain.nativeCurrency.decimals)
        }
    }

    @Test
    fun `names and native currency`() {
        assertEquals("Diesis", DiesisChains.diesis.name)
        assertEquals("Diesis Testnet", DiesisChains.diesisTestnet.name)
        assertEquals(NativeCurrency("DS", "DS", 18), DiesisChains.diesis.nativeCurrency)
        assertEquals(NativeCurrency("DS", "DS", 18), DiesisChains.diesisTestnet.nativeCurrency)
        assertFalse(DiesisChains.diesis.testnet)
        assertTrue(DiesisChains.diesisTestnet.testnet)
    }
}
