package com.example;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import net.corda.core.concurrent.CordaFuture;
import net.corda.core.identity.CordaX500Name;
import net.corda.testing.driver.DriverParameters;
import net.corda.testing.driver.NodeHandle;
import net.corda.testing.driver.NodeParameters;
import net.corda.testing.node.User;

import static net.corda.testing.driver.Driver.driver;

/**
 * This file is exclusively for being able to run your nodes through an IDE (as opposed to using deployNodes)
 * Do not use in a production environment.
 *
 * To debug your CorDapp:
 * 
 * 1. Firstly, run the "Run Example CorDapp - Java" run configuration.
 * 2. Wait for all the nodes to start.
 * 3. Note the debug ports which should be output to the console for each node. They typically start at 5006, 5007,
 * 5008. The "Debug CorDapp" configuration runs with port 5007, which should be "NodeB". In any case, double check
 * the console output to be sure.
 * 4. Set your breakpoints in your CorDapp code.
 * 5. Run the "Debug CorDapp" remote debug run configuration.
 */
public class NodeDriver {
    public static void main(String[] args) {
        final User user = new User("user1", "test", ImmutableSet.of("ALL"));
        driver(new DriverParameters().withIsDebug(true).withWaitForAllNodesToFinish(true), dsl -> {
                    CordaFuture<NodeHandle> socGenFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("SocGen", "London", "GB"))
                            .withRpcUsers(ImmutableList.of(user)));
                    CordaFuture<NodeHandle> natixisFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("Natixis", "New York", "US"))
                            .withRpcUsers(ImmutableList.of(user)));
                    CordaFuture<NodeHandle> custodianFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("Custodian", "Paris", "FR"))
                            .withRpcUsers(ImmutableList.of(user)));
                    CordaFuture<NodeHandle> brFuture = dsl.startNode(new NodeParameters()
                            .withProvidedName(new CordaX500Name("BR", "Paris", "FR"))
                            .withRpcUsers(ImmutableList.of(user)));

                    try {
                        dsl.startWebserver(socGenFuture.get());
                        dsl.startWebserver(natixisFuture.get());
                        dsl.startWebserver(custodianFuture.get());
                        dsl.startWebserver(brFuture.get());
                    } catch (Throwable e) {
                        System.err.println("Encountered exception in node startup: " + e.getMessage());
                        e.printStackTrace();
                    }

                    return null;
                }
        );
    }
}
