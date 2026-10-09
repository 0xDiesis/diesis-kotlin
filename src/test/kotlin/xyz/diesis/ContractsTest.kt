package xyz.diesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import xyz.diesis.contracts.DiesisStaking

class ContractsTest {
    private val fixed = TestData.publicContracts.filter { it.third != null }

    @Test
    fun `table has all 28 fixed-address contracts in order`() {
        assertEquals(28, DiesisContracts.ALL.size)
        assertEquals(fixed.map { it.first }, DiesisContracts.ALL.map { it.key })
        assertEquals(fixed.map { it.second }, DiesisContracts.ALL.map { it.contractName })
    }

    @Test
    fun `table addresses match canonical json`() {
        val addresses = TestData.canonical["addresses"]
        for ((key, _, addressName) in fixed) {
            val entry = DiesisContracts.byKey(key)!!
            assertEquals(DiesisAddresses.ALL.getValue(addressName!!), entry.address, key)
            assertEquals(addresses[addressName].asString(), entry.address.lowercase(), key)
        }
    }

    @Test
    fun `table ABIs are the generated ABIs`() {
        for (entry in DiesisContracts.ALL) {
            val generated = Class.forName("xyz.diesis.contracts.${entry.contractName}").getField("JSON").get(null)
            assertSame(generated, entry.abi, entry.key)
        }
        assertSame(DiesisStaking.JSON, DiesisContracts.staking.abi)
    }

    @Test
    fun `lookup by key`() {
        assertSame(DiesisContracts.staking, DiesisContracts.byKey("staking"))
        assertEquals(DiesisAddresses.DIESIS_STAKING, DiesisContracts.staking.address)
        assertEquals(DiesisAddresses.LIQUID_STAKED_DS, DiesisContracts.liquidStakedDS.address)
        assertNull(DiesisContracts.byKey("validatorShare"))
    }
}
