package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@CordaSerializable
public class AccountSchemaV1 extends MappedSchema {
    public AccountSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(AccountSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "account_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "provider")
        private Party provider;
        @Column(name = "accountId")
        private String accountId;
        @Column(name = "observer")
        private Party observer;
        @Column(name = "owner")
        private Party owner;
        @Column(name = "linearId")
        private UUID linearId;


        public PersistentOper(Party provider, String accountId, Party observer, Party owner, UUID linearId) {
            this.provider = provider;
            this.accountId = accountId;
            this.observer = observer;
            this.owner = owner;
            this.linearId = linearId;
        }
        public PersistentOper(){
            this.provider = null;
            this.accountId = null;
            this.observer =null;
            this.owner=null;
            this.linearId = null;
        }

        public Party getProvider() {
            return provider;
        }

        public String getAccountId() {
            return accountId;
        }

        public Party getObserver() {
            return observer;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public Party getOwner() {
            return owner;
        }
    }

    }
