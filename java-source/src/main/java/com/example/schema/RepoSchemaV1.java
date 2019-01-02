package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@CordaSerializable
public class RepoSchemaV1 extends MappedSchema {
    public RepoSchemaV1() {
        super(IOUSchema.class,1, ImmutableList.of(RepoSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "repo_states")
    public static class PersistentOper extends PersistentState{

        @Column(name = "buyer")
        private Party buyer;
        @Column(name = "seller")
        private Party seller;
        @Column(name = "repoId")
        private String repoId;
        @Column(name = "eligibilityCriteriaDataId")
        private String eligibilityCriteriaDataId;
        @Column(name = "linearId")
        private final UUID linearId;
        @Column(name = "startDate")
        private String startDate;
        @Column(name = "endDate")
        private String endDate;
        @Column(name = "terminationPaymentLeg")
        private String terminationPaymentLeg;
        @Column(name="agent")
        private Party agent;
        @Column(name = "status")
        private String status;

        public PersistentOper(Party buyer, Party seller, String repoId, String eligibilityCriteriaDataId,
                              UUID linearId, String startDate, String endDate, String terminationPaymentLeg,
                              Party agent, String status) {
            this.buyer = buyer;
            this.seller = seller;
            this.repoId = repoId;
            this.eligibilityCriteriaDataId = eligibilityCriteriaDataId;
            this.linearId = linearId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.terminationPaymentLeg = terminationPaymentLeg;
            this.agent = agent;
            this.status = status;
        }

        public PersistentOper() {
            this.buyer = null;
            this.seller = null;
            this.repoId = null;
            this.eligibilityCriteriaDataId = null;
            this.linearId = null;
            this.startDate = null;
            this.endDate = null;
            this.terminationPaymentLeg = null;
            this.agent = null;
            this.status = null;
        }

        public Party getBuyer() {
            return buyer;
        }

        public Party getSeller() {
            return seller;
        }

        public String getRepoId() {
            return repoId;
        }

        public String getEligibilityCriteriaDataId() {
            return eligibilityCriteriaDataId;
        }

        public UUID getLinearId() {
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

        public String getStatus() {
            return status;
        }
    }
}
