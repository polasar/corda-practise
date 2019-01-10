package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import com.example.state.Asset;
import net.corda.core.contracts.*;
import net.corda.core.flows.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.transactions.SignedTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.core.utilities.OpaqueBytes;
import net.corda.core.utilities.ProgressTracker;

import java.util.Arrays;
import java.util.Currency;

import static com.example.contract.IOUContract.IOU_CONTRACT_ID;
import static net.corda.finance.Currencies.DOLLARS;
import static net.corda.finance.Currencies.issuedBy;


public class CashOnboarding {

    @StartableByRPC
    @InitiatingFlow
    public static class Initiator extends FlowLogic<SignedTransaction> {
        private AbstractParty provider;
        private AbstractParty owner;
        private Party observer;
        private Long quantity;
        private String instrumentId;
        private final byte[] defaultRef = {1};
        private String accountId;
        private String status;

        public Initiator(AbstractParty provider, AbstractParty owner, Party observer, long quantity, String instrumentId,String accountId,String status) {
            this.provider = provider;
            this.owner = owner;
            this.observer = observer;
            this.quantity = quantity;
            this.instrumentId = instrumentId;
            this.accountId= accountId;
            this.status =status;
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

            //Get the notaries
            Party notary = getServiceHub().getNetworkMapCache().getNotaryIdentities().get(0);
            //Get the Party
            Party me = getServiceHub().getMyInfo().getLegalIdentities().get(0);
            progressTracker.setCurrentStep(GENERATE_TRANSACTION);
            SignedTransaction signedTransaction1 = null;
            //Create the state
           // Amount<Issued<Currency>> issuedAmount = issuedBy(DOLLARS(quantity), owner.ref(defaultRef));
            if(me.equals(provider)) {
                Command command = new Command(new Asset.Commands.Issue(), Arrays.asList(getOurIdentity().getOwningKey()));
                PartyAndReference partyAndReference = new PartyAndReference(getOurIdentity(), OpaqueBytes.of(defaultRef));
                Amount<Currency> amount = DOLLARS(quantity);
                // Amount<Issued<Currency>> issuedAmount = issuedBy(DOLLARS(quantity), owner.ref(defaultRef));
                Issued issuerAndToken = new Issued(partyAndReference, amount.getToken());
                Amount issuedAmount = new Amount(amount.getQuantity(), issuerAndToken);
                UniqueIdentifier uniqueIdentifier = new UniqueIdentifier();
                Asset.Cash cashState = new Asset.Cash(this.provider,this.owner,this.observer,issuedAmount,this.instrumentId,this.accountId/*,owner.ref(defaultRef)*/,this.status,uniqueIdentifier);
                TransactionBuilder transactionBuilder = new TransactionBuilder(notary)
                        .addOutputState(cashState, Asset.PROGRAM_ID)
                        .addCommand(command);

                SignedTransaction signedTransaction = getServiceHub().signInitialTransaction(transactionBuilder);
                signedTransaction1 = subFlow(new FinalityFlow(signedTransaction));
            }
            return signedTransaction1;
        }
    }
}
