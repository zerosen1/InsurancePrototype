package com.example.flow

import co.paralleluniverse.fibers.Suspendable
import com.example.contract.InsuranceContract
import com.example.contract.InsuranceContract.Companion.Insurance_CONTRACT_ID
import com.example.flow.ExampleFlow.Initiator
import com.example.state.BankModel
import com.example.state.InsuranceState
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateAndContract
import net.corda.core.contracts.requireThat
import net.corda.core.flows.*
import net.corda.core.identity.Party
import net.corda.core.node.NodeInfo
import net.corda.core.node.ServiceHub
import net.corda.core.node.services.queryBy
import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.finance.contracts.asset.Cash
import java.util.ArrayList


/**
 * This flow allows two parties (the [Initiator] and the [Acceptor]) to come to an agreement about the Insurance encapsulated
 * within an [InsuranceState].
 *
 * In our simple example, the [Acceptor] always accepts a valid Insurance.
 *
 * These flows have deliberately been implemented by using only the call() method for ease of understanding. In
 * practice we would recommend splitting up the varInsurances stages of the flow into sub-routines.
 *
 * All methods called within the [FlowLogic] sub-class need to be annotated with the @Suspendable annotation.
 */
object ExampleFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(val NRIC: String,
                    val Name: String,
                    val policyID: String,
                    val InsuranceValue: Int) : FlowLogic<SignedTransaction>() {
        /**
         * The progress tracker checkpoints each stage of the flow and outputs the specified messages when each
         * checkpoint is reached in the code. See the 'progressTracker.currentStep' expressions within the call() function.
         */
        companion object {
            object GENERATING_TRANSACTION : Step("Generating transaction based on new Insurance.")
            object VERIFYING_TRANSACTION : Step("Verifying contract constraints.")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGS : Step("Gathering the counterparty's signature.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }

            object FINALISING_TRANSACTION : Step("Obtaining notary signature and recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                    GENERATING_TRANSACTION,
                    VERIFYING_TRANSACTION,
                    SIGNING_TRANSACTION,
                    GATHERING_SIGS,
                    FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        /**
         * The flow logic is encapsulated within the call() method.
         */
        @Suspendable
        override fun call(): SignedTransaction {
            // Obtain a reference to the notary we want to use.
            val notary = serviceHub.networkMapCache.notaryIdentities[0]

            // Stage 1.
            progressTracker.currentStep = GENERATING_TRANSACTION
            // Generate an unsigned transaction.
            val InsuranceState = InsuranceState(NRIC,Name,policyID,InsuranceValue, serviceHub.myInfo.legalIdentities.first())
            val txCommand = Command(InsuranceContract.Commands.Create(), InsuranceState.participants.map { it.owningKey })
            val txBuilder = TransactionBuilder(notary).withItems(StateAndContract(InsuranceState, Insurance_CONTRACT_ID), txCommand)

            // Stage 2.
            progressTracker.currentStep = VERIFYING_TRANSACTION
            // Verify that the transaction is valid.
            txBuilder.verify(serviceHub)

            // Stage 3.
            progressTracker.currentStep = SIGNING_TRANSACTION
            // Sign the transaction.
            val partSignedTx = serviceHub.signInitialTransaction(txBuilder)
            serviceHub.recordTransactions(partSignedTx)

            return partSignedTx
        }
    }
}