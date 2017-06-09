package com.am.inventory;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.am.inventory.data.ProductContract;
import com.am.inventory.data.ProductDbHelper;

public class MainActivity extends AppCompatActivity {

    private ProductDbHelper mProductDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mProductDbHelper = new ProductDbHelper(this);

        // get instance of {@link ProductCursorAdapter} and set it as adapter to listvie
        ProductCursorAdapter adapter = new ProductCursorAdapter(this, null);
        ListView listView = (ListView) findViewById(R.id.listview);
        listView.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem){
        switch (menuItem.getItemId()){
            case R.id.dummy_data:
                //insert dummy data
                insertDataIntoDatabase();
                return true;
            case R.id.delete:
                // delete product data
                return true;
            default:
                 return super.onOptionsItemSelected(menuItem);
        }

    }

    private void insertDataIntoDatabase(){

        SQLiteDatabase db = mProductDbHelper.getWritableDatabase();

        ContentValues values = new ContentValues();

        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_NAME,"water");
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_PRICE, 0.19);
        values.put(ProductContract.ProductEntry.COLUMN_PRODUCT_QUANTITY, 6);

        long newRowId = db.insert(ProductContract.ProductEntry.TABLE_NAME, null, values);

        if (newRowId == -1){
            Toast.makeText(this, "Error in inserting dummy data", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Dummy data is added", Toast.LENGTH_SHORT).show();
        }

    }

    private void displayDbInfo(){

        mProductDbHelper = new ProductDbHelper(this);
        SQLiteDatabase db =  mProductDbHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM " + ProductContract.ProductEntry.TABLE_NAME,null);

        try{


        }finally {
            cursor.close();
        }

    }


}
