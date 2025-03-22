package io.playersjob.adapters.web.transfermarkt

import io.playersjob.adapters.dto.TransfermarktResponse
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient(baseUri="https://transfermarkt-api.fly.dev")
interface TransfermarktRestClient {
    @GET
    @Path("/clubs/{clubId}/players")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPlayers(@PathParam("clubId") clubId: Int): TransfermarktResponse
}