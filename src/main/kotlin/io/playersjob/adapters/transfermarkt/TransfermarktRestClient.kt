package io.playersjob.adapters.transfermarkt

import io.playersjob.adapters.dto.TransfermarktPlayer
import io.smallrye.mutiny.Uni
import jakarta.ws.rs.GET
import jakarta.ws.rs.Path
import jakarta.ws.rs.PathParam
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.eclipse.microprofile.rest.client.inject.RegisterRestClient

@RegisterRestClient
interface TransfermarktRestClient {
    @GET
    @Path("/clubs/{clubId}/players")
    @Produces(MediaType.APPLICATION_JSON)
    fun getPlayers(@PathParam("clubId") clubId: Int): Uni<List<TransfermarktPlayer>>
}