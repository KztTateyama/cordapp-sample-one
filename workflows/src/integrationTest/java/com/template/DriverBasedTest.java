// package com.template;
//
// import com.google.common.collect.ImmutableList;
// import com.google.common.collect.ImmutableSet;
// import com.template.flows.IssueSampleFlow;
// import com.template.flows.TransferSampleFlow;
// import net.corda.client.rpc.CordaRPCClient;
// import net.corda.core.concurrent.CordaFuture;
// import net.corda.core.identity.CordaX500Name;
// import net.corda.core.messaging.CordaRPCOps;
// import net.corda.testing.core.TestIdentity;
// import net.corda.testing.driver.DriverParameters;
// import net.corda.testing.driver.NodeHandle;
// import net.corda.testing.driver.NodeParameters;
// import net.corda.testing.node.TestCordapp;
// import net.corda.testing.node.User;
// import org.junit.Test;
//
// import java.util.Collections;
// import java.util.List;
//
// import static java.util.Arrays.asList;
// import static net.corda.testing.common.internal.ParametersUtilitiesKt.testNetworkParameters;
// import static net.corda.testing.driver.Driver.driver;
// import static org.junit.Assert.assertEquals;
//
// public class DriverBasedTest {
//     private final TestIdentity holderA = new TestIdentity(new CordaX500Name("HolderA", "", "GB"));
//     private final TestIdentity holderB = new TestIdentity(new CordaX500Name("HolderB", "", "US"));
//
//     final List<User> rpcUsers = ImmutableList.of(
//             new User("user1", "test", ImmutableSet.of("ALL")));
//
//     @Test
//     public void SampleTest() {
//         driver(new DriverParameters()
//                 .withIsDebug(true)
//                 .withStartNodesInProcess(true)
//                 .withNetworkParameters(testNetworkParameters(Collections.emptyList(), 4))
//                 .withCordappsForAllNodes(asList(
//                         TestCordapp.findCordapp("com.template.flows"),
//                         TestCordapp.findCordapp("com.template.contracts")
//                 )), dsl -> {
//
//             // Start nodes and wait for them both to be ready.
//             List<CordaFuture<NodeHandle>> handleFutures = ImmutableList.of(
//                     dsl.startNode(new NodeParameters().withProvidedName(holderA.getName()).withRpcUsers(rpcUsers)),
//                     dsl.startNode(new NodeParameters().withProvidedName(holderB.getName()).withRpcUsers(rpcUsers))
//             );
//
//             try {
//                 NodeHandle holderAHandle = handleFutures.get(0).get();
//                 CordaRPCClient rpcClientHolderA = new CordaRPCClient(holderAHandle.getRpcAddress());
//                 CordaRPCOps rpcProxyHolderA = rpcClientHolderA.start("user1", "test").getProxy();
//
//                 NodeHandle holderBHandle = handleFutures.get(1).get();
//                 CordaRPCClient rpcClientHolderB = new CordaRPCClient(holderBHandle.getRpcAddress());
//                 CordaRPCOps rpcProxyHolderB = rpcClientHolderB.start("user1", "test").getProxy();
//
//                 // From each node, make an RPC call to retrieve another node's name from the network map, to verify that the
//                 // nodes have started and can communicate.
//
//                 // HolderA issue Sample
//                 rpcProxyHolderA.startFlowDynamic(
//                         IssueSampleFlow.InitiatorFlow.class,
//                         holderAHandle.getNodeInfo().getLegalIdentities().get(0),
//                         100
//                 ).getReturnValue().get();
//
//                 rpcProxyHolderA.startFlowDynamic(
//                         TransferSampleFlow.InitiatorFlow.class,
//
//                         holderBHandle.getNodeInfo().getLegalIdentities().get(0)
//                 );
//
//
//                 // This is a very basic test: in practice tests would be starting flows, and verifying the states in the vault
//                 // and other important metrics to ensure that your CorDapp is working as intended.
//                 assertEquals(partyAHandle.getRpc().wellKnownPartyFromX500Name(holderB.getName()).getName(), holderB.getName());
//                 assertEquals(partyBHandle.getRpc().wellKnownPartyFromX500Name(holderA.getName()).getName(), holderA.getName());
//             } catch (Exception e) {
//                 throw new RuntimeException("Caught exception during test: ", e);
//             }
//
//             return null;
//         });
//     }
// }