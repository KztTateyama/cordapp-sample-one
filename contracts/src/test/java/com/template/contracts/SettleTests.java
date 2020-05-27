package com.template.contracts;

import com.template.states.SampleState;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.TypeOnlyCommandData;
import net.corda.testing.node.MockServices;
import org.junit.Test;

import java.util.Arrays;

import static com.template.TestUtils.*;
import static com.template.TestUtils.BCity;
import static net.corda.testing.node.NodeTestUtils.ledger;

public class SettleTests {

    // A pre-defined dummy command.
    public interface Commands extends CommandData {
        class DummyCommand extends TypeOnlyCommandData implements Commands{}
    }

    static private final MockServices ledgerServices = new MockServices(
            Arrays.asList("com.template", "net.corda.finance.contracts")
    );


    @Test
    public void mustIncludeDeleteCommand() {

        SampleState iou =
                new SampleState(ACity.getParty(),100);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Settle()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Task 4.
     * Make sure that only the signature of currentCity is valid.
     */
    @Test
    public void signerCheckOnlyCurrentCity() {

        SampleState iou =
                new SampleState(ACity.getParty(),100);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(BCity.getPublicKey(), new SampleContract.Commands.Settle()); // Correct type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(Arrays.asList(ACity.getPublicKey(), BCity.getPublicKey()), new SampleContract.Commands.Settle()); // Correct type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Settle()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }
    
    
}