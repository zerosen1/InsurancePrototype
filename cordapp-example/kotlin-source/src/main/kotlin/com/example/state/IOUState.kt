package com.example.state

import com.example.schema.InsuranceSchemaV1
import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.LinearState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.schemas.QueryableState
import net.corda.core.serialization.CordaSerializable

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class BankModel(val bic : String = "",
                     val X500Name: String = "",
                     val NRIC:String= "",
                     val Name:String= "",
                     val policyID: List<String>,
                     val policyAmount: List<Int>)

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class test(val NRIC: String,
                val Name: String,
                val policyID: String,
                val policyAmount: Int)

/**
 * The state object recording Insurance agreements between two parties.
 *
 * A state must implement [ContractState] or one of its descendants.
 *
 * @param value the value of the Insurance.
 * @param lender the party issuing the Insurance.
 * @param borrower the party receiving and approving the Insurance.
 */
data class InsuranceState(val NRIC: String,
                          val Name: String,
                          val policyID: String,
                          val policyAmount: Int,
                          val Self: Party,
                          override val linearId: UniqueIdentifier = UniqueIdentifier()):
        LinearState, QueryableState {
    /** The public keys of the involved parties. */
    override val participants: List<AbstractParty> get() = listOf(Self)
//    override fun supportedSchemas() = listOf(InsuranceSchemaV1)

    override fun generateMappedObject(schema: MappedSchema): PersistentState {
        return when (schema) {
            is InsuranceSchemaV1 -> InsuranceSchemaV1.PersistentInsurance(
                    this.NRIC,
                    this.Name,
                    this.policyID,
                    this.policyAmount,
                    this.Self.name.toString(),
                    this.linearId.id
            )
            else -> throw IllegalArgumentException("Unrecognised schema $schema")
        }
    }
    override fun supportedSchemas(): Iterable<MappedSchema> = listOf(InsuranceSchemaV1)
}

@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
data class ExceptionModel(var statusCode: Int? = null,
                          var transId: String? = null,
                          var msg: String? = null)