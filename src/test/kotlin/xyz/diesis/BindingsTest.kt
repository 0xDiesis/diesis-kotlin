package xyz.diesis

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Assumptions.assumeTrue
import org.junit.jupiter.api.Test
import org.web3j.abi.FunctionEncoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.StructType
import org.web3j.abi.datatypes.generated.Bytes32
import org.web3j.crypto.Hash
import org.web3j.protocol.core.methods.response.AbiDefinition
import xyz.diesis.contracts.IDiesisMarkets
import xyz.diesis.contracts.IDiesisBundleEscrow
import java.lang.reflect.Modifier
import java.math.BigInteger

/**
 * Parses the ABI embedded in every generated contract object with web3j, and
 * checks the generated signatures, selectors, topics, and classes against it.
 */
class BindingsTest {
    private fun generated(contract: String): Class<*> = Class.forName("xyz.diesis.contracts.$contract")

    private fun embeddedAbi(contract: String): String = generated(contract).getField("JSON").get(null) as String

    private fun abi(json: String): List<AbiDefinition> {
        val node = TestData.mapper.readTree(json)
        return (0 until node.size()).map { TestData.mapper.treeToValue(node[it], AbiDefinition::class.java) }
    }

    private fun canonicalType(type: AbiDefinition.NamedType): String =
        if (type.type.startsWith("tuple")) {
            type.components.joinToString(",", "(", ")", transform = ::canonicalType) + type.type.removePrefix("tuple")
        } else {
            type.type
        }

    private fun signature(definition: AbiDefinition): String =
        definition.name + definition.inputs.joinToString(",", "(", ")", transform = ::canonicalType)

    @Test
    fun `every embedded ABI parses with web3j`() {
        for ((_, contract, _) in TestData.publicContracts) {
            val definitions = abi(embeddedAbi(contract))
            assertTrue(definitions.any { it.type == "function" }, contract)
        }
    }

    @Test
    fun `embedded ABIs match the contract artifacts`() {
        assumeTrue(TestData.contractsDir.resolve("out").isDirectory, "contracts checkout not found")
        for ((_, contract, _) in TestData.publicContracts) {
            val artifact = TestData.mapper.readTree(TestData.contractsDir.resolve("out/$contract.sol/$contract.json"))
            assertEquals(artifact["abi"], TestData.mapper.readTree(embeddedAbi(contract)), contract)
        }
    }

