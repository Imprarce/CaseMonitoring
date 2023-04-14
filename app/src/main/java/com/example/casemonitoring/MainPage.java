package com.example.casemonitoring;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity {

    private static List<String> caseInfoBase = new ArrayList<>();
    private static final String url = "https://csgostash.com/containers/skin-cases";
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
            new SteamCasePriceParser().execute(url);
        });
    }

    private class SteamCasePriceParser extends AsyncTask<String, Void, List<String[]>> {
        final List<String[]> data = new ArrayList<>();

        @Override
        protected List<String[]> doInBackground(String... urls) {
            try {
                Document doc = (Document) Jsoup.connect(urls[0]).userAgent("Chrome/4.0.249.0 Safari/532.5").get();
                Element table = doc.select("body > div.container.main-content > div:nth-child(7)").first();
                Elements rows = table.getElementsByClass("col-lg-4 col-md-6 col-widen text-center");
                for (Element row : rows) {
                    String caseName = row.select("div > a > h4").text();
                    String casePrice = row.select("div > a > div > p").text();
                    String caseCount = row.getElementsByClass("btn btn-default market-button-item").text();
                    addData(caseName, casePrice, caseCount);
                }
            } catch (IOException e) {
                Log.e("Error", "Error while parsing data from website", e);
            }
            return data;
        }

        private void addData(String name, String price, String count) {
            StringBuilder priceChange = new StringBuilder(price);
            StringBuilder countChange = new StringBuilder(count);
            int check = 0;
            char c;
            while (true) {
                c = priceChange.charAt(check);
                if (!(Character.isDigit(c)) && c != ',') {
                    priceChange.deleteCharAt(check);
                } else check++;
                if (check == priceChange.length()) break;
            }
            priceChange.append(" руб.");
            check = 0;
            while(true){
                if (check == countChange.length()) break;
                c = countChange.charAt(check);
                if (!(Character.isDigit(c))) {
                    countChange.deleteCharAt(check);
                } else check++;
            }
            String[] row = {name, countChange.toString(), priceChange.toString()};
            data.add(row);
        }

        @Override
        protected void onPostExecute(List<String[]> data) {
            for (String[] row : data) {
                String out = row[0] + " " + row[1] + " " + row[2];
                caseInfoBase.add(out);
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
}