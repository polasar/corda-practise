package com.example.contract;

import net.corda.core.contracts.ContractState;
import net.corda.core.identity.AbstractParty;
import net.corda.core.serialization.CordaSerializable;

import java.util.List;

@CordaSerializable
public interface ContractParticipants {
    
    List<AbstractParty> parties();
}
