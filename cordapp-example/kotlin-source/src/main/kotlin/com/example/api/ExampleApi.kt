package com.example.api


import com.example.flow.ExampleFlow
import com.example.flow.QueryAll
import com.example.state.ExceptionModel
import com.example.state.InsuranceState
import com.example.state.test
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.CordaRPCOps
import net.corda.core.messaging.vaultQueryBy
import net.corda.core.utilities.getOrThrow
import net.corda.core.utilities.loggerFor
import org.slf4j.Logger
import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import javax.ws.rs.core.Response
import javax.ws.rs.core.Response.Status.BAD_REQUEST

val SERVICE_NAMES = listOf("Controller", "Network Map Service")

// This API is accessible from /api/example. All paths specified below are relative to it.
@Path("example")
class ExampleApi(private val rpcOps: CordaRPCOps) {
    private val myLegalName: CordaX500Name = rpcOps.nodeInfo().legalIdentities.first().name
    private val myLegalName2: String = rpcOps.nodeInfo().legalIdentities.first().name.organisation
    companion object {
        private val logger: Logger = loggerFor<ExampleApi>()
    }

    /**
     * Returns the node's name.
     */
    @GET
    @Path("me")
    @Produces(MediaType.APPLICATION_JSON)
    fun whoami() = mapOf("me" to myLegalName2)

    /**
     * Returns all parties registered with the [NetworkMapService]. These names can be used to look up identities
     * using the [IdentityService].
     */
    @GET
    @Path("peers")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = rpcOps.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
                .map { it.legalIdentities.first().name }
                //filter out myself, notary and eventual network map started by driver
                .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Insurance states that exist in the node's vault.
     */
    @GET
    @Path("Insurances")
    @Produces(MediaType.APPLICATION_JSON)
    fun getInsurances() = rpcOps.vaultQueryBy<InsuranceState>().states

    /**
     * Testing:
     * Gather data from other party
     *
     */

    @GET
    @Path("QueryAll")
    @Produces(MediaType.APPLICATION_JSON)
    fun QueryAll(): Response {
        logger.info("Getting all Query")
        val (status, message) = try {
            val flowHandle = rpcOps.startTrackedFlowDynamic(QueryAll.Initiator::class.java)
            flowHandle.progress.subscribe { logger.info("CashApi.getBankBalances: $it") }
            val result = flowHandle.use {
                it.returnValue.getOrThrow()
            }
            Response.Status.OK to result

        } catch (ex: Exception) {
            logger.error("Exception during transferCash: $ex")
            Response.Status.INTERNAL_SERVER_ERROR to ExceptionModel(statusCode = Response.Status.INTERNAL_SERVER_ERROR.statusCode, msg = ex.message.toString())
        }
        return Response.status(status).entity(message).build()
    }

    /**
     * Initiates a flow to agree an Insurance between two parties.
     *
     * Once the flow finishes it will have written the Insurance to ledger. Both the lender and the borrower will be able to
     * see it when calling /api/example/Insurances on their respective nodes.
     *
     * This end-point takes a Party name parameter as part of the path. If the serving node can't find the other party
     * in its network map cache, it will return an HTTP bad request.
     *
     * The flow is invoked asynchronously. It returns a future when the flow's call() method returns.
     */
    @PUT
    @Path("create-Insurance")
    @Produces(MediaType.APPLICATION_JSON)
    fun createInsurance(@QueryParam("NRIC") NRIC: String,@QueryParam("Name") Name: String,
                        @QueryParam("policyID") policyID: String,@QueryParam("InsuranceValue") InsuranceValue: Int): Response {
        if (InsuranceValue <= 0) {
            return Response.status(BAD_REQUEST).entity("Query parameter 'InsuranceValue' must be non-negative.\n").build()
        }
        val (status, message) = try {
            val flowHandle = rpcOps.startTrackedFlowDynamic(ExampleFlow.Initiator::class.java,NRIC,Name,policyID, InsuranceValue)
            flowHandle.progress.subscribe { logger.info("Creating Post: $it") }
            val result = flowHandle.use {
                it.returnValue.getOrThrow()
            }
//            return Response.status(Response.Status.CREATED).entity("Transaction id ${result.id} committed to ledger.\n").build()
            Response.Status.CREATED to result.tx.outputsOfType<InsuranceState>()
//
        } catch (ex: Exception) {
            logger.error("Exception during issueRedeem: $ex")
//            Response.status(BAD_REQUEST).entity(ex.message!!).build()
            Response.Status.INTERNAL_SERVER_ERROR to ExceptionModel(statusCode = Response.Status.INTERNAL_SERVER_ERROR.statusCode, msg = ex.message.toString())
        }
        return Response.status(status).entity(message).build()
    }


@PUT
@Path("create-Insurance2")
@Produces(MediaType.APPLICATION_JSON)
fun createInsurance2(state: test): Response {
    if (state.policyAmount <= 0) {
        return Response.status(BAD_REQUEST).entity("Query parameter 'InsuranceValue' must be non-negative.\n").build()
    }
    val (status, message) = try {
        val flowHandle = rpcOps.startTrackedFlowDynamic(ExampleFlow.Initiator::class.java,state.NRIC,state.Name,state.policyID, state.policyAmount)
        flowHandle.progress.subscribe { logger.info("Creating Post: $it") }
        val result = flowHandle.use {
            it.returnValue.getOrThrow()
        }
        Response.Status.CREATED to result.tx.outputsOfType<InsuranceState>()
//        Response.Status.CREATED to ("Transaction id ${result.id} committed to ledger.\n")
    } catch (ex: Exception) {
        logger.error("Exception during issueRedeem: $ex")
        Response.Status.INTERNAL_SERVER_ERROR to ExceptionModel(statusCode = Response.Status.INTERNAL_SERVER_ERROR.statusCode, msg = ex.message.toString())
    }
    return Response.status(status).entity(message).build()
    }

    @GET
    @Path("QueryNRIC{noop: (/)?}{transType: (.+)?}")
    @Produces(MediaType.APPLICATION_JSON)
    fun QueryNRIC(@PathParam("transType") transType: String = ""): Response {
        logger.info("transtype path param " + transType)
//        var selectedTransType: Int? = null
//        val result = mutableListOf<TransactionModel>()
        val (status, message) = try {
            val flowHandle = rpcOps.startTrackedFlowDynamic(com.example.flow.QueryNRIC.QuerybyNRIC::class.java,transType)
            flowHandle.progress.subscribe { logger.info("CashApi.getBankBalances: $it") }
            val result = flowHandle.use {
                it.returnValue.getOrThrow()
            }
            Response.Status.OK to result
        } catch (ex: Exception) {
            logger.error("Exception during transferCash: $ex")
            Response.Status.INTERNAL_SERVER_ERROR to ExceptionModel(statusCode = Response.Status.INTERNAL_SERVER_ERROR.statusCode, msg = ex.message.toString())
        }
        return Response.status(status).entity(message).build()
    }
}
