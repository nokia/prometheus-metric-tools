package com.nokia.as.main.jetty;


import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;

/**
 * Entry points for the Jetty server
 */
@Path(App.API_ROOT)
public class EntryPoint {

    @GET
    @Path("test")
    @Produces(MediaType.APPLICATION_JSON)
    public String test() {
        return "test";
    }

}