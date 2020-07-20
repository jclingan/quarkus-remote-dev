package org.acme;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("/hello")
public class Hello {

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        String returnval="whoami output\n";

        Runtime r = Runtime.getRuntime();
        try {
            Process p = r.exec("/bin/id");
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));

            while (br.ready()) {
                returnval += br.readLine();
            }
        } catch (Exception ex) {
            returnval = ex.toString();
        }

        return returnval;

        // return "hello";
        // return System.getenv("HOSTNAME");
        // return System.getenv("USER");
    }
}