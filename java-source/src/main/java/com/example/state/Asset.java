package com.example.state;


import com.example.schema.CashSchemaV1;
import com.google.common.collect.ImmutableList;
import kotlin.Pair;
import kotlin.Unit;
import kotlin.jvm.functions.Function0;
import net.corda.core.contracts.*;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.PartyAndCertificate;
import net.corda.core.node.ServiceHub;
import net.corda.core.schemas.MappedSchema;
import net.corda.core.schemas.PersistentState;
import net.corda.core.schemas.QueryableState;
import net.corda.core.transactions.LedgerTransaction;
import net.corda.core.transactions.TransactionBuilder;
import net.corda.finance.contracts.asset.PartyAndAmount;
import net.corda.finance.contracts.asset.cash.selection.AbstractCashSelection;
import net.corda.finance.utils.StateSumming;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.security.PublicKey;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.time.Instant;
import java.util.*;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;
import static net.corda.finance.Currencies.DOLLARS;

public class Asset implements Contract {

    public static final String PROGRAM_ID = "com.example.state.Asset";




    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {



        // Group by everything except owner: any modification to the CP at all is considered changing it fundamentally.
        final List<LedgerTransaction.InOutGroup<Asset.Cash, Asset.Cash>> groups = tx.groupStates(Asset.Cash.class, Asset.Cash::withoutOwner );

        // There are two possible things that can be done with this CP. The first is trading it. The second is redeeming
        // it for cash on or after the maturity date.
        final List<CommandWithParties<CommandData>> commands = tx.getCommands();
        final CommandWithParties<CommandData> command = onlyElementOf(commands);
        final TimeWindow timeWindow = tx.getTimeWindow();


            final List<StateAndRef<ContractState>> inputs = tx.getInputs();
            final List<TransactionState<ContractState>> outputs = tx.getOutputs();
            if (command.getValue() instanceof Commands.Move) {
                final CommandWithParties<Commands.Move> cmd = requireSingleCommand(tx.getCommands(), Commands.Move.class);
                // There should be only a single input due to aggregation above
                final Asset.Cash input = (Cash) inputs.get(0).getState().getData();

                if (!cmd.getSigners().contains(input.getProvider().getOwningKey()))
                    throw new IllegalStateException("Failed requirement: the transaction is signed by the owner of the CP");

                // Check the output CP state is the same as the input state, ignoring the owner field.
//                if (outputs.size() != 1) {
//                    throw new IllegalStateException("the state is propagated");
//                }
            } else if (command.getValue() instanceof Commands.Exit) {
                final CommandWithParties<Commands.Exit> cmd = requireSingleCommand(tx.getCommands(), Commands.Exit.class);

                // There should be only a single input due to aggregation above
                final Asset.Cash input = (Cash) inputs.get(0).getState().getData();

                if (!cmd.getSigners().contains(input.getOwner().getOwningKey()))
                    throw new IllegalStateException("Failed requirement: the transaction is signed by the owner of the CP");

                final Instant time = timeWindow == null
                        ? null
                        : timeWindow.getUntilTime();
                final Amount<Issued<Currency>> received = StateSumming.sumCashBy(tx.getOutputStates(), input.getOwner());

                requireThat(require -> {
                    /*require.using("must be timestamped", timeWindow != null);
                    require.using("received amount equals the face value: "
                            + received + " vs " + input.getFaceValue(), received.equals(input.getFaceValue()));
                    require.using("the paper must have matured", time != null && !time.isBefore(input.getMaturityDate()));
                    require.using("the received amount equals the face value", input.getFaceValue().equals(received));
                    require.using("the paper must be destroyed", outputs.isEmpty());*/
                    return Unit.INSTANCE;
                });
            } else if (command.getValue() instanceof Asset.Commands.Issue) {
                final CommandWithParties<Asset.Commands.Issue> cmd = requireSingleCommand(tx.getCommands(), Asset.Commands.Issue.class);
                final Asset.Cash output = (Cash) outputs.get(0).getData();
                // There should be only a single input due to aggregation above
//                final Asset.Cash input = (Cash) inputs.get(0).getState().getData();

                if (!cmd.getSigners().contains(output.getOwner().getOwningKey()))
                    throw new IllegalStateException("Failed requirement: the transaction is signed by the owner of the CP");
                if (!cmd.getSigners().contains(output.getObserver().getOwningKey()))
                    throw new IllegalStateException("Observer signature is required");

                requireThat(require -> {
                    /*require.using("output values sum to more than the inputs", inputs.isEmpty());
                    require.using("output values sum to more than the inputs", output.faceValue.getQuantity() > 0);
                    require.using("must be timestamped", timeWindow != null);
                    require.using("the maturity date is not in the past", time != null && time.isBefore(output.getMaturityDate()));
                    require.using("output states are issued by a command signer", cmd.getSigners().contains(output.issuance.getParty().getOwningKey()));*/
                    return Unit.INSTANCE;
                });
            }



    }

    private static <T> T onlyElementOf(Iterable<T> iterable) {
        Iterator<T> iter = iterable.iterator();
        T item = iter.next();
        if (iter.hasNext()) {
            throw new IllegalArgumentException("Iterable has more than one element!");
        }
        return item;
    }


    public interface Commands extends CommandData{
        class Move implements MoveCommand {


            @Nullable
            @Override
            public Class<Asset> getContract() {
                return Asset.class;
            }
        }

        class Issue extends TypeOnlyCommandData{

        }

        class Exit implements CommandData{
            public Exit(Amount<Issued<Currency>> amount) {
            }

        }
    }


    public static class Cash implements LinearState, QueryableState {

