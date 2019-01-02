package com.example;


import net.corda.core.identity.CordaX500Name;
import org.json.JSONObject;

public class Utilities {

    public static class RepoRequest {

        private CordaX500Name counterParty;
        private boolean applicantIsBuyer;
        private String repoId;
        private String eligibilityCriteriaDataId;
        private String startDate;
        private String endDate;
        private String terminationPaymentLeg;
        private CordaX500Name agent;
        private String cashInstrumentId;
        private Long cashPrice;
        private String ustInstrumentId;
        private Long ustPrice;
        private String status;

        public RepoRequest(String jsonStringParser) {

            JSONObject obj = new JSONObject(jsonStringParser);
            JSONObject repoRequest = (JSONObject) obj.get("RepoRequest");
            PartySubClass partySubClass = new PartySubClass((JSONObject) repoRequest.get("counterParty"));
            counterParty = partySubClass.getCordaX500Name();
            applicantIsBuyer = (boolean) obj.get("applicantIsBuyer");
            repoId = (String) obj.get("repoId");
            eligibilityCriteriaDataId = (String) obj.get("eligibilityCriteriaDataId");
            startDate = (String) obj.get("startDate");
            endDate = (String) obj.get("endDate");
            terminationPaymentLeg = (String) obj.get("terminationPaymentLeg");
            PartySubClass agentClass = new PartySubClass((JSONObject) repoRequest.get("counterParty"));
            agent = agentClass.getCordaX500Name();
            cashInstrumentId = (String) obj.get("cashInstrumentId");
            cashPrice = (Long) obj.get("cashPrice");
            ustInstrumentId = (String) obj.get("ustInstrumentId");
            ustPrice = (Long) obj.get("ustPrice");
            status = (String) obj.get("status");

        }

        public CordaX500Name getCounterParty() {
            return counterParty;
        }

        public void setCounterParty(CordaX500Name counterParty) {
            this.counterParty = counterParty;
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

        public String getStartDate() {
            return startDate;
        }

        public void setStartDate(String startDate) {
            this.startDate = startDate;
        }

        public String getEndDate() {
            return endDate;
        }

        public void setEndDate(String endDate) {
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

        public String getCashInstrumentId() {
            return cashInstrumentId;
        }

        public void setCashInstrumentId(String cashInstrumentId) {
            this.cashInstrumentId = cashInstrumentId;
        }

        public Long getCashPrice() {
            return cashPrice;
        }

        public void setCashPrice(Long cashPrice) {
            this.cashPrice = cashPrice;
        }

        public String getUstInstrumentId() {
            return ustInstrumentId;
        }

        public void setUstInstrumentId(String ustInstrumentId) {
            this.ustInstrumentId = ustInstrumentId;
        }

        public Long getUstPrice() {
            return ustPrice;
        }

        public void setUstPrice(Long ustPrice) {
            this.ustPrice = ustPrice;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
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

    public static class PartySubClass {
        CordaX500Name cordaX500Name;
        private String name;
        private String organisation;
        private String locality;

        public PartySubClass(JSONObject jSonString) {

//            JSONObject obj = new JSONObject(jSonString);
            name =  jSonString.getString("name");
            organisation =  jSonString.getString("organisation");
            locality = jSonString.getString("locality");
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
