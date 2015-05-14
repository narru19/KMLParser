# KMLParser
A simple parser to read KML files in Android and use it on Google Maps


## Usage - TL:DR
Simply copy the AsyncTask (PaintBorder) and modify it as you want. 

```
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
            try {
                InputStream inputStream = getResources().openRawResource(Integer.parseInt(params[0]));
                DocumentBuilder docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
                Document document = docBuilder.parse(inputStream);
                NodeList coordList;

                if (document == null) return null;

                coordList = document.getElementsByTagName("coordinates");

                for (int i = 0; i < coordList.getLength(); i++) {
                    String[] coordinatePairs = coordList.item(i).getFirstChild().getNodeValue().trim().split(" ");
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
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            int i;
            for (i = 0; i < border_fragment.size(); i++) {
                PolylineOptions dots = new PolylineOptions();
                for (int j = 0; j < border_fragment.get(i).size(); j++) dots.add(border_fragment.get(i).get(j));
                border.add(dots);
            }

            for (i = 0; i < border.size(); i++) lines.add(map.addPolyline(border.get(i)));

            for (i = 0; i < lines.size(); i++) {
                lines.get(i).setWidth(2);
                lines.get(i).setColor(Color.RED);
                lines.get(i).setGeodesic(false);
                lines.get(i).setVisible(true);
            }
        }
}
```

## Usage - Extended
I have commented step by step what the parser and the post-execution do, just take a look at KMLParserExampleActivity.
If you still have questions, I recommend you to read through this documentation as it will make it clearer:

[AsyncTask Documentation](http://developer.android.com/reference/android/os/AsyncTask.html)

[Polyline Documentation](https://developer.android.com/reference/com/google/android/gms/maps/model/Polyline.html)


## Execution
Feel free to test this as it is fully operational. Modify it, break it, make it better, whatever. In order to do this, you'll need to set up google play services and get a Google Maps API v2 Key if you haven't already. To do so simply follow these instructions:

[Google Play Services](developer.android.com/google/play-services/)

[Google Maps](developers.google.com/maps/documentation/android/start#specify_app_settings_in_the_application_manifest)

Remember to change the API key value in the AndroidManifest.xml