        private AbstractParty provider;
        private AbstractParty owner;
        private AbstractParty observer;
        private Amount<Issued<Currency>> amount;
        private String instrumentId;
        private String accountId;
        private String status;
//        public PartyAndReference deposit;

        public Cash(AbstractParty provider, AbstractParty owner, AbstractParty observer, Amount<Issued<Currency>> amount,
                     String instrumentId, String accountId,String status) {
            this.provider = provider;
            this.owner = owner;
            this.observer = observer;
            this.amount = amount;
            this.instrumentId = instrumentId;
            this.accountId = accountId;
            this.status = status;
//            this.deposit = deposit;
        }

        public Cash(){

        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public AbstractParty getProvider() {
            return provider;
        }

        public AbstractParty getObserver() {
            return observer;
        }

//        public PartyAndReference getDeposit() {
//            return deposit;
//        }

        public String getAccountId() {
            return accountId;
        }

        public void setOwner(AbstractParty owner){
            this.owner= owner;
        }

        public void setAmount(Amount<Issued<Currency>> amount){
            this.amount= amount;
        }

        @NotNull
        public Amount<Issued<Currency>> getAmount() {
            return this.amount;
        }

        /*@NotNull
        @Override
        public Collection<PublicKey> getExitKeys() {
            Set<PublicKey> singleton = Collections.singleton(owner.getOwningKey());
            singleton.add(amount.getToken().getIssuer().getParty().getOwningKey());
            return singleton;
        }*/

        /*@NotNull
        @Override
        public FungibleAsset<Currency> withNewOwnerAndAmount(Amount<Issued<Currency>> newAmount, AbstractParty newOwner) {
            return new Cash(this.provider, newOwner, this.observer, newAmount, this.instrumentId, this.accountId*//*,this.deposit*//*,this.status);
        }*/

        @NotNull
        public AbstractParty getOwner() {
            return this.owner;
        }

        /*@NotNull
        @Override
        public CommandAndState withNewOwner(AbstractParty newOwner) {
            return new CommandAndState(new Commands.Move(), new Cash(this.provider, newOwner, this.observer, this.amount, this.instrumentId, this.accountId*//*,this.deposit*//*,this.status));
        }*/

        @NotNull
        @Override
        public Iterable<MappedSchema> supportedSchemas() {
            return ImmutableList.of(new CashSchemaV1());
        }

        @NotNull
        @Override
        public PersistentState generateMappedObject(MappedSchema schema) {
            if (schema instanceof CashSchemaV1) {
                return new CashSchemaV1.PersistentOper(this.owner,this.amount.getQuantity(),
                        this.amount.getToken().getProduct().getCurrencyCode(),
                        this.amount.getToken().getIssuer().getParty().getOwningKey().toString(),
                        this.amount.getToken().getIssuer().getReference().getBytes(),
                        provider, this.instrumentId);
            } else {
                throw new IllegalArgumentException("unrecognised schema");
            }
        }


        Asset.Cash withoutOwner() {
            return new Cash(this.provider, this.owner, this.observer, this.amount, this.instrumentId, this.accountId/*,this.deposit*/,this.status);
        }

        @NotNull
        @Override
        public List<AbstractParty> getParticipants() {
            return Arrays.asList(this.owner,this.observer,this.provider);
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(String status){
            this.status = status;
        }

        @NotNull
        @Override
        public UniqueIdentifier getLinearId() {
            UniqueIdentifier uuid = new UniqueIdentifier();
            return uuid;
        }
    }


    public Pair<TransactionBuilder, List<PublicKey>> generateSpend(ServiceHub serviceHub,
                  TransactionBuilder tx,
                  Amount<Currency> amount, PartyAndCertificate ourIdentity,
                  AbstractParty to, Set<AbstractParty> onlyFromParties) throws SQLException {
        return generateSpend(serviceHub,tx,Collections.singletonList(new PartyAndAmount(to,amount)),ourIdentity,onlyFromParties);

    }

    private Pair<TransactionBuilder,List<PublicKey>> generateSpend(ServiceHub services, TransactionBuilder tx,
                                                                   List<PartyAndAmount<Currency>> payments,
                                                                   PartyAndCertificate ourIdentity,
                                                                       Set<AbstractParty> onlyFromParties) throws SQLException {
        Amount<Currency> totalAmount = null;
        for(int i=0;i<=payments.size();i++){
            totalAmount = payments.get(i).getAmount();
        }
        Amount amount = DOLLARS(100);
        AbstractCashSelection cashSelection = AbstractCashSelection.Companion.getInstance((Function0<? extends DatabaseMetaData>) services.jdbcSession().getMetaData());
        cashSelection.unconsumedCashStatesForSpending(services, totalAmount, onlyFromParties, tx.getNotary(), tx.getLockId(), Collections.EMPTY_SET);
        List<StateAndRef<Asset.Cash>> acceptableCoins = cashSelection.unconsumedCashStatesForSpending(services, totalAmount, onlyFromParties,
                tx.getNotary(), tx.getLockId(), Collections.EMPTY_SET);
        StateAndRef<Asset.Cash> cashStateAndRef = acceptableCoins.get(0);
        boolean revocationEnabled = false;
        TransactionState<Cash> cashTransactionState = null;

        PartyAndCertificate changeIdentity = services.getKeyManagementService().freshKeyAndCert(ourIdentity, revocationEnabled);
//        return OnLedgerAsset.generateSpend(tx,payments,acceptableCoins,changeIdentity.getParty().anonymise(),txState,this::generateMoveCommand);
        return generateSpend(services,tx,payments,ourIdentity,onlyFromParties);

    }



}
