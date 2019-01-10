package com.example.schema;

import com.google.common.collect.ImmutableList;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.serialization.CordaSerializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.UUID;

@CordaSerializable
public class CollateralSchemaV1 extends MappedSchema {
    public CollateralSchemaV1() {
        super(IOUSchema.class, 1, ImmutableList.of(CollateralSchemaV1.PersistentOper.class));
    }

    @Entity
    @Table(name = "collateral_states")
    public static class PersistentOper extends PersistentState {

        @Column(name = "totalCashAmount")
        private Long totalCashAmount;
        @Column(name = "totalPrincipal")
        private Long totalPrincipal;
        @Column(name = "totalNetConsideration")
        private String totalNetConsideration;
        @Column(name = "linearId")
        private final UUID linearId;

        public PersistentOper(Long totalCashAmount, Long totalPrincipal, String totalNetConsideration, UUID linearId) {
            this.totalCashAmount = totalCashAmount;
            this.totalPrincipal = totalPrincipal;
            this.totalNetConsideration = totalNetConsideration;
            this.linearId = linearId;
        }

        public PersistentOper(){
            this.totalCashAmount = null;
            this.totalPrincipal = null;
            this.totalNetConsideration = null;
            this.linearId = null;
        }


        public final UUID getLinearId() {
            return linearId;
        }

        public Long getTotalCashAmount() {
            return totalCashAmount;
        }

        public Long getTotalPrincipal() {
            return totalPrincipal;
        }

        public String getTotalNetConsideration() {
            return totalNetConsideration;
        }
    }
}
