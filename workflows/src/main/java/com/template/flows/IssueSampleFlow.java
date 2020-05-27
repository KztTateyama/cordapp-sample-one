package com.template.flows;

import co.paralleluniverse.fibers.Suspendable;
import com.template.contracts.SampleContract;
import com.template.states.SampleState;
import net.corda.core.contracts.Command;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Collections;
import java.util.List;

// ******************
// * Initiator flow *
// ******************
public class IssueSampleFlow {

    @InitiatingFlow(version = 2)
    @StartableByRPC
    public static class InitiatorFlow extends FlowLogic<SignedTransaction> {
        private final ProgressTracker progressTracker = new ProgressTracker();

        public final Party holder;
        public final int price;

        public InitiatorFlow(Party holder, int price) {
            this.holder      = holder;
            this.price       = price;
        }

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // Step 1. Get a reference to the notary service on our network and our key pair.
            // Note: ongoing work to support multiple notary identities is still in progress.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // Step Create outputState
            SampleState holderA =
                    new SampleState(holder, price);

            // Step 2. Create a new issue command.
            // Remember that a command is a CommandData object and a list of CompositeKeys

            final Command<SampleContract.Commands.Issue> registerCommand = new Command<>(
                    new SampleContract.Commands.Issue(),holderA.getHolder().getOwningKey());

            // Step 3. Create a new TransactionBuilder object.
            final TransactionBuilder builder = new TransactionBuilder(notary);

            // Step 4. Add the iou as an output state, as well as a command to the transaction builder.

            builder.addOutputState(holderA, SampleContract.IOU_CONTRACT_ID);
            builder.addCommand(registerCommand);

            // Step 5. Verify and sign it with our KeyPair.
            builder.verify(getServiceHub());
            final SignedTransaction ptx = getServiceHub().signInitialTransaction(builder);

            // Step 6. The second argument of FinalityFlow should be empty,
            // as no signatures on other nodes are needed.
            List<FlowSession> sessions = Collections.emptyList();
            return subFlow(new FinalityFlow(ptx, sessions));

        }
    }
}
