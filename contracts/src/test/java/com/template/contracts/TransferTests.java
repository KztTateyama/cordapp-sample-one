package com.template.contracts;

import com.template.states.SampleState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.testing.contracts.DummyState;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static com.template.TestUtils.*;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class TransferTests {

    // A pre-defined dummy command.
    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands{}
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );


    @Test
    public void mustIncludeIssueCommand() {

        SampleState iou =
                new SampleState(ACity.getParty(),"apple",100);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Transfer()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.output(SampleContract.IOU_CONTRACT_ID, iou.withNewHolder(XCity.getParty()));
                tx.command(Arrays.asList(XCity.getPublicKey(), ACity.getPublicKey()), new SampleContract.Commands.Transfer()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

}