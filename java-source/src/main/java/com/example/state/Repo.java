package com.example.state;


import com.example.schema.RepoAllegeSchemaV1;
import com.example.schema.RepoSchemaV1;
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

public class Repo implements LinearState, QueryableState {

    private Party buyer;
    private Party seller;
    private String repoId;
    private String eligibilityCriteriaDataId;
    private final UniqueIdentifier linearId;
    private String startDate;
    private String endDate;
    private String terminationPaymentLeg;
    private Party agent;
    private String status;


    public Repo(Party buyer, Party seller, String repoId, String eligibilityCriteriaDataId,
                UniqueIdentifier linearId, String startDate, String endDate, String terminationPaymentLeg, Party agent, String status) {
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



    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return new UniqueIdentifier(repoId,linearId.getId());
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.buyer, this.seller, this.agent);
    }

    public Party getBuyer() {
        return buyer;
    }

    public void setBuyer(Party buyer){
        this.buyer = buyer;
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

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {
        if(schema instanceof RepoSchemaV1)
        {
            return new RepoSchemaV1.PersistentOper(this.buyer,this.seller,this.repoId,this.eligibilityCriteriaDataId,
                    this.linearId.getId(),this.startDate,this.endDate,this.terminationPaymentLeg,
                    this.agent,this.status);
        }
        else{
            throw new IllegalArgumentException("unrecognised schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new RepoSchemaV1()) ;
    }
}
