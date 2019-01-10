package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.schema.CashSchemaV1;
import com.example.state.Asset;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.TransactionState;
import net.corda.core.flows.*;
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
import java.util.Arrays;

public class FixedRateBondOnboardingApproval {

@InitiatingFlow
@StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {
    private String instrumentId;
    private SignedTransaction ftx;

    public Initiator(String instrumentId) {
        this.instrumentId = instrumentId;
    }

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


    @Override
    public ProgressTracker getProgressTracker() {
        return progressTracker;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        // We retrieve the notary identity from the network map.
        final Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
        VaultService vaultService = getServiceHub().getVaultService();
        Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
        // We create the transaction components.
        QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Field instrument = null;
        try {
            // instrumentId = CashSchemaV1.PersistentOper.class.getDeclaredField("instrumentId");
            instrument = CashSchemaV1.PersistentOper.class.getDeclaredField("instrument");
            //  accountId = AssetSchemaV1.PersistentOper.class.getDeclaredField("accountId");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        CriteriaExpression instrumentCriteria = Builder.equal(instrument, instrumentId);
        QueryCriteria instrumentQueryCriteria = new QueryCriteria.VaultCustomQueryCriteria(instrumentCriteria);
        Vault.Page<Asset.Cash> inputCashStateRef = vaultService.queryBy(new Asset.Cash().getClass(), instrumentQueryCriteria);

        progressTracker.setCurrentStep(GENERATING_TRANSACTION);
        StateAndRef<Asset.Cash> cashStateAndRef = inputCashStateRef.getStates().get(0);

        TransactionState<Asset.Cash> assetState = inputCashStateRef.getStates().get(0).getState();
        Asset.Cash assetStateData = assetState.getData();
        //create the state
        assetStateData.setStatus("Asset Onboarded");
        Asset.Cash cashState = new Asset.Cash(assetStateData.getProvider(), assetStateData.getOwner(), assetStateData.getObserver(),
                assetStateData.getAmount(), assetStateData.getInstrumentId(), assetStateData.getAccountId(), assetStateData.getStatus(),assetStateData.getLinearId());
        Command command = new Command(new Asset.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey()));
        TransactionBuilder tx = new TransactionBuilder(notary)
                .addInputState(cashStateAndRef)
                .addOutputState(cashState, Asset.PROGRAM_ID)
                .addCommand(command);
        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx, getOurIdentity().getOwningKey());

        tx.verify(getServiceHub());
//            FlowSession flowSession = initiateFlow((Party) cashState.getOwner());
//            final SignedTransaction fullySignedTx = subFlow(
//                    new CollectSignaturesFlow(signedTransaction, ImmutableSet.of(flowSession), CollectSignaturesFlow.Companion.tracker()));
     //   subFlow(new BroadCastTxn(signedTransaction, cashState.getProvider()));
        ftx = subFlow(new FinalityFlow(signedTransaction));


        getLogger().info("XXXXXX Observable States execution XXXXX", ftx);


        return ftx;
    }
}
    }

//    @InitiatedBy(Initiator.class)
//    public static class Acceptor extends FlowLogic<SignedTransaction> {
//
//        private final FlowSession otherPartyFlow;
//
//        public Acceptor(FlowSession otherPartyFlow) {
//            this.otherPartyFlow = otherPartyFlow;
//        }
//
//        @Suspendable
//        @Override
//        public SignedTransaction call() throws FlowException {
//            class SignTxFlow extends SignTransactionFlow {
//                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
//                    super(otherPartyFlow, progressTracker);
//                }
//
//                @Override
//                protected void checkTransaction(SignedTransaction stx) {
//                    requireThat(require -> {
//                        return null;
//                    });
//                }
//            }
//            SignedTransaction signedTransaction = subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
//            return signedTransaction;
//
//        }
//    }
//}


/*@InitiatingFlow
class ReportTxn extends FlowLogic<Void>{
    private SignedTransaction stx;
    private AbstractParty provider;
    public ReportTxn(SignedTransaction stx,AbstractParty provider){
        this.stx =stx;
        this.provider = provider;
    }
    @Suspendable
    @Override
    public Void call() throws FlowException {

        Iterator<Party> custodian = getServiceHub().getIdentityService().partiesFromName("BR", true).iterator();
        getLogger().info(" XXXXXXXX Observable States Party XXXXX", custodian);
        Party next = custodian.next();

        getLogger().info(" XXXXXXXX Custodian Party XXXXX", next.getName());
        getLogger().info(" XXXXXXXX Provider Party XXXXX", provider);
        FlowSession flowSession = initiateFlow(next);
        return subFlow(new SendTransactionFlow(flowSession,stx));

    }
}


@InitiatedBy(ReportTxn.class)
class ReceiveTransaction extends FlowLogic<Void> {
    private final FlowSession otherPartyFlow;
    public ReceiveTransaction(FlowSession otherPartyFlow) {
        this.otherPartyFlow = otherPartyFlow;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {
        subFlow(new ReceiveTransactionFlow(otherPartyFlow, true, StatesToRecord.ALL_VISIBLE));
        return null;
    }
}*/
