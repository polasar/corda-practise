package com.example;


import com.example.state.AssetIssuanceRequest;
import com.example.state.Collateral;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.serialization.CordaSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

@CordaSerializable
public class Utilities implements Serializable{


    public static class RepoRequest {

        private CordaX500Name counterParty;
        private boolean applicantIsBuyer;
        private String repoId;
        private String eligibilityCriteriaDataId;
        private Date startDate;
        private Date endDate;
        private String terminationPaymentLeg;
        private CordaX500Name agent;
        private String status;
        private String accountId;
        private Long amount;
        private CordaX500Name provider;
        private Long totalCashAmount;
        private Long totalPrincipal;
        private Long totalNetConsideration;
        private String token;
        private String tokenDescription;
        private String assetType;
        private int cleanPrice;
        private Long quantity;
        private Long collateralPrincipal;
        private int dirtyPrice;
        private int hairCut;
        private Long netConsideration;
        private Long currentQuantity;
        private Long currentValue;



        List<HashMap<String, Object>> pledgeArrayList = new ArrayList<>();
        List<HashMap<String, Object>> borrowerArrayList = new ArrayList<>();
        public RepoRequest(String jsonString, CordaRPCOps rpcOps) {

            JSONObject obj = new JSONObject(jsonString);
            JSONObject repoRequest = (JSONObject) obj.get("RepoRequest");
            setRepoId((String) repoRequest.get("repoId"));
            setAmount(Long.valueOf((String) repoRequest.get("quantity")));
            JSONObject collateralData  = repoRequest.getJSONObject("Collateral");
            JSONArray jsonArray = collateralData.getJSONArray("CollateralDetails");


            for(int i=0; i<jsonArray.length(); i++){
                Collateral collateral;
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject pledgeDetails = (JSONObject) jsonObject.get("PledgeDetails");
                HashMap<String,Object> pledgeData= new LinkedHashMap<>();
                pledgeData.put("amount",Long.valueOf((String) pledgeDetails.get("quantity")));
                pledgeData.put("instrumentId",(String)pledgeDetails.get("token"));
                pledgeArrayList.add( pledgeData);

                JSONObject deliveryDetails = (JSONObject) jsonObject.get("DeliveryDetails");
                HashMap<String,Object> borrowerData= new LinkedHashMap<>();
                borrowerData.put("amount",Long.valueOf((String) deliveryDetails.get("quantity")));
                borrowerData.put("instrumentId",(String)pledgeDetails.get("token"));
                borrowerArrayList.add(borrowerData);
            }

        }

        public CordaX500Name getCounterParty() {
            return counterParty;
        }

        public void setCounterParty(CordaX500Name counterParty) {
            this.counterParty = counterParty;
        }

        public CordaX500Name getProvider(){
            return provider;
        }

        public void setProvider(CordaX500Name provider){
            this.provider = provider;
        }

        public boolean isApplicantIsBuyer() {
            return applicantIsBuyer;
        }

        public void setApplicantIsBuyer(boolean applicantIsBuyer) {
            this.applicantIsBuyer = applicantIsBuyer;
        }

        public String getRepoId() {
            return repoId;
        }

        public void setRepoId(String repoId) {
            this.repoId = repoId;
        }



        public String getEligibilityCriteriaDataId() {
            return eligibilityCriteriaDataId;
        }

        public void setEligibilityCriteriaDataId(String eligibilityCriteriaDataId) {
            this.eligibilityCriteriaDataId = eligibilityCriteriaDataId;
        }

        public Date getStartDate() {
            return startDate;
        }

        public void setStartDate(Date startDate) {
            this.startDate = startDate;
        }

        public Date getEndDate() {
            return endDate;
        }

        public void setEndDate(Date endDate) {
            this.endDate = endDate;
        }

        public String getTerminationPaymentLeg() {
            return terminationPaymentLeg;
        }

        public void setTerminationPaymentLeg(String terminationPaymentLeg) {
            this.terminationPaymentLeg = terminationPaymentLeg;
        }

        public CordaX500Name getAgent() {
            return agent;
        }

        public void setAgent(CordaX500Name agent) {
            this.agent = agent;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }


        public String getAccountId() {
            return accountId;
        }

        public void setAccountId(String accountId) {
            this.accountId = accountId;
        }

        public Long getAmount() {
            return amount;
        }

        public void setAmount(Long amount) {
            this.amount = amount;
        }

        public Long getTotalCashAmount() {
            return totalCashAmount;
        }

        public void setTotalCashAmount(Long totalCashAmount) {
            this.totalCashAmount = totalCashAmount;
        }

