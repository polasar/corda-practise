package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CordaSerializable
public class DvPSchemaV1 extends MappedSchema {
    public DvPSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(DvPSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "dvp_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "buyer")
        private Party buyer;
        @Column(name = "seller")
        private Party seller;
        @Column(name = "agent")
        private Party agent;
        @Column(name = "dvpId")
        private String dvpId;
        @Column(name = "linearId")
        private UUID linearId;
        @Column(name = "settlementDate")
        private String settlementDate;
        @Column(name = "repoId")
        private String repoId;


        public PersistentOper(UUID linearId,String repoId) {

            this.linearId = linearId;
            this.repoId = repoId;
        }

        public PersistentOper() {
            this.buyer = null;
            this.seller = null;
            this.agent = null;
            this.dvpId = null;
            this.linearId = null;
            this.settlementDate = null;
            this.repoId = null;
        }

        public Party getBuyer() {
            return buyer;
        }

        public Party getSeller() {
            return seller;
        }

        public Party getAgent() {
            return agent;
        }

        public String getDvpId() {
            return dvpId;
        }

        public String getRepoId() {
            return repoId;
        }

    }
}
