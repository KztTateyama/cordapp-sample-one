package com.template.contracts;

import net.corda.core.contracts.*;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

import com.template.states.SampleState;

// ************
// * Contract *
// ************
@LegalProseReference(uri = "<prose_contract_uri>")
public class SampleContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String IOU_CONTRACT_ID = "com.template.contracts.SampleContract";

    /**
     * The IOUContract can handle three transaction types involving [SampleState]s.
     * - RegisterInformation: Issuing a new [SampleState] on the ledger.
     * - Transfer: Re-assigning the holder.
     * - Settle: deleting  [SampleState] on the ledger.
     */
    public interface Commands extends CommandData {
        class Issue extends TypeOnlyCommandData implements Commands{}
        class Transfer extends TypeOnlyCommandData implements Commands{}
        class Settle extends TypeOnlyCommandData implements Commands{}
    }
    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(LedgerTransaction tx) {

        // We can use the requireSingleCommand function to extract command data from transaction.
        final CommandWithParties<Commands> command = requireSingleCommand(tx.getCommands(), Commands.class);
        final Commands commandData = command.getValue();
        String commandName = commandData.getClass().getSimpleName();

        if (commandData.equals(new Commands.Issue())) {

            requireThat(require -> {

                require.using("No inputs should be consumed when issuing an IOU.", tx.getInputStates().size() == 0);
                require.using("Only one output state should be created when issuing an IOU.", tx.getOutputStates().size() == 1);

                List<SampleState> desiredOutputStates = tx.outputsOfType(SampleState.class);
                SampleState outputState = desiredOutputStates.get(0);
                require.using("A newly issued RegisterInformation must have a holder.", !outputState.getHolder().equals(""));
                require.using("A newly issued RegisterInformation must have a price.", outputState.getPrice() > 0);

                final List<PublicKey> requiredSigners = command.getSigners();
                List<PublicKey> expectedSigners = new ArrayList<>();
                expectedSigners.add(outputState.holder.getOwningKey());

                // Verifies right number of signers are required in the command
                if (requiredSigners.size() != expectedSigners.size()) {
                    throw new IllegalArgumentException(String.format("%s requires exactly %d signers.", commandName, expectedSigners.size()));
                }

                // Verifies required signers covers all participants in the purchase order
                if (!requiredSigners.containsAll(expectedSigners)) {
                    throw new IllegalArgumentException(String.format("%s requires signatures from all contract participants."));
                }

                return null;
            });
        }
        else if (commandData.equals(new Commands.Transfer())) {

                requireThat(require -> {

                    require.using("An IOU change transaction should only consume one input state.", tx.getInputStates().size() == 1);
                    require.using("An IOU change transaction should only create one output state.", tx.getOutputStates().size() == 1);

                    // Copy of input with new holder;
                    SampleState inputState = tx.inputsOfType(SampleState.class).get(0);
                    SampleState outputState = tx.outputsOfType(SampleState.class).get(0);

                    require.using("The holder property must change in a transfer.", !inputState.holder.getOwningKey().equals(outputState.holder.getOwningKey()));

                    // Compare State key and Signers key for equality.
                    List<PublicKey> listOfPublicKeys = new ArrayList<>();
                    listOfPublicKeys.add(outputState.holder.getOwningKey());
                    listOfPublicKeys.add(inputState.holder.getOwningKey());
                    Set<PublicKey> setOfPublicKeys = new HashSet<>(listOfPublicKeys);

                    List<PublicKey> arrayOfSigners = command.getSigners();
                    Set<PublicKey> setOfSigners = new HashSet<>(arrayOfSigners);
                    require.using("The borrower, old holder and new holder only must sign an IOU change transaction", setOfSigners.equals(setOfPublicKeys) && setOfSigners.size() == 2);

                    return null;
                });


            }
        else if (commandData.equals(new Commands.Settle())) {

            requireThat(require -> {

                require.using("No inputs should be consumed when issuing an IOU.", tx.getInputStates().size() == 1);
                require.using( "Only one output state should be created when issuing an IOU.", tx.getOutputStates().size() == 0);

                SampleState inputState = tx.inputsOfType(SampleState.class).get(0);

                final List<PublicKey> requiredSigners = command.getSigners();
                List<PublicKey> expectedSigners = new ArrayList<>();
                expectedSigners.add(inputState.holder.getOwningKey());

                // Verifies right number of signers are required in the command
                if ( requiredSigners.size() != expectedSigners.size()) {
                    throw new IllegalArgumentException(String.format("%s requires exactly %d signers.", commandName, expectedSigners.size()));
                }

                // Verifies required signers covers all participants in the purchase order
                if( !requiredSigners.containsAll(expectedSigners) ) {
                    throw new IllegalArgumentException(String.format("%s requires signatures from all contract participants."));
                }

                return null;
            });

        }
                        
        
    }

}