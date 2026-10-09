package xyz.diesis

import org.web3j.abi.FunctionEncoder
import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.Type
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.DefaultBlockParameter
import org.web3j.protocol.core.DefaultBlockParameterName
import org.web3j.protocol.core.methods.request.Transaction
import org.web3j.tx.TransactionManager
import java.io.IOException
import java.math.BigInteger

/**
 * A system contract at its fixed address.
 *
 * [contractName] is the Solidity contract the bindings in [xyz.diesis.contracts]
 * were generated from, and the name of the generated object that holds its
 * types and selectors. [abi] is that object's JSON ABI.
 * [call] and [send] take a web3j [Function] and target [address].
 */
public class DiesisContract(
    @JvmField public val key: String,
    @JvmField public val contractName: String,
    @JvmField public val address: String,
    @JvmField public val abi: String,
) {
    /**
     * Runs [function] with `eth_call` against [address] and decodes its outputs.
     *
     * @throws IOException if the node request fails.
     * @throws DiesisCallException if the node returns an error or the call reverts.
     */
    @JvmOverloads
    @Throws(IOException::class)
    public fun call(
        web3j: Web3j,
        function: Function,
        from: String? = null,
        block: DefaultBlockParameter = DefaultBlockParameterName.LATEST,
    ): List<Type<*>> {
        val data = FunctionEncoder.encode(function)
        val response = web3j.ethCall(Transaction.createEthCallTransaction(from, address, data), block).send()
        if (response.hasError()) {
            throw DiesisCallException("$contractName.${function.name}: ${response.error.message}", response.revertReason)
        }
        if (response.isReverted) {
            throw DiesisCallException("$contractName.${function.name} reverted", response.revertReason)
        }
        @Suppress("UNCHECKED_CAST")
        return FunctionReturnDecoder.decode(response.value, function.outputParameters) as List<Type<*>>
    }

    /**
     * Signs and sends [function] to [address] through [transactionManager], and
     * returns the transaction hash.
     *
     * @throws IOException if the node request fails.
     * @throws DiesisCallException if the node rejects the transaction.
     */
    @JvmOverloads
    @Throws(IOException::class)
    public fun send(
        transactionManager: TransactionManager,
        function: Function,
        gasPrice: BigInteger,
        gasLimit: BigInteger,
        value: BigInteger = BigInteger.ZERO,
    ): String {
        val response = transactionManager.sendTransaction(
            gasPrice,
            gasLimit,
            address,
            FunctionEncoder.encode(function),
            value,
        )
        if (response.hasError()) {
            throw DiesisCallException("$contractName.${function.name}: ${response.error.message}", null)
        }
        return response.transactionHash
    }

    override fun toString(): String = "DiesisContract($key, $contractName, $address)"
}

/** A contract call or transaction that the node rejected or that reverted. */
public class DiesisCallException(
    message: String,
    @JvmField public val revertReason: String?,
) : IOException(message)
