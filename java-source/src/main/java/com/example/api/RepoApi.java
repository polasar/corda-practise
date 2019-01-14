package com.example.api;

import com.example.Utilities;
import com.example.flow.AssetOnboardingRequest;
import com.example.flow.CashSetUp;
import com.example.flow.CustodianInviationFlow;
import com.example.flow.RepoRequest;
import com.example.schema.AccountSchemaV1;
import com.example.schema.AssetIssuanceSchemaV1;
import com.example.state.Account;
import com.example.state.AssetIssuanceRequest;
import com.example.state.Custodian;
import com.example.state.RepoAllege;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import net.corda.core.contracts.StateAndRef;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.node.services.Vault;
import net.corda.core.node.services.VaultService;
import net.corda.core.node.services.vault.Builder;
import net.corda.core.node.services.vault.CriteriaExpression;
import net.corda.core.node.services.vault.QueryCriteria;
import net.corda.core.transactions.SignedTransaction;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.Response.Status.BAD_REQUEST;
import static javax.ws.rs.core.Response.Status.CREATED;


@Path("bilateralrepo")
public class RepoApi {

    private final CordaRPCOps rpcOps;
    private final CordaX500Name myLegalName;
    private final List<String> serviceNames = ImmutableList.of("Notary");
    static private final Logger logger = LoggerFactory.getLogger(RepoApi.class);

    public RepoApi(CordaRPCOps rpcOps) {
        this.rpcOps = rpcOps;
        this.myLegalName = rpcOps.nodeInfo().getLegalIdentities().get(0).getName();
    }

    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, CordaX500Name> whoami() {
        return ImmutableMap.of("me", myLegalName);
    }

    @GET
    @Path("repo/repoStates")
    @Produces(MediaType.APPLICATION_JSON)
    public List<StateAndRef<RepoAllege>> getRepoStates() {
        return rpcOps.vaultQuery(RepoAllege.class).getStates();
    }

    @GET
    @Path("invitation/custodian-invitation")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getCustodianInvitations() {
        List<StateAndRef<Custodian.Test>> states = rpcOps.vaultQuery(Custodian.Test.class).getStates();
        List<String> stringList = new ArrayList<String>();

        for(int i=0;i<=states.size();i++){
            StateAndRef<Custodian.Test> testStateAndRef = states.get(i);
            Utilities.ParseCustodianInvitationstates parseCustodianInvitationstates = new Utilities.ParseCustodianInvitationstates();
            JSONObject jsonObject = parseCustodianInvitationstates.ParseCustodianInvitationstates(testStateAndRef.toString());
            String s = jsonObject.toString();
            stringList.add(jsonObject.toString());
        }

        return stringList;
    }

    @GET
    @Path("asset-notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public List<String> getAssetNotifications() {


        QueryCriteria criteria1 = new QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED);
        Field notificationStatus = null;
        Field notificationType = null;
        try {
             notificationStatus = AssetIssuanceSchemaV1.PersistentOper.class.getDeclaredField("notificationStatus");
             notificationType = AssetIssuanceSchemaV1.PersistentOper.class.getDeclaredField("notificationType");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        CriteriaExpression notificationStatusCriteria = Builder.equal(notificationStatus, "Pending");
        QueryCriteria notificationQueryCriteria = new QueryCriteria.VaultCustomQueryCriteria(notificationStatusCriteria);
        CriteriaExpression digitalAssetIssuanceCriteria = Builder.equal(notificationType, "DigitalAssetIssuance");
        QueryCriteria digitalAssetQueryCriteria = new QueryCriteria.VaultCustomQueryCriteria(digitalAssetIssuanceCriteria);
        QueryCriteria criteria = criteria1.and(notificationQueryCriteria.and(digitalAssetQueryCriteria));
        List<StateAndRef<AssetIssuanceRequest>> results = rpcOps.vaultQueryByCriteria(criteria,AssetIssuanceRequest.class).getStates();
        List<String> stringList = new ArrayList<String>();

        for(int i=0;i<results.size();i++){
            StateAndRef<AssetIssuanceRequest> testStateAndRef = results.get(i);
            AssetIssuanceRequest data = testStateAndRef.getState().getData();

            Utilities.ParseAssetIssuanceRequest parseAssetIssuanceRequest = new Utilities.ParseAssetIssuanceRequest();
            JSONObject jsonObject = parseAssetIssuanceRequest.ParseAssetIssuanceRequest(data);
            String s = jsonObject.toString();
            stringList.add(jsonObject.toString());
        }

        return stringList;
    }

