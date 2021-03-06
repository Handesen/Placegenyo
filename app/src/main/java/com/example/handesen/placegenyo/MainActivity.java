package com.example.handesen.placegenyo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toast;



public class MainActivity extends Activity implements OnItemClickListener,GeoTask.Geo {
    EditText edttxt_from,edttxt_to,uar,atlagfogy;

    String str_from,str_to;
    TextView tv_result1,tv_result2,cim1,cim2,aroda,arodavissza;
    private static final String LOG_TAG = "Google Places Autocomplete";
    private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
    private static final String TYPE_AUTOCOMPLETE = "/autocomplete";
    private static final String OUT_JSON = "/json";

    private static final String API_KEY = "AIzaSyCaIA2IzVL2pgS7w2msigFYpQlFjrLidJs";





    @Override
    public void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initialize();
        final AutoCompleteTextView autoCompView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView);
        final AutoCompleteTextView autoCompView2 = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView2);

        autoCompView.setAdapter(new GooglePlacesAutocompleteAdapter(this, R.layout.list_item));
        autoCompView.setOnItemClickListener(this);

        autoCompView2.setAdapter(new GooglePlacesAutocompleteAdapter(this,R.layout.list_item));
        autoCompView2.setOnItemClickListener(this);

        final Button button = (Button) findViewById(R.id.button3);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {

                String stri = autoCompView.getText().toString();
                String stri1 =  autoCompView2.getText().toString();
                stri = stri.replaceAll("\\s+","+");
                stri = stri.replaceAll(",","");

                stri1 = stri1.replaceAll("\\s+","+");
                stri1 = stri1.replaceAll(",","");
                str_from = stri;
                str_to = stri1;

                String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + str_from + "&destinations=" + str_to + "&mode=driving&language=hu&avoid=tolls&key=AIzaSyCaIA2IzVL2pgS7w2msigFYpQlFjrLidJs";
                new GeoTask(MainActivity.this).execute(url);
            }
        });

    }



    public void setDouble(String result) {
        String res[]=result.split(",");
        Double min=Double.parseDouble(res[0])/60;
        double dist= Double.parseDouble(res[1]) / 1000;
        DecimalFormat km = new DecimalFormat("#.#");
        tv_result1.setText("Idő= " + (int) (min / 60) + " óra " + (int) (min % 60) + " perc");
        tv_result2.setText("Távolság= " + km.format(dist) + " kilométer");
        double atlagfogyasztas = Double.parseDouble(atlagfogy.getText().toString());
        double uzemanyagar = Double.parseDouble(uar.getText().toString());
        double fogyoda = (dist/100) * atlagfogyasztas * uzemanyagar;
        DecimalFormat formater = new DecimalFormat("#");
        aroda.setText(aroda.getText().toString() + " " + formater.format(fogyoda) + " FT");
        arodavissza.setText(arodavissza.getText().toString() + " " + formater.format(fogyoda*2) + " FT");

    }
    public void initialize()
    {


        tv_result1= (TextView) findViewById(R.id.textView_result1);
        tv_result2=(TextView) findViewById(R.id.textView_result2);
        cim1 = (TextView) findViewById(R.id.cim1);
        cim2 = (TextView) findViewById(R.id.cim2);
        aroda = (TextView) findViewById(R.id.aroda);
        arodavissza = (TextView) findViewById(R.id.araodavissz);
        uar = (EditText) findViewById(R.id.uar);
        atlagfogy = (EditText) findViewById(R.id.atlagfogy);


    }

    public void onItemClick(AdapterView adapterView, View view, int position, long id) {
        String str = (String) adapterView.getItemAtPosition(position);
        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();

    }

    public static ArrayList autocomplete(String input) {
        ArrayList resultList = null;

        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_AUTOCOMPLETE + OUT_JSON);
            sb.append("?key=" + API_KEY);
            sb.append("&components=country:hu");
            sb.append("&input=" + URLEncoder.encode(input, "utf8"));

            URL url = new URL(sb.toString());
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, "Error proc Places API URL", e);
            return resultList;
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error conn to Plac API", e);
            return resultList;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }

        try {
            // Create a JSON object hierarchy from the results
            JSONObject jsonObj = new JSONObject(jsonResults.toString());
            JSONArray predsJsonArray = jsonObj.getJSONArray("predictions");

            // Extract the Place descriptions from the results
            resultList = new ArrayList(predsJsonArray.length());
            for (int i = 0; i < predsJsonArray.length(); i++) {
                System.out.println(predsJsonArray.getJSONObject(i).getString("description"));
                System.out.println("============================================================");
                resultList.add(predsJsonArray.getJSONObject(i).getString("description"));
            }
        } catch (JSONException e) {
            Log.e(LOG_TAG, "Cant pro JSON results", e);
        }

        return resultList;
    }

    class GooglePlacesAutocompleteAdapter extends ArrayAdapter implements Filterable {
        private ArrayList resultList;

        public GooglePlacesAutocompleteAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            return resultList.size();
        }

        @Override
        public String getItem(int index) {
            return (String) resultList.get(index);
        }

        @Override
        public Filter getFilter() {
            Filter filter = new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence constraint) {
                    FilterResults filterResults = new FilterResults();
                    if (constraint != null) {
                        // Retrieve the autocomplete results.
                        resultList = autocomplete(constraint.toString());

                        // Assign the data to the FilterResults
                        filterResults.values = resultList;
                        filterResults.count = resultList.size();
                    }
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence constraint, FilterResults results) {
                    if (results != null && results.count > 0) {
                        notifyDataSetChanged();
                    } else {
                        notifyDataSetInvalidated();
                    }
                }
            };
            return filter;
        }
    }

}

