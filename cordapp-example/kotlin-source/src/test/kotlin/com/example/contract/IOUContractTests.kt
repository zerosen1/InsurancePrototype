//package com.example.contract
//
//import com.example.contract.InsuranceContract.Companion.Insurance_CONTRACT_ID
//import com.example.state.InsuranceState
//import net.corda.testing.*
//import org.junit.After
//import org.junit.Before
//import org.junit.Test
//
//class InsuranceContractTests {
//
//    @Before
//    fun setup() {
//        setCordappPackages("com.example.contract")
//    }
//
//    @After
//    fun tearDown() {
//        unsetCordappPackages()
//    }
//
//    @Test
//    fun `transaction must include Create command`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                fails()
//                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                verifies()
//            }
//        }
//    }
//
//    @Test
//    fun `transaction must have no inputs`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                input(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                command(MEGA_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("No inputs should be consumed when issuing an Insurance.")
//            }
//        }
//    }
//
//    @Test
//    fun `transaction must have one output`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("Only one output state should be created.")
//            }
//        }
//    }
//
//    @Test
//    fun `lender must sign transaction`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                command(MINI_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("All of the participants must be signers.")
//            }
//        }
//    }
//
//    @Test
//    fun `borrower must sign transaction`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                command(MEGA_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("All of the participants must be signers.")
//            }
//        }
//    }
//
//    @Test
//    fun `lender is not borrower`() {
//        val Insurance = 1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MEGA_CORP, MEGA_CORP) }
//                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("The lender and the borrower cannot be the same entity.")
//            }
//        }
//    }
//
//    @Test
//    fun `cannot create negative-value Insurances`() {
//        val Insurance = -1
//        ledger {
//            transaction {
//                output(Insurance_CONTRACT_ID) { InsuranceState(Insurance, MINI_CORP, MEGA_CORP) }
//                command(MEGA_CORP_PUBKEY, MINI_CORP_PUBKEY) { InsuranceContract.Commands.Create() }
//                `fails with`("The Insurance's value must be non-negative.")
//            }
//        }
//    }
//}