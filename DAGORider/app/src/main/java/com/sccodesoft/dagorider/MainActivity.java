package com.sccodesoft.dagorider;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Model.Rider;

import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class MainActivity extends AppCompatActivity {

    Button btnContinue;
    RelativeLayout rootLayout;

    FirebaseAuth auth;
    FirebaseDatabase db;
    DatabaseReference users;

    TextView txtForgotPwd;

    FirebaseUser currentuser;

    SpotsDialog waitingDialog;

   /* @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }*/

    @Override
    protected void onStart() {
        super.onStart();

        if(currentuser != null)
        {
            waitingDialog = new SpotsDialog(MainActivity.this);
            waitingDialog.show();

            btnContinue.setEnabled(false);
            loginUser();
        }
    }

    private void loginUser() {
        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Common.currentUser = dataSnapshot.getValue(Rider.class);

                            startActivity(new Intent(MainActivity.this, Home.class));
                            waitingDialog.dismiss();
                            finish();
                        }
                        else
                        {
                            btnContinue.setEnabled(true);
                            waitingDialog.dismiss();
                            Toast.makeText(MainActivity.this, "Click On Continue..", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        waitingDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Cancelled..", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       /* CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/Arkhip_font.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build());*/
        setContentView(R.layout.activity_main);


        auth = FirebaseAuth.getInstance();
        db = FirebaseDatabase.getInstance();
        users = db.getReference(Common.user_rider_tbl);

        currentuser = FirebaseAuth.getInstance().getCurrentUser();

        btnContinue = (Button)findViewById(R.id.btnContinue);

        rootLayout = (RelativeLayout)findViewById(R.id.rootLayout);

        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signInWithPhone();
            }
        });
    }

    private void signInWithPhone() {
        Intent intent = new Intent(MainActivity.this,PhoneLogin.class);
        startActivity(intent);
    }

   /* private void autoLogin(String user, String pwd) {
        final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
        waitingDialog.show();
        btnSignIn.setEnabled(false);

        auth.signInWithEmailAndPassword(user,pwd)
                .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {

                        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                        if(dataSnapshot.exists()) {
                                            Common.currentUser = dataSnapshot.getValue(Rider.class);
                                            waitingDialog.dismiss();
                                            startActivity(new Intent(MainActivity.this,Home.class));
                                            finish();
                                        }
                                        else
                                        {
                                            Snackbar.make(rootLayout,"You Are Using Invaild Credentials..",Snackbar.LENGTH_SHORT).show();
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError databaseError) {

                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        waitingDialog.dismiss();
                        Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                        btnSignIn.setEnabled(true);
                    }
                });
    }
*/
    /*private void showDialogForgotPwd() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle("RESET PASSWORD");
        alertDialog.setMessage("Please enter your email address");

        LayoutInflater inflater = LayoutInflater.from(this);
        View forgot_password_layout = inflater.inflate(R.layout.layout_forgot_password,null);

        final MaterialEditText edtEmail = forgot_password_layout.findViewById(R.id.edtEmail);
        alertDialog.setView(forgot_password_layout);

        alertDialog.setPositiveButton("RESET", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                waitingDialog.show();

                auth.sendPasswordResetEmail(edtEmail.getText().toString().trim())
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout,"Password Reset Link Sent To Your E-mail",Snackbar.LENGTH_LONG).show();

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                dialog.dismiss();
                                waitingDialog.dismiss();

                                Snackbar.make(rootLayout,e.getMessage(),Snackbar.LENGTH_LONG).show();

                            }
                        });
            }
        });

        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        alertDialog.show();
    }

    private void showLoginDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("SIGN IN");
        dialog.setMessage("Please use email to sign in");

        LayoutInflater inflater = LayoutInflater.from(this);
        View login_layout = inflater.inflate(R.layout.layout_login,null);

        final MaterialEditText edtEmail = login_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = login_layout.findViewById(R.id.edtPassword);

        dialog.setView(login_layout);

        dialog.setPositiveButton("SIGN IN", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty(edtEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter Email Address",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edtPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter Password",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(edtPassword.getText().toString().length()<6)
                {
                    Snackbar.make(rootLayout,"Password Not Correct !!",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                    waitingDialog.show();
                    btnSignIn.setEnabled(false);

                    auth.signInWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {

                                    FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                                            .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                                            .addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                                    if(dataSnapshot.exists()) {
                                                        Common.currentUser = dataSnapshot.getValue(Rider.class);

                                                        Paper.book().write(Common.user_field,edtEmail.getText().toString());
                                                        Paper.book().write(Common.pwd_field,edtPassword.getText().toString());

                                                        waitingDialog.dismiss();
                                                        startActivity(new Intent(MainActivity.this,Home.class));
                                                        finish();
                                                    }
                                                    else
                                                    {
                                                        Snackbar.make(rootLayout,"You Are Using Invaild Credentials..",Snackbar.LENGTH_SHORT).show();
                                                    }
                                                }

                                                @Override
                                                public void onCancelled(@NonNull DatabaseError databaseError) {

                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                    Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                    btnSignIn.setEnabled(true);
                                }
                            });
                }
            }
        });

        dialog.setNegativeButton("CANCEL",   new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER ");
        dialog.setMessage("Please use email to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View resigter_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtEmail = resigter_layout.findViewById(R.id.edtEmail);
        final MaterialEditText edtPassword = resigter_layout.findViewById(R.id.edtPassword);
        final MaterialEditText edtconfirmPassword = resigter_layout.findViewById(R.id.edtConfirmPassword);
        final MaterialEditText edtName = resigter_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = resigter_layout.findViewById(R.id.edtPhone);

        dialog.setView(resigter_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty(edtEmail.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter Email Address",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edtPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter A Password",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(edtPassword.getText().toString().length()<6)
                {
                    Snackbar.make(rootLayout,"Password Too Short !! Use At Least 6 Characters",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edtconfirmPassword.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Confirm Your Password",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if((edtconfirmPassword.getText().toString()).equals(edtPassword.getText().toString())==false)
                {
                    Snackbar.make(rootLayout,"Confirm Password Doesn't Match",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else if(TextUtils.isEmpty(edtPhone.getText().toString()))
                {
                    Snackbar.make(rootLayout,"Please Enter Phone Number",Snackbar.LENGTH_SHORT).show();
                    return;
                }
                else
                {
                    final SpotsDialog waitingDialog = new SpotsDialog(MainActivity.this);
                    waitingDialog.show();

                    auth.createUserWithEmailAndPassword(edtEmail.getText().toString(),edtPassword.getText().toString())
                            .addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                                @Override
                                public void onSuccess(AuthResult authResult) {
                                    Rider rider = new Rider();
                                    rider.setEmail(edtEmail.getText().toString());
                                    rider.setName(edtName.getText().toString());
                                    rider.setPhone(edtPhone.getText().toString());
                                    rider.setPassword(edtPassword.getText().toString());
                                    rider.setAvatarUrl("");
                                    rider.setRates("0");

                                    users.child(auth.getCurrentUser().getUid())
                                            .setValue(rider)
                                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void aVoid) {
                                                    waitingDialog.dismiss();
                                                    Snackbar.make(rootLayout,"User Registered Successfully !!",Snackbar.LENGTH_SHORT).show();
                                                }
                                            })
                                            .addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    waitingDialog.dismiss();
                                                    Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                                }
                                            });
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                    Snackbar.make(rootLayout,"Failed "+e.getMessage(),Snackbar.LENGTH_SHORT).show();
                                }
                            });
                }
            }
        });

        dialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();
            }
        });

        dialog.show();
    }*/


}
