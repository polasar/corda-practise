package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.RepoContract;
import com.example.schema.CollateralSchemaV1;
import com.example.schema.RepoAllegeSchemaV1;
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
import net.corda.core.utilities.ProgressTracker;
import net.corda.core.utilities.ProgressTracker.Step;

import java.lang.reflect.Field;
import java.security.PublicKey;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.contract.RepoContract.REPO_CONTRACT_ID;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class RepoMatch {
    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private Party operator;

        private final Step GENERATING_TRANSACTION = new Step("Generating transaction based on new Repo.");
        private final Step VERIFYING_TRANSACTION = new Step("Verifying contract constraints.");
        private final Step SIGNING_TRANSACTION = new Step("Signing transaction with our private key.");
        private final Step GATHERING_SIGS = new Step("Gather signatures from the parties.");
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
                GATHERING_SIGS,
                FINALISING_TRANSACTION
        );

        public Initiator(Party operator) {
            this.operator = operator;

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
            VaultService vaultService = getServiceHub().getVaultService();
            Field applicantIsBuyer=null;
            Field collateralLinearId = null;
            // Stage 1.
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            Repo repo = null;
            DvPStart DvPStart = null;
            DvPEnd DvPEnd = null;
            RepoAllege repoAllege = null;
            RepoAllege repoAllege1 = null;
            QueryCriteria criteria1 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            try {
                 applicantIsBuyer = RepoAllegeSchemaV1.PersistentOper.class.getDeclaredField("applicantIsBuyer");
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }
            CriteriaExpression applicantIsBuyerTrue = Builder.equal(applicantIsBuyer, true);
            QueryCriteria lenderCriteria = new QueryCriteria.VaultCustomQueryCriteria(applicantIsBuyerTrue);
            QueryCriteria criteria = criteria1.and(lenderCriteria);
            Vault.Page<RepoAllege> results = vaultService.queryBy(RepoAllege.class, criteria);
            List<StateAndRef<RepoAllege>> buyerStates = results.getStates();

            CriteriaExpression applicantIsSellerTrue = Builder.equal(applicantIsBuyer, false);
            QueryCriteria lenderCriteriaSeller = new QueryCriteria.VaultCustomQueryCriteria(applicantIsSellerTrue);
            QueryCriteria criteriaSeller = criteria1.and(lenderCriteriaSeller);
            Vault.Page<RepoAllege> sellerResults = vaultService.queryBy(RepoAllege.class, criteriaSeller);
            List<StateAndRef<RepoAllege>> sellerStates = sellerResults.getStates();
            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            if(me.equals(operator)) {
                for (int i = 0; i < buyerStates.size(); i++) {

                    for (int j = 0; j < sellerStates.size(); j++) {
                        StateAndRef<RepoAllege> repoAllegeStateAndRef = buyerStates.get(i);
                        StateAndRef<RepoAllege> repoAllegeStateAndRef1 = sellerStates.get(j);
                        RepoAllege buyerData = repoAllegeStateAndRef.getState().getData();
                        RepoAllege sellerData = repoAllegeStateAndRef1.getState().getData();
                        /*

                        GET COLLATERALS

                         */
                        try {
                            collateralLinearId = CollateralSchemaV1.PersistentOper.class.getDeclaredField("linearId");
                        } catch (NoSuchFieldException e) {
                            e.printStackTrace();
                        }
                        CriteriaExpression collateralData = Builder.equal(collateralLinearId, buyerData.getLinearId().getId());
                        QueryCriteria collateralCriteria = new QueryCriteria.VaultCustomQueryCriteria(applicantIsBuyerTrue);
                        QueryCriteria collateralQuery = criteria1.and(collateralCriteria);
                        Vault.Page<Collateral> collateralResults = vaultService.queryBy(Collateral.class, collateralQuery);
                        Collateral collateral = collateralResults.getStates().get(0).getState().getData();
// TODO: 12/17/2018 check instrument id from both the states
                        if (!repoAllegeStateAndRef.getState().toString().isEmpty() && !repoAllegeStateAndRef1.getState().toString().isEmpty()
                                && buyerData.getStatus().equalsIgnoreCase("UNMATCHED") && sellerData.getStatus().equalsIgnoreCase("UNMATCHED")) {


                            if (buyerData.getApplicant().equals(sellerData.getCounterParty()) && buyerData.getCounterParty().equals(sellerData.getApplicant())) {
                                buyerData.setStatus("MATCHED");
                                sellerData.setStatus("MATCHED");
                                UniqueIdentifier uniqueIdentifier = new UniqueIdentifier();
                                repo = new Repo(buyerData.getApplicant(), buyerData.getCounterParty(), buyerData.getRepoId(), buyerData.getEligibilityCriteriaDataId(), uniqueIdentifier,
                                        buyerData.getStartDate(),
                                        buyerData.getEndDate(), buyerData.getTerminationPaymentLeg(), buyerData.getAgent(),buyerData.getAccountId(),buyerData.getAmount());
                                DvPStart = new DvPStart(buyerData.getApplicant(), buyerData.getCounterParty(), "DvP-START", uniqueIdentifier,
                                        buyerData.getEndDate(),collateral.getPledgeCollateralData(), collateral.getBorrowerCollateralData(), buyerData.getAgent(), buyerData.getRepoId());
                                DvPEnd = new DvPEnd(buyerData.getApplicant(), buyerData.getCounterParty(), "DvP-END", uniqueIdentifier,
                                        buyerData.getEndDate(), collateral.getPledgeCollateralData(),collateral.getBorrowerCollateralData(),buyerData.getAgent(),buyerData.getRepoId());
                                repoAllege = new RepoAllege(buyerData.getApplicant(), buyerData.getCounterParty(), buyerData.isApplicantIsBuyer(), buyerData.getRepoId(), buyerData.getEligibilityCriteriaDataId(),
                                        buyerData.getLinearId(), buyerData.getStartDate(), buyerData.getEndDate(), buyerData.getTerminationPaymentLeg(), buyerData.getAgent(), buyerData.getStatus(),
                                        buyerData.getAccountId(),buyerData.getAmount());
                                repoAllege1 = new RepoAllege(sellerData.getApplicant(), sellerData.getCounterParty(), sellerData.isApplicantIsBuyer(), sellerData.getRepoId(), sellerData.getEligibilityCriteriaDataId(), sellerData.getLinearId(), sellerData.getStartDate(),
                                        sellerData.getEndDate(), sellerData.getTerminationPaymentLeg(), sellerData.getAgent(), sellerData.getStatus(),buyerData.getAccountId(),buyerData.getAmount());

                                //Initialize commandData
                                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                                CommandData cmdType = new RepoContract.Commands.Approve();
                                Command cmd = new Command<>(cmdType, getOurIdentity().getOwningKey());
                                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                                        .addInputState(repoAllegeStateAndRef)
                                        .addInputState(repoAllegeStateAndRef1)
                                        .addOutputState(repo, REPO_CONTRACT_ID)
                                        .addOutputState(DvPStart, REPO_CONTRACT_ID)
                                        .addOutputState(DvPEnd, REPO_CONTRACT_ID)
                                        .addOutputState(repoAllege, REPO_CONTRACT_ID)
                                        .addOutputState(repoAllege1, REPO_CONTRACT_ID)
                                        .addCommand(cmd);

                                // Stage 2.
                                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                                // Verify that the transaction is valid.
                                txBuilder.verify(getServiceHub());

                                // Stage 3.
                                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                                // Sign the transaction.
                                final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
                                FlowSession otherparty = initiateFlow(repo.getBuyer());
                                final SignedTransaction fullySignedTx = subFlow(
                                        new CollectSignaturesFlow(signedTx, ImmutableSet.of(otherparty), CollectSignaturesFlow.Companion.tracker()));
                                // Stage 5.
                                progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                                // Notarise and record the transaction in both parties' vaults.
                                return subFlow(new FinalityFlow(signedTx));


                            }
                        }
                    }
                }
            }
            return null;

        }
    }

    @InitiatedBy(Initiator.class)
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
                    super(otherPartyFlow, progressTracker   );
                }

                @Override
                protected void checkTransaction(SignedTransaction stx) {
                    Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
                    Set<PublicKey> requiredSigningKeys = stx.getTx().getRequiredSigningKeys();
                    requireThat(require -> {
                        ContractState output = stx.getTx().getOutputs().get(0).getData();
                        require.using("This must be an Repo transaction.", output instanceof Repo);
                        RepoAllege repoState = (RepoAllege) output;
//                        Map paymentLegs = repoState.getPaymentLegs();
//                        Object price = paymentLegs.get("price");
                        /*require.using("Repo quantity must be greater than Zero.", Double.parseDouble(repoState.getPaymentLegs()) > 100);
                        require.using("Owner of the Repo must be requiredSigner", requiredSigningKeys.contains(me.getOwningKey()));
                        getLogger().info("XXXX get info " + repoState.getCounterParty().getCpparticipantID());*/
                        return null;
                    });
                }
            }

            return subFlow(new SignTxFlow(otherPartyFlow, SignTransactionFlow.Companion.tracker()));
        }
    }
}
