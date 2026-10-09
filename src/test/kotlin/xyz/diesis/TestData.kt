package xyz.diesis

import tools.jackson.databind.JsonNode
import tools.jackson.databind.json.JsonMapper
import java.io.File

internal object TestData {
    val mapper: JsonMapper = JsonMapper.builder().build()

    val canonical: JsonNode by lazy {
        mapper.readTree(File(System.getProperty("diesis.canonicalJson")))
    }

    val contractsDir: File = File(System.getProperty("diesis.contractsDir"))

    /** The 29 public contracts, in the order of diesis-js's contract bindings script. */
    val publicContracts: List<Triple<String, String, String?>> = listOf(
        Triple("markets", "IDiesisMarkets", "DIESIS_MARKETS"),
        Triple("spotBook", "IDiesisSpotBook", "DIESIS_SPOT_BOOK"),
        Triple("perpsBook", "IDiesisPerpsBook", "DIESIS_PERPS_BOOK"),
        Triple("margin", "IDiesisMargin", "DIESIS_MARGIN"),
        Triple("settlement", "IDiesisSettlement", "DIESIS_SETTLEMENT"),
        Triple("settlementRouter", "DiesisSettlementRouter", "DIESIS_SETTLEMENT_ROUTER"),
        Triple("conductors", "IDiesisConductors", "DIESIS_CONDUCTORS"),
        Triple("erc20Factory", "IDiesisErc20Factory", "DIESIS_ERC20_FACTORY"),
        Triple("perpDeploy", "IDiesisPerpDeploy", "DIESIS_PERP_DEPLOY"),
        Triple("operatorBond", "IDiesisOperatorBond", "DIESIS_OPERATOR_BOND"),
        Triple("bundleEscrow", "IDiesisBundleEscrow", "DIESIS_BUNDLE_ESCROW"),
        Triple("staking", "DiesisStaking", "DIESIS_STAKING"),
        Triple("position", "IDiesisPosition", "DIESIS_POSITION"),
        Triple("validatorShare", "IValidatorShare", null),
        Triple("liquidStakedDS", "ILiquidStakedDS", "LIQUID_STAKED_DS"),
        Triple("wrappedDS", "IWrappedDS", "WRAPPED_DS"),
        Triple("patron", "DiesisPatron", "DIESIS_PATRON"),
        Triple("config", "DiesisConfig", "DIESIS_CONFIG"),
        Triple("coreVault", "IDiesisCoreVault", "DIESIS_CORE_VAULT"),
        Triple("issuanceAuction", "IDiesisIssuanceAuction", "DIESIS_ISSUANCE_AUCTION"),
        Triple("buybackBurn", "IDiesisBuybackBurn", "DIESIS_BUYBACK_BURN"),
        Triple("shieldedPool", "DiesisShieldedPool", "SHIELDED_POOL"),
        Triple("privacyPools", "DiesisPrivacyPools", "PRIVACY_POOLS"),
        Triple("nameRegistry", "IDiesisNameRegistry", "DIESIS_NAME_REGISTRY"),
        Triple("baseRegistrar", "DiesisBaseRegistrar", "DIESIS_BASE_REGISTRAR"),
        Triple("publicResolver", "DiesisPublicResolver", "DIESIS_PUBLIC_RESOLVER"),
        Triple("reverseRegistrar", "IDiesisReverseRegistrar", "DIESIS_REVERSE_REGISTRAR"),
        Triple("nameVerifier", "IDiesisNameVerifier", "DIESIS_NAME_VERIFIER"),
        Triple("namePolicy", "IDiesisNamePolicy", "DIESIS_NAME_POLICY"),
    )
}
