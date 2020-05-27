package com.template;

import com.template.contracts.SampleContract;
import com.template.states.SampleState;
import com.template.flows.IssueSampleFlow.*;
import net.corda.core.contracts.Command;
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
import java.util.concurrent.Future;

import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;

public class IssueSampleFlowTests {

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

        //IssueSampleFlow.InitiatorFlow flow = new IssueSampleFlow.InitiatorFlow(iou);
        InitiatorFlow flow =
                new InitiatorFlow(ACity,100);

        Future<SignedTransaction> future = a.startFlow(flow);
        mockNetwork.runNetwork();

        // Return the unsigned(!) SignedTransaction object from the IssueSampleFlow.
        SignedTransaction ptx = future.get();

        // Print the transaction for debugging purposes.
        System.out.println(ptx.getTx());

        // Check the transaction is well formed...
        // No inputs, one output SampleState and a command with the right properties.
        assert (ptx.getTx().getInputs().isEmpty());
        assert (ptx.getTx().getOutputs().get(0).getData() instanceof SampleState);

        // Check that command is Issue.
        Command command = ptx.getTx().getCommands().get(0);
        assert (command.getValue() instanceof SampleContract.Commands.Issue);

        // Check that All expected signers signed.
        final List<PublicKey> requiredSigners = command.getSigners();
        List<PublicKey> expectedSigners = new ArrayList<>();
        expectedSigners.add(iou.getHolder().getOwningKey());

        assert (requiredSigners.containsAll(expectedSigners));


    }

}
