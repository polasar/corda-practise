package com.example.state;

import com.example.schema.CollateralSchemaV1;
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

public class Collateral implements QueryableState, LinearState {

    private Long totalCashAmount;
    private Long totalPrincipal;
    private Long totalNetConsideration;
    private ArrayList<LinkedHashMap<String, Object>> pledgeCollateralData;
    private ArrayList<LinkedHashMap<String, Object>> borrowerCollateralData;
    private UniqueIdentifier uniqueIdentifier;
    private Party agent;
    private Party applicant;


    public Collateral(ArrayList<LinkedHashMap<String, Object>> pledgeCollateralData,
                      ArrayList<LinkedHashMap<String, Object>> borrowerCollateralData, UniqueIdentifier uniqueIdentifier) {

        this.pledgeCollateralData = pledgeCollateralData;
        this.borrowerCollateralData = borrowerCollateralData;
        this.uniqueIdentifier = uniqueIdentifier;

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

    public ArrayList<LinkedHashMap<String, Object>> getPledgeCollateralData() {
        return pledgeCollateralData;
    }

    public ArrayList<LinkedHashMap<String, Object>> getBorrowerCollateralData() {
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
            return new CollateralSchemaV1.PersistentOper(getLinearId().getId());
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
