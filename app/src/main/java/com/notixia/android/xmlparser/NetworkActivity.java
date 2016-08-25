package com.notixia.android.xmlparser;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.webkit.WebView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.jar.Pack200;

/**
 * Created by rsn on 24/08/16.
 */
public class NetworkActivity extends Activity {
    /*public static final String WIFI  = "Wi-Fi";
    public static final String ANY = "Any";*/
    private static final String URL = Environment.getExternalStorageDirectory().toString() + "/example.xml";
    private static final int PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 100;

    /*// Whether there is a Wi-Fi connection.
    private static boolean wifiConnected = false;
    // Whether there is a mobile connection.
    private static boolean mobileConnected = false;
    // Whether the display should be refreshed.
    public static boolean refreshDisplay = true;
    public static String sPref = "";*/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*setContentView(R.layout.activity_main);*/

        int permissionCheck = ContextCompat.checkSelfPermission(NetworkActivity.this, android.Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(NetworkActivity.this, new String[] {android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
        } else {
            new DownloadXmlTask().execute(URL);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch(requestCode) {
            case PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    new DownloadXmlTask().execute(URL);
                } else {
                    // permission denied: disable the functionality that depends on this persmission
                }
                break;
        }
    }

    public class DownloadXmlTask extends AsyncTask<String, Void, String> {
        @Override
        protected String doInBackground(String... params) {
            try {
                return loadXmlFromFileSystem(params[0]);
            } catch (IOException e) {
                return getResources().getString(R.string.connection_error);
            } catch (XmlPullParserException e) {
                return getResources().getString(R.string.xml_error);
            }
        }

        @Override
        protected void onPostExecute(String result) {
            setContentView(R.layout.activity_main);
            // Displays the HTML string in the UI via a WebView
            WebView webView = (WebView) findViewById(R.id.webView);
            webView.loadData(result, "text/html", null);
        }

        private String loadXmlFromFileSystem(String path) throws XmlPullParserException, IOException {
            InputStream stream = null;
            // Instantiate the parser
            ExampleParser exampleParser = new ExampleParser();
            List<ExampleParser.Club> clubs = null;
            String name = null;
            String link = null;
            Calendar rightNow = Calendar.getInstance();
            DateFormat formatter = new SimpleDateFormat("MMM dd h:mmaa");

            StringBuilder htmlString = new StringBuilder();
            htmlString.append("<h3>" + getResources().getString(R.string.page_title) + "</h3>");
            htmlString.append("<em>" + getResources().getString(R.string.updated) + " " + formatter.format(rightNow.getTime()) + "</em>");

            try {
                stream = getFileInputStream(path);
                clubs = exampleParser.parse(stream);
                // Makes sure that the InputStream is closed after the app is
                // finished using it.
            } finally {
                if (stream != null) {
                    stream.close();
                }
            }

            for (ExampleParser.Club club : clubs) {
                htmlString.append("<p><a href='");
                htmlString.append(club.link);
                htmlString.append("'>" + club.name + "</a></p>");
            }
            return htmlString.toString();
        }

        // Given a string representation of a URL, sets up a connection and get
        // an input stream.
        private FileInputStream getFileInputStream(String path) throws IOException {
            File file = new File(path);

            FileInputStream fileInputStream = null;

            try {
                fileInputStream = new FileInputStream(file);
            } catch (IOException e) {
                e.printStackTrace();
            } /*finally {
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
            }*/

            return  fileInputStream;
        }
    }
}
