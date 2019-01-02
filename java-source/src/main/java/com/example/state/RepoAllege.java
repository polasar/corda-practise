package com.example.state;

import com.example.schema.RepoAllegeSchemaV1;
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

public class RepoAllege implements LinearState, QueryableState {

    private Party applicant;
    private Party counterParty;
    private boolean applicantIsBuyer;
    private String repoId;
    private String eligibilityCriteriaDataId;
    private final UniqueIdentifier linearId;
    private String startDate;
    private String endDate;
    private String terminationPaymentLeg;
    private Map<String,Object> deliveryLegs;
    private Map<String,Object> paymentLegs;
    private Party agent;
    private String status;
    private String instrumentId;
    private Long quantity;


    public RepoAllege(Party applicant, Party counterParty, boolean applicantIsBuyer, String repoId, String eligibilityCriteriaDataId,
                      UniqueIdentifier linearId, String startDate, String endDate, String terminationPaymentLeg,
                      Party agent, Map deliveryLegs, Map paymentLegs, String status,String instrumentId, Long quantity) {
        this.applicant = applicant;
        this.counterParty = counterParty;
        this.applicantIsBuyer = applicantIsBuyer;
        this.repoId = repoId;
        this.eligibilityCriteriaDataId = eligibilityCriteriaDataId;
        this.linearId = linearId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.terminationPaymentLeg = terminationPaymentLeg;
        this.agent = agent;
        this.deliveryLegs = deliveryLegs;
        this.paymentLegs = paymentLegs;
        this.status = status;
        this.instrumentId = instrumentId;
        this.quantity = quantity;
    }


    public Party getApplicant() {
        return applicant;
    }

    public Party getCounterParty() {
        return counterParty;
    }

    public boolean isApplicantIsBuyer() {
        return applicantIsBuyer;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getEligibilityCriteriaDataId() {
        return eligibilityCriteriaDataId;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getTerminationPaymentLeg() {
        return terminationPaymentLeg;
    }

    public Party getAgent() {
        return agent;
    }

    public Map<String,Object> getDeliveryLegs(){
        return  deliveryLegs;
    }

    public  Map<String,Object> getPaymentLegs(){
        return  paymentLegs;
    }

    public String getStatus(){
        return  status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.agent,this.applicant);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        Map<String, Object> paymentLegs = this.paymentLegs;

        String paymentLegInstrumentId = (String) paymentLegs.get("instrumentId");
        Long paymentLegPrice = (Long) paymentLegs.get("price");
        Map<String,Object> deliveryLegString = (Map) deliveryLegs;

        String deliveryLegInstrumentId = (String) deliveryLegString.get("instrumentId");
        Long deliveryLegPrice = (Long) deliveryLegString.get("price");

        if(schema instanceof RepoAllegeSchemaV1){
            return new RepoAllegeSchemaV1.PersistentOper(this.applicant,this.counterParty,this.applicantIsBuyer,this.repoId,
                    this.eligibilityCriteriaDataId,this.startDate,this.endDate,this.terminationPaymentLeg,
                    this.status,this.agent,this.linearId.getId(),paymentLegInstrumentId,paymentLegPrice,deliveryLegInstrumentId,deliveryLegPrice,this.instrumentId,this.quantity);
        }
        else{
            throw new IllegalArgumentException("Unrecognised schema exception");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new RepoAllegeSchemaV1());
    }

    public String getInstrumentId() {
        return instrumentId;
    }

    public Long getQuantity() {
        return quantity;
    }
}
