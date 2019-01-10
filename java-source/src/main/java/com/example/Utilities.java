package com.example;


import com.example.state.CollateralData;
import net.corda.core.identity.CordaX500Name;
import net.corda.core.identity.Party;
import net.corda.core.messaging.CordaRPCOps;
import net.corda.core.serialization.CordaSerializable;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.*;

@CordaSerializable
public class Utilities implements Serializable{

    @CordaSerializable
    public static class RepoRequest implements  Serializable{

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

        private List<CollateralData.Pledge> pledgeArrayList ;
        private List<CollateralData.Borrower> borrowerArrayList;


        public RepoRequest(String jsonString, CordaRPCOps rpcOps) {

            JSONObject obj = new JSONObject(jsonString);
            JSONObject repoRequest = (JSONObject) obj.get("RepoRequest");
            PartySubClass partySubClass = new PartySubClass((JSONObject) repoRequest.get("counterParty"));
            setCounterParty(partySubClass.getCordaX500Name());
            setApplicantIsBuyer((boolean) repoRequest.get("applicantIsBuyer"));
            setRepoId((String) repoRequest.get("repoId"));
            setEligibilityCriteriaDataId ((String) repoRequest.get("eligibilityCriteriaDataId"));
            setStartDate((Date) repoRequest.get("startDate"));
            setEndDate((Date)repoRequest.get("endDate"));
            setTerminationPaymentLeg((String)repoRequest.get("terminationPaymentLeg"));
            PartySubClass agentClass = new PartySubClass((JSONObject) repoRequest.get("agent"));
            setAgent(agentClass.getCordaX500Name());
            setStatus((String) repoRequest.get("status"));
            setAccountId((String) repoRequest.get("accountId"));
            setAmount(Long.valueOf((Integer) (repoRequest.get("amount"))));
            JSONObject collateralData  = repoRequest.getJSONObject("Collateral");
            setTotalCashAmount(Long.valueOf((Integer) (collateralData.get("total cash amount"))));
            setTotalPrincipal(Long.valueOf((Integer) (collateralData.get("total principal"))));
            setTotalNetConsideration(Long.valueOf((Integer) (collateralData.get("total net consideration"))));
            JSONArray jsonArray = collateralData.getJSONArray("Collateral Details");


            for(int i=0; i<jsonArray.length(); i++){
                CollateralData.Pledge pledge;
                CollateralData.Borrower borrower;
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                JSONObject pledgeDetails = (JSONObject) jsonObject.get("Pledge Details");
                setToken((String)pledgeDetails.get("token"));
                setTokenDescription((String)pledgeDetails.get("token description"));
                setAssetType((String)pledgeDetails.get("assetType"));
                setCleanPrice((int)pledgeDetails.get("cleanPrice"));
                setQuantity(Long.valueOf((Integer)pledgeDetails.get("quantity")));
                setCollateralPrincipal(Long.valueOf((Integer)pledgeDetails.get("principal")));
                setDirtyPrice((Integer)pledgeDetails.get("dirtyPrice"));
                setHairCut((Integer)pledgeDetails.get("hairCut"));
                setNetConsideration(Long.valueOf((Integer)pledgeDetails.get("net consideration")));
                setCurrentQuantity(Long.valueOf((Integer)pledgeDetails.get("current quantity")));
                setCurrentValue(Long.valueOf((Integer)pledgeDetails.get("current value")));
                pledge = new CollateralData.Pledge(getToken(),getTokenDescription(),getAssetType(),getCleanPrice(),getQuantity(),getDirtyPrice(),
                        getHairCut(),getNetConsideration(),getCurrentQuantity(),getCurrentValue());

                pledgeArrayList.add(pledge);

                JSONObject deliveryDetails = (JSONObject) jsonObject.get("Delivery Details");
                setToken((String)deliveryDetails.get("token"));
                setTokenDescription((String)deliveryDetails.get("token description"));
                setAssetType((String)deliveryDetails.get("assetType"));
                setCleanPrice((int)deliveryDetails.get("cleanPrice"));
                setQuantity(Long.valueOf((Integer)deliveryDetails.get("quantity")));
                setCollateralPrincipal(Long.valueOf((Integer)deliveryDetails.get("principal")));
                setDirtyPrice((Integer)deliveryDetails.get("dirtyPrice"));
                setHairCut((Integer)deliveryDetails.get("hairCut"));
                setNetConsideration(Long.valueOf((Integer)deliveryDetails.get("net consideration")));
                setCurrentQuantity(Long.valueOf((Integer)deliveryDetails.get("current quantity")));
                setCurrentValue(Long.valueOf((Integer)deliveryDetails.get("current value")));
                borrower = new CollateralData.Borrower(getToken(),getTokenDescription(),getAssetType(),getCleanPrice(),getQuantity(),getDirtyPrice(),
                        getHairCut(),getNetConsideration(),getCurrentQuantity(),getCurrentValue());
                borrowerArrayList.add(borrower);
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

        public List<CollateralData.Pledge> getPledgeArrayList() {
            return pledgeArrayList;
        }

        public List<CollateralData.Borrower> getBorrowerArrayList() {
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

    @CordaSerializable
    public static class PartySubClass implements Serializable {
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

}
