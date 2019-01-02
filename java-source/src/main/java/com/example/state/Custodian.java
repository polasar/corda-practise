package com.example.state;

import com.example.schema.CustodianSchemaV1;
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

public class Custodian  {

    public static class Test implements QueryableState {
        private Party provider;
        private Party agent;
        private String status;

        public Test(Party provider, Party agent, String status) {
            this.provider = provider;
            this.agent = agent;
            this.status = status;
        }


        public Party getAgent() {
            return agent;
        }

        public String getStatus() {
            return status;
        }

        public Party getProvider() {
            return provider;
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return Arrays.asList(agent, provider);
        }

        @NotNull
        @Override
        public Iterable<MappedSchema> supportedSchemas() {
            return ImmutableList.of(new CustodianSchemaV1());
        }

        @NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            if(schema instanceof CustodianSchemaV1){
                return new CustodianSchemaV1.PersistentOper(this.provider,this.agent,this.status);
            }
            else{
                throw new IllegalArgumentException("Unrecognised schema exception");
            }
        }
    }

    public static class Test1 implements QueryableState,LinearState {
        private Party provider;
        private Party agent;
        private String omniBusAccountId;
        private String externalId;

        public Test1(Party provider, Party agent, String omniBusAccountId) {
            this.provider = provider;
            this.agent = agent;
            this.omniBusAccountId = omniBusAccountId;
        }


        public Party getAgent() {
            return agent;
        }

        public String getOmniBusAccountId() {
            return omniBusAccountId;
        }

        public Party getProvider() {
            return provider;
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return Arrays.asList(agent, provider);
        }


        @NotNull
        @Override
        public Iterable<MappedSchema> supportedSchemas() {
            return ImmutableList.of(new CustodianSchemaV1());
        }

        @NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            if(schema instanceof CustodianSchemaV1){
                return new CustodianSchemaV1.PersistentOper(this.provider,this.agent,this.omniBusAccountId);
            }
            else{
                throw new IllegalArgumentException("Unrecognised schema exception");
            }
        }

        @NotNull
        @Override
        public UniqueIdentifier getLinearId() {
            return new UniqueIdentifier(externalId,new UniqueIdentifier().getId());
        }
    }
}
