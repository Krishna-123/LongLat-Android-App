package android.aitlindia.com.longlat;

/**
 * Created by krishna on 25/3/17.
 */


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

class JSONParser {

    private HttpURLConnection conn;
    private StringBuilder result;


    String makeHttpRequest(String url, String method,
                           String params) {

        if (method.equals("POST")) {
            // request method is POST
            try {
                URL urlObj = new URL(url);

                conn = (HttpURLConnection) urlObj.openConnection();

                conn.setDoOutput(true);

                conn.setRequestMethod("POST");

                conn.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);

                conn.connect();

                DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                wr.writeBytes(params);
                wr.flush();
                wr.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try {
            //Receive the response from the server
            InputStream in = new BufferedInputStream(conn.getInputStream());
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));
            result = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                result.append(line);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        conn.disconnect();

        return result.toString();
    }
}
