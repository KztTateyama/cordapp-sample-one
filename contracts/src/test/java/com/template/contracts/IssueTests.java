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

public class IssueTests {

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
                new SampleState(ACity.getParty(),100);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.output(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new Commands.DummyCommand()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.output(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Issue()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }

    /**
     * Task 2.
     * Make sure States only sets output.
     * If it doesn't include output,or the state contains input, throw an error.
     */
    @Test
    public void issueTransactionMustHaveNoInputs() {

        SampleState iou =
                new SampleState(ACity.getParty(),100);

        ledger(ledgerServices, l -> {
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, new DummyState());
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Issue()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.input(SampleContract.IOU_CONTRACT_ID, new DummyState());
                tx.output(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Issue()); // Wrong type.
                return tx.fails();
            });
            l.transaction(tx -> {
                tx.output(SampleContract.IOU_CONTRACT_ID, iou);
                tx.command(ACity.getPublicKey(), new SampleContract.Commands.Issue()); // Correct type.
                return tx.verifies();
            });
            return null;
        });
    }



}