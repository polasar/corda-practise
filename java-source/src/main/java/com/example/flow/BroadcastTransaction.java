package com.example.flow;

import co.paralleluniverse.fibers.Suspendable;
import net.corda.core.flows.*;
import net.corda.core.identity.Party;
import net.corda.core.node.StatesToRecord;
import net.corda.core.transactions.SignedTransaction;

@InitiatingFlow
public class BroadcastTransaction extends FlowLogic<Void> {

    private final Party provider;
    private SignedTransaction signedTransaction;
    public BroadcastTransaction(SignedTransaction signedTransaction,Party provider)

    {
        this.signedTransaction = signedTransaction;
        this.provider = provider;
    }

    @Override
    @Suspendable
    public Void call() throws FlowException {

        FlowSession custodianSession =  initiateFlow( provider);
        return subFlow(new SendTransactionFlow(custodianSession,signedTransaction));

    }


}

@InitiatedBy(BroadcastTransaction.class)
class RecordTransactionAsObserver extends FlowLogic<SignedTransaction> {

    private FlowSession otherSession;
    public RecordTransactionAsObserver(FlowSession otherSession){

        this.otherSession = otherSession;
    }

    @Override
    @Suspendable
    public SignedTransaction call() throws FlowException {
        ReceiveTransactionFlow receiveTransactionFlow = new ReceiveTransactionFlow(otherSession, true, StatesToRecord.ALL_VISIBLE);

       return subFlow(receiveTransactionFlow);

    }
}
