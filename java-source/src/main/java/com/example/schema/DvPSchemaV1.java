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
        @Column(name = "paymentlegInstrumentId")
        private String paymentlegInstrumentId;
        @Column(name = "price")
        private Long price;
        @Column(name="deliveryLegInstrumentId")
        private String deliveryLegInstrumentId;
        @Column(name = "deliveryLegPrice")
        private Long deliveryLegPrice;
        @Column(name = "linearId")
        private UUID linearId;
        @Column(name = "settlementDate")
        private String settlementDate;
        @Column(name = "repoId")
        private String repoId;



        public PersistentOper(Party buyer, Party seller, Party agent, String dvpId,
                               UUID linearId, String settlementDate,String paymentlegInstrumentId,Long price,String deliveryLegInstrumentId,Long deliveryLegPrice,String repoId) {
            this.buyer = buyer;
            this.seller = seller;
            this.agent = agent;
            this.dvpId = dvpId;
            this.paymentlegInstrumentId = paymentlegInstrumentId;
            this.price = price;
            this.deliveryLegInstrumentId = deliveryLegInstrumentId;
            this.deliveryLegPrice = deliveryLegPrice;
            this.linearId = linearId;
            this.settlementDate = settlementDate;
            this.repoId = repoId;
        }

        public PersistentOper() {
            this.buyer = null;
            this.seller = null;
            this.agent = null;
            this.dvpId = null;
            this.paymentlegInstrumentId = null;
            this.price = null;
            this.linearId = null;
            this.settlementDate = null;
            this.deliveryLegInstrumentId = null;
            this.deliveryLegPrice = null;
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

        public String getPaymentlegInstrumentId() {
            return paymentlegInstrumentId;
        }

        public Long getPrice() {
            return price;
        }

        public UUID getLinearId() {
            return linearId;
        }

        public String getSettlementDate() {
            return settlementDate;
        }

        public String getDeliveryLegInstrumentId() {
            return deliveryLegInstrumentId;
        }

        public Long getDeliveryLegPrice() {
            return deliveryLegPrice;
        }

        public String getRepoId() {
            return repoId;
        }
    }
}