    @POST
    @Path("asset/assetIssuance")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createAssetIssuanceRequest(String jsonString){

        try {

            logger.warn(jsonString);

            Utilities.DigitalAssetIssuance util = new Utilities.DigitalAssetIssuance(jsonString);
            Party owner =  rpcOps.wellKnownPartyFromX500Name(util.getOwner());
            Party provider = rpcOps.wellKnownPartyFromX500Name(util.getProvider());
            Party operator = rpcOps.wellKnownPartyFromX500Name(util.getOperator());
            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(AssetOnboardingRequest.Initiator.class,owner,provider,operator,util.getQuantity(),
                            util.getInstrumentId(),util.getOwnerAccountId(),util.getOmniBusAccountId(),
                            util.getNotificationStatus(),util.getNotificationType())
                    .getReturnValue()
                    .get();

            final String msg = String.format(String.valueOf(signedTx.getId()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","SUCCESS");
            jsonObject.put("error", "NA");
            jsonObject.put("responseBody", "");
            return Response.status(CREATED).entity(jsonObject).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","SUCCESS");
            jsonObject.put("error", "NA");
            jsonObject.put("responseBody", "");
            logger.error(ex.getMessage(), ex.toString());
            return Response.status(BAD_REQUEST).entity(jsonObject).build();
        }
    }

    @POST
    @Path("create-custodian")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createCustodianInvitation(String jsonString) {

        try {

            logger.warn(jsonString);
            Utilities.CustodianInvitation util = new Utilities.CustodianInvitation(jsonString);
            Party provider = rpcOps.wellKnownPartyFromX500Name(util.getProvider());
            Party agent = rpcOps.wellKnownPartyFromX500Name(util.getAgent());
            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(CustodianInviationFlow.Initiator.class,provider,agent,util.getStatus())
                    .getReturnValue()
                    .get();

            final String msg = String.format(String.valueOf(signedTx.getId()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","SUCCESS");
            jsonObject.put("error", "NA");
            jsonObject.put("responseBody", "");
            return Response.status(CREATED).entity(jsonObject).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","SUCCESS");
            jsonObject.put("error", ex.toString());
            jsonObject.put("responseBody", "");
            return Response.status(BAD_REQUEST).entity(jsonObject).build();
        }
    }


    @POST
    @Path("repo-request")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Response createRepoRequest(String jsonString) {

        try {
            Utilities.RepoRequest repoRequest = new Utilities.RepoRequest(jsonString,rpcOps);
            logger.warn(jsonString);
            Party counterParty = rpcOps.wellKnownPartyFromX500Name(repoRequest.getCounterParty());
            Party agent = rpcOps.wellKnownPartyFromX500Name(repoRequest.getAgent());
            final SignedTransaction signedTx = rpcOps
                    .startTrackedFlowDynamic(RepoRequest.Initiator.class,counterParty,repoRequest.isApplicantIsBuyer(),repoRequest.getRepoId(),
                            repoRequest.getEligibilityCriteriaDataId(),repoRequest.getStartDate(),repoRequest.getEndDate(),
                            repoRequest.getTerminationPaymentLeg(),agent,repoRequest.getStatus(),repoRequest.getAccountId(),repoRequest.getAmount()
                    ,repoRequest.getTotalCashAmount(),repoRequest.getTotalPrincipal(),repoRequest.getTotalNetConsideration(),repoRequest.getPledgeArrayList(),repoRequest.getBorrowerArrayList())
                    .getReturnValue()
                    .get();

            final String msg = String.format(String.valueOf(signedTx.getId()));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","SUCCESS");
            jsonObject.put("error", "NA");
            jsonObject.put("responseBody", "");
            return Response.status(CREATED).entity(jsonObject).build();

        } catch (Throwable ex) {
            final String msg = ex.getMessage();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("message",msg);
            jsonObject.put("status","FAILED");
            jsonObject.put("error", ex);
            jsonObject.put("responseBody", "");
            logger.error(ex.getMessage(), ex);
            return Response.status(BAD_REQUEST).entity(jsonObject).build();
        }
    }

}
