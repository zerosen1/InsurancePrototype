package com.example.schema

import com.fasterxml.jackson.annotation.JsonInclude
import net.corda.core.schemas.MappedSchema
import net.corda.core.schemas.PersistentState
import net.corda.core.serialization.CordaSerializable
import java.util.*
import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Table

/**
 * The family of schemas for InsuranceState.
 */
object InsuranceSchema

/**
 * An InsuranceState schema.
 */
@CordaSerializable
@JsonInclude(JsonInclude.Include.NON_NULL)
object InsuranceSchemaV1 : MappedSchema(
        schemaFamily = InsuranceSchema.javaClass,
        version = 1,
        mappedTypes = listOf(PersistentInsurance::class.java)) {
    @Entity
    @Table(name = "Insurance_states")
    class PersistentInsurance(
            @Column(name = "NRIC")
            var NRIC: String,

            @Column(name = "Name")
            var Name: String,

            @Column(name = "policyID")
            var policyID: String,

            @Column(name = "policyAmount")
            var value: Int,

            @Column(name = "self")
            var self: String,

            @Column(name = "linear_id")
            var linearId: UUID
    ) : PersistentState()
}