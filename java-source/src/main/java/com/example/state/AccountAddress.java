package com.example.state;

import net.corda.core.contracts.LinearState;
import net.corda.core.contracts.UniqueIdentifier;
import net.corda.core.identity.AbstractParty;
import net.corda.core.identity.CordaX500Name;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

public class AccountAddress {
     private String accountId;
     private CordaX500Name party;


    public AccountAddress(String accountId, CordaX500Name party) {
        this.accountId = accountId;
        this.party = party;
    }

    public AccountAddress parse(String address){

        List<String> split = Arrays.asList(address.split("@"));

        if (split.size() != 2) {
            throw new RuntimeException("address is malformed: $address");
        }
        return new AccountAddress(split.get(0),CordaX500Name.parse(split.get(1)));
    }

    public String getUri(){
        return (accountId+""+party);
    }

    public AccountAddress create(String accountId,CordaX500Name party){
        return new AccountAddress(accountId,party);
    }

    public String getAccountId(){
        return  this.accountId;
    }

    public  CordaX500Name getParty(){
        return this.party;
    }
}
