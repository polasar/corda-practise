package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.RepoContract;
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
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.UUID;

import static com.example.contract.RepoContract.REPO_CONTRACT_ID;

public class RepoRequest {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction>{

        private Party counterParty;
        private boolean applicantIsBuyer;
        private String repoId;
        private String eligibilityCriteriaDataId;
        private String startDate;
        private String endDate;
        private String terminationPaymentLeg;
        private Party agent;
        private String cashInstrumentId;
        private Long cashPrice;
        private String ustInstrumentId;
        private Long ustPrice;
        private String status;


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



        public Initiator(Party counterParty, boolean applicantIsBuyer, String repoId, String eligibilityCriteriaDataId,
                         String startDate, String endDate, String terminationPaymentLeg, Party agent,
                         String cashInstrumentId, Long cashPrice, String ustInstrumentId, Long ustPrice, String status) {
            this.counterParty = counterParty;
            this.applicantIsBuyer = applicantIsBuyer;
            this.repoId = repoId;
            this.eligibilityCriteriaDataId = eligibilityCriteriaDataId;
            this.startDate = startDate;
            this.endDate = endDate;
            this.terminationPaymentLeg = terminationPaymentLeg;
            this.agent = agent;
            this.cashInstrumentId = cashInstrumentId;
            this.cashPrice = cashPrice;
            this.ustInstrumentId = ustInstrumentId;
            this.ustPrice = ustPrice;
            this.status = status;
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

            UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("12",UUID.randomUUID());
            try {
                Statement statement = getServiceHub().jdbcSession().createStatement();
            } catch (SQLException e) {
                e.printStackTrace();
            }
            //Delivery legs
            LinkedHashMap deliveryLegsLocal = new LinkedHashMap();
            deliveryLegsLocal.put("provider",agent);
            deliveryLegsLocal.put("instrumentId",cashInstrumentId );
            deliveryLegsLocal.put("price",cashPrice);

            //Payment legs
            LinkedHashMap paymentLegsLocal = new LinkedHashMap();
            paymentLegsLocal.put("provider",agent);
            paymentLegsLocal.put("instrumentId",ustInstrumentId);
            paymentLegsLocal.put("price",ustPrice);
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(Calendar.getInstance().getTime());
            String concatRepoID = repoId.concat(timeStamp);
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            RepoAllege outputState = new RepoAllege(getOurIdentity(),counterParty,applicantIsBuyer,concatRepoID,eligibilityCriteriaDataId,
                    new UniqueIdentifier(),startDate,endDate,terminationPaymentLeg,agent,deliveryLegsLocal,paymentLegsLocal,status,ustInstrumentId,ustPrice);

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
             subFlow(new FinalityFlow(signedTx));
             return null;
        }
    }

}
