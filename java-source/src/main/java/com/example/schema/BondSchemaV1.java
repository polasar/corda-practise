package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@CordaSerializable
public class BondSchemaV1 extends MappedSchema {
    public BondSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(BondSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "account_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "provider")
        private Party provider;
        @Column(name = "owner")
        private Party owner;
        @Column(name = "instrumentId")
        private String instrumentId;
        @Column(name = "omniBusAccountId")
        private String omniBusAccountId;
        @Column(name = "accountId")
        private String accountId;
        @Column(name = "amount")
        private Long amount;
        @Column(name = "operator")
        private Party operator;


        public PersistentOper(Party provider, Party owner, String instrumentId,String omniBusAccountId, String accountId, Long amount,Party operator) {
            this.provider = provider;
            this.owner = owner;
            this.instrumentId = instrumentId;
            this.omniBusAccountId = omniBusAccountId;
            this.accountId = accountId;
            this.amount = amount;
            this.operator = operator;
        }

        public PersistentOper() {
            this.provider = null;
            this.owner = null;
            this.instrumentId = null;
            this.omniBusAccountId = null;
            this.accountId = null;
            this.amount = null;
            this.operator =null;
        }

        public Party getProvider() {
            return provider;
        }

        public Party getOwner() {
            return owner;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public String getAccountId() {
            return accountId;
        }

        public Long getAmount() {
            return amount;
        }

        public Party getOperator() {
            return operator;
        }

        public String getOmniBusAccountId() {
            return omniBusAccountId;
        }
    }

}
