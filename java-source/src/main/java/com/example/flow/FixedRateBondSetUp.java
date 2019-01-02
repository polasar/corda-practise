package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.IOUContract;
import com.example.schema.CashSchemaV1;
import com.example.schema.FixedRateBondSchemaV1;
import com.example.state.Cash;
import com.example.state.FixedRateBond;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.lang.reflect.Field;
import java.util.List;

import static com.example.contract.IOUContract.IOU_CONTRACT_ID;


public class FixedRateBondSetUp {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private AbstractParty provider;
        private String instrumentId;
        private AbstractParty observer;
        private String couponDataTriggered;
        private String cusip;
        private String isIn;
        private float rate;
        private float denomination;
        private String paymentLag;
        private String couponDate;

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

        public Initiator(AbstractParty provider, String instrumentId, AbstractParty observer,
                                  String couponDataTriggered, String cusip, String isIn, float rate, float denomination, String paymentLag, String couponDate) {
            this.provider = provider;
            this.instrumentId = instrumentId;
            this.observer = observer;
            this.couponDataTriggered = couponDataTriggered;
            this.cusip = cusip;
            this.isIn = isIn;
            this.rate = rate;
            this.denomination = denomination;
            this.paymentLag = paymentLag;
            this.couponDate = couponDate;
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
            if (me.equals(provider)) {


                QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
                Field provider = null;
                VaultService vaultService = getServiceHub().getVaultService();
                List<StateAndRef<Cash>> states = null;
                try {
                    provider = CashSchemaV1.PersistentOper.class.getDeclaredField("provider");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                CriteriaExpression providerMatched = Builder.equal(provider, this.provider);
                QueryCriteria lenderCriteria = new QueryCriteria.VaultCustomQueryCriteria(providerMatched);
                QueryCriteria criteria = generalCriteria.and(lenderCriteria);
                states = vaultService.queryBy(Cash.class, criteria).getStates();
                //Initialize commandData
                if (me.equals(this.provider)) {
                    Cash data = states.get(0).getState().getData();
                    progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                    FixedRateBond fixedRateBond = new FixedRateBond(this.provider, this.observer, this.instrumentId, this.couponDataTriggered, this.cusip, this.isIn,
                            data.getInstrumentId(), this.rate, this.denomination, this.paymentLag, this.couponDate);
                    CommandData cmdType = new IOUContract.Commands.Create();
                    Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());

                    // We create a transaction builder and add the components.
                    progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                    final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                            .addOutputState(fixedRateBond, IOU_CONTRACT_ID)
                            .addCommand(cmd);
                    //Verify the transaction
                    txBuilder.verify(getServiceHub());
                    // Signing the transaction.
                    progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                    final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);

                    // Finalising the transaction.
                    subFlow(new FinalityFlow(signedTx));
                }
            }
            return null;
        }
    }
}
