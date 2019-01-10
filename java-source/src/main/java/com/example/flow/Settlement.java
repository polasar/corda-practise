package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.schema.CashSchemaV1;
import com.example.schema.DvPSchemaV1;
import com.example.schema.RepoSchemaV1;
import com.example.state.Asset;
import com.example.state.CollateralData;
import com.example.state.DvPStart;
import com.example.state.Repo;
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

import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.finance.Currencies.DOLLARS;

public class Settlement {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction>{
        private String repoId;
        private UUID uuid;

        public Initiator(String repoId, UUID uuid)
        {
            this.repoId =repoId;
            this.uuid =uuid;
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

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {

            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            //get the notary identification
            progressTracker.setCurrentStep(GENERATE_TRANSACTION);
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            TransactionBuilder transactionBuilder = new TransactionBuilder(notary);
            SignedTransaction signedTransaction1=null;
            VaultService vaultService = getServiceHub().getVaultService();
            Field repoId =null;
            Field dvpRepoId = null;
            Field uuid = null;

                // Get the repo, dvp legs,asset,account for asset transfer
                QueryCriteria criteria1 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
                QueryCriteria criteria12 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
                try {
                    repoId = RepoSchemaV1.PersistentOper.class.getDeclaredField("repoId");
                    uuid = RepoSchemaV1.PersistentOper.class.getDeclaredField("linearId");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }
                CriteriaExpression isrepoId = Builder.equal(repoId, this.repoId);
                QueryCriteria lenderCriteria = new QueryCriteria.VaultCustomQueryCriteria(isrepoId);
                CriteriaExpression linearIdCheck = Builder.equal(uuid, this.uuid);
                QueryCriteria lenderCriteriaForUUID = new QueryCriteria.VaultCustomQueryCriteria(linearIdCheck);
                QueryCriteria criteria = criteria1.and(lenderCriteria.and(lenderCriteriaForUUID));

                /*
                * REPO states
                *
                * */
                Vault.Page<Repo> results = vaultService.queryBy(Repo.class, criteria);
                StateAndRef<Repo> buyerStates = results.getStates().get(0);

                //DvP
                try {
                    dvpRepoId = DvPSchemaV1.PersistentOper.class.getDeclaredField("repoId");
                    uuid = DvPSchemaV1.PersistentOper.class.getDeclaredField("linearId");
                } catch (NoSuchFieldException e) {
                    e.printStackTrace();
                }

                CriteriaExpression isDvPRepoId = Builder.equal(dvpRepoId, this.repoId);
                QueryCriteria dvpCriteria = new QueryCriteria.VaultCustomQueryCriteria(isDvPRepoId);
                CriteriaExpression linearIdCheckForDvP = Builder.equal(uuid, this.uuid);
                QueryCriteria lenderCriteriaUUID = new QueryCriteria.VaultCustomQueryCriteria(linearIdCheckForDvP);
                QueryCriteria dvpCriteriaData = criteria12.and(lenderCriteriaUUID.and(dvpCriteria));

                /*
                * DVP Start states
                *
                * */

                Vault.Page<DvPStart> dvpResults = vaultService.queryBy(DvPStart.class, dvpCriteriaData);
                StateAndRef<DvPStart> dvpStates = dvpResults.getStates().get(0);

                /*

                BUYER AND SELLER
                 */
                Party buyer = buyerStates.getState().getData().getBuyer();
                Party seller = buyerStates.getState().getData().getSeller();

                /*
                PAYMENT AND DELIVERY LEGS
                 */

            List<CollateralData.Pledge> pledgeList = dvpStates.getState().getData().getPledgeList();
            List<CollateralData.Borrower> borrowerList = dvpStates.getState().getData().getBorrowerList();
                for(int i=0, j=0; i<pledgeList.size() && j<borrowerList.size(); i++,j++) {
                    CollateralData.Pledge pledge = pledgeList.get(i);
                    String paymentInstrumentId = pledge.getInstrumentId();
                    Long paymentPrice = pledge.getCurrentQuantity();
                    CollateralData.Borrower borrower = borrowerList.get(j);
                    String deliveryLegInstrumentId = borrower.getInstrumentId();
                    Long deliveryLegPrice = borrower.getCurrentQuantity();
                /*

                ASSET TRANSFER FLOW STARTS
                 */
                    transactionBuilder = AssetTransfer(buyer, seller, paymentInstrumentId, deliveryLegInstrumentId, paymentPrice, deliveryLegPrice, transactionBuilder);

                }
                /*

            CREATE MOVE COMMAND WITH OWNER, COUNTERPARTY,CUSTODIAN SIGNATURES

             */
            Command command = new Command(new Asset.Commands.Move(), Arrays.asList(getOurIdentity().getOwningKey(), buyer.getOwningKey(), seller.getOwningKey()));

            /*

                /*

                CONSUME DVP-START AND REPO STATES
                 */

                transactionBuilder.addInputState(dvpStates);
                transactionBuilder.addCommand(command);

                /*

                SIGNING THE TRANSACTION
                 */

                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder,getOurIdentity().getOwningKey());

                /*
                INITIATE FLOW SESSION AND GET SIGNATURES FROM COUNTER PARTIES
                 */
                FlowSession deliveryOwnerSession = initiateFlow(buyer);
                FlowSession paymentOwnerSession = initiateFlow(seller);
                final SignedTransaction fullySignedTx = subFlow(
                    new CollectSignaturesFlow(signedTransaction, ImmutableSet.of(deliveryOwnerSession,paymentOwnerSession), CollectSignaturesFlow.Companion.tracker()));

                /*
                VERIFY THE SIGNATURE
                 */
                transactionBuilder.verify(getServiceHub());
                /*
                FINALITY FLOW
                 */
                return subFlow(new FinalityFlow(fullySignedTx));

        }

