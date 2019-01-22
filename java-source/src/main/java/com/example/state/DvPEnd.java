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


public class DvPEnd implements LinearState, QueryableState {

    private Party buyer;
    private Party seller;
    private Party agent;
    private String dvpId;
    private ArrayList<LinkedHashMap<String, Object>> pledgeList;
    private ArrayList<LinkedHashMap<String, Object>> borrowerList;
    private UniqueIdentifier linearId;
    private Date settlementDate;
    private String repoId;


    public DvPEnd(
            UniqueIdentifier linearId, ArrayList<LinkedHashMap<String, Object>> pledgeList, ArrayList<LinkedHashMap<String, Object>> borrowerList, String repoId) {

        this.linearId = linearId;
        this.pledgeList = pledgeList;
        this.borrowerList = borrowerList;
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
            return new DvPSchemaV1.PersistentOper(this.linearId.getId(),repoId);
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

    public ArrayList<LinkedHashMap<String, Object>> getPledgeList() {
        return pledgeList;
    }

    public ArrayList<LinkedHashMap<String, Object>> getBorrowerList() {
        return borrowerList;
    }
}
