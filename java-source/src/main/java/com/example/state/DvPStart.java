package com.example.state;

import com.example.schema.DvPSchemaV1;
import com.google.common.collect.ImmutableList;
import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import org.jetbrains.annotations.NotNull;

import java.util.*;


public class DvPStart implements LinearState, QueryableState {

    private Party buyer;
    private Party seller;
    private Party agent;
    private String dvpId;
    private List<CollateralData.Pledge> pledgeList;
    private List<CollateralData.Borrower> borrowerList;
    private final UniqueIdentifier linearId;
    private Date settlementDate;
    private String repoId;


    public DvPStart(Party buyer, Party seller, String dvpId,
                    UniqueIdentifier linearId, Date settlementDate, List<CollateralData.Pledge> pledgeList, List<CollateralData.Borrower> borrowerList, Party agent, String repoId) {
        this.buyer = buyer;
        this.seller = seller;
        this.dvpId = dvpId;
        this.linearId = linearId;
        this.settlementDate = settlementDate;
        this.pledgeList = pledgeList;
        this.borrowerList = borrowerList;
        this.agent = agent;
        this.repoId = repoId;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.buyer, this.seller, this.agent);
    }

    public String getDvpId() {
        return dvpId;
    }

    public Date getSettlementDate() {
        return settlementDate;
    }

    public Party getBuyer(){
        return buyer;
    }

    public Party getSeller(){
        return seller;
    }

    public Party getAgent(){
        return agent;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if(schema instanceof DvPSchemaV1){
            return new DvPSchemaV1.PersistentOper(this.buyer,this.seller,this.agent,this.dvpId,
                     this.linearId.getId(),this.settlementDate.toString(),repoId);
        }
        else{
            throw new IllegalArgumentException("unrecognised schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new DvPSchemaV1());
    }

    public String getRepoId() {
        return repoId;
    }

    public List<CollateralData.Pledge> getPledgeList() {
        return pledgeList;
    }

    public List<CollateralData.Borrower> getBorrowerList() {
        return borrowerList;
    }
}
