package xyz.diesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.TypeEncoder
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Hash
import org.web3j.protocol.core.methods.response.Log
import org.web3j.utils.Numeric
import xyz.diesis.contracts.DiesisStaking
import java.io.File
import java.math.BigInteger

class ReadmeExamplesTest {
    @Test
    fun `function examples encode the right selectors`() {
        assertEquals(
            DiesisStaking.UNCLAIMED_REWARDS_SELECTOR,
            FunctionEncoder.encode(ReadmeExamples.unclaimedRewardsFunction()).substring(0, 10),
        )
        assertEquals(DiesisStaking.STAKE_SELECTOR + "0".repeat(63) + "1", FunctionEncoder.encode(ReadmeExamples.stakeFunction()))
    }

    @Test
    fun `event example decodes into the generated class`() {
        val delegator = "0x00000000000000000000000000000000000000aa"
        val topic = { value: Long -> Numeric.toHexStringWithPrefixZeroPadded(BigInteger.valueOf(value), 64) }
        val log = Log()
        log.topics = listOf(
            Hash.sha3String("Staked(address,uint256,uint256,uint256)"),
            "0x" + TypeEncoder.encode(Address(delegator)),
            topic(1),
            topic(7),
        )
        log.data = "0x" + TypeEncoder.encode(Uint256(BigInteger.TEN))
        val event = ReadmeExamples.decodeStaked(log)!!
        assertEquals(delegator, event.delegator)
        assertEquals(BigInteger.ONE, event.toValidatorId)
        assertEquals(BigInteger.valueOf(7), event.tokenId)
        assertEquals(BigInteger.TEN, event.amount)
    }

    @Test
    fun `README code blocks appear in the compiled examples`() {
        val readme = File(System.getProperty("diesis.readme")).readText()
        val sources = mapOf(
            "kotlin" to File("src/test/kotlin/xyz/diesis/ReadmeExamples.kt").readText(),
            "java" to File("src/test/java/xyz/diesis/ReadmeJavaExample.java").readText(),
        )
        val blocks = Regex("```(kotlin|java)\\n(.*?)```", RegexOption.DOT_MATCHES_ALL).findAll(readme).toList()
        assertTrue(blocks.size >= 4, "expected Kotlin and Java examples in README.md")
        for (block in blocks) {
            if (block.groupValues[2].startsWith("// build.gradle.kts")) continue
            val source = sources.getValue(block.groupValues[1]).lines().map(String::trim).toSet()
            for (line in block.groupValues[2].lines().map(String::trim)) {
                if (line.isEmpty()) continue
                assertTrue(line in source, "README line not in the compiled ${block.groupValues[1]} examples: $line")
            }
        }
    }
}
