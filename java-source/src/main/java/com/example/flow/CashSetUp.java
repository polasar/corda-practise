package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.Utilities;
import com.example.contract.IOUContract;
import com.example.state.Cash;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.LinkedHashMap;
import java.util.List;

import static com.example.contract.IOUContract.IOU_CONTRACT_ID;

public class CashSetUp {

@StartableByRPC
@InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private String instrumentId;
        private String currency;
        private AbstractParty provider;
        private AbstractParty observer;
        Utilities.RepoRequest jsonString;

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

        public Initiator(Utilities.RepoRequest util) {
            this.jsonString = util;
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
//            getLogger().info("XXXXXX Repo request XXXXXX",jsonString);
//            List<LinkedHashMap<String, Object>> deliveryLegsList = jsonString.getDeliveryLegsList();
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            if (me.equals(provider)) {

//                Cash cashData = new Cash(instrumentId, currency, provider, observer);

                //Initialize commandData
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                CommandData cmdType = new IOUContract.Commands.Create();
                Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());

                // We create a transaction builder and add the components.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
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
