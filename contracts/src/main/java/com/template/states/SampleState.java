package com.template.states;

import com.template.contracts.SampleContract;
import net.corda.core.contracts.BelongsToContract;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.serialization.ConstructorForDeserialization;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(SampleContract.class)
public class SampleState implements ContractState, LinearState {

    public final Party holder;
//    public final String item;
    public final int price;
    private final UniqueIdentifier linearId;


    @ConstructorForDeserialization
//    public SampleState(Party holder, String item, int price,UniqueIdentifier linearId) {
    public SampleState(Party holder, int price,UniqueIdentifier linearId) {
        this.holder = holder;
        this.price = price;
        this.linearId = linearId;
    }

    public Party getHolder(){ return holder; }

//    public String getItem(){ return item; }

    public int getPrice(){ return price; }


//    public SampleState(Party holder, String item, int price){
//        this(holder, item, price, new UniqueIdentifier());
//    }

    public SampleState(Party holder, int price){
        this(holder, price, new UniqueIdentifier());
    }
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(holder);
    }

    /* withNewHolder
     * change currentcity and Address.
     */
    public SampleState withNewHolder(Party newHolder) {
        return new SampleState(newHolder, price);
    }

}