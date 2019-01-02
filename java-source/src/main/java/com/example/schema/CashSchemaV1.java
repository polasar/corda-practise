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
public class CashSchemaV1 extends MappedSchema {
    public CashSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(CashSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "cash_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "owner")
        private AbstractParty owner;
        @Column(name = "quantity")
        private Long quantity;
        @Column(name = "currencyCode")
        private String currencyCode;
        @Column(name = "owningKey")
        private String owningKey;
        @Column(name = "bytes")
        private byte[] bytes;
        @Column(name = "provider")
        private AbstractParty provider;
        @Column(name = "instrument")
        private String instrument;


        public PersistentOper() {
            this.provider = provider;
            this.owner = null;
            this.quantity= null;
            this.currencyCode=null;
            this.owningKey=null;
            this.bytes = null;
            this.provider =null;
            this.instrument=null;
        }

        public PersistentOper(AbstractParty owner, Long quantity, String currencyCode, String owningKey, byte[] bytes, AbstractParty provider, String instrument){
            this.owner = owner;
            this.quantity= quantity;
            this.currencyCode=currencyCode;
            this.owningKey=owningKey;
            this.bytes = bytes;
            this.provider = provider;
            this.instrument = instrument;
        }


        public AbstractParty getOwner() {
            return owner;
        }

        public Long getQuantity() {
            return quantity;
        }

        public String getCurrencyCode() {
            return currencyCode;
        }

        public String getOwningKey() {
            return owningKey;
        }

        public byte[] getBytes() {
            return bytes;
        }

        public AbstractParty getProvider() {
            return provider;
        }

        public String getInstrument() {
            return instrument;
        }
    }
}
