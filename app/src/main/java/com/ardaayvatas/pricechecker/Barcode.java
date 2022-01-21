package com.ardaayvatas.pricechecker;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

public class Barcode extends AppCompatActivity {

    TextView product;
    String barcodeNum;
    String productID;
    int arrayLength;
    String title;
    String price;
    String image;
    String merchant;
    ImageView imageView;
    String link;
    ListView listView;


    ArrayList<HashMap<String,String>> productList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode);
        product = findViewById(R.id.productName);
        listView = findViewById(R.id.lv);
        Intent intent = getIntent();
        barcodeNum = intent.getStringExtra("barcodeNumber");
        imageView = findViewById(R.id.img);
        //product.setText(barcodeNum);
        productList = new ArrayList<>();

        new JsonTask().execute("https://api.scaleserp.com/search?api_key=YOUR_API_KEY_HERE&q="+barcodeNum+"&google_domain=google.com.tr&location=Istanbul,Istanbul,Turkey&gl=tr&hl=tr&search_type=shopping");
    }


    private class JsonTask extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try
            {
                arrayLength = new JSONObject(result).getJSONArray("shopping_results").length();
                System.out.println(arrayLength);
                for(int i=0; i<arrayLength; i++)
                {
                    JSONObject jsonObject = new JSONObject(result).getJSONArray("shopping_results").getJSONObject(i);
                    if (jsonObject.has("merchant_count") && !jsonObject.isNull("merchant_count"))
                    {
                        productID = jsonObject.getString("id");
                        System.out.println(jsonObject.getInt("merchant_count"));
                        new JsonTaskProduct().execute("https://api.scaleserp.com/search?api_key=YOUR_API_KEY_HERE&google_domain=google.com.tr&location=Turkey&gl=tr&hl=tr&search_type=product&product_id="+productID);
                        break;
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }


    private class JsonTaskProduct extends AsyncTask<String, String, String> {

        protected void onPreExecute() {
            super.onPreExecute();

        }

        protected String doInBackground(String... params) {


            HttpURLConnection connection = null;
            BufferedReader reader = null;

            try {
                URL url = new URL(params[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();


                InputStream stream = connection.getInputStream();

                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";

                while ((line = reader.readLine()) != null) {
                    buffer.append(line+"\n");

                }

                return buffer.toString();


            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            try
            {
                arrayLength = new JSONObject(result).getJSONObject("product_results").getJSONArray("sellers_online").length();
                JSONObject general = new JSONObject(result).getJSONObject("product_results");
                if (general.has("title")&& !general.isNull("title") && general.has("primary_image") && !general.isNull("primary_image"))
                {
                    title = general.getString("title");
                    image = general.getString("primary_image");
                    product.setText(title);
                    Glide.with(Barcode.this).load(image).into(imageView);
                }

                System.out.println(arrayLength);
                for(int i=0; i<arrayLength; i++)
                {
                    JSONObject jsonObject = new JSONObject(result).getJSONObject("product_results").getJSONArray("sellers_online").getJSONObject(i);
                    if (jsonObject.has("merchant") && !jsonObject.isNull("merchant") && jsonObject.has("total_price") && !jsonObject.isNull("total_price") )
                    {
                        HashMap<String,String> products = new HashMap<>();
                        price = jsonObject.getString("total_price");
                        merchant = jsonObject.getString("merchant");
                        System.out.println("Merchant: "+ merchant);
                        products.put("merchant",merchant);
                        System.out.println("Price: "+ price);
                        products.put("price",price);
                        if (jsonObject.has("link") && !jsonObject.isNull("link"))
                        {
                            link = jsonObject.getString("link");
                            products.put("link",link);
                        }
                        productList.add(products);
                    }
                }

            } catch (JSONException e) {
                e.printStackTrace();
            }

            ListAdapter listAdapter = new SimpleAdapter(Barcode.this,productList,R.layout.product_layout,
                    new String[]{"merchant","price"},
                    new int[]{R.id.productMerchantTv, R.id.priceTv});
            listView.setAdapter(listAdapter);

            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    startActivity( new Intent(Intent.ACTION_VIEW, Uri.parse(productList.get(position).get("link"))));
                }
            });

        }
    }



    public void goScan(View view)
    {
        Intent intent = new Intent(this,Scan.class);
        startActivity(intent);
    }

    public void goMain(View view)
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
    }
}