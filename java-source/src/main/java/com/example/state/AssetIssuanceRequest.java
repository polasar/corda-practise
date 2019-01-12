package com.example.state;

import com.example.schema.AssetIssuanceSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;

import java.util.*;



public  class AssetIssuanceRequest implements LinearState, QueryableState {
        private Party provider;
        private Party operator;
        private Party owner;
        private Long quantity;
        private String instrumentId;
        private String omniBusAccountId;
        private String accountId;
        private String notificationStatus;
        private String notificationType;

    public AssetIssuanceRequest(Party provider, Party operator, Party owner, Long quantity, String instrumentId,
                                String omniBusAccountId, String accountId, String notificationStatus, String notificationType) {
        this.provider = provider;
        this.operator =operator;
        this.owner = owner;
        this.quantity = quantity;
        this.instrumentId = instrumentId;
        this.omniBusAccountId = omniBusAccountId;
        this.accountId = accountId;
        this.notificationStatus = notificationStatus;
        this.notificationType = notificationType;
    }


    // For serialization

        @org.jetbrains.annotations.NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            if (schema instanceof AssetIssuanceSchemaV1) {
                return new AssetIssuanceSchemaV1.PersistentOper(this.provider,
                        this.owner,
                        this.instrumentId,
                        this.omniBusAccountId,
                        this.accountId,
                        this.quantity,
                        this.operator,
                        this.notificationStatus,
                        this.notificationType
                );
            } else {
                throw new IllegalArgumentException("unrecognised schema");
            }
        }
    @org.jetbrains.annotations.NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return  ImmutableList.of(new AssetIssuanceSchemaV1());
    }


    @org.jetbrains.annotations.NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier();
    }



    @org.jetbrains.annotations.NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(owner,provider,operator);
    }

    public String getNotificationStatus() {
        return notificationStatus;
    }

    public void setNotificationStatus(String status){
        this.notificationStatus = status;
    }

    public String getNotificationType(){
        return notificationType;
    }

    public Party getProvider(){
        return provider;
    }

    public Party getOwner(){
        return owner;
    }

    public Long getQuantity(){
        return quantity;
    }

    public String getInstrumentId(){
        return instrumentId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Party getOperator() {
        return operator;
    }

    public String getOmniBusAccountId(){
        return omniBusAccountId;
    }
}




