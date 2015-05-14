package com.example.toni.kmlparser;


import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

public class KMLParserExampleActivity extends Activity implements View.OnClickListener {

    private GoogleMap map;
    private long zindex;
    private Button load_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kmlparserexample);

        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        map.setMapType(GoogleMap.MAP_TYPE_SATELLITE);

        load_button = (Button) findViewById(R.id.load_border_button);
        Button clear_button = (Button) findViewById(R.id.clear_map_button);
        load_button.setOnClickListener(this);
        clear_button.setOnClickListener(this);
        zindex = 0;
        setCameraSpain();
    }

    @Override
    public void onClick(View v) {
        // No need to check for the id, as only one button is present
        if (v.getId() == load_button.getId()) {
        /* This is the path where your kml file is located. I suggest you place your kml files
           inside the raw folder (res/raw) since you can loop through all the files from this
           folder this way:

               Field[] files = R.raw.class.getFields();
               for (int i = 0; i < files.length; ++i) {
                    String path = String.valueOf(getResources().getIdentifier(files[i].getName(),
                                                 "raw", getPackageName()));
                    new PaintBorder().execute(path);
               }
        */
            String path = String.valueOf(getResources().getIdentifier("spain", "raw", getPackageName()));
            new PaintBorder().execute(path);
        /* Notice I am using weak references as I don't care how much it takes to paint the geometry.
           If you want it to alert you once it's done painting, simply use a strong reference and any
           alert trick (e.g. a boolean set to true once it's done painting) so that the garbage
           collector does not destroy the AsyncTask once it's done.
        */
        }
        else map.clear();
    }

    private class PaintBorder extends AsyncTask<String, Void, Void> {

        Vector<Polyline> lines;
        Vector<PolylineOptions> border;
        Vector<Vector<LatLng>> border_fragment;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            lines = new Vector<>();
            border = new Vector<>();
            border_fragment = new Vector<>();
        }

        @Override
        protected Void doInBackground(String... params) {
            // PARSER STARTS
            try {
                InputStream inputStream = getResources().openRawResource(Integer.parseInt(params[0]));
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = docBuilder.parse(inputStream);
                NodeList coordList;

                if (document == null) return null;

                //Separate data by the <coordinates> tag.
                coordList = document.getElementsByTagName("coordinates");

                for (int i = 0; i < coordList.getLength(); i++) {
                    String[] coordinatePairs = coordList.item(i)
                                                        .getFirstChild()
                                                        .getNodeValue()
                                                        .trim()
                                                        .split(" ");
                    Vector<LatLng> positions = new Vector<>();
                    for (String coord : coordinatePairs) {
                        positions.add(new LatLng(Double.parseDouble(coord.split(",")[1]),
                                                 Double.parseDouble(coord.split(",")[0])));
                    }
                    border_fragment.add(positions);
                }
            } catch (ParserConfigurationException | IOException | SAXException e) {
                e.printStackTrace();
            }
            return null;
            // PARSER ENDS
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // THE PAINTING IS DONE HERE SINCE WE NEED TO ACCESS THE GUI AND WE CANNOT DO THAT
            // UNLESS WE'RE INSIDE THE onPostExecute METHOD AND WE HAVE ACCESS TO THE GUI

            int i;
            for (i = 0; i < border_fragment.size(); i++) {
                PolylineOptions dots = new PolylineOptions();
                for (int j = 0; j < border_fragment.get(i).size(); j++) {
                    dots.add(border_fragment.get(i).get(j));
                }
                border.add(dots);
            }

            for (i = 0; i < border.size(); i++) {
                lines.add(map.addPolyline(border.get(i)));
            }

            for (i = 0; i < lines.size(); i++) {
                // The following are up to you, just remember that if you have two lines drawn in
                // the same position, only the one with higher zindex will be visible. It's up to
                // you what you want to do with zindex.
                lines.get(i).setWidth(2);
                lines.get(i).setColor(Color.RED);
                lines.get(i).setGeodesic(false);
                lines.get(i).setVisible(true);
                lines.get(i).setZIndex(zindex);
            }
            zindex++;
        }
    }

    // NOT RELEVANT
    private void setCameraSpain() {
        LatLng centerSpain = new LatLng(39.990002, -4.416125);
        CameraPosition spainfocus = new CameraPosition.Builder().target(centerSpain)
                .zoom(2f)
                .build();
        map.animateCamera(CameraUpdateFactory.newCameraPosition(spainfocus));
    }

    // NOT RELEVANT
    @Override
    protected void onResume() {
        super.onResume();

        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(KMLParserExampleActivity.this);
        if (status != ConnectionResult.SUCCESS) {
            if (GooglePlayServicesUtil.isUserRecoverableError(status)) {
                Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(status, KMLParserExampleActivity.this, 1);
                if (errorDialog != null) errorDialog.show();
            } else
                Toast.makeText(this, "Google Play Services not found", Toast.LENGTH_SHORT).show();
        }
    }

}
