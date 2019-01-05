package point.dot.carpoint;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.api.Api;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Query;

//this class used data from API in order to get predicted price. We used Retrofit library to build the request
public class CarDetailsPrice extends AppCompatActivity {

    Button checkPrice, checkPriceNext;
    EditText year, mileage, city, make, model, price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details_price);

        checkPrice = (Button)findViewById(R.id.CheckPrice);
        checkPriceNext = (Button)findViewById(R.id.PriceNextButton);

        year = (EditText)findViewById(R.id.CarYearEditText);
        mileage = (EditText)findViewById(R.id.CarMileAgeEditText);
        city = (EditText)findViewById(R.id.CityEditText);
        make = (EditText)findViewById(R.id.MakeEditText);
        model = (EditText)findViewById(R.id.ModelEditText);
        price = (EditText)findViewById(R.id.PriceEditText);

        checkPrice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Retrofit retrofit = new Retrofit.Builder()

                        // url of our API. The results can be checked using Postman
                        .baseUrl("http://34.245.87.185:5000/")
                        .build();

                Api api = retrofit.create(Api.class);

                //description of runbyall is beloww.
                api.runbyall(year.getText().toString(), mileage.getText().toString(),city.getText().toString(),make.getText().toString(),model.getText().toString()).enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try{
                            Log.d("RetrofitCheckInLOGS", response.body().string());
                            Toast.makeText(getApplicationContext(), "Price: "+ response.body().toString(), Toast.LENGTH_LONG).show();

                        } catch (IOException e){
                            e.printStackTrace();
                        }
                    }
                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {

                    }
                });
            }
        });

        checkPriceNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(CarDetailsPrice.this, FinalWindow.class);
                startActivity(intent);
            }
        });
    }

    interface Api{
        @GET("/predict")
        Call<ResponseBody>getIp();

        @GET("/predict")
        Call<ResponseBody>postBody(@Body RequestBody requestBody);

        //we used this GET request with multiple parameters in order to build URL
        @GET("/predict")
        Call<ResponseBody> runbyall (@Query("Year") String Year,
                                     @Query("Mileage") String Mileage,
                                     @Query("City") String City,
                                     @Query("Make") String Make,
                                     @Query("Model") String Model);
    }
}
