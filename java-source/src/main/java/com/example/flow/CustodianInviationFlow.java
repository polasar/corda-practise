package com.example.flow;


import co.paralleluniverse.fibers.Suspendable;
import com.example.Utilities;
import com.example.contract.RepoContract;
import com.example.state.Custodian;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

public class CustodianInviationFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private Party provider;
        private Party agent;
        private String status;


        private final ProgressTracker.Step GENERATING_TRANSACTION = new ProgressTracker.Step("Generating transaction based on new Repo.");
        private final ProgressTracker.Step VERIFYING_TRANSACTION = new ProgressTracker.Step("Verifying contract constraints.");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing transaction with our private key.");
        private final ProgressTracker.Step FINALISING_TRANSACTION = new ProgressTracker.Step("Obtaining notary signature and recording transaction.") {
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

        public Initiator(Party provider, Party agent, String status) {
            this.provider = provider;
            this.agent = agent;
            this.status = status;
        }

//        public Initiator(String jsonString){
//            if(jsonString.isEmpty() || jsonString== null){
//                throw  new IllegalArgumentException("JSON string  is null");
//            }
//            else {
//                Utilities.CustodianInvitation util = new Utilities.CustodianInvitation(jsonString);
//            }
//            this.status = "Custodian Invitation";
//        }


        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            // We retrieve the notary identity from the network map.
            final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            SignedTransaction signedTransaction = null;
                    // We create the transaction components.

            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            if(me.equals(agent)) {
                Custodian.Test custodian = new Custodian.Test(provider, getOurIdentity(), status);


                //Initialize commandData
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                CommandData cmdType = new RepoContract.Commands.Issue();
                Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());

                // We create a transaction builder and add the components.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        .addOutputState(custodian, RepoContract.REPO_CONTRACT_ID)
                        .addCommand(cmd);
                //Verify the transaction
                txBuilder.verify(getServiceHub());
                // Signing the transaction.
                progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

                // Finalising the transaction.
               signedTransaction = subFlow(new FinalityFlow(signedTx));
            }
            return signedTransaction;
        }
    }
}
