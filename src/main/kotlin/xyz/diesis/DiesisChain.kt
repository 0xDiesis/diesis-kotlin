package xyz.diesis

/** Native currency metadata for a chain. */
public data class NativeCurrency(
    val name: String,
    val symbol: String,
    val decimals: Int,
)

/**
 * A Diesis network. [id] is a `long` so it can be passed straight to web3j's
 * `RawTransactionManager` and `TransactionEncoder`.
 */
public data class DiesisChain(
    val id: Long,
    val name: String,
    val nativeCurrency: NativeCurrency,
    val rpcUrl: String,
    val explorerUrl: String,
    val testnet: Boolean,
)
