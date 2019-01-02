package com.example.state;

import com.example.schema.FixedRateBondSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class FixedRateBond implements LinearState, QueryableState {

    private AbstractParty provider;
    private AbstractParty observer;
    private String instrumentId;
    private String couponDataTriggered;
    private String cusip;
    private String isIn;
    private String cashInstrumentId;
    private float rate;
    private float denominationLeg;
    private String paymentLag;
    private String couponDate;

    public FixedRateBond(AbstractParty provider, AbstractParty observer, String instrumentId, String couponDataTriggered,
                         String cusip, String isIn, String cashInstrumentId,
                         float rate, float denominationLeg, String paymentLag, String couponDate) {
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

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if(schema instanceof FixedRateBondSchemaV1)
        {
            return new FixedRateBondSchemaV1.PersistentOper(this.provider,this.observer,this.instrumentId,this.couponDataTriggered,this.cusip,
                    this.isIn,this.cashInstrumentId,this.rate,this.denominationLeg,this.paymentLag,this.couponDate);
        }
        else{
           throw new IllegalArgumentException("Unrecognised Schema");
        }

    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new FixedRateBondSchemaV1());
    }
}
