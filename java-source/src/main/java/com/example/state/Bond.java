package com.example.state;

import com.example.schema.BondSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.*;



public  class Bond implements LinearState, QueryableState {
        private Party provider;
        private Party operator;
        private Party owner;
        private Long amount;
        private String instrumentId;
        private String omniBusAccountId;
        private String accountId;
        private String status;

    public Bond(Party provider,Party operator, Party owner, Long amount, String instrumentId,String omniBusAccountId, String accountId, String status) {
        this.provider = provider;
        this.operator =operator;
        this.owner = owner;
        this.amount = amount;
        this.instrumentId = instrumentId;
        this.omniBusAccountId = omniBusAccountId;
        this.accountId = accountId;
        this.status = status;
    }


    // For serialization

        @org.jetbrains.annotations.NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            if (schema instanceof BondSchemaV1) {
                return new BondSchemaV1.PersistentOper(this.provider,
                        this.owner,
                        this.instrumentId,
                        this.omniBusAccountId,
                        this.accountId,
                        this.amount,
                        this.operator
                );
            } else {
                throw new IllegalArgumentException("unrecognised schema");
            }
        }
    @org.jetbrains.annotations.NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return  ImmutableList.of(new BondSchemaV1());
    }


    @org.jetbrains.annotations.NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier();
    }



    @org.jetbrains.annotations.NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner,provider,operator);
    }

    public String getStatus() {
        return status;
    }

    public void  setStatus(String status){
        this.status = status;
    }

    public Party getProvider(){
        return provider;
    }

    public Party getOwner(){
        return owner;
    }

    public Long getAmount(){
        return amount;
    }

    public String getInstrumentId(){
        return instrumentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Party getOperator() {
        return operator;
    }
    public String getOmniBusAccountId(){
        return omniBusAccountId;
    }
}




