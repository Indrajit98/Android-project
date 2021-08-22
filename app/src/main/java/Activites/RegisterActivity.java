package Activites;

import androidx.appcompat.app.AppCompatActivity;
import androidx.preference.PreferenceManager;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.indrajit.Donor_Hub.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import Utils.Endpoints;
import Utils.VolleySingleton;

public class RegisterActivity extends AppCompatActivity {
    private EditText nameEt, cityEt, blood_GroupEt, passwordEt, mobileEt;
    private Button submitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        nameEt = findViewById(R.id.name);
        cityEt = findViewById(R.id.city);
        blood_GroupEt = findViewById(R.id.blood_group);
        passwordEt = findViewById(R.id.password);
        mobileEt = findViewById(R.id.number);
        submitButton = findViewById(R.id.submit_button);
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String name, city, blood_group, password, mobile;
                name = nameEt.getText().toString();
                city = cityEt.getText().toString();
                blood_group = blood_GroupEt.getText().toString();
                password = passwordEt.getText().toString();
                mobile = mobileEt.getText().toString();
               // showMessage(name+"\n"+city+"\n"+blood_group+"\n"+password+"\n"+mobile);
                if (isValid(name, city, blood_group, password, mobile)){
                    register(name, city, blood_group, password, mobile);
                }
            }
        });

    }
    private void register(final String name, final String city, final String blood_group, final String password, final String mobile){
        StringRequest stringRequest = new StringRequest(Request.Method.POST, Endpoints.register_url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                if (response.equals("Success")){
                    PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putString("city",city).apply();
                    Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(RegisterActivity.this,MainActivity.class));
                    RegisterActivity.this.finish();
                }else {
                    Toast.makeText(RegisterActivity.this, response, Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(RegisterActivity.this, "Something went wrong:", Toast.LENGTH_SHORT).show();
                Log.d("VOLLEY",error.getMessage());
            }
        }){
            @Override
            protected Map<String, String> getParams() throws AuthFailureError{
                Map<String, String> params = new HashMap<>();
                params.put("name", name);
                params.put("city", city);
                params.put("blood_group", blood_group);
                params.put("password", password);
                params.put("number", mobile);
                return params;
            }
        };
        VolleySingleton.getInstance(this).addToRequestQueue(stringRequest);
    }


    private boolean isValid(String name, String city, String blood_group, String password, String mobile){
        List<String> valid_blood_group = new ArrayList<>();
        valid_blood_group.add("A+");
        valid_blood_group.add("A-");
        valid_blood_group.add("B+");
        valid_blood_group.add("B-");
        valid_blood_group.add("AB+");
        valid_blood_group.add("AB-");
        valid_blood_group.add("O+");
        valid_blood_group.add("O-");
        if (name.isEmpty()){
            showMessage("Name is empty");
            return false;
        }else if (city.isEmpty()){
            showMessage("City name is required");
            return false;
        }else if (!valid_blood_group.contains(blood_group)){
            showMessage("Blood group invalid choose from "+valid_blood_group);
            return false;
        }else if (mobile.length() !=11){
            showMessage("Invalid mobile number,number should be of 11 digits");
            return false;
        }
        return true;
    }

    private void showMessage(String msg){
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }
}