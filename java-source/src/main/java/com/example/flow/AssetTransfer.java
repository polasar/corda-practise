package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.schema.CashSchemaV1;
import com.example.state.Asset;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.*;
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
import java.util.*;

import static net.corda.finance.Currencies.DOLLARS;

public class AssetTransfer {

    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction>{
        private AbstractParty counterParty;
        private AbstractParty owner;
        private String paymentInstrumentId;
        private String deliveryInstrumentId;
        private Long paymentPrice;
        private Long deliveryPrice;

        public Initiator(Party owner, Party counterParty,String paymentInstrumentId,String deliveryInstrumentId,Long paymentPrice,Long deliveryPrice) {
            this.owner = owner;
            this.counterParty = counterParty;
            this.paymentInstrumentId = paymentInstrumentId;
            this.deliveryInstrumentId = deliveryInstrumentId;
            this.paymentPrice =paymentPrice;
            this.deliveryPrice = deliveryPrice;
        }

        private final ProgressTracker.Step GENERATE_TRANSACTION = new ProgressTracker.Step("Generate transaction based on new repo");
        private final ProgressTracker.Step VERIFY_TRANSACTION = new ProgressTracker.Step("Verify contracts");
        private final ProgressTracker.Step SIGNING_TRANSACTION = new ProgressTracker.Step("Signing the transaction");
        private final ProgressTracker.Step FINALISE_TRANSACTION = new ProgressTracker.Step("Finalise the transaction") {
            @Override
            public ProgressTracker childProgressTracker() {
                return FinalityFlow.tracker();
            }
        };

        private final ProgressTracker progressTracker = new ProgressTracker(
                GENERATE_TRANSACTION,
                VERIFY_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISE_TRANSACTION
        );

        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Override
        @Suspendable
        public SignedTransaction call() throws FlowException {
            //Get the notaries
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            //Get the Party
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            progressTracker.setCurrentStep(GENERATE_TRANSACTION);
            //Create the state
            Field ownerId = null;
            Field instrument = null;
            QueryCriteria criteria1 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            VaultService vaultService =getServiceHub().getVaultService();
            try {

                ownerId = CashSchemaV1.PersistentOper.class.getDeclaredField("owner");
                instrument = CashSchemaV1.PersistentOper.class.getDeclaredField("instrument");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            CriteriaExpression buyer = Builder.equal(ownerId,owner);
            QueryCriteria borrowerCriteria = new QueryCriteria.VaultCustomQueryCriteria(buyer);
            CriteriaExpression instrumentId = Builder.equal(instrument,paymentInstrumentId);
            QueryCriteria instrumentCriteria = new QueryCriteria.VaultCustomQueryCriteria(instrumentId);
            QueryCriteria criteriaBuyer = criteria1.and(borrowerCriteria.and(instrumentCriteria));
            //Input States
            Vault.Page<Asset.Cash> inputCashStateRef  = vaultService.queryBy(new Asset.Cash().getClass(),criteriaBuyer);


            QueryCriteria criteria12 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            try {

                ownerId = CashSchemaV1.PersistentOper.class.getDeclaredField("owner");
                instrument = CashSchemaV1.PersistentOper.class.getDeclaredField("instrument");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            CriteriaExpression seller = Builder.equal(ownerId,counterParty);
            QueryCriteria sellerCriteria = new QueryCriteria.VaultCustomQueryCriteria(seller);
            CriteriaExpression deliveryInstrument = Builder.equal(instrument,deliveryInstrumentId);
            QueryCriteria deliveryInstrumentCriteria = new QueryCriteria.VaultCustomQueryCriteria(deliveryInstrument);
            QueryCriteria criteriaSeller = criteria12.and(sellerCriteria.and(deliveryInstrumentCriteria));
            //Input States
            Vault.Page<Asset.Cash> inputBondStateRef  = vaultService.queryBy(new Asset.Cash().getClass(),criteriaSeller);


            StateAndRef<Asset.Cash> state = inputCashStateRef.getStates().get(0);
            Amount<Currency> amount = DOLLARS(paymentPrice);

            //Delivery legs
            StateAndRef<Asset.Cash> deliveryState = inputBondStateRef.getStates().get(0);
            Amount<Currency> amount1 = DOLLARS(deliveryPrice);

            Command command = new Command(new Asset.Commands.Move(), Arrays.asList(getOurIdentity().getOwningKey(),state.getState().getData().getOwner(),state.getState().getData().getProvider(),deliveryState.getState().getData().getOwner()));
            List<Asset.Cash> outPuts = getOutPuts(owner, counterParty, state,deliveryState,amount,amount1);
            TransactionBuilder tx = new TransactionBuilder(notary)
                    .addInputState(state)
                    .addInputState(deliveryState)
                    .addOutputState(outPuts.get(0), Asset.PROGRAM_ID)
                    .addOutputState(outPuts.get(1),Asset.PROGRAM_ID)
                    .addOutputState(outPuts.get(2),Asset.PROGRAM_ID)
                    .addOutputState(outPuts.get(3),Asset.PROGRAM_ID)
                    .addCommand(command);
            SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(tx);
            FlowSession custodianSession = initiateFlow((Party) state.getState().getData().getProvider());
            FlowSession deliveryOwnerSession = initiateFlow((Party) deliveryState.getState().getData().getOwner());
            FlowSession paymentOwnerSession = initiateFlow((Party) state.getState().getData().getOwner());
            final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(signedTransaction, ImmutableSet.of(custodianSession,deliveryOwnerSession,paymentOwnerSession), CollectSignaturesFlow.Companion.tracker()));
            return subFlow(new FinalityFlow(fullySignedTx));

        }

        private List<Asset.Cash> getOutPuts(AbstractParty owner, AbstractParty counterParty, StateAndRef<Asset.Cash> state,StateAndRef<Asset.Cash> deliveryState, Amount<Currency> amount,Amount<Currency> amount1) {
            long total = state.getState().getData().getAmount().getQuantity();
            PartyAndReference issuer = state.getState().getData().getAmount().getToken().getIssuer();
            long total1 = deliveryState.getState().getData().getAmount().getQuantity();
            String accountId = state.getState().getData().getAccountId();
            String deliveryAccountId = deliveryState.getState().getData().getAccountId();
            long remainder = total - amount.getQuantity();
            long remainder1 = total1 - amount1.getQuantity();
            TransactionState<Asset.Cash> templateState = state.getState();
            TransactionState<Asset.Cash> templateState1 = deliveryState.getState();

//            List<Asset.Cash> assetList = new ArrayList((Collection) new Asset.Cash());
            List<Asset.Cash> assetList = new ArrayList<Asset.Cash>();
            //Form outputs
            if(remainder>0 && remainder1>0){
                assetList.add(deriveState(templateState, new Amount(remainder, new Issued(issuer, amount.getToken())), owner,deliveryAccountId));
                assetList.add(deriveState(templateState1, new Amount(remainder1, new Issued(issuer, amount1.getToken())), counterParty,accountId));

            }
            assetList.add(deriveState(templateState, new Amount(amount.getQuantity(), new Issued(issuer,amount.getToken())), counterParty,accountId));
            assetList.add(deriveState(templateState1, new Amount(amount1.getQuantity(), new Issued(issuer,amount1.getToken())), owner,deliveryAccountId));

        return assetList;
        }

        private Asset.Cash deriveState(TransactionState<Asset.Cash> templateState, Amount amount, AbstractParty owner,String accountId) {
            Asset.Cash data = templateState.getData();
            Asset.Cash assetCash = new Asset.Cash(data.getProvider(), owner,data.getObserver(),amount,data.getInstrumentId(),accountId/*,data.getDeposit()*/,"");
            return assetCash;
        }


    }
}
