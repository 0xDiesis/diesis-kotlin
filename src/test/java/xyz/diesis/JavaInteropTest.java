package xyz.diesis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.math.BigInteger;
import org.junit.jupiter.api.Test;
import org.web3j.abi.FunctionEncoder;
import xyz.diesis.contracts.DiesisStaking;
import xyz.diesis.contracts.IDiesisMarkets;

class JavaInteropTest {
    @Test
    void staticAccessFromJava() {
        assertEquals(1980L, DiesisChains.diesis.getId());
        assertEquals(19803L, DiesisChains.diesisTestnet.getId());
        assertEquals(DiesisAddresses.DIESIS_STAKING, DiesisContracts.staking.address);
        assertSame(DiesisContracts.staking, DiesisContracts.byKey("staking"));
        assertEquals(28, DiesisContracts.ALL.size());
        assertEquals(DiesisAddresses.DIESIS_SPOT_BOOK, DiesisAddresses.ALL.get("DIESIS_SPOT_BOOK"));
        assertSame(DiesisStaking.JSON, DiesisContracts.staking.abi);
    }

    @Test
    void generatedTypesFromJava() {
        IDiesisMarkets.CreateSpotMarketParams params = new IDiesisMarkets.CreateSpotMarketParams(
                DiesisAddresses.WRAPPED_DS, DiesisAddresses.WRAPPED_DS, BigInteger.ONE, BigInteger.TWO, BigInteger.ZERO);
        assertEquals(BigInteger.TWO, params.getLotSize());
        assertEquals("0x6fc2e470", IDiesisMarkets.CREATE_SPOT_MARKET_SELECTOR);
    }

    @Test
    void readmeFunctionEncodes() {
        String data = FunctionEncoder.encode(ReadmeJavaExample.unclaimedRewardsFunction());
        assertEquals(10 + 64, data.length());
        assertEquals(DiesisStaking.UNCLAIMED_REWARDS_SELECTOR, data.substring(0, 10));
        assertEquals("000000000000000000000000000000000000000000000000000000000000002a", data.substring(10));
    }
}