    @Test
    fun `signatures, selectors, topics, and classes match the ABI`() {
        val problems = mutableListOf<String>()
        for ((_, contract, _) in TestData.publicContracts) {
            val type = generated(contract)
            val constants = type.fields
                .filter { Modifier.isStatic(it.modifiers) && it.type == String::class.java }
                .associate { it.name to it.get(null) as String }
            val definitions = abi(embeddedAbi(contract))
            val overloaded = definitions.filter { it.type == "function" }.groupingBy { it.name }.eachCount().filterValues { it > 1 }.keys
            for (definition in definitions) {
                val kind = when (definition.type) {
                    "function" -> ""
                    "event" -> "_EVENT"
                    "error" -> "_ERROR"
                    else -> continue
                }
                val signature = signature(definition)
                // abi-typegen appends a number to a constant name that would clash,
                // such as DEFAULT_YEAR_PRICE_SIGNATURE2 for `defaultYearPrice()`
                // next to `DEFAULT_YEAR_PRICE()`.
                val matches = constants.mapNotNull { (name, value) ->
                    val match = Regex("(.*)_SIGNATURE(\\d*)").matchEntire(name) ?: return@mapNotNull null
                    val prefix = match.groupValues[1]
                    val sameKind = if (kind.isEmpty()) !prefix.endsWith("_EVENT") && !prefix.endsWith("_ERROR") else prefix.endsWith(kind)
                    if (value == signature && sameKind) prefix to match.groupValues[2] else null
                }
                if (matches.size != 1) {
                    problems += "$contract: ${definition.type} $signature has signature constants $matches"
                    continue
                }
                val (prefix, clash) = matches.single()
                val hash = Hash.sha3String(signature)
                val (constant, expected) = if (definition.type == "event") {
                    "${prefix}_TOPIC$clash" to hash
                } else {
                    "${prefix}_SELECTOR$clash" to hash.substring(0, 10)
                }
                if (constants[constant] != expected) {
                    problems += "$contract.$constant != $expected"
                }
                if (definition.inputs.isEmpty()) continue
                val base = definition.name.replaceFirstChar(Char::uppercaseChar)
                val className = when (definition.type) {
                    "function" -> base + (if (definition.name in overloaded) prefix.substringAfterLast('_') else "") + "Params"
                    "event" -> base + "Event"
                    else -> base + "Error"
                }
                // A tuple class can take the name first, and then the argument class
                // gets a number, like IDiesisErc20Factory.Deploy2Params.
                val exactNameIsTuple = type.declaredClasses.any {
                    it.simpleName == className && StructType::class.java.isAssignableFrom(it)
                }
                val nested = type.declaredClasses.firstOrNull {
                    Regex("${Regex.escape(className)}\\d*").matches(it.simpleName) &&
                        !StructType::class.java.isAssignableFrom(it)
                } ?: if (definition.type == "function" && definition.name !in overloaded && exactNameIsTuple) {
                    type.declaredClasses.firstOrNull {
                        Regex("${Regex.escape(base)}\\d+Params").matches(it.simpleName) &&
                            !StructType::class.java.isAssignableFrom(it)
                    }
                } else null
                if (nested == null) {
                    problems += "$contract.$className is missing"
                    continue
                }
                val fields = nested.declaredFields.count { !Modifier.isStatic(it.modifiers) }
                if (fields != definition.inputs.size) {
                    problems += "$contract.${nested.simpleName} has $fields fields, ABI has ${definition.inputs.size}"
                }
            }
        }
        assertEquals(emptyList<String>(), problems)
    }

    @Test
    fun `bundle escrow uses initial wire selectors`() {
        val expected = mapOf(
            "reserveBundle" to "0x793baef2",
            "finalizeBundle" to "0x1e73b2e6",
            "cancelBundle" to "0xd307c0a3",
            "reclaimExpiredBundle" to "0x3dc727f7",
        )
        val selectors = mapOf(
            "reserveBundle" to IDiesisBundleEscrow.RESERVE_BUNDLE_SELECTOR,
            "finalizeBundle" to IDiesisBundleEscrow.FINALIZE_BUNDLE_SELECTOR,
            "cancelBundle" to IDiesisBundleEscrow.CANCEL_BUNDLE_SELECTOR,
            "reclaimExpiredBundle" to IDiesisBundleEscrow.RECLAIM_EXPIRED_BUNDLE_SELECTOR,
        )
        assertEquals(expected, selectors)
        val names = abi(IDiesisBundleEscrow.JSON).filter { it.type == "function" }.map { it.name }.toSet()
        assertTrue(names.containsAll(expected.keys))
        assertTrue(names.none { it in setOf("reserveBundleV2", "finalizeBundleV2", "cancelBundleV2", "reclaimExpiredBundleV2") })
    }

    @Test
    fun `tuple classes encode as web3j structs`() {
        val market = IDiesisMarkets.PackedMarket(
            slot0 = Bytes32(ByteArray(32) { 1 }),
            slot1 = Bytes32(ByteArray(32) { 2 }),
            maxOpenInterest = BigInteger.TEN,
        )
        val encoded = FunctionEncoder.encode(Function("market", listOf(market), emptyList()))
        assertEquals(10 + 3 * 64, encoded.length)
        assertEquals("01".repeat(32) + "02".repeat(32) + "%064x".format(10), encoded.substring(10))
    }
}
