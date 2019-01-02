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
public class CustodianSchemaV1 extends MappedSchema {

    public CustodianSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(CustodianSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "custodian_states")
    public static  class PersistentOper extends PersistentState {

        @Column(name = "provider")
        private final Party provider;
        @Column(name= "agent")
        private final Party agent;
        @Column(name = "status")
        private final String status;
        @Column(name = "externalId")
        private String externalId;
        @Column(name = "linearId")
        private UUID linearId;


        public PersistentOper(Party provider, Party agent, String status) {
            this.provider = provider;
            this.agent = agent;
            this.status = status;
            /*this.externalId = externalId;
            this.linearId = linearId;*/
        }

        public PersistentOper() {
            this.provider = null;
            this.agent = null;
            this.status = null;
            this.externalId = null;
            this.linearId = null;
        }

        public Party getProvider() {
            return provider;
        }

        public Party getAgent() {
            return agent;
        }

        public String getStatus() {
            return status;
        }
    }
}
