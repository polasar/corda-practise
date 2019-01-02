package com.example.schema;


import com.google.common.collect.ImmutableList;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.math.BigDecimal;

@CordaSerializable
public class FixedRateBondSchemaV1 extends MappedSchema {
    public FixedRateBondSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(FixedRateBondSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "fixedrate_states")
    public static class PersistentOper extends PersistentState{

        @Column(name = "provider")
        private AbstractParty provider;
        @Column(name = "observer")
        private AbstractParty observer;
        @Column(name = "instrumentId")
        private String instrumentId;
        @Column(name = "couponDataTriggered")
        private String couponDataTriggered;
        @Column(name = "cusip")
        private String cusip;
        @Column(name = "isIn")
        private String isIn;
        @Column(name = "cashInstrumentId")
        private String cashInstrumentId;
        @Column(name = "rate")
        private float rate;
        @Column(name = "denominationLeg")
        private float denominationLeg;
        @Column(name = "paymentLag")
        private String paymentLag;
        @Column(name = "couponDate")
        private String couponDate;


        public PersistentOper(AbstractParty provider, AbstractParty observer, String instrumentId, String couponDataTriggered,
                                String cusip, String isIn, String cashInstrumentId, float rate, float denominationLeg,
                                String paymentLag, String couponDate) {
            this.provider = provider;
            this.observer = observer;
            this.instrumentId = instrumentId;
            this.couponDataTriggered = couponDataTriggered;
            this.cusip = cusip;
            this.isIn = isIn;
            this.cashInstrumentId = cashInstrumentId;
            this.rate = rate;
            this.denominationLeg = denominationLeg;
            this.paymentLag = paymentLag;
            this.couponDate = couponDate;
        }


        public PersistentOper() {
            this.provider = null;
            this.observer = null;
            this.instrumentId = null;
            this.couponDataTriggered = null;
            this.cusip = null;
            this.isIn = null;
            this.cashInstrumentId = null;
            this.rate = Float.parseFloat(null);
            this.denominationLeg = Float.parseFloat(null);
            this.paymentLag = null;
            this.couponDate = null;
        }

        public AbstractParty getProvider() {
            return provider;
        }

        public AbstractParty getObserver() {
            return observer;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public String getCouponDataTriggered() {
            return couponDataTriggered;
        }

        public String getCusip() {
            return cusip;
        }

        public String getIsIn() {
            return isIn;
        }

        public String getCashInstrumentId() {
            return cashInstrumentId;
        }

        public float getRate() {
            return rate;
        }

        public float getDenominationLeg() {
            return denominationLeg;
        }

        public String getPaymentLag() {
            return paymentLag;
        }

        public String getCouponDate() {
            return couponDate;
        }
    }
}
