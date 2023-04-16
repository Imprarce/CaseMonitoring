package com.example.casemonitoring.second_page;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
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

import com.example.casemonitoring.R;

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

    private Base_Case mDBHelperCase;

    private static final String DATABASE_TABLE ="CaseInfo";
    public static final String KEY_COUNT = "Count";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ID = "_id";

    private SQLiteDatabase mDbCase;
    TextView Logo;

    Button refresh;
    ListView caseList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_page);
        getSupportActionBar().hide();

        mDBHelperCase = new Base_Case(this);

        try {
            mDBHelperCase.updateDataBase();
        } catch (IOException mIOException) {
            throw new Error("UnableToUpdateDatabase");
        }

        try {
            mDbCase = mDBHelperCase.getWritableDatabase();
        } catch (SQLException mSQLException) {
            throw mSQLException;
        }


        Logo = (TextView) findViewById(R.id.Logo);

        caseList = (ListView) findViewById(R.id.caseList);

        caseView();

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
            Cursor cursor = mDbCase.rawQuery("SELECT * FROM CaseInfo", null);
            cursor.moveToFirst();
            // Добавление/изменение элементов в БД
            while (true) {
                for (String[] Abr : data) {

                    while (!cursor.isAfterLast()) {
                        if (!(Abr[0].equals(cursor.getString(1)))) {
                            cursor.moveToNext();
                        } else{
                            ContentValues cv = new ContentValues();
                            cv.put("NAME", Abr[0]);
                            cv.put("COUNT", Integer.parseInt(Abr[1]));
                            cv.put("PRICE", Abr[2]);
                            if(!Abr[2].equals(cursor.getString(3))) {
                                mDbCase.update(DATABASE_TABLE, cv, KEY_ID + "=" + cursor.getString(0), null);
                                System.out.println("Прозошли измнения " + cv);
                            }
                            break;
                        }
                    }

                    if (cursor.isAfterLast()) {
                        String new_Case = "INSERT INTO CaseInfo (name, count, price) VALUES" + "('" + Abr[0] + "', '" + Abr[1] + "', '" + Abr[2] + "')";
                        mDbCase.execSQL(new_Case);
                        cursor.moveToFirst();
                    } else cursor.moveToFirst();
                }
                break;
            }
            cursor.close();
            caseView();
        }
    }

    protected void caseView(){
        Cursor cursor = mDbCase.rawQuery("SELECT * FROM CaseInfo", null);
        cursor.moveToFirst();
        if(caseInfoBase.size() >= 40) caseInfoBase.clear();

        // Удаление элементов, если count = 0 || убрать?
        while (!cursor.isAfterLast()) {
            if(cursor.getString(2).equals("0")) mDbCase.delete(DATABASE_TABLE, KEY_COUNT + "=" + cursor.getString(2), null);
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        //Удаление элементов с одинаковыми именами || Доделать или убрать?

        while (!cursor.isAfterLast()) {
            if(cursor.getString(1).contains(mDbCase.toString())) mDbCase.delete(DATABASE_TABLE, KEY_NAME + "=" + cursor.getString(1), null);
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        // Добавление элементов в список для их вывода
        while (!cursor.isAfterLast()) {
            if(!cursor.getString(2).equals("0")) caseInfoBase.add(cursor.getString(1) + " " + cursor.getString(2) + " " + cursor.getString(3));
            cursor.moveToNext();
        }

        cursor.close();
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
            System.out.println("Все обновилось");
            caseList.setAdapter(adapter);
        }

}