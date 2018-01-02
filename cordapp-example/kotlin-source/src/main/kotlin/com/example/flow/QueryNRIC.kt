package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.schema.InsuranceSchema
import com.example.schema.InsuranceSchemaV1
import com.example.state.BankModel
import com.example.state.InsuranceState
import net.corda.core.contracts.StateAndRef
import net.corda.core.crypto.SecureHash
import net.corda.core.flows.*
import net.corda.core.node.services.Vault
import net.corda.core.node.services.queryBy
import net.corda.core.node.services.vault.Builder
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.node.services.vault.builder
import net.corda.core.schemas.PersistentState
import net.corda.core.utilities.unwrap
import java.util.*

object QueryNRIC {
    @StartableByRPC
    @InitiatingFlow
    class QuerybyNRIC(val trans:String) : FlowLogic<List<BankModel>>() {
        @Suspendable
        override fun call(): List<BankModel> {
            logger.info("Querying other bank node")
            val bankList = ArrayList<BankModel>()
            val networkParticipants = serviceHub.networkMapCache.allNodes
            val filterBanksOnly = networkParticipants.filter {
                nodeInfo -> nodeInfo.isNotary(serviceHub).not() && nodeInfo.isMe(serviceHub.myInfo).not()
            }
            filterBanksOnly.forEach {
                val session = initiateFlow(it.legalIdentities.first())
                logger.info("Requesting balance for bank ${it.legalIdentities.first().name.organisation}")
                val eachBank = session.sendAndReceive<BankModel>(trans).unwrap{it}
                bankList.add(eachBank)
            }
            return bankList
        }
    }

    /**
     * The other side of the above flow. For the purposes of this PoC, we won't add any additional checking.
     */
    @InitiatedBy(QueryNRIC.QuerybyNRIC::class)
    class QueryNRICrespond(val session: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
//            val counterParty = session.counterparty
            val receivedNRIC=session.receive<String>().unwrap{it}
            logger.info("get detail from NRIC:$receivedNRIC")
            val allQuery = serviceHub.vaultService.queryBy<InsuranceState>().states

            val NRIC = allQuery.map { it.state.data.NRIC }
            val Name = allQuery.map { it.state.data.Name }
            val policyID = allQuery.map { it.state.data.policyID }
            val policyAmount = allQuery.map { it.state.data.policyAmount}

            var eachBank = BankModel(
                    policyID = emptyList() ,
                    policyAmount = emptyList())
            for (i in NRIC){
                if (i == receivedNRIC){
                    eachBank = BankModel(
                            bic = serviceHub.myInfo.legalIdentities.first().name.organisation,
                            X500Name = serviceHub.myInfo.legalIdentities.first().name.toString(),
                            NRIC = NRIC[0],
                            Name = Name[0],
                            policyID = policyID ,
                            policyAmount = policyAmount)}}
            session.send(eachBank)

//            val generalCriteria = QueryCriteria.VaultQueryCriteria(Vault.StateStatus.UNCONSUMED)
//            val equal = builder{InsuranceSchemaV1.PersistentInsurance::NRIC.equal(NRIC)}
//            val QueryCriteria = QueryCriteria.VaultCustomQueryCriteria(equal)
//            val redeemStateAndRef = serviceHub.vaultService.queryBy<InsuranceState>(generalCriteria.and(QueryCriteria)).states
//
//            subFlow(SendStateAndRefFlow(session,redeemStateAndRef))

//            val transaction = serviceHub.validatedTransactions.getTransaction(SecureHash.parse(NRIC)) ?: throw IllegalArgumentException("NRIC $NRIC not found")
//            val state = transaction.tx.outputsOfType<InsuranceState>().single()

//            val allQuery2 = serviceHub.vaultService.queryBy<InsuranceState>().states
//                subFlow(SendStateAndRefFlow(session,allQuery))
            }
        }
    }
