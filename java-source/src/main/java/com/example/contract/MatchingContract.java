package com.example.contract;

import net.corda.core.contracts.CommandData;
import net.corda.core.contracts.CommandWithParties;
import net.corda.core.contracts.Contract;
import net.corda.core.transactions.LedgerTransaction;

import static net.corda.core.contracts.ContractsDSL.requireSingleCommand;
import static net.corda.core.contracts.ContractsDSL.requireThat;

public class MatchingContract implements Contract {

    public static final String REPO_MATCHING_CONTRACT = "com.example.contract.MatchingContract";

    @Override
    public void verify(LedgerTransaction tx) throws IllegalArgumentException {
        final CommandWithParties<MatchingContract.Commands.create> command = requireSingleCommand(tx.getCommands(), MatchingContract.Commands.create.class);
        requireThat(require -> {

            return null;
        });
    }

    public interface Commands extends CommandData{

        class create implements Commands{

        }

    }
}
