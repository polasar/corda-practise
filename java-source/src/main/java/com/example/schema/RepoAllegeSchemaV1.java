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
public class RepoAllegeSchemaV1 extends MappedSchema {

    public RepoAllegeSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(RepoAllegeSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "oper_states")
    public static  class PersistentOper extends PersistentState{

        @Column(name = "applicant")
        private final Party applicant;
        @Column(name= "counterParty")
        private final Party counterParty;
        @Column(name="applicantIsBuyer")
        private final Boolean applicantIsBuyer;
        @Column(name= "repoId")
        private final String repoId;
        @Column(name = "eligibiltyCriteriaId")
        private final String eligibilityCriteriaId;
        @Column(name = "startDate")
        private final String startDate;
        @Column(name = "endDate")
        private final String endDate;
        @Column(name = "terminationPaymentLeg")
        private final String terminationPaymentLeg;
        @Column(name = "status")
        private final String status;
        @Column(name = "agent")
        private final Party agent;
        @Column(name = "linearId")
        private final UUID linearId;
        @Column(name = "paymentLegInstrumentId")
        private String paymentLegInstrumentId;
        @Column(name = "paymentLegPrice")
        private Long paymentLegPrice;
        @Column(name = "deliveryLegInstrumentId")
        private String deliveryLegInstrumentId;
        @Column(name = "deliveryLegPrice")
        private Long deliveryLegPrice;
        @Column(name = "instrumentId")
        private String instrumentId;
        @Column(name = "quantity")
        private Long quantity;

        public PersistentOper(Party applicant, Party counterParty, Boolean applicantIsBuyer, String repoId,
                              String eligibilityCriteriaId, String startDate, String endDate, String terminationPaymentLeg,
                              String status, Party agent, UUID linearId,String paymentLegInstrumentId,Long paymentLegPrice,String deliveryLegInstrumentId, Long deliveryLegPrice,String instrumentId,Long quantity) {
            this.applicant = applicant;
            this.counterParty = counterParty;
            this.applicantIsBuyer = applicantIsBuyer;
            this.repoId = repoId;
            this.eligibilityCriteriaId = eligibilityCriteriaId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.terminationPaymentLeg = terminationPaymentLeg;
            this.status = status;
            this.agent = agent;
            this.linearId = linearId;
            this.paymentLegInstrumentId = paymentLegInstrumentId;
            this.paymentLegPrice = paymentLegPrice;
            this.deliveryLegInstrumentId = deliveryLegInstrumentId;
            this.deliveryLegPrice = deliveryLegPrice;
            this.instrumentId = instrumentId;
            this.quantity = quantity;
        }

        public PersistentOper() {
            this.applicant = null;
            this.counterParty = null;
            this.applicantIsBuyer = null;
            this.repoId = null;
            this.eligibilityCriteriaId = null;
            this.startDate = null;
            this.endDate = null;
            this.terminationPaymentLeg = null;
            this.status = null;
            this.agent = null;
            this.linearId = null;
            this.paymentLegInstrumentId = null;
            this.paymentLegPrice = null;
            this.deliveryLegInstrumentId = null;
            this.deliveryLegPrice = null;
            this.instrumentId =null;
            this.quantity= null;
        }

        public Party getApplicant() {
            return applicant;
        }

        public Party getCounterParty() {
            return counterParty;
        }

        public Boolean getApplicantIsBuyer() {
            return applicantIsBuyer;
        }

        public String getRepoId() {
            return repoId;
        }

        public String getEligibilityCriteriaId() {
            return eligibilityCriteriaId;
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


        public String getStatus() {
            return status;
        }

        public Party getAgent() {
            return agent;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public String getPaymentLegInstrumentId() {
            return paymentLegInstrumentId;
        }

        public Long getPaymentLegPrice() {
            return paymentLegPrice;
        }


        public String getDeliveryLegInstrumentId() {
            return deliveryLegInstrumentId;
        }

        public void setDeliveryLegInstrumentId(String deliveryLegInstrumentId) {
            this.deliveryLegInstrumentId = deliveryLegInstrumentId;
        }

        public Long getDeliveryLegPrice() {
            return deliveryLegPrice;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public Long getQuantity() {
            return quantity;
        }
    }
}
