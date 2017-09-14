package com.example.kamran.sigeupandroid.activity;

/**
 * Created by jaimechizavane on 8/1/17.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;



import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

//import com.example.kamran.sigeupandroid.R;
import com.android.volley.toolbox.Volley;
import com.example.kamran.navigationdrawer.R;
import com.example.kamran.sigeupandroid.app.AppConfig;
import com.example.kamran.sigeupandroid.app.AppController;
import com.example.kamran.sigeupandroid.helper.SQLiteHandler;
import com.example.kamran.sigeupandroid.helper.SessionManager;
import com.example.kamran.sigeupandroid.MainActivity;



public class LoginActivity extends Activity {

    private static final String TAG = MainActivity.class.getSimpleName();

    private Button btnLogin;

    private EditText inputUserID;
    private EditText inputPassword;

    private TextView studantName;
    private TextView studantEmail;

    private Button btnClear;

    private ProgressDialog pDialog;
    private SessionManager session;
    private SQLiteHandler db;

    //private RequestQueue requestQueue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_activity);

        inputUserID = (EditText) findViewById(R.id.editText_userid);
        inputPassword = (EditText) findViewById(R.id.passwordLogin);
        btnLogin = (Button) findViewById(R.id.buttonAuthenticar);
        btnClear = (Button) findViewById(R.id.buttonClear);
        studantName = (TextView) findViewById(R.id.studentNametextView);
        studantEmail = (TextView) findViewById(R.id.studentEmailtextView);

        // Progress dialog
        pDialog = new ProgressDialog(this);
        pDialog.setCancelable(false);

        // SQLite database handler
        db = new SQLiteHandler(getApplicationContext());

        // Session manager
        session = new SessionManager(getApplicationContext());

        // Check if user is already logged in or not
        if (session.isLoggedIn()) {
            // User is already logged in. Take him to main activity
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }

        // Login button Click Event
        btnLogin.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                String username = inputUserID.getText().toString().trim();
                String password = inputPassword.getText().toString().trim();

                        // Check for empty data in the form
                        if (!username.isEmpty() && !password.isEmpty()) {

                            //Just testing if the user parsing is correct
                            Toast.makeText(getApplicationContext(), username, Toast.LENGTH_LONG);


                            // login user
                            checkLogin(username /*, password*/);

                            //Here I' just checking the second method attempt
                            checkLogin1(username);

                            /*String name = "Jaime Chizavane";
                            String email = "jaime.jcm@gmail.com";

                            studantName.setText(name);
                            studantEmail.setText(email);*/


                            Intent intent = new Intent(LoginActivity.this,
                                    MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            // Prompt user to enter credentials
                            Toast.makeText(getApplicationContext(),
                                    "Por favor insira as credenciais, certifique que elas estao correctas!", Toast.LENGTH_LONG)
                                    .show();
                        }
            }

        });

        // Clear button Click Event
        btnClear.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                inputUserID.setText("");
                inputPassword.setText("");

            }

        });
    }

    private void checkLogin1(String username){
        //Here I'm starting a new Request Queue
        RequestQueue requestQueue = Volley.newRequestQueue(this);

        //Parsing the username in a JsonObjectRequest
        String url = AppConfig.URL_USUARIO+username;

        // prepare the Request
        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // display response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //Well in the tutorial it was written response (what doesnt make sense) so i took the freedom to change to error.toString()
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        // add it to the RequestQueue
        requestQueue.add(getRequest);

    }

    private void checkLogin(final String username /* , final String password*/) {



        // Tag used to cancel the request
        String tag_string_req = "req_login";
        final String result = "estudante";

        pDialog.setMessage("Entrando ...");
        showDialog();

        StringRequest strReq = new StringRequest(Method.GET,
                AppConfig.URL_USUARIO+username, new Response.Listener<String>() {



            @Override
            public void onResponse(String response) {
                Log.d(TAG, "Login Response: " + toString());
                hideDialog();



                try
                {
                    JSONObject jObj = new JSONObject();

                    List<String> listobject = new ArrayList<String>();

                    //listobject = <String> jObj;

                    JSONArray cast = jObj.getJSONArray(result);

                    // Check for error node in json
                    if (cast.length() > 0) {
                        // user successfully logged in
                        // Create login session
                        session.setLogin(true);


                        jObj = cast.getJSONObject(1);

                        String username = jObj.getString("username");
                        String email = jObj.getString("email");
                        String uid = jObj.getString("id");
                        String name = jObj.getString("nome");
                        String birthDate = jObj.getString("data_nascimento");
                        String gender = jObj.getString("genero");
                        String profile = jObj.getString("perfil");
                        String confirmation = jObj.getString("confirmacao");
                        String token = jObj.getString("token");

                        // Inserting row in users table
                        db.addUser(uid, name, email, username, birthDate, gender, profile, confirmation, token);


                        //Trying to pass String to Main Menu activity
                        studantName.setText(name);
                        studantEmail.setText(email);

                        //Toast.makeText(getApplicationContext(), name + email, Toast.LENGTH_LONG).show();


                        // Launch main activity
                        Intent intent = new Intent(LoginActivity.this,
                                MainActivity.class);
                        startActivity(intent);
                        finish();
                    } else {
                        // Error in login. Get the error message
                        String errorMsg = "Objecto não encontrado";
                        Toast.makeText(getApplicationContext(),
                                errorMsg, Toast.LENGTH_LONG).show();
                    }

                    } catch (JSONException e) {
                    // JSON error
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Json error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }



            }
        }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e(TAG, "Login Error: " + error.getMessage());
                Toast.makeText(getApplicationContext(),
                        error.getMessage(), Toast.LENGTH_LONG).show();
                hideDialog();
            }
        }) {

            @Override
            protected Map<String, String> getParams() {
                // Posting parameters to login url
                Map<String, String> params = new HashMap<String, String>();
                params.put("username", username);
                //params.put("password", password);

                return params;
            }

        };

        // Adding request to request queue
        AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
    }



    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }


}
