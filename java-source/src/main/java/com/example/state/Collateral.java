package com.example.state;

import com.example.schema.CollateralSchemaV1;
import com.example.schema.RepoAllegeSchemaV1;
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

public class Collateral implements QueryableState, LinearState {

    private Long totalCashAmount;
    private Long totalPrincipal;
    private Long totalNetConsideration;
    private List<CollateralData.Pledge> pledgeCollateralData;
    private List<CollateralData.Borrower> borrowerCollateralData;
    private UniqueIdentifier uniqueIdentifier;
    private Party agent;
    private Party applicant;


    public Collateral(Long totalCashAmount, Long totalPrincipal, Long totalNetConsideration, List<CollateralData.Pledge> pledgeCollateralData,
                      List<CollateralData.Borrower> borrowerCollateralData,UniqueIdentifier uniqueIdentifier,Party agent, Party applicant) {
        this.totalCashAmount = totalCashAmount;
        this.totalPrincipal = totalPrincipal;
        this.totalNetConsideration = totalNetConsideration;
        this.pledgeCollateralData = pledgeCollateralData;
        this.borrowerCollateralData = borrowerCollateralData;
        this.uniqueIdentifier = uniqueIdentifier;
        this.agent = agent;
        this.applicant = applicant;
    }

    public Long getTotalCashAmount() {
        return totalCashAmount;
    }

    public Long getTotalPrincipal() {
        return totalPrincipal;
    }

    public Long getTotalNetConsideration() {
        return totalNetConsideration;
    }

    public List<CollateralData.Pledge> getPledgeCollateralData() {
        return pledgeCollateralData;
    }

    public List<CollateralData.Borrower> getBorrowerCollateralData() {
        return borrowerCollateralData;
    }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return uniqueIdentifier;
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {

        if (schema instanceof CollateralSchemaV1) {
            return new CollateralSchemaV1.PersistentOper(getTotalCashAmount(),getTotalPrincipal(),getTotalNetConsideration().toString(),getLinearId().getId());
        } else {
            throw new IllegalArgumentException("Unrecognised schema exception");
        }

    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new CollateralSchemaV1());
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.agent,this.applicant);
    }

    public Party getAgent() {
        return agent;
    }

    public Party getApplicant() {
        return applicant;
    }
}
