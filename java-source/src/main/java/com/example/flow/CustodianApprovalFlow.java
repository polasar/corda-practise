package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.contract.IOUContract;
import com.example.contract.RepoContract;
import com.example.state.Account;
import com.example.state.AccountAddress;
import com.example.state.Custodian;
import com.example.state.IOUState;
import com.google.common.collect.ImmutableSet;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.ProgressTracker;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.example.contract.IOUContract.IOU_CONTRACT_ID;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class CustodianApprovalFlow {

    @InitiatingFlow
    @StartableByRPC
    public static class Initiator extends FlowLogic<SignedTransaction> {

        private String omniBusAccountId;

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

        public Initiator(String omniBusAccountId) {
            this.omniBusAccountId = omniBusAccountId;
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
            QueryCriteria generalCriteria = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.ALL);
            List<StateAndRef<Custodian.Test>> states = vaultService.queryBy(Custodian.Test.class, generalCriteria).getStates();

            progressTracker.setCurrentStep(GENERATING_TRANSACTION);
            StateAndRef<Custodian.Test> testStateAndRef = states.get(0);
            Custodian.Test data = testStateAndRef.getState().getData();
            if(data.getStatus().equalsIgnoreCase("Custodian Invitation")&& me.equals(data.getProvider())) {


                Custodian.Test1 custodian = new Custodian.Test1(data.getProvider(), data.getAgent(),omniBusAccountId);
                Account account = new Account(data.getProvider(),omniBusAccountId,data.getAgent(),me,new UniqueIdentifier(omniBusAccountId, UUID.randomUUID()));
                //Initialize commandData
                progressTracker.setCurrentStep(VERIFYING_TRANSACTION);
                Command command = new Command(new RepoContract.Commands.Approve(), Arrays.asList(getOurIdentity().getOwningKey(),data.getProvider().getOwningKey()));

                // We create a transaction builder and add the components.
                progressTracker.setCurrentStep(SIGNING_TRANSACTION);
                final TransactionBuilder txBuilder = new TransactionBuilder(notary)
                        .addInputState(testStateAndRef)
                        .addOutputState(custodian, RepoContract.REPO_CONTRACT_ID)
                        .addOutputState(account,RepoContract.REPO_CONTRACT_ID)
                        .addCommand(command);
                //Verify the transaction
                  txBuilder.verify(getServiceHub());
                // Signing the transaction.
                progressTracker.setCurrentStep(FINALISING_TRANSACTION);
                final SignedTransaction signedTx = getServiceHub().signInitialTransaction(txBuilder);
                FlowSession custodianSession = initiateFlow(data.getProvider());
                final SignedTransaction fullySignedTx = subFlow(
                        new CollectSignaturesFlow(signedTx, ImmutableSet.of(custodianSession), CollectSignaturesFlow.Companion.tracker()));
                // Finalising the transaction.
                subFlow(new FinalityFlow(fullySignedTx));

            }
            return null;
        }
    }

    @InitiatedBy(CustodianApprovalFlow.Initiator.class)
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
