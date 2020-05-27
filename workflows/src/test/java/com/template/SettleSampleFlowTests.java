package com.template;

import com.template.contracts.SampleContract;
import com.template.flows.SettleSampleFlow;
import com.template.flows.IssueSampleFlow;
//import com.template.flows.RegisterInformationFlow;
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

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class SettleSampleFlowTests {

    private MockNetwork mockNetwork;
    private StartedMockNode a, b;

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

        ArrayList<StartedMockNode> startedNodes = new ArrayList<>();
        startedNodes.add(a);
        startedNodes.add(b);

        // For real nodes this happens automatically, but we have to manually register the flow for tests
        mockNetwork.runNetwork();
    }

    @After
    public void tearDown() {
        mockNetwork.stopNodes();
    }

    private SignedTransaction registerIOU(SampleState iouState) throws InterruptedException, ExecutionException {

        IssueSampleFlow.InitiatorFlow flow =
                new IssueSampleFlow.InitiatorFlow(iouState.holder,iouState.price);

        CordaFuture future = a.startFlow(flow);
        mockNetwork.runNetwork();
        return (SignedTransaction) future.get();
    }


    @Rule
    public final ExpectedException exception = ExpectedException.none();

    /**
     * Task 1.
     * Build Transaction from the flow, sign it, and confirm that State is generated.
     */
    @Test
    public void flowReturnsCorrectlyFormedPartiallySignedTransaction() throws Exception {

        Party ACity = a.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();
        Party BCity = b.getInfo().getLegalIdentitiesAndCerts().get(0).getParty();

        SampleState iou =
                new SampleState( ACity,100);


        SignedTransaction stx = registerIOU(iou);
        SampleState inputIOU = stx.getTx().outputsOfType(SampleState.class).get(0);
        SettleSampleFlow.InitiatorFlow flow = new SettleSampleFlow.InitiatorFlow(inputIOU.getLinearId(),inputIOU.getHolder());

        Future<SignedTransaction> future = a.startFlow(flow);
        mockNetwork.runNetwork();

        // Return the unsigned(!) SignedTransaction object from the SettleSampleFlow.
        SignedTransaction ptx = future.get();

        // Print the transaction for debugging purposes.
        System.out.println(ptx.getTx());

        // Check the transaction is well formed...
        // No inputs, one output SampleState and a command with the right properties.
        assert (ptx.getTx().getInputs().get(0).equals(new StateRef(stx.getId(), 0)));
        assert (ptx.getTx().getOutputs().isEmpty());

        // Check that command is Settle.
        Command command = ptx.getTx().getCommands().get(0);
        assert (command.getValue() instanceof SampleContract.Commands.Settle);

        // Check that All expected signers signed.
        final List<PublicKey> requiredSigners = command.getSigners();
        List<PublicKey> expectedSigners = new ArrayList<>();
        expectedSigners.add(iou.getHolder().getOwningKey());

        assert (requiredSigners.containsAll(expectedSigners));

    }

}
