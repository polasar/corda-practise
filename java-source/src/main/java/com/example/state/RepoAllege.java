package com.example.state;

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

import java.util.*;

public class RepoAllege implements LinearState, QueryableState {

    private Party applicant;
    private Party counterParty;
    private boolean applicantIsBuyer;
    private String repoId;
    private String eligibilityCriteriaDataId;
    private final UniqueIdentifier linearId;
    private Date startDate;
    private Date endDate;
    private String terminationPaymentLeg;
    private Party agent;
    private String status;
    private String accountId;
    private Long amount;


    public RepoAllege(Party applicant, Party counterParty, boolean applicantIsBuyer, String repoId, String eligibilityCriteriaDataId,
                      UniqueIdentifier linearId, Date startDate, Date endDate, String terminationPaymentLeg,
                      Party agent, String status,String accountId, Long amount) {
        this.applicant = applicant;
        this.counterParty = counterParty;
        this.applicantIsBuyer = applicantIsBuyer;
        this.repoId = repoId;
        this.eligibilityCriteriaDataId = eligibilityCriteriaDataId;
        this.linearId = linearId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.terminationPaymentLeg = terminationPaymentLeg;
        this.agent = agent;
        this.status = status;
        this.accountId = accountId;
        this.amount = amount;
    }


    public Party getApplicant() {
        return applicant;
    }

    public Party getCounterParty() {
        return counterParty;
    }

    public boolean isApplicantIsBuyer() {
        return applicantIsBuyer;
    }

    public String getRepoId() {
        return repoId;
    }

    public String getEligibilityCriteriaDataId() {
        return eligibilityCriteriaDataId;
    }

    public UniqueIdentifier getLinearId() {
        return linearId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public String getTerminationPaymentLeg() {
        return terminationPaymentLeg;
    }

    public Party getAgent() {
        return agent;
    }

    public String getStatus(){
        return  status;
    }

    public void setStatus(String status){
        this.status = status;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(this.agent,this.applicant);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(MappedSchema schema) {

            if (schema instanceof RepoAllegeSchemaV1) {
                return new RepoAllegeSchemaV1.PersistentOper(this.applicant, this.counterParty, this.applicantIsBuyer, this.repoId,
                        this.eligibilityCriteriaDataId, this.startDate.toString(), this.endDate.toString(), this.terminationPaymentLeg,
                        this.status, this.accountId,this.amount, this.agent, this.linearId.getId());
            } else {
                throw new IllegalArgumentException("Unrecognised schema exception");
            }

    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return ImmutableList.of(new RepoAllegeSchemaV1());
    }

    public String getAccountId() {
        return accountId;
    }

    public Long getAmount() {
        return amount;
    }
}
