package com.example.state;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CollateralData {


    public static class Pledge implements LinearState, QueryableState {

        private String instrumentId;
        private String tokenDescription;
        private String assetType;
        private int cleanPrice;
        private Long quantity;
        private int dirtyPrice;
        private int hairCut;
        private Long netConsideration;
        private Long currentQuantity;
        private Long currentValue;

        public Pledge(String instrumentId, String tokenDescription, String assetType, int cleanPrice, Long quantity, int dirtyPrice, int hairCut, Long netConsideration, Long currentQuantity, Long currentValue) {
            this.instrumentId = instrumentId;
            this.tokenDescription = tokenDescription;
            this.assetType = assetType;
            this.cleanPrice = cleanPrice;
            this.quantity = quantity;
            this.dirtyPrice = dirtyPrice;
            this.hairCut = hairCut;
            this.netConsideration = netConsideration;
            this.currentQuantity = currentQuantity;
            this.currentValue = currentValue;
        }


        @NotNull
        @Override
        public UniqueIdentifier getLinearId() {
            return null;
        }

        @NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            return null;
        }

        @NotNull
        @Override
        public Iterable<MappedSchema> supportedSchemas() {
            return null;
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return null;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public String getTokenDescription() {
            return tokenDescription;
        }

        public String getAssetType() {
            return assetType;
        }

        public int getCleanPrice() {
            return cleanPrice;
        }

        public Long getQuantity() {
            return quantity;
        }

        public int getDirtyPrice() {
            return dirtyPrice;
        }

        public int getHairCut() {
            return hairCut;
        }

        public Long getNetConsideration() {
            return netConsideration;
        }

        public Long getCurrentQuantity() {
            return currentQuantity;
        }

        public Long getCurrentValue() {
            return currentValue;
        }
    }

    public static class Borrower implements LinearState, QueryableState {

        private String instrumentId;
        private String tokenDescription;
        private String assetType;
        private int cleanPrice;
        private Long quantity;
        private int dirtyPrice;
        private int hairCut;
        private Long netConsideration;
        private Long currentQuantity;
        private Long currentValue;

        public Borrower(String instrumentId, String tokenDescription, String assetType, int cleanPrice, Long quantity, int dirtyPrice, int hairCut, Long netConsideration, Long currentQuantity, Long currentValue) {
            this.instrumentId = instrumentId;
            this.tokenDescription = tokenDescription;
            this.assetType = assetType;
            this.cleanPrice = cleanPrice;
            this.quantity = quantity;
            this.dirtyPrice = dirtyPrice;
            this.hairCut = hairCut;
            this.netConsideration = netConsideration;
            this.currentQuantity = currentQuantity;
            this.currentValue = currentValue;
        }


        @NotNull
        @Override
        public UniqueIdentifier getLinearId() {
            return null;
        }

        @NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            return null;
        }

        @NotNull
        @Override
        public Iterable<MappedSchema> supportedSchemas() {
            return null;
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return null;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public String getTokenDescription() {
            return tokenDescription;
        }

        public String getAssetType() {
            return assetType;
        }

        public int getCleanPrice() {
            return cleanPrice;
        }

        public Long getQuantity() {
            return quantity;
        }

        public int getDirtyPrice() {
            return dirtyPrice;
        }

        public int getHairCut() {
            return hairCut;
        }

        public Long getNetConsideration() {
            return netConsideration;
        }

        public Long getCurrentQuantity() {
            return currentQuantity;
        }

        public Long getCurrentValue() {
            return currentValue;
        }
    }

}
