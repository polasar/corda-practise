package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.RepoContract;
import com.example.state.Collateral;
import com.example.state.CollateralData;
import com.example.state.RepoAllege;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.*;

import static com.example.contract.RepoContract.REPO_CONTRACT_ID;

public class RepoRequest {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction>{

        private Party counterParty;
        private boolean applicantIsBuyer;
        private String repoId;
        private String eligibilityCriteriaDataId;
        private Date startDate;
        private Date endDate;
        private String terminationPaymentLeg;
        private Party agent;
        private String status;
        private String accountId;
        private Long amount;
        private Long totalCashAmount;
        private Long totalPrincipal;
        private Long totalNetConsideration;
        private List<HashMap<String, Object>> pledgeData;
        private List<HashMap<String, Object>> borrowerData;


        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new Repo.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step FINALISING_TRANSACTION = new Step("Obtaining notary signature and recording transaction.") {
        @Override
        public ProgressTracker childProgressTracker() {
                return FinalityFlow.Companion.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATING_TRANSACTION,
                VERIFYING_TRANSACTION,
                SIGNING_TRANSACTION,
            //    GATHERING_SIGS
                FINALISING_TRANSACTION
        );



        public Initiator(String repoId, Long amount,
                         List<HashMap<String, Object>> pledgeData, List<HashMap<String, Object>> borrowerData) {

            this.repoId = repoId;
            this.amount = amount;
            this.pledgeData = pledgeData;
            this.borrowerData = borrowerData;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // We retrieve the notary identity from the network map.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            // We create the transaction components.

            UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("R1",UUID.randomUUID());
            try {
                Statement statement = getServiceHub().jdbcSession().createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }


            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String concatRepoID = repoId.concat(timeStamp);
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            RepoAllege outputState = new RepoAllege(getOurIdentity(),counterParty,applicantIsBuyer,concatRepoID,eligibilityCriteriaDataId,
                    uniqueIdentifier,startDate,endDate,terminationPaymentLeg,agent,status,this.accountId,this.amount);
            //Collateral States
          //  Collateral collateral = new Collateral(pledgeData,borrowerData,uniqueIdentifier);
            //Initialize commandData
            progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
            CommandData cmdType = new RepoContract.Commands.Issue();
            Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());

            // We create a transaction builder and add the components.
            progressTracker.setCurrentStep(SIGNING_TRANSACTION);
            final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                    .addOutputState(outputState, REPO_CONTRACT_ID)
                    .addCommand(cmd);
            //Verify the transaction
            txBuilder.verify(getServiceHub());

            // Signing the transaction.
            progressTracker.setCurrentStep(FINALISING_TRANSACTION);
            final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

            // Finalising the transaction.
           return  subFlow(new FinalityFlow(signedTx));

        }
    }

}
