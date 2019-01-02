package com.example.contract;


import net.corda.core.contracts.Command;
import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.Contract;
import net.corda.core.contracts.ContractState;
import net.corda.core.transactions.LedgerTransaction;

import java.security.PublicKey;
import java.util.List;

import static net.corda.core.contracts.ContractsDSL.requireThat;

public class RepoContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String REPO_CONTRACT_ID = "com.example.contract.RepoContract";

    /**
     * A transaction is considered valid if the verify() function of the contract of each of the transaction's input
     * and output states does not throw an exception.
     */
    @Override
    public void verify(LedgerTransaction tx) {


        //Verification to be added
        if(tx.getCommands().size()!=1){
            throw new IllegalArgumentException("Transaction must have only one command");
        }
        Command command = tx.getCommand(0);
        List<PublicKey> requiredSigners = command.getSigners();
        CommandData commandType =command.getValue();

        if(commandType instanceof  Commands.Issue){

            if(tx.getInputs().size()!=0){
                throw new IllegalArgumentException("Repo Capture transaction must have no inputs");
            }
            if(tx.getOutputs().size()!=1){
                throw new IllegalArgumentException("Repo capture transaction must have one Output");
            }

        } else if(commandType instanceof  Commands.Approve){

            requireThat(require ->{
                require.using("Inputs should not be empty" ,!tx.getInputs().isEmpty());

                return null;
            });
        }  else if (commandType instanceof Commands.Evaluate) {
            requireThat(require -> {
                require.using("Inputs should not be empty", !tx.getInputs().isEmpty());
                require.using("Output should be one", tx.getOutputs().size() == 1);

                return null;
            });
        }

        else if (commandType instanceof Commands.Settlement) {
            requireThat(require -> {
                require.using("Inputs should not be empty", !tx.getInputs().isEmpty());
                //require.using("Output should be one", tx.getOutputs().size() == 1);

                return null;
            });
        }


    }


    public interface Commands extends CommandData {


        class Issue implements Commands {}

        class Approve implements Commands { }

        class Evaluate implements Commands { }

        class Settlement implements Commands { }

    }
}
