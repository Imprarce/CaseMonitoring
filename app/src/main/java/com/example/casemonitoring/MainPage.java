package com.example.casemonitoring;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MainPage extends AppCompatActivity {

    private static List<String> caseInfoBase = new ArrayList<>();
    private static final int MAX_PAGES = 4;
    TextView Logo;

    Button refresh;
    ListView caseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        getSupportActionBar().hide();

        Logo = (TextView) findViewById(R.id.Logo);

        caseList = (ListView) findViewById(R.id.caseList);

        refresh = (Button) findViewById(R.id.refreshCases);

        refresh.setOnClickListener(view -> {
            makeRequest(1);
        });
    }

    private void makeRequest(int page) {
        String url = "https://steamcommunity.com/market/listings/730/Snakebite%20Case";
        RequestQueue queue = Volley.newRequestQueue(this);
        Log.d("URL", url);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        processPrices(response);
                        if (page < MAX_PAGES) {
                            makeRequest(page + 1);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(error);
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = super.getHeaders();
                if (headers == null || headers.equals(Collections.emptyMap())) {
                    headers = new HashMap<>();
                }
                headers.put("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3");
                return headers;
            }
        };
        stringRequest.setShouldCache(false);
        queue.add(stringRequest);
    }

    private void processPrices(String response) {
        final List<String[]> data = new ArrayList<>();

        Document doc = (Document) Jsoup.parse(response);
        Elements cases = doc.select(".market_listing_row");
        for (Element caseItem : cases) {
            String caseName = caseItem.select(".market_listing_item_name").text();
            Log.d("CaseName", caseName);
            String casePrice = caseItem.select("span.market_table_value.normal_price > span.normal_price").text();
            Log.d("CasePrice", casePrice);
            String caseCount = caseItem.select(".market_listing_num_listings_qty").text();
            Log.d("CaseCount", caseCount);
            addData(caseName, casePrice, caseCount, data);
        }

    }

    private void handleError(VolleyError error) {
        Log.e("ERROR", error.getMessage());
    }

        private void addData(String name, String price, String count, List<String[]> data) {

            StringBuilder priceChange = new StringBuilder(price);
            int check = 0;
            char c;
            while(true){
                c = priceChange.charAt(check);
                if(!(Character.isDigit(c)) && c != '.'){
                    priceChange.deleteCharAt(check);
                } else check++;
                if(check == priceChange.length()) break;
            }
            priceChange.append("$");
            String[] row = {name,  count, priceChange.toString()};
            data.add(row);
            if(data.size() >= 10) printInfo(data);
        }

        private void printInfo(List<String[]> data) {
            for (String[] row : data) {
                String out =  row[0];
                if(!caseInfoBase.contains(out)) caseInfoBase.add(out);
            }
            if (caseInfoBase.size() >= 10) {
                final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainPage.this,
                        android.R.layout.simple_list_item_1, caseInfoBase) {
                    @Override
                    public View getView(int position, View convertView, ViewGroup parent) {
                        View view = super.getView(position, convertView, parent);

                        TextView textView = (TextView) view.findViewById(android.R.id.text1);

                        textView.setTextColor(Color.WHITE);

                        return view;
                    }
                };

                caseList.setAdapter(adapter);
            }
        }

}
