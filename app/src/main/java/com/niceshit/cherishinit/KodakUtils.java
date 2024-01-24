package com.niceshit.cherishinit;

import android.util.Base64;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;


class Pair<X,Y> {
    private final X k;
    private final Y v;
    public Pair(X k, Y v) {
        this.k = k;
        this.v = v;
    }

    public X retrieveKey() {
        return this.k;
    }
    public Y retrieveVal() {
        return this.v;
    }
}
public class KodakUtils {

    public static String getTimeZone() {
        String str;
        TimeZone timeZone = TimeZone.getDefault();
        int offset = timeZone.getOffset(Calendar.getInstance(timeZone).getTimeInMillis());
        String format = String.format(Locale.US, "%02d.%02d", Integer.valueOf(Math.abs(offset / 3600000)), Integer.valueOf(Math.abs((offset / 60000) % 60)));
        StringBuilder sb = new StringBuilder();
        if (offset >= 0) {
            str = "+";
        } else {
            str = "-";
        }
        sb.append(str);
        sb.append(format);
        return sb.toString();
    }

    public static int getTimeZoneOffsetRaw() {
        return TimeZone.getDefault().getRawOffset();
    }

    public static String base64Encode(String str) {
        if (str != null) {
            return Base64.encodeToString(str.getBytes(StandardCharsets.UTF_8), 2);
        }
        return null;
    }
    public static String getTimeZoneName() {
        return TimeZone.getDefault().getID();
    }

    public static String convertToNoQuotedString(String str) {
        if (str != null && str.indexOf("\"") == 0 && str.lastIndexOf("\"") == str.length() - 1) {
            return str.substring(1, str.length() - 1);
        }
        return str;
    }
    public static int getLocalHertz() {
        int timeZoneOffsetRaw = getTimeZoneOffsetRaw() / 3600000;
        if (timeZoneOffsetRaw > -4 && timeZoneOffsetRaw < 9) {
            return 50;
        }
        return 60;
    }

    public static String readFile(InputStream inputStream) throws IOException {
        byte[] b = new byte[inputStream.available()];
        inputStream.read(b);

        return (new String(b));
    }

    public static Pair<Boolean, String> sendCommand(String[] commandArray) {
        //commandarray[0] = url
        //commandarray[1] = command
        //commandarray[2] = params
        //commandarray[3] = expexted return type

        String msg = "";
        String response = "";
        Boolean success = false;
        try {
            URL url;
            if (commandArray[2] != null) {
                url = new URL( commandArray[0] + commandArray[1] + commandArray[2]);
            } else {
                url = new URL( commandArray[0] + commandArray[1]);
            }

            URLConnection openConnection = url.openConnection();
            //openConnection.addRequestProperty(HttpHeaders.AUTHORIZATION, "Basic " + Base64.encodeToString(format.getBytes("UTF-8"), 2));
            openConnection.setRequestProperty("Authorization", "Basic " + Base64.encodeToString(String.format("%s:%s", "", "").getBytes("UTF-8"), 2));
            openConnection.setConnectTimeout(10000);
            openConnection.setReadTimeout(10000);

            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(openConnection.getInputStream()));
                try {
                    StringBuilder sb = new StringBuilder();
                    String readLine = "";
                    while ((readLine = bufferedReader.readLine()) != null) {
                        sb.append(readLine);
                        sb.append('\n');
                    }
                    String trim = sb.toString().trim();
                    if (trim.equals(commandArray[1] + ": -1")) {
                        success = false;
                    } else {
                        switch (commandArray[3]) {
                            case "boolean":
                                success = trim.equalsIgnoreCase(commandArray[1] + ": 0");
                                break;

                            case "string":
                                success = true;
                                response = trim;
                                break;
                        }
                    }

                    if (response.equals("")) {
                        msg = "Command: " + commandArray[1] + " - Response: " + String.valueOf(success) + " - Params: " + commandArray[2];
                    } else {
                        msg = "Command: " + commandArray[1] + " - Response: " + String.valueOf(success) + " - Params: " + commandArray[2] + " - Response: " + response;
                    }

                    Log.d("CherishInit", msg);

                    try {
                        bufferedReader.close();
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    }
                    return new Pair<Boolean, String>(success, msg);
                } catch (Exception ex) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                        msg = "Failed to close BufferReader -> " + e2.getMessage();
                        Log.d("Cherish", msg);
                        return new Pair<Boolean, String>(success, msg);
                    }
                }
            } catch (Exception ex){
                msg ="Command: " + commandArray[1] + " - Response: " + String.valueOf(success) + " - Params: " + commandArray[2] + " - Error: " + ex.getMessage();;
                Log.d("CherishInit", msg);
                return new Pair<Boolean, String>(false, msg);
            }
        } catch (Exception ex) {
            msg = "Exception on opening Connection. -> " + ex.getMessage();
            Log.d("CherishInit", msg);
            return new Pair<Boolean, String>(false, msg);
        }
        return new Pair<Boolean, String>(false, "Should not come here...");
    }
}
