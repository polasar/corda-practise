package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.RepoContract;
import com.example.state.AssetIssuanceRequest;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import static com.example.contract.RepoContract.REPO_CONTRACT_ID;

public class AssetOnboardingRequest {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private Party owner;
        private Party provider;
        private Party operator;
        private Long quantity;
        private String instrumentId;
        private String ownerAccountId;
        private String omniBusAccountId;
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

        public Initiator(Party owner, Party provider, Party operator, Long quantity, String instrumentId, String ownerAccountId, String omniBusAccountId, String status) {
            this.owner = owner;
            this.provider = provider;
            this.operator = operator;
            this.quantity = quantity;
            this.instrumentId = instrumentId;
            this.ownerAccountId = ownerAccountId;
            this.omniBusAccountId = omniBusAccountId;
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
            //Get the Party
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            // We create the transaction components.
            if(me.equals(owner)) {
                progressTracker.setCurrentStep(GENERATING_TRANSACTION);
                AssetIssuanceRequest assetIssuanceRequestState = new AssetIssuanceRequest(provider,operator, getOurIdentity(), this.quantity, this.instrumentId,this.omniBusAccountId, this.ownerAccountId, this.status);

                //Initialize commandData
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                CommandData cmdType = new RepoContract.Commands.Issue();
                Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());

                // We create a transaction builder and add the components.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        .addOutputState(assetIssuanceRequestState, REPO_CONTRACT_ID)
                        .addCommand(cmd);
                //Verify the transaction
                txBuilder.verify(getServiceHub());

                // Signing the transaction.
                progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

                // Finalising the transaction.
                subFlow(new FinalityFlow(signedTx));
            }
            return null;
        }
    }

}
