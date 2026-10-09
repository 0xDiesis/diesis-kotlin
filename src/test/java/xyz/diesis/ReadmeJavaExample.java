// The Java code block in README.md, compiled here so it stays correct.
package xyz.diesis;

import java.io.IOException;
import java.math.BigInteger;
import java.util.List;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import xyz.diesis.contracts.DiesisStaking;

final class ReadmeJavaExample {
    private ReadmeJavaExample() {}

    static Function unclaimedRewardsFunction() {
        DiesisStaking.UnclaimedRewardsParams params =
                new DiesisStaking.UnclaimedRewardsParams(BigInteger.valueOf(42));
        Function unclaimedRewards = new Function(
                "unclaimedRewards",
                List.of(new Uint256(params.getTokenId())),
                List.of(new TypeReference<Uint256>() {}));
        return unclaimedRewards;
    }

    static BigInteger read() throws IOException {
        Function unclaimedRewards = unclaimedRewardsFunction();
        Web3j web3j = Web3j.build(new HttpService(DiesisChains.diesis.getRpcUrl()));
        DiesisContract staking = DiesisContracts.staking;
        BigInteger rewards = (BigInteger) staking.call(web3j, unclaimedRewards).get(0).getValue();
        String spotBook = DiesisAddresses.DIESIS_SPOT_BOOK;
        long chainId = DiesisChains.diesisTestnet.getId();
        String selector = DiesisStaking.UNCLAIMED_REWARDS_SELECTOR;
        return rewards;
    }
}
