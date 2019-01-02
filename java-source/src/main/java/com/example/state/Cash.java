package com.example.state;

import com.example.schema.AssetSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class Cash implements LinearState, QueryableState,ContractState {

    private String instrumentId;
    private String currency;
    private AbstractParty provider;
    private AbstractParty observer;


    public Cash(String instrumentId, String currency, AbstractParty provider, AbstractParty observer) {
        this.instrumentId = instrumentId;
        this.currency = currency;
        this.provider = provider;
        this.observer = observer;

    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier();
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(provider,observer);
    }


    public String getInstrumentId() {
        return instrumentId;
    }

    public String getCurrency() {
        return currency;
    }

    public  AbstractParty getProvider(){
        return provider;

    }

    public AbstractParty getObserver(){
        return observer;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if(schema instanceof AssetSchemaV1){
            return new AssetSchemaV1.PersistentOper(this.instrumentId,this.currency,this.provider,this.observer);
        }
        else{
            throw new IllegalArgumentException("unrecognised schema");
        }

    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return  ImmutableList.of(new AssetSchemaV1());
    }


}