        public Long getTotalPrincipal() {
            return totalPrincipal;
        }

        public void setTotalPrincipal(Long totalPrincipal) {
            this.totalPrincipal = totalPrincipal;
        }

        public Long getTotalNetConsideration() {
            return totalNetConsideration;
        }

        public void setTotalNetConsideration(Long totalNetConsideration) {
            this.totalNetConsideration = totalNetConsideration;
        }

        public List<HashMap<String, Object>> getPledgeArrayList() {
            return pledgeArrayList;
        }

        public List<HashMap<String, Object>> getBorrowerArrayList() {
            return borrowerArrayList;
        }

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getTokenDescription() {
            return tokenDescription;
        }

        public void setTokenDescription(String tokenDescription) {
            this.tokenDescription = tokenDescription;
        }

        public String getAssetType() {
            return assetType;
        }

        public void setAssetType(String assetType) {
            this.assetType = assetType;
        }

        public int getCleanPrice() {
            return cleanPrice;
        }

        public void setCleanPrice(int cleanPrice) {
            this.cleanPrice = cleanPrice;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public Long getCollateralPrincipal() {
            return collateralPrincipal;
        }

        public void setCollateralPrincipal(Long collateralPrincipal) {
            this.collateralPrincipal = collateralPrincipal;
        }

        public int getDirtyPrice() {
            return dirtyPrice;
        }

        public void setDirtyPrice(int dirtyPrice) {
            this.dirtyPrice = dirtyPrice;
        }

        public int getHairCut() {
            return hairCut;
        }

        public void setHairCut(int hairCut) {
            this.hairCut = hairCut;
        }

        public Long getNetConsideration() {
            return netConsideration;
        }

        public void setNetConsideration(Long netConsideration) {
            this.netConsideration = netConsideration;
        }

        public Long getCurrentQuantity() {
            return currentQuantity;
        }

        public void setCurrentQuantity(Long currentQuantity) {
            this.currentQuantity = currentQuantity;
        }

        public Long getCurrentValue() {
            return currentValue;
        }

        public void setCurrentValue(Long currentValue) {
            this.currentValue = currentValue;
        }
    }

    public static class CustodianInvitation {

        private CordaX500Name provider;
        private CordaX500Name agent;
        private String status;


        public CustodianInvitation(String jsonStringParser) {

            JSONObject obj = new JSONObject(jsonStringParser);
            JSONObject custodianInvitationJSON = obj.getJSONObject("CustodianInvitation");
            PartySubClass providerParty = new PartySubClass((custodianInvitationJSON.getJSONObject("provider")));
            provider = providerParty.getCordaX500Name();
            PartySubClass agentParty = new PartySubClass(custodianInvitationJSON.getJSONObject("agent"));
            agent = agentParty.getCordaX500Name();
            status =  custodianInvitationJSON.getString("status");
        }


        public CordaX500Name getProvider() {
            return provider;
        }

        public void setProvider(CordaX500Name provider) {
            this.provider = provider;
        }

        public CordaX500Name getAgent() {
            return agent;
        }

        public void setAgent(CordaX500Name agent) {
            this.agent = agent;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }


    public static class DigitalAssetIssuance {

        private CordaX500Name owner;
        private CordaX500Name provider;
        private CordaX500Name operator;
        private Long quantity;
        private String instrumentId;
        private String ownerAccountId;
        private String omniBusAccountId;
        private String notificationType;
        private String notificationStatus;
        private String status;


        public DigitalAssetIssuance(String jsonStringParser) {

            JSONObject obj = new JSONObject(jsonStringParser);
            JSONObject digitalAssetIssuance = obj.getJSONObject("DigitalAssetIssuance");
            PartySubClass ownerParty = new PartySubClass((digitalAssetIssuance.getJSONObject("owner")));
            setOwner(ownerParty.getCordaX500Name());
            PartySubClass providerParty = new PartySubClass(digitalAssetIssuance.getJSONObject("provider"));
            setProvider(providerParty.getCordaX500Name());
            PartySubClass operatorParty = new PartySubClass(digitalAssetIssuance.getJSONObject("operator"));
            setOperator(operatorParty.getCordaX500Name());
            setQuantity(Long.valueOf((Integer)digitalAssetIssuance.get("quantity")));
            setInstrumentId((String) digitalAssetIssuance.get("instrumentId"));
            setOwnerAccountId(digitalAssetIssuance.getString("ownerAccountId"));
            setOmniBusAccountId(digitalAssetIssuance.getString("omniBusAccountId"));
            setNotificationType(digitalAssetIssuance.getString("notificationType"));
            setNotificationStatus(digitalAssetIssuance.getString("notificationStatus"));
        }

