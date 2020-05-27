package com.template;

import com.template.contracts.SampleContract;
import com.template.flows.TransferSampleFlow;
import com.template.flows.IssueSampleFlow;
import com.template.states.SampleState;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateRef;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.testing.node.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class TransferSampleFlowTests {

    private MockNetwork mockNetwork;
    private StartedMockNode a, b, c;

    @Before
    public void setup() {
        MockNetworkParameters mockNetworkParameters = new MockNetworkParameters().withCordappsForAllNodes(
                Arrays.asList(
                        TestCordapp.findCordapp("com.template.flows"),
                        TestCordapp.findCordapp("com.template.contracts")
                )
        ).withNetworkParameters(testNetworkParameters(Collections.emptyList(), 4));
        mockNetwork = new MockNetwork(mockNetworkParameters);
        System.out.println(mockNetwork);

        a = mockNetwork.createNode(new MockNodeParameters());
        b = mockNetwork.createNode(new MockNodeParameters());
        c = mockNetwork.createNode(new MockNodeParameters());

        ArrayList<StartedMockNode> startedNodes = new ArrayList<>();
        startedNodes.add(a);
        startedNodes.add(b);
        startedNodes.add(c);

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        startedNodes.forEach(el -> el.registerInitiatedFlow(TransferSampleFlow.Responder.class));
        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private SignedTransaction issueIOU(SampleState iouState) throws InterruptedException, ExecutionException {
        IssueSampleFlow.InitiatorFlow flow =
                new IssueSampleFlow.InitiatorFlow(iouState.holder,iouState.price);
        CordaFuture future = a.startFlow(flow);
        mockNetwork.runNetwork();
        return (SignedTransaction) future.get();
    }

    /**
     * Task 1.
     * Build out the beginnings of [TransferSampleFlow]!
     */
    @Test
    public void flowReturnsCorrectlyFormedPartiallySignedTransaction() throws Exception {

        Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
        Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

        SampleState iou =
                new SampleState( ACity,100);

        SignedTransaction stx = issueIOU(iou);
        SampleState inputIou = (SampleState) stx.getTx().getOutputs().get(0).getData();
        TransferSampleFlow.InitiatorFlow flow = new TransferSampleFlow.InitiatorFlow(inputIou.getLinearId(),BCity);
        Future<SignedTransaction> future = a.startFlow(flow);

        mockNetwork.runNetwork();

        SignedTransaction ptx = future.get();

        // Check the transaction is well formed...
        // One output SampleState, one input state reference and a Transfer command with the right properties.
        assert (ptx.getTx().getInputs().size() == 1);
        assert (ptx.getTx().getOutputs().size() == 1);
        assert (ptx.getTx().getOutputs().get(0).getData() instanceof SampleState);
        assert (ptx.getTx().getInputs().get(0).equals(new StateRef(stx.getId(), 0)));

        Command command = ptx.getTx().getCommands().get(0);

        assert (command.getValue().equals(new SampleContract.Commands.Transfer()));
        ptx.verifySignaturesExcept(b.getInfo().getLegalIdentities().get(0).getOwningKey(), c.getInfo().getLegalIdentities().get(0).getOwningKey(), mockNetwork.getDefaultNotaryIdentity().getOwningKey());

    }

       /**
        * Task 2.
        * Check that an [SampleState] cannot be transferred to the same currentCity.
        */
       @Test
       public void iouCannotBeTransferredToSameParty() throws Exception {

           Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
           Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

           SampleState iou =
                   new SampleState( ACity,100);

           SignedTransaction stx = issueIOU(iou);
           SampleState inputIou = (SampleState) stx.getTx().getOutputs().get(0).getData();
           TransferSampleFlow.InitiatorFlow flow = new TransferSampleFlow.InitiatorFlow(inputIou.getLinearId(),BCity);
           Future<SignedTransaction> future = a.startFlow(flow);

           try {
               mockNetwork.runNetwork();
               future.get();
           } catch (Exception exception) {
               System.out.println(exception.getMessage());
               assert exception.getMessage().contains("Failed requirement: The currentCity property must change in a transfer.");
           }

   }

       /**
        * Task 3.
        * Get the borrowers and the new lenders signatures.
        * TODO: Amend the [TransferSampleFlow] to handle collecting signatures from multiple parties.
        * Hint: use [initiateFlow] and the [CollectSignaturesFlow] in the same way you did for the [IssueSampleFlow].
        */
       @Test
       public void flowReturnsTransactionSignedBtAllParties() throws Exception {

           Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
           Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

           SampleState iou =
                   new SampleState( ACity,100);


            SignedTransaction stx = issueIOU(iou);
            SampleState inputIou = (SampleState) stx.getTx().getOutputs().get(0).getData();
            TransferSampleFlow.InitiatorFlow flow = new TransferSampleFlow.InitiatorFlow(inputIou.getLinearId(),BCity);
            Future<SignedTransaction> future = a.startFlow(flow);

            try {
                mockNetwork.runNetwork();
                future.get();
                stx.verifySignaturesExcept(mockNetwork.getDefaultNotaryIdentity().getOwningKey());
            } catch (Exception exception) {
                System.out.println(exception.getMessage());
            }

    }

      /**
       * Task 4.
       * We need to get the transaction signed by the notary service
       * TODO: Use a subFlow call to the [FinalityFlow] to get a signature from the lender.
       */
      @Test
      public void flowReturnsTransactionSignedByAllPartiesAndNotary() throws Exception {

          Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
          Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

          SampleState iou =
                  new SampleState( ACity,100);

           SignedTransaction stx = issueIOU(iou);
           SampleState inputIou = (SampleState) stx.getTx().getOutputs().get(0).getData();
           TransferSampleFlow.InitiatorFlow flow = new TransferSampleFlow.InitiatorFlow(inputIou.getLinearId(),BCity);
           Future<SignedTransaction> future = a.startFlow(flow);

           try {
               mockNetwork.runNetwork();
               future.get();
               stx.verifyRequiredSignatures();
           } catch (Exception exception) {
               System.out.println(exception.getMessage());
           }

      }
}