        private TransactionBuilder AssetTransfer(Party owner, Party counterParty,String paymentInstrumentId,String deliveryInstrumentId,Long paymentPrice,Long deliveryPrice,TransactionBuilder tx) {

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
            /*

            GET BUYER ASSETS
             */
            Vault.Page<Asset.Cash> inputBondStateRef  = vaultService.queryBy(new Asset.Cash().getClass(),criteriaBuyer);


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
            /*

            GET SELLER ASSETS

             */
            Vault.Page<Asset.Cash> inputCashStateRef  = vaultService.queryBy(new Asset.Cash().getClass(),criteriaSeller);
            /*

            CREATE MOVE COMMAND WITH OWNER, COUNTERPARTY,CUSTODIAN SIGNATURES

             */
            Command command = new Command(new Asset.Commands.Move(), Arrays.asList(getOurIdentity().getOwningKey(), owner.getOwningKey(), counterParty.getOwningKey()));

            /*
            BUYER STATES
             */
            List<StateAndRef<Asset.Cash>> bondStates = inputBondStateRef.getStates();

            /*
            SELLER STATES
             */
            List<StateAndRef<Asset.Cash>> cashStates = inputCashStateRef.getStates();

            long addBondAmount=0;
            long addCashAmount=0;
            /*

            ITERATE TO ADD ALL THE ASSET FOR SAME INSTRUMENT AND OWNER
             */
            for(int  i=0; i<bondStates.size();i++){
                StateAndRef<Asset.Cash> bondStateAndRef = bondStates.get(i);
                long amount = bondStateAndRef.getState().getData().getAmount().getQuantity();
                addBondAmount += amount;

            }

            for(int i=0; i<cashStates.size();i++){
                StateAndRef<Asset.Cash> cashStateAndRef = cashStates.get(i);
                long quantity = cashStateAndRef.getState().getData().getAmount().getQuantity();
                addCashAmount += quantity;
            }


            Amount<Currency> bondAmount = DOLLARS(paymentPrice);
            Amount<Currency> cashAmount = DOLLARS(deliveryPrice);
            if(addBondAmount>=bondAmount.getQuantity() && addCashAmount>=cashAmount.getQuantity()) {

                    tx = getOutPuts(owner, counterParty, bondStates, cashStates, bondAmount, cashAmount,tx);

            }
            else {
                throw new IllegalArgumentException("There are no enough positions");
            }


            tx.addCommand(command);
           return tx;

        }