        public CordaX500Name getOwner() {
            return owner;
        }

        public void setOwner(CordaX500Name owner) {
            this.owner = owner;
        }

        public CordaX500Name getProvider() {
            return provider;
        }

        public void setProvider(CordaX500Name provider) {
            this.provider = provider;
        }

        public CordaX500Name getOperator() {
            return operator;
        }

        public void setOperator(CordaX500Name operator) {
            this.operator = operator;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        public Long getQuantity() {
            return quantity;
        }

        public void setQuantity(Long quantity) {
            this.quantity = quantity;
        }

        public String getInstrumentId() {
            return instrumentId;
        }

        public void setInstrumentId(String instrumentId) {
            this.instrumentId = instrumentId;
        }

        public String getOwnerAccountId() {
            return ownerAccountId;
        }

        public void setOwnerAccountId(String ownerAccountId) {
            this.ownerAccountId = ownerAccountId;
        }

        public String getOmniBusAccountId() {
            return omniBusAccountId;
        }

        public void setOmniBusAccountId(String omniBusAccountId) {
            this.omniBusAccountId = omniBusAccountId;
        }

        public String getNotificationType() {
            return notificationType;
        }

        public void setNotificationType(String notificationType) {
            this.notificationType = notificationType;
        }

        public String getNotificationStatus() {
            return notificationStatus;
        }

        public void setNotificationStatus(String notificationStatus) {
            this.notificationStatus = notificationStatus;
        }
    }

    public static class PartySubClass {
        CordaX500Name cordaX500Name;
        private String name;
        private String organisation;
        private String locality;

        public PartySubClass(JSONObject jsonString) {

//            JSONObject obj = new JSONObject(jSonString);
            name =  jsonString.getString("name");
            organisation =  jsonString.getString("organisation");
            locality = jsonString.getString("locality");
            cordaX500Name = new CordaX500Name(name, organisation, locality);

        }

        public CordaX500Name getCordaX500Name() {
            return cordaX500Name;
        }

    }

    public static class ParseCustodianInvitationstates{

        private CordaX500Name provider;
        private CordaX500Name agent;
        private String status;

        public JSONObject ParseCustodianInvitationstates(String jsonString){
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONObject getFormattedJSONString = new JSONObject();
             provider = CordaX500Name.parse(jsonObject.getString("provider"));
             agent = CordaX500Name.parse(jsonObject.getString("agent"));
             status = jsonObject.getString("status");
             getFormattedJSONString.put("provider",provider);
             getFormattedJSONString.put("agent", agent);
             getFormattedJSONString.put("status", status);
             return getFormattedJSONString;
        }
    }

    public static class ParseAssetIssuanceRequest{

        private String providerName;
        private String operatorName;
        private String ownerName;
        private String quantity;
        private String instrumentId;
        private String ownerAccountId;
        private String notificationType;
        private String omniBusAccountId;
        private String notificationStatus;

        public JSONObject ParseAssetIssuanceRequest(AssetIssuanceRequest assetOnboardingRequest){
            providerName = assetOnboardingRequest.getProvider().getName().getOrganisation();
            operatorName = assetOnboardingRequest.getOperator().getName().getOrganisation();
            ownerName = assetOnboardingRequest.getOwner().getName().getOrganisation();
            quantity = String.valueOf(assetOnboardingRequest.getQuantity());
            instrumentId = assetOnboardingRequest.getInstrumentId();
            ownerAccountId = assetOnboardingRequest.getAccountId();
            omniBusAccountId = assetOnboardingRequest.getOmniBusAccountId();
            notificationStatus = assetOnboardingRequest.getNotificationStatus();
            notificationType = assetOnboardingRequest.getNotificationType();

            JSONObject getFormattedJSONString = new JSONObject();
            getFormattedJSONString.put("provider", providerName);
            getFormattedJSONString.put("operator", operatorName);
            getFormattedJSONString.put("owner", ownerName);
            getFormattedJSONString.put("quantity", quantity);
            getFormattedJSONString.put("instrumentId", instrumentId);
            getFormattedJSONString.put("ownerAccountId", ownerAccountId);
            getFormattedJSONString.put("omniBusAccountId", omniBusAccountId);
            getFormattedJSONString.put("notificationType", notificationType);
            getFormattedJSONString.put("notificationStatus", notificationStatus);
            return getFormattedJSONString;
        }
    }

}
