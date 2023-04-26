package com.example.casemonitoring.second_page;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.casemonitoring.R;
import com.example.casemonitoring.first_page.Authorization;
import com.squareup.picasso.Picasso;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainPage extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, FavoriteFragmentListener {

    private static List<String> caseInfoBase = new ArrayList<>();
    private static final String url = "https://csgostash.com/containers/skin-cases";

    private Base_Case mDBHelperCase;

    private static final String DATABASE_TABLE ="CaseInfo";
    public static final String KEY_COUNT = "Count";
    public static final String KEY_NAME = "Name";
    public static final String KEY_ID = "_id";

    private SQLiteDatabase mDbCase;
    private TextView Logo;

    private Switch refresh10sec;
    private Switch refresh30sec;
    private Button refresh;
    private ListView caseList;

    private ImageView favorite;
    private ImageView exit;



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

        refresh10sec = (Switch) findViewById(R.id.refresh10sec);
        refresh30sec = (Switch) findViewById(R.id.refresh30sec);

        if(refresh10sec != null){
            refresh10sec.setOnCheckedChangeListener(this);
        }

        if(refresh30sec != null){
            refresh30sec.setOnCheckedChangeListener(this);
        }

        Logo = (TextView) findViewById(R.id.Logo);

        caseList = (ListView) findViewById(R.id.caseList);

        caseView();

        refresh = (Button) findViewById(R.id.refreshCases);

        refresh.setOnClickListener(view -> {
            new SteamCasePriceParser().execute(url);
        });

        favorite = findViewById(R.id.favorite);
        exit = findViewById(R.id.exit);

        exit.setOnClickListener(v -> {
            startActivity(new Intent(MainPage.this, Authorization.class));
            finish();
        });

        favorite.setOnClickListener(v -> {

            Favorite_Fragment fragment = new Favorite_Fragment();

            FragmentManager fragmentManager = getSupportFragmentManager();

            FragmentTransaction transaction = fragmentManager.beginTransaction();

            transaction.replace(R.id.main_constraint, fragment);

            transaction.addToBackStack(null);

            fragment.setListener(this);

            transaction.commit();

            refresh10sec.setClickable(false);
            refresh30sec.setClickable(false);
            refresh.setClickable(false);
            caseList.setClickable(false);
            favorite.setClickable(false);
            exit.setClickable(false);
        });
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if(refresh10sec.isChecked() && refresh30sec.isChecked())
        {
            refresh10sec.setChecked(false);
            refresh30sec.setChecked(false);
            Toast.makeText(this, "Выберите что-то одно", Toast.LENGTH_SHORT).show();
        }
        if(refresh10sec.isChecked()){
            final Handler hf = new Handler();
            hf.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new SteamCasePriceParser().execute(url);
                    if(refresh10sec.isChecked()) hf.postDelayed(this, 10000);
                }
            } ,10000);
        }
        if(refresh30sec.isChecked()){
            final Handler hf = new Handler();
            hf.postDelayed(new Runnable() {
                @Override
                public void run() {
                    new SteamCasePriceParser().execute(url);
                    if(refresh30sec.isChecked()) hf.postDelayed(this, 30000);
                }
            } ,30000);
        }
    }

    @Override
    public void onExitClicked() {
        refresh10sec.setClickable(true);
        refresh30sec.setClickable(true);
        refresh.setClickable(true);
        caseList.setClickable(true);
        favorite.setClickable(true);
        exit.setClickable(true);
        onBackPressed();
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
                    String imageURL = row.getElementsByClass("img-responsive center-block").attr("src");
                    String caseName = row.select("div > a > h4").text();
                    String casePrice = row.select("div > a > div > p").text();
                    String caseCount = row.getElementsByClass("btn btn-default market-button-item").text();
                    addData(caseName, casePrice, caseCount, imageURL);
                }
            } catch (IOException e) {
                Log.e("Error", "Error while parsing data from website", e);
            }
            return data;
        }

        private void addData(String name, String price, String count, String imageURL) {
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
            String[] row = {name, countChange.toString(), priceChange.toString(), imageURL};
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
                            cv.put("IMAGE_URL", Abr[3]);
                            if(!Abr[2].equals(cursor.getString(3))) {
                                mDbCase.update(DATABASE_TABLE, cv, KEY_ID + "=" + cursor.getString(0), null);
                            }
                            break;
                        }
                    }

                    if (cursor.isAfterLast()) {
                        String new_Case = "INSERT INTO CaseInfo (name, count, price, image_url) VALUES" + "('" + Abr[0] + "', '" + Abr[1] + "', '" + Abr[2] + "', '" + Abr[3] + "')";
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

        // Удаление элементов, если count = 0
        while (!cursor.isAfterLast()) {
            if(cursor.getString(2).equals("0")) mDbCase.delete(DATABASE_TABLE, KEY_COUNT + "=" + cursor.getString(2), null);
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        //Удаление элементов с одинаковыми именами

        while (!cursor.isAfterLast()) {
            if(cursor.getString(1).contains(mDbCase.toString())) mDbCase.delete(DATABASE_TABLE, KEY_NAME + "=" + cursor.getString(1), null);
            cursor.moveToNext();
        }
        cursor.moveToFirst();

        // Добавление элементов в список для их вывода
        while (!cursor.isAfterLast()) {
            if(!cursor.getString(2).equals("0")) caseInfoBase.add(cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4));
            cursor.moveToNext();
        }

        cursor.close();
            final ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainPage.this,
                    R.layout.list_item_4column, R.id.Name, caseInfoBase) {
                @Override
                public View getView(int position, View convertView, ViewGroup parent) {
                    View view = super.getView(position, convertView, parent);

                    String[] itemParts = getItem(position).split(", ");

                    TextView nameView = view.findViewById(R.id.Name);
                    TextView countView = view.findViewById(R.id.Count);
                    TextView priceView = view.findViewById(R.id.Price);
                    ImageView imageView = view.findViewById(R.id.Image);

                    File f = new File(String.valueOf(Uri.parse(itemParts[3])));
                    nameView.setText(itemParts[0]);
                    countView.setText(itemParts[1]);
                    priceView.setText(itemParts[2]);
                    Picasso.get().load(itemParts[3]).into(imageView);

                    return view;
                }
            };
            Toast.makeText(this, "Данные обновились", Toast.LENGTH_SHORT).show();
            caseList.setAdapter(adapter);
        }
}