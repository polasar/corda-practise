package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@CordaSerializable
public class AssetSchemaV1 extends MappedSchema {
    public AssetSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(AssetSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "cash_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "instrumentId")
        private String instrumentId;
        @Column(name = "currency")
        private String currency;
        @Column(name = "provider")
        private AbstractParty provider;
        @Column(name = "observer")
        private AbstractParty observer;


        public PersistentOper(String instrumentId, String currency, AbstractParty provider, AbstractParty observer) {
            this.instrumentId = instrumentId;
            this.currency = currency;
            this.provider = provider;
            this.observer = observer;
        }

        public PersistentOper() {
            this.instrumentId = null;
            this.currency = null;
            this.provider = null;
            this.observer = null;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public String getCurrency() {
            return currency;
        }

        public AbstractParty getProvider() {
            return provider;
        }

        public AbstractParty getObserver() {
            return observer;
        }
    }
}