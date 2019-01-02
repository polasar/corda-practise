package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.IOUContract;
import com.example.contract.RepoContract;
import com.example.state.Asset;
import com.example.state.Custodian;
import com.example.state.MarketParticipant;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;

import static com.example.contract.IOUContract.IOU_CONTRACT_ID;

public class MarketParticipantInvitation {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private Party provider;
        private Party agent;
        private String status;
        private Party participant;


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

        public Initiator(Party participant,Party provider, Party agent, String status) {
            this.participant = participant;
            this.provider = provider;
            this.agent = agent;
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

            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            if(me.equals(provider)) {
                MarketParticipant.Participant marketParticipant = new MarketParticipant.Participant(participant,provider, agent, status);
                //Initialize commandData
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                Command command = new Command(new RepoContract.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey()));
                // We create a transaction builder and add the components.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        .addOutputState(marketParticipant, RepoContract.REPO_CONTRACT_ID)
                        .addCommand(command);
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
