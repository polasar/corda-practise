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
        private final String accountId;
        @Column(name = "accountId")
        private final Long amount;
        @Column(name = "amount")
        private final Party agent;
        @Column(name = "linearId")
        private final UUID linearId;


        public PersistentOper(Party applicant, Party counterParty, Boolean applicantIsBuyer, String repoId,
                              String eligibilityCriteriaId, String startDate, String endDate, String terminationPaymentLeg,
                              String status,String accountId, Long amount, Party agent, UUID linearId) {
            this.applicant = applicant;
            this.counterParty = counterParty;
            this.applicantIsBuyer = applicantIsBuyer;
            this.repoId = repoId;
            this.eligibilityCriteriaId = eligibilityCriteriaId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.terminationPaymentLeg = terminationPaymentLeg;
            this.status = status;
            this.accountId = accountId;
            this.amount = amount;
            this.agent = agent;
            this.linearId = linearId;
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
            this.accountId = null;
            this.amount = null;
            this.agent = null;
            this.linearId = null;
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

        public String getAccountId() {
            return accountId;
        }

        public Long getAmount() {
            return amount;
        }
    }
}
