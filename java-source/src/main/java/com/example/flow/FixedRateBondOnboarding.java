package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.schema.AccountSchemaV1;
import com.example.state.*;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.core.utilities.ProgressTracker;

import java.lang.reflect.Field;
import java.util.*;

import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.finance.Currencies.DOLLARS;
import static net.corda.finance.Currencies.issuedBy;


public class FixedRateBondOnboarding {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private Party omniBusProvider;
        private Party omniBusOwner;
        private String omniBusAccountId;
        private Party accountProvider;
        private Party accountOwner;
        private String accountId;

        private final byte[] defaultRef = {1};

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

        public Initiator(Party omniBusProvider, Party omniBusOwner, String omniBusAccountId, Party accountProvider, Party accountOwner, String accountId) {
            this.omniBusProvider = omniBusProvider;
            this.omniBusOwner = omniBusOwner;
            this.omniBusAccountId = omniBusAccountId;
            this.accountProvider = accountProvider;
            this.accountOwner = accountOwner;
            this.accountId = accountId;
        }


        @Override
        public ProgressTracker getProgressTracker() {
            return progressTracker;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {


            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);

            byte[] bytes = new byte[0];

            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            SignedTransaction ftx = null;
            Field omniBusAccountOwner =null;
            Field omniBusAccountProvider = null;
            Field omniBusAccountId =null;
            VaultService vaultService = getServiceHub().getVaultService();
            QueryCriteria criteria1 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            QueryCriteria criteria11 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
//            List<StateAndRef<Account>> states = vaultService.queryBy(Account.class, criteria1).getStates();
            try {
                omniBusAccountProvider = AccountSchemaV1.PersistentOper.class.getDeclaredField("provider");
                omniBusAccountId = AccountSchemaV1.PersistentOper.class.getDeclaredField("accountId");
                omniBusAccountOwner = AccountSchemaV1.PersistentOper.class.getDeclaredField("owner");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            CriteriaExpression isOmniAccountProvides = Builder.equal(omniBusAccountProvider, omniBusProvider);
            QueryCriteria lenderCriteria1 = new QueryCriteria.VaultCustomQueryCriteria(isOmniAccountProvides);
            CriteriaExpression isOmniAccountOwner = Builder.equal(omniBusAccountOwner, omniBusOwner);
            QueryCriteria lenderCriteria2 = new QueryCriteria.VaultCustomQueryCriteria(isOmniAccountOwner);
            CriteriaExpression isOmniAccountId = Builder.equal(omniBusAccountId, this.omniBusAccountId);
            QueryCriteria lenderCriteria3 = new QueryCriteria.VaultCustomQueryCriteria(isOmniAccountId);
            QueryCriteria criteria = criteria1.and(lenderCriteria2.and(lenderCriteria1).and(lenderCriteria3));

            Vault.Page<Account> results = vaultService.queryBy(Account.class, criteria);
            StateAndRef<Account> omniAccountStates = results.getStates().get(0);
            Account omniAccountData = omniAccountStates.getState().getData();

            CriteriaExpression isAccountProvides = Builder.equal(omniBusAccountProvider, accountProvider);
            QueryCriteria lenderCriteria4 = new QueryCriteria.VaultCustomQueryCriteria(isAccountProvides);
            CriteriaExpression isAccountOwner = Builder.equal(omniBusAccountOwner, accountOwner);
            QueryCriteria lenderCriteria5 = new QueryCriteria.VaultCustomQueryCriteria(isAccountOwner);
            CriteriaExpression isAccountId = Builder.equal(omniBusAccountId, accountId);
            QueryCriteria lenderCriteria6 = new QueryCriteria.VaultCustomQueryCriteria(isAccountId);
            QueryCriteria  criteriadata = criteria11.and(lenderCriteria4.and(lenderCriteria5).and(lenderCriteria6));

            Vault.Page<Account> accountResults = vaultService.queryBy(Account.class, criteriadata);
            StateAndRef<Account> accountStates = accountResults.getStates().get(0);
            Account accountData = accountStates.getState().getData();

            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            List<StateAndRef<AssetIssuanceRequest>> assetOnboardRequests = vaultService.queryBy(AssetIssuanceRequest.class, generalCriteria).getStates();


            if(accountStates!= null && omniAccountStates!= null) {
                for (int i = 0; i < assetOnboardRequests.size(); i++) {

                    StateAndRef<AssetIssuanceRequest> bondStateAndRef = assetOnboardRequests.get(i);
                    AssetIssuanceRequest assetIssuanceRequestData = bondStateAndRef.getState().getData();
                    if (me.equals(assetIssuanceRequestData.getProvider())&& assetIssuanceRequestData.getOmniBusAccountId().equals(omniAccountData.getAccountId())&& assetIssuanceRequestData.getProvider().equals(omniAccountData.getOwner())
                    && assetIssuanceRequestData.getProvider().equals(accountData.getProvider()) && assetIssuanceRequestData.getOwner().equals(accountData.getOwner())&& assetIssuanceRequestData.getAccountId().equals(accountData.getAccountId())
                    && assetIssuanceRequestData.getNotificationStatus().equals("Pending")) {
                        //Create transaction components
                        Command command = new Command(new Asset.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey(), assetIssuanceRequestData.getOperator().getOwningKey(), assetIssuanceRequestData.getOwner().getOwningKey()));
                        PartyAndReference partyAndReference = new PartyAndReference(assetIssuanceRequestData.getOperator(), OpaqueBytes.of(defaultRef));
                        Amount<Currency> amount = DOLLARS(assetIssuanceRequestData.getQuantity());
                         Amount<Issued<Currency>> issuedAmount = issuedBy(DOLLARS(assetIssuanceRequestData.getQuantity()), assetIssuanceRequestData.getOperator().ref(defaultRef));
                        Issued issuerAndToken = new Issued(partyAndReference, amount.getToken());
//                        Amount issuedAmount = new Amount(amount.getQuantity(), issuerAndToken);
                        assetIssuanceRequestData.setNotificationStatus("Approved");
                        Asset.Cash cashState = new Asset.Cash(getOurIdentity(), assetIssuanceRequestData.getOwner(), assetIssuanceRequestData.getOperator(), issuedAmount,
                                assetIssuanceRequestData.getInstrumentId(), assetIssuanceRequestData.getAccountId(), assetIssuanceRequestData.getNotificationStatus(), assetIssuanceRequestData.getLinearId());
                        TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                                .addInputState(bondStateAndRef)
                                .addOutputState(cashState, Asset.PROGRAM_ID)
                                .addCommand(command);

                        SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder, getOurIdentity().getOwningKey());
                        FlowSession issuerSignature = initiateFlow(assetIssuanceRequestData.getOperator());
                        FlowSession ownerSignature = initiateFlow(assetIssuanceRequestData.getOwner());
                        final SignedTransaction fullySignedTx = subFlow(
                                new CollectSignaturesFlow(signedTransaction, ImmutableSet.of(issuerSignature, ownerSignature), CollectSignaturesFlow.Companion.tracker()));
                        transactionBuilder.verify(getServiceHub());

                        ftx = subFlow(new FinalityFlow(fullySignedTx));

                    }
                }
            }
            return ftx;
        }
    }


}
/*@InitiatingFlow
class BroadCastTxn extends FlowLogic<Void>{
    private SignedTransaction stx;
    private AbstractParty provider;
    public BroadCastTxn(SignedTransaction stx,AbstractParty provider){
        this.stx =stx;
        this.provider = provider;
    }
    @Suspendable
    @Override
    public Void call() throws FlowException {

        Iterator<Party> custodian = getServiceHub().getIdentityService().partiesFromName("Custodian", true).iterator();
        getLogger().info(" XXXXXXXX Observable States Party XXXXX", custodian);
        Party next = custodian.next();

        getLogger().info(" XXXXXXXX Custodian Party XXXXX", next.getName());
        getLogger().info(" XXXXXXXX Provider Party XXXXX", provider);
        FlowSession flowSession = initiateFlow(next);
        return subFlow(new SendTransactionFlow(flowSession,stx));

    }
}


@InitiatedBy(BroadCastTxn.class)
class ReceiveRegulatoryReportFlow extends FlowLogic<Void> {
    private final FlowSession otherPartyFlow;
    public ReceiveRegulatoryReportFlow(FlowSession otherPartyFlow) {
        this.otherPartyFlow = otherPartyFlow;
    }

    @Suspendable
    @Override
    public Void call() throws FlowException {
         subFlow(new ReceiveTransactionFlow(otherPartyFlow, true, StatesToRecord.ALL_VISIBLE));
        return null;
    }
}*/

@InitiatedBy(FixedRateBondOnboarding.Initiator.class)
class Acceptor extends FlowLogic<SignedTransaction> {
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
                    /*ContractState output = stx.getTx().getOutputs().get(0).getData();
                    require.using("This must be an IOU transaction.", output instanceof IOUState);
                    IOUState iou = (IOUState) output;
                    require.using("I won't accept IOUs with a value over 100.", iou.getValue() <= 100);*/
                    return null;
                });
            }
        }

        return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
    }
}



