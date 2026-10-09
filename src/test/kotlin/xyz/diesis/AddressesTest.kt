package xyz.diesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.web3j.crypto.Keys
import org.web3j.crypto.WalletUtils

class AddressesTest {
    private val canonicalAddresses: Map<String, String> =
        TestData.canonical["addresses"].properties().associate { (name, value) -> name to value.asString() }

    @Test
    fun `every canonical address is exported in order`() {
        assertEquals(canonicalAddresses.keys.toList(), DiesisAddresses.ALL.keys.toList())
    }

    @Test
    fun `addresses match canonical json and are EIP-55 checksummed`() {
        for ((name, expected) in canonicalAddresses) {
            val actual = DiesisAddresses.ALL.getValue(name)
            assertEquals(expected.lowercase(), actual.lowercase(), name)
            assertEquals(Keys.toChecksumAddress(expected), actual, name)
            assert(WalletUtils.isValidAddress(actual)) { name }
        }
    }

    @Test
    fun `constants and the map agree`() {
        assertEquals("0xd1e5150000000000000000000000000000000001", DiesisAddresses.DIESIS_STAKING)
        assertEquals(DiesisAddresses.DIESIS_SPOT_BOOK, DiesisAddresses.ALL["DIESIS_SPOT_BOOK"])
        assertEquals(DiesisAddresses.PERMIT2, DiesisAddresses.ALL["PERMIT2"])
    }
}
