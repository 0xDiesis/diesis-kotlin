// The Kotlin code blocks in README.md, compiled here so they stay correct.
// ReadmeExamplesTest checks that each README line appears in this file.
package xyz.diesis

import org.web3j.abi.FunctionReturnDecoder
import org.web3j.abi.TypeReference
import org.web3j.abi.Utils
import org.web3j.abi.datatypes.Function
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.crypto.Credentials
import org.web3j.protocol.Web3j
import org.web3j.protocol.core.methods.response.Log
import org.web3j.protocol.http.HttpService
import org.web3j.tx.RawTransactionManager
import org.web3j.utils.Convert
import xyz.diesis.contracts.DiesisStaking
import java.math.BigInteger

internal object ReadmeExamples {
    fun connect(): Web3j {
        val web3j = Web3j.build(HttpService(DiesisChains.diesis.rpcUrl))
        return web3j
    }

    fun unclaimedRewardsFunction(): Function {
        val params = DiesisStaking.UnclaimedRewardsParams(tokenId = BigInteger.valueOf(42))
        val unclaimedRewards = Function(
            "unclaimedRewards",
            listOf(Uint256(params.tokenId)),
            listOf(object : TypeReference<Uint256>() {}),
        )
        return unclaimedRewards
    }

    fun read(web3j: Web3j): BigInteger {
        val unclaimedRewards = unclaimedRewardsFunction()
        val rewards = DiesisContracts.staking.call(web3j, unclaimedRewards)[0].value as BigInteger
        return rewards
    }

    fun stakeFunction(): Function {
        val params = DiesisStaking.StakeParams(toValidatorId = BigInteger.ONE)
        val stake = Function("stake", listOf(Uint256(params.toValidatorId)), emptyList())
        return stake
    }

    fun send(web3j: Web3j): String {
        val stake = stakeFunction()
        val credentials = Credentials.create("0x...")
        val transactionManager = RawTransactionManager(web3j, credentials, DiesisChains.diesis.id)

        val txHash = DiesisContracts.staking.send(
            transactionManager,
            stake,
            gasPrice = web3j.ethGasPrice().send().gasPrice,
            gasLimit = BigInteger.valueOf(300_000),
            value = Convert.toWei("100", Convert.Unit.ETHER).toBigIntegerExact(),
        )
        return txHash
    }

    fun decodeStaked(log: Log): DiesisStaking.StakedEvent? {
        if (log.topics[0] != DiesisStaking.STAKED_EVENT_TOPIC) return null
        val data = FunctionReturnDecoder.decode(log.data, Utils.convert(listOf(object : TypeReference<Uint256>() {})))
        return DiesisStaking.StakedEvent(
            delegator = FunctionReturnDecoder.decodeAddress(log.topics[1]),
            toValidatorId = BigInteger(log.topics[2].removePrefix("0x"), 16),
            tokenId = BigInteger(log.topics[3].removePrefix("0x"), 16),
            amount = data[0].value as BigInteger,
        )
    }

    fun table() {
        val staking = DiesisContracts.staking
        println("${staking.key} ${staking.contractName} ${staking.address}")
        val abiJson: String = staking.abi
        println(abiJson.length)

        for (contract in DiesisContracts.ALL) {
            println("${contract.key} -> ${contract.address}")
        }

        val spotBook: String = DiesisAddresses.DIESIS_SPOT_BOOK
        println(spotBook)
    }
}
