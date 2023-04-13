package com.example.casemonitoring;

import static android.R.layout.simple_list_item_1;
import static java.util.Arrays.asList;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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


    private static final String TAG = "MainActivity";
    private static final List<String> URL = asList("https://steamcommunity.com/market/search?category_730_ItemSet%5B%5D=any&category_730_ProPlayer%5B%5D=any&category_730_StickerCapsule%5B%5D=any&category_730_TournamentTeam%5B%5D=any&category_730_Weapon%5B%5D=any&category_730_Type%5B%5D=tag_CSGO_Type_WeaponCase&appid=730&q=case#p1_price_asc",
            "https://steamcommunity.com/market/search?category_730_ItemSet%5B%5D=any&category_730_ProPlayer%5B%5D=any&category_730_StickerCapsule%5B%5D=any&category_730_TournamentTeam%5B%5D=any&category_730_Weapon%5B%5D=any&category_730_Type%5B%5D=tag_CSGO_Type_WeaponCase&appid=730&q=case#p2_price_asc",
            "https://steamcommunity.com/market/search?category_730_ItemSet%5B%5D=any&category_730_ProPlayer%5B%5D=any&category_730_StickerCapsule%5B%5D=any&category_730_TournamentTeam%5B%5D=any&category_730_Weapon%5B%5D=any&category_730_Type%5B%5D=tag_CSGO_Type_WeaponCase&appid=730&q=case#p3_price_asc",
            "https://steamcommunity.com/market/search?category_730_ItemSet%5B%5D=any&category_730_ProPlayer%5B%5D=any&category_730_StickerCapsule%5B%5D=any&category_730_TournamentTeam%5B%5D=any&category_730_Weapon%5B%5D=any&category_730_Type%5B%5D=tag_CSGO_Type_WeaponCase&appid=730&q=case#p4_price_asc");

    TextView Logo;

    Button refresh;
    ListView caseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);

        Logo = (TextView) findViewById(R.id.Logo);

        caseList = (ListView) findViewById(R.id.caseList);

        refresh = (Button) findViewById(R.id.refreshCases);

        refresh.setOnClickListener(view -> {
            for(int i = 0; i < 4; i++) new SteamCasePriceParser().execute(URL.get(i));
        });
    }

    private class SteamCasePriceParser extends AsyncTask<String, Void, List<String[]>> {
        final List<String[]> data = new ArrayList<>();
        @Override
        protected List<String[]> doInBackground(String... urls) {
            try {
                Document doc = (Document) Jsoup.connect(urls[0]).userAgent("Chrome/4.0.249.0 Safari/532.5").get();
                Elements cases = doc.select(".market_listing_row_link");
                for (Element caseItem : cases) {
                    String caseName = caseItem.select(".market_listing_item_name").text();
                    String casePrice = caseItem.select("span.market_table_value.normal_price > span.normal_price").text();
                    String caseCount = caseItem.select(".market_listing_num_listings_qty").text();
                    addData(caseName, casePrice, caseCount);
                }
            } catch (IOException e) {
                Log.e(TAG, "Error while parsing data from Steam website", e);
            }
            return data;
        }

        private void addData(String name, String price, String count) {
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
        }

        @Override
        protected void onPostExecute(List<String[]> data) {
            List<String> outCase = new ArrayList<>();
            for (String[] row : data) {
                String out = "Name - " + row[0] + ", Count - " + row[1] + ", Price - " + row[2];
                outCase.add(out);
            }
            final ArrayAdapter<String> adapter = new ArrayAdapter<>(MainPage.this,
                    simple_list_item_1, outCase);
            caseList.setAdapter(adapter);

        }
    }
}