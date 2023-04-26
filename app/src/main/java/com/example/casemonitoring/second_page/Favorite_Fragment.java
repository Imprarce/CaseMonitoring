package com.example.casemonitoring.second_page;

import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.casemonitoring.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class Favorite_Fragment extends Fragment {

    private static List<String> caseName = new ArrayList<>();

    private List<String> listItems = new ArrayList<>();

    private List<String> list_for_adapter = new ArrayList<>();
    private ImageView exit;
    private FavoriteFragmentListener listener;

    private Spinner spinner;

    private Base_Case mDBHelperCase;
    private SQLiteDatabase mDbCase;

    private Button delete_item;

    private Button add_item;

    private ListView caseList_Favorite;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference myRef = database.getReference("my_data");

    private String userId = mAuth.getCurrentUser().getUid();


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorite, container, false);

        mDBHelperCase = new Base_Case(getContext());

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

        exit = view.findViewById(R.id.exit_from_fragment);

        spinner = view.findViewById(R.id.spinner);

        caseList_Favorite = view.findViewById(R.id.caseList_favorite);

        delete_item = view.findViewById(R.id.delete);

        add_item = view.findViewById(R.id.add);

        spinnerAdd();
        fromDataBase();

        exit.setOnClickListener(v -> {
            if (listener != null) {

                listener.onExitClicked();
            }
        });

        delete_item.setOnClickListener(v -> {
            deleteItem();
        });

        add_item.setOnClickListener(v -> {
            addItem();
        });

        return view;
    }

    protected void spinnerAdd(){
        Cursor cursor = mDbCase.rawQuery("SELECT * FROM CaseInfo", null);
        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            if(!cursor.getString(2).equals("0")) caseName.add(cursor.getString(1));
            cursor.moveToNext();
        }
        cursor.close();

        final ArrayAdapter<String> adapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, caseName);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    protected void deleteItem(){
        String selectedItem = spinner.getSelectedItem().toString();

        int position = 0;

        while(position < listItems.size()) {
            String[] itemParts = listItems.get(position).split(", ");
            if (itemParts[0].contains(selectedItem)) {
                listItems.remove(position);
                listUpdate();
                break;
            } else position++;
        }
    }

    protected void addItem(){
        String selectedItem = spinner.getSelectedItem().toString();

        Cursor cursor = mDbCase.rawQuery("SELECT * FROM CaseInfo", null);
        cursor.moveToFirst();
        int position = 0;

        while(position < listItems.size()) {
            String[] itemParts = listItems.get(position).split(", ");
            if (itemParts[0].contains(selectedItem)) {
                cursor.moveToLast();
                break;
            } else position++;
        }

        position = 0;

        while (!cursor.isAfterLast()) {
            if(cursor.getString(1).equals(selectedItem)){
                if(listItems.size() > 0) {
                    String[] itemParts = listItems.get(position).split(", ");
                    if (itemParts[0].contains(selectedItem)) {
                        break;
                    } else if(position+1 < listItems.size()) {
                        position++;
                    }
                    else{
                        listItems.add(cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4));
                        listUpdate();
                        break;
                    }
                } else {
                    listItems.add(cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4));
                    listUpdate();
                    break;
                }
            } else cursor.moveToNext();
        }
        cursor.close();
    }

    protected void fromDataBase(){
        myRef.child(userId).child("case_list_favorite").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    String caseName =  snapshot.getValue(String.class);
                    if(list_for_adapter.contains(caseName)){
                        myRef.child(userId).child("case_list_favorite").child(snapshot.getKey()).removeValue();
                    } else list_for_adapter.add(caseName);
                }
                if(listItems.size() == 0) listItems = list_for_adapter;
                addFromDataBase();
            }
            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    protected void addFromDataBase(){
        Cursor cursor = mDbCase.rawQuery("SELECT * FROM CaseInfo", null);
        cursor.moveToFirst();
        int position = 0;

        while (!cursor.isAfterLast()) {
            if(cursor.getString(1).equals(listItems.get(position))){
                if(listItems.size() > 0) {
                    {
                        listItems.set(position,(cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4)));
                        if(position + 1 < listItems.size()) {
                            position++;
                            cursor.moveToFirst();
                        } else break;
                    }
                } else {
                    listItems.add(cursor.getString(1) + ", " + cursor.getString(2) + ", " + cursor.getString(3) + ", " + cursor.getString(4));
                    if(position + 1 < listItems.size()) {
                        position++;
                        cursor.moveToFirst();
                    } else break;
                }
            } else {
                cursor.moveToNext();
            }
        }
        cursor.close();
        listUpdate();
    }

    protected void listUpdate(){

        final ArrayAdapter<String> listAdapter = new ArrayAdapter<String>(getContext(),
                R.layout.list_item_4column, R.id.Name, listItems) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);

                String[] itemParts = getItem(position).split(", ");


                TextView nameView = view.findViewById(R.id.Name);
                TextView countView = view.findViewById(R.id.Count);
                TextView priceView = view.findViewById(R.id.Price);
                ImageView imageView = view.findViewById(R.id.Image);

                nameView.setText(itemParts[0]);
                countView.setText(itemParts[1]);
                priceView.setText(itemParts[2]);
                Picasso.get().load(itemParts[3]).into(imageView);

                return view;
            }
        };

        for(int i = 0; i < listItems.size(); i++){
            String[] itemParts = listItems.get(i).split(", ");
            myRef.child(userId).child("case_list_favorite").child("" + i).setValue(itemParts[0]);
        }

        caseList_Favorite.setAdapter(listAdapter);
    }

            public void setListener(FavoriteFragmentListener listener) {this.listener = listener;}
}