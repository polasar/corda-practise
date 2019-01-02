package com.example.Utils;



import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.serialization.CordaSerializable;


import javax.json.stream.JsonParser;
import java.io.Serializable;

@CordaSerializable
public class Generic implements Serializable {

    private String jSon;
    private CordaRPCOps rpcOps;


    public Generic(String json, CordaRPCOps rpcOps){
        this.jSon = json;
        this.rpcOps = rpcOps ;
    }



}