        private TransactionBuilder getOutPuts(AbstractParty owner, AbstractParty counterParty, List<StateAndRef<Asset.Cash>> bondStates,
                                              List<StateAndRef<Asset.Cash>> cashStates, Amount<Currency> bondAmount, Amount<Currency> cashAmount,TransactionBuilder tx) {

            int i;
            int j;
            /*
            GET THE BOND AND CASH AMOUNT VALUES IN LONG FOR SUBTRACTION AND ADDITION
             */
            long bondAmountValue =bondAmount.getQuantity();
            long cashAmountValue = cashAmount.getQuantity();
            for( i=0,j=0;i< bondStates.size() && j< cashStates.size(); i++, j++) {

                StateAndRef<Asset.Cash> bondState = bondStates.get(i);
                StateAndRef<Asset.Cash> cashState = cashStates.get(j);

                long bondAsset = bondState.getState().getData().getAmount().getQuantity();
                PartyAndReference issuer = bondState.getState().getData().getAmount().getToken().getIssuer();

                String accountId = bondState.getState().getData().getAccountId();
                String deliveryAccountId = cashState.getState().getData().getAccountId();
                List<Asset.Cash> assetList = new ArrayList<Asset.Cash>();
                List<Asset.Cash> cashAssetList = new ArrayList<Asset.Cash>();
                TransactionState<Asset.Cash> templateState = bondState.getState();
                TransactionState<Asset.Cash> templateState1 = cashState.getState();

                if (bondAmountValue!= 0) {

                    if (bondAsset > bondAmountValue) {
                        bondAsset = bondAsset - bondAmountValue;
                        assetList.add(deriveState(templateState, new Amount(bondAsset, new Issued(issuer, bondAmount.getToken())), owner, accountId));
                        assetList.add(deriveState(templateState, new Amount(bondAmountValue, new Issued(issuer, bondAmount.getToken())), counterParty, deliveryAccountId));
                        tx.addOutputState(assetList.get(0), Asset.PROGRAM_ID);
                        tx.addOutputState(assetList.get(1), Asset.PROGRAM_ID);
                        bondAmountValue = 0;
                    } else if (bondAsset < bondAmountValue) {
                         bondAmountValue = bondAmountValue - bondAsset;
                        assetList.add(deriveState(templateState, new Amount(bondAsset, new Issued(issuer, bondAmount.getToken())), counterParty, deliveryAccountId));
                        tx.addOutputState(assetList.get(0), Asset.PROGRAM_ID);
                    } else if (bondAsset == bondAmountValue) {
                        assetList.add(deriveState(templateState, new Amount(bondAsset, new Issued(issuer, bondAmount.getToken())), counterParty, deliveryAccountId));
                        tx.addOutputState(assetList.get(0), Asset.PROGRAM_ID);
                        bondAmountValue = 0;
                    }

                }

                long cashAsset = cashState.getState().getData().getAmount().getQuantity();

                if (cashAmountValue != 0) {

                    if (cashAsset > cashAmountValue) {
                        cashAsset = cashAsset - cashAmountValue;
                        cashAssetList.add(deriveState(templateState1, new Amount(cashAsset, new Issued(issuer, cashAmount.getToken())), counterParty, deliveryAccountId));
                        cashAssetList.add(deriveState(templateState1, new Amount(cashAmountValue, new Issued(issuer, cashAmount.getToken())), owner, accountId));
                        tx.addOutputState(cashAssetList.get(0), Asset.PROGRAM_ID);
                        tx.addOutputState(cashAssetList.get(1), Asset.PROGRAM_ID);
                        cashAmountValue = 0;
                    } else if (cashAsset < cashAmountValue) {
                        cashAmountValue = cashAmountValue - cashAsset;
                        cashAssetList.add(deriveState(templateState1, new Amount(cashAsset, new Issued(issuer, cashAmount.getToken())), owner, accountId));
                        tx.addOutputState(cashAssetList.get(0), Asset.PROGRAM_ID);
                    } else if (cashAsset == cashAmountValue) {
                        cashAssetList.add(deriveState(templateState1, new Amount(cashAsset, new Issued(issuer, cashAmount.getToken())), owner, accountId));
                        tx.addOutputState(cashAssetList.get(0), Asset.PROGRAM_ID);
                        cashAmountValue=0;
                    }

                }
                tx.addInputState(bondState);
                tx.addInputState(cashState);
            }
            return tx;
        }

        private Asset.Cash deriveState(TransactionState<Asset.Cash> templateState, Amount amount, AbstractParty owner,String accountId) {
            Asset.Cash data = templateState.getData();
            Asset.Cash assetCash = new Asset.Cash(data.getProvider(), owner,data.getObserver(),amount,data.getInstrumentId(),accountId/*,data.getDeposit()*/,"Settled",data.getLinearId());
            return assetCash;
        }


    }

    @InitiatedBy(Settlement.Initiator.class)
    public static class Acceptor extends FlowLogic<SignedTransaction> {

        private final FlowSession otherPartyFlow;

        public Acceptor(FlowSession otherPartyFlow) {
            this.otherPartyFlow = otherPartyFlow;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            class SignTxFlow extends SignTransactionFlow {
                private SignTxFlow(FlowSession otherPartyFlow, ProgressTracker progressTracker) {
                    super(otherPartyFlow, progressTracker);
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        return null;
                    });
                }
            }

            return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
        }
    }
}
