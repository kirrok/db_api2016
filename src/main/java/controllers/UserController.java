package controllers;

import javax.inject.Singleton;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.sql.*;
import java.util.Collection;

/**
 * Created by a.serebrennikova
 */
@Singleton
@Path("/user")
public class UserController {
    //private static final String URL = "jdbc:mysql://localhost:3306/TPForum";
    private Connection connection;

    public UserController(Connection connection) { this.connection = connection; }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("details")
    public Response details(/*@QueryParam("user") String email*/) {

        try {
            Statement stat = connection.createStatement();
            int rez = stat.executeUpdate("update user set name='hi' where username='qwe'");
            String json = "{ \"qwe\": \"qaz\" }";
            return Response.status(Response.Status.OK).entity(json).build();
        } catch (SQLException e) {
            e.printStackTrace();
            String json = "{ \"qwe\": \"qaz\" }";
            return Response.status(Response.Status.OK).entity(json).build();
        }
    }

}
