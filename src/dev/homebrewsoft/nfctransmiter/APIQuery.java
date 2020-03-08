package dev.homebrewsoft.nfctransmiter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;

public class APIQuery {
	
	private String url;
	private String body;
	
	public APIQuery(String url, String body) {
		this.url = url;
		this.body = body;
	}
	
    public String getResponse() {
        HttpURLConnection con;
        StringBuffer content = new StringBuffer();
        try {
            URL url = new URL(this.url);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("POST");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            con.setInstanceFollowRedirects(false);
            con.setRequestProperty("Content-Type", "application/json");
            con.setDoOutput(true);
            OutputStream oStream = con.getOutputStream();
            oStream.write(body.getBytes());
            oStream.flush();
            oStream.close();
            // to execute the request
            int status = con.getResponseCode();
            // TODO: handle response code errors
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            con.disconnect();

        } 
        catch (IOException e) {
            e.printStackTrace();
        }
        return content.toString();
    }
}