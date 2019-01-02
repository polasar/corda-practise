package com.example.state;

import com.example.schema.AccountSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Account implements LinearState,QueryableState {
    private Party provider;
    private String accountId;
    private Party observer;
    private UniqueIdentifier linearId;
    private Party owner;

    public Account(Party provider,String accountId, Party observer,Party owner,UniqueIdentifier linearId) {
        this.provider = provider;
        this.accountId = accountId;
        this.observer = observer;
        this.owner = owner;
        this.linearId=linearId;
    }


    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(provider,observer,owner);
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {

        return linearId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Party getProvider(){
        return provider;
    }

    public Party getObserver(){
        return observer;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if(schema instanceof AccountSchemaV1)
        {
            return new AccountSchemaV1.PersistentOper(this.provider,this.accountId,this.observer, owner, this.linearId.getId());
        }
        else{
            throw  new IllegalArgumentException("unrecognised schema");
        }

    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new AccountSchemaV1());
    }

    public Party getOwner() {
        return owner;
    }
}
