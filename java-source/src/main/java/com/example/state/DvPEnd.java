package com.example.state;

import com.example.schema.DvPSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.Map;


public class DvPEnd implements LinearState, QueryableState {

    private Party buyer;
    private Party seller;
    private Party agent;
    private String dvpId;
    private Map paymentLegs;
    private Map deliveryLegs;
    private UniqueIdentifier linearId;
    private String settlementDate;
    private String repoId;


    public DvPEnd(Party buyer, Party seller, String dvpId,
                    UniqueIdentifier linearId, String settlementDate,Map paymentLegs, Map deliveryLegs, Party agent,String repoId) {
        this.buyer = buyer;
        this.seller = seller;
        this.dvpId = dvpId;
        this.linearId = linearId;
        this.settlementDate = settlementDate;
        this.paymentLegs = paymentLegs;
        this.deliveryLegs = deliveryLegs;
        this.agent = agent;
        this.repoId = repoId;
    }



    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.buyer, this.seller, this.agent);
    }

    public String getDvpId() {
        return dvpId;
    }

    public Map getPaymentLegs() {
        return paymentLegs;
    }

    public Map getDeliveryLegs() {
        return deliveryLegs;
    }

    public String getSettlementDate() {
        return settlementDate;
    }

    public Party getBuyer(){
        return buyer;
    }

    public Party getSeller(){
        return seller;
    }

    public Party getAgent(){
        return agent;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        Map<String,Object> paymentLegsString= (Map) paymentLegs;
        String paymentlegInstrumentId = (String) paymentLegsString.get("instrumentId");
        Long price = (Long) paymentLegsString.get("price");
        Map<String,Object>deliveryLegString = (Map) deliveryLegs;
        String deliveryLegInstrumentId = (String) deliveryLegString.get("instrumentId");
        Long deliveryLegPrice = (Long) deliveryLegString.get("price");
        if(schema instanceof DvPSchemaV1){
            return new DvPSchemaV1.PersistentOper(this.buyer,this.seller,this.agent,this.dvpId, this.linearId.getId(),this.settlementDate,paymentlegInstrumentId,price,
                    deliveryLegInstrumentId,deliveryLegPrice,repoId);
        }
        else{
            throw new IllegalArgumentException("unrecognised schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new DvPSchemaV1());
    }
}
