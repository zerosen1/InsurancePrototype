package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.state.BankModel
import com.example.state.InsuranceState
import net.corda.core.contracts.StateAndRef
import net.corda.core.flows.*
import net.corda.core.node.NodeInfo
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.queryBy
import java.util.ArrayList

fun NodeInfo.isNotary(services: ServiceHub) = services.networkMapCache.notaryIdentities.any { this.isLegalIdentity(it) }
fun NodeInfo.isMe(me: NodeInfo) = this.legalIdentities.first().name == me.legalIdentities.first().name

object QueryAll {
    @StartableByRPC
    @InitiatingFlow
    class Initiator : FlowLogic<List<BankModel>>() {
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
                session.send(Unit)

                val cashStates = subFlow(ReceiveStateAndRefFlow<InsuranceState>(session))

                val NRIC = cashStates.map { it.state.data.NRIC }
                val Name = cashStates.map { it.state.data.Name }
                val policyID = cashStates.map { it.state.data.policyID }
                val policyAmount = cashStates.map { it.state.data.policyAmount}
                val eachBank = BankModel(
                        bic = it.legalIdentities.first().name.organisation,
                        X500Name = it.legalIdentities.first().name.toString(),
                        NRIC = NRIC[0],
                        Name = Name[0],
                        policyID = policyID ,
                        policyAmount = policyAmount)
                bankList.add(eachBank)
            }
            return bankList
        }
    }

    /**
     * The other side of the above flow. For the purposes of this PoC, we won't add any additional checking.
     */
    @InitiatedBy(QueryAll.Initiator::class)
    class Responder(val session: FlowSession) : FlowLogic<Unit>() {
        @Suspendable
        override fun call() {
            val counterParty = session.counterparty
            logger.info("BalanceByBanksFlow.Responder: Requested balance from bank ${counterParty.name.organisation}")
//            if(counterParty.name != CENTRAL_PARTY_X500){
//                throw FlowException("Initiator is not central bank")
//            }
            session.receive<Unit>()
            val allQuery = serviceHub.vaultService.queryBy<InsuranceState>().states
            subFlow(SendStateAndRefFlow(session, allQuery))
        }
    }
}