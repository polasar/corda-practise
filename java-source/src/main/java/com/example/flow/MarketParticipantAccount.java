package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.RepoContract;
import com.example.state.Account;
import com.example.state.MarketParticipant;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.Command;
import net.corda.core.contracts.ContractState;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class MarketParticipantAccount {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {


        private String accountId;

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

        public Initiator( String accountId) {
            this.accountId = accountId;
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

            VaultService vaultService =getServiceHub().getVaultService();
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            // We create the transaction components.
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
            List<StateAndRef<MarketParticipant.Participant>> states = vaultService.queryBy(MarketParticipant.Participant.class, generalCriteria).getStates();

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            for(int  i =0; i< states.size(); i++) {
                StateAndRef<MarketParticipant.Participant> testStateAndRef = states.get(i);
                MarketParticipant.Participant data = testStateAndRef.getState().getData();
                // We create the transaction components.

                progressTracker.setCurrentStep(GENERATING_TRANSACTION);
                if (me.equals(data.getOwner())) {
                    // AccountAddress accountAddress = new AccountAddress(accountId,owner.getName());
                    Account marketParticipantAccount = new Account(data.getProvider(), accountId, data.getAgent(), data.getOwner(), new UniqueIdentifier(accountId, UUID.randomUUID()));
                    //Initialize commandData
                    progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                    Command command = new Command(new RepoContract.Commands.Approve(), Arrays.asList(getOurIdentity().getOwningKey(), data.getAgent().getOwningKey(), data.getProvider().getOwningKey()));

                    // We create a transaction builder and add the components.
                    progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                    final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                            .addInputState(testStateAndRef)
                            .addOutputState(marketParticipantAccount, RepoContract.REPO_CONTRACT_ID)
                            .addCommand(command);
                    //Verify the transaction
                    txBuilder.verify(getServiceHub());

                    // Signing the transaction.
                    progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                    final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
                    FlowSession agentSession = initiateFlow(data.getAgent());
                    FlowSession ownerSession = initiateFlow(data.getProvider());
                    final SignedTransaction fullySignedTx = subFlow(
                            new CollectSignaturesFlow(signedTx, ImmutableSet.of(agentSession, ownerSession), CollectSignaturesFlow.Companion.tracker()));
                    // Finalising the transaction.
                    subFlow(new FinalityFlow(fullySignedTx));
                }
            }
            return null;
        }
    }

    @InitiatedBy(MarketParticipantAccount.Initiator.class)
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
