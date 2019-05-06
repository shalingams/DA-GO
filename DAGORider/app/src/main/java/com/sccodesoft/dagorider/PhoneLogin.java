package com.sccodesoft.dagorider;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.sccodesoft.dagorider.Common.Common;
import com.sccodesoft.dagorider.Home;
import com.sccodesoft.dagorider.Model.Rider;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
import dmax.dialog.SpotsDialog;

public class PhoneLogin extends AppCompatActivity {

    private Button sendCode;
    private MaterialEditText phoneNumber,verificationCode;
    private TextView txt94;
    private ImageView iconphone;

    SpotsDialog waitingDialog;

    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private FirebaseAuth mAuth;

    DatabaseReference users;

    private ProgressDialog loadingBar;

    //Firebase Storage
    FirebaseStorage firebaseStorage;
    StorageReference storageReference;
    String avatarUrl=null;

    CircleImageView image_upload;

    CheckBox tnc;

    Boolean agreedtnc=false;

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        sendCode = (Button)findViewById(R.id.btnSendVCode);
        phoneNumber = (MaterialEditText)findViewById(R.id.edtPhoneNumb);
        verificationCode = (MaterialEditText)findViewById(R.id.edtVerificationCode);
        txt94 = (TextView)findViewById(R.id.txt94);
        iconphone = (ImageView)findViewById(R.id.iconphone);
        tnc = (CheckBox)findViewById(R.id.chktnc);
        loadingBar = new ProgressDialog(this);

        waitingDialog = new SpotsDialog(PhoneLogin.this);

        firebaseStorage = FirebaseStorage.getInstance();
        storageReference = firebaseStorage.getReference();

        users = FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl);

        mAuth = FirebaseAuth.getInstance();

        tnc.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked)
                {
                    Toast.makeText(PhoneLogin.this, "Please Read the Terms & Conditions First..", Toast.LENGTH_SHORT).show();
                    showTermsConditions();
                }
                else
                {
                    agreedtnc = false;
                }
            }
        });

        sendCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(sendCode.getText().toString().equals("Send Verification Code")) {
                    String phoneNumb = "+94"+phoneNumber.getText().toString();

                    if (TextUtils.isEmpty(phoneNumber.getText().toString())) {
                        Toast.makeText(PhoneLogin.this, "Please Enter Your Phone Number..", Toast.LENGTH_SHORT).show();
                    }
                    else if (agreedtnc==false) {
                        Toast.makeText(PhoneLogin.this, "Please Read and Accept Terms & Conditions..", Toast.LENGTH_SHORT).show();
                        showTermsConditions();
                    } else {
                        sendVerification(phoneNumb);
                    }
                }
                else if(sendCode.getText().toString().equals("Verify"))
                {
                    String veificationCode = verificationCode.getText().toString();

                    if(TextUtils.isEmpty(veificationCode))
                    {
                        Toast.makeText(PhoneLogin.this, "Please Enter Verification Code..", Toast.LENGTH_SHORT).show();
                    }
                    else
                    {
                        loadingBar.setTitle("Code Verification");
                        loadingBar.setMessage("Please wait, We are verifying your code..");
                        loadingBar.setCanceledOnTouchOutside(false);
                        loadingBar.show();

                        PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, veificationCode);
                        signInWithPhoneAuthCredential(credential);
                    }
                }
            }
        });

        mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            @Override
            public void onVerificationCompleted(PhoneAuthCredential credential) {
                signInWithPhoneAuthCredential(credential);
            }

            @Override
            public void onVerificationFailed(FirebaseException e) {

                loadingBar.dismiss();

                Toast.makeText(PhoneLogin.this, "Invaild Phone Number.. Please Enter a Valid Phone Number..", Toast.LENGTH_SHORT).show();

                sendCode.setText("Send Verification Code");
                phoneNumber.setVisibility(View.VISIBLE);
                txt94.setVisibility(View.VISIBLE);
                iconphone.setVisibility(View.VISIBLE);
                verificationCode.setVisibility(View.GONE);
            }

            @Override
            public void onCodeSent(String verificationId,
                                   PhoneAuthProvider.ForceResendingToken token) {

                loadingBar.dismiss();

                mVerificationId = verificationId;
                mResendToken = token;

                tnc.setVisibility(View.GONE);

                Toast.makeText(PhoneLogin.this, "Verification Code Sent Successfully..", Toast.LENGTH_SHORT).show();

                sendCode.setText("Verify");
                phoneNumber.setVisibility(View.GONE);
                txt94.setVisibility(View.GONE);
                iconphone.setVisibility(View.GONE);
                verificationCode.setVisibility(View.VISIBLE);

            }
        };
    }

    private void showTermsConditions() {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);

        LayoutInflater inflater = LayoutInflater.from(this);
        View tnc_layout = inflater.inflate(R.layout.layout_tnc,null);

        dialog.setView(tnc_layout);

        dialog.setPositiveButton("ACCEPT", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                agreedtnc=true;
                tnc.setChecked(true);
                dialogInterface.dismiss();
            }
        });

        dialog.setNegativeButton("DECLINE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                agreedtnc=false;
                tnc.setChecked(false);
                dialogInterface.dismiss();
            }
        });

        dialog.show();

    }

    private void sendVerification(String phonenu) {

        loadingBar.setTitle("Phone Verification");
        loadingBar.setMessage("Please wait, We are authentication your phone..");
        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();

        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phonenu,        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                PhoneLogin.this,               // Activity (for callback binding)
                mCallbacks);        // OnVerificationStateChangedCallbacks

    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful())
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLogin.this, "You are Verified Successfully..", Toast.LENGTH_SHORT).show();

                            loginUser();
                        }
                        else
                        {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLogin.this, "Error : " + task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    private void loginUser() {
        FirebaseDatabase.getInstance().getReference(Common.user_rider_tbl)
                .child(FirebaseAuth.getInstance().getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if(dataSnapshot.exists()) {
                            Common.currentUser = dataSnapshot.getValue(Rider.class);
                            if(waitingDialog.isShowing())
                                waitingDialog.dismiss();
                            Intent intent = new Intent(PhoneLogin.this, Home.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                            finish();
                        }
                        else
                        {
                            if(waitingDialog.isShowing())
                                waitingDialog.dismiss();
                            showRegisterDialog();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(PhoneLogin.this, "Cancelled..", Toast.LENGTH_SHORT).show();
                    }
                });

    }

    private void showRegisterDialog() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("REGISTER");
        dialog.setMessage("Please fill details to register");

        LayoutInflater inflater = LayoutInflater.from(this);
        View resigter_layout = inflater.inflate(R.layout.layout_register,null);

        final MaterialEditText edtName = resigter_layout.findViewById(R.id.edtName);
        final MaterialEditText edtPhone = resigter_layout.findViewById(R.id.edtPhone);
        final MaterialEditText edtIntroduceCode = resigter_layout.findViewById(R.id.edtIntroduceCode);
        image_upload = resigter_layout.findViewById(R.id.image_upload);

        image_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                chooseImage();
            }
        });

        edtPhone.setText(phoneNumber.getText().toString());

        dialog.setView(resigter_layout);

        dialog.setPositiveButton("REGISTER", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int which) {
                dialogInterface.dismiss();

                if(TextUtils.isEmpty(edtName.getText().toString()))
                {
                    Toast.makeText(PhoneLogin.this, "Please Enter Your Name..", Toast.LENGTH_SHORT).show();
                    showRegisterDialog();
                    return;
                }
                else if(avatarUrl==null)
                {
                    Toast.makeText(PhoneLogin.this, "Please Select Profile Image..", Toast.LENGTH_SHORT).show();
                    showRegisterDialog();
                    return;
                }
                else
                {
                    waitingDialog.show();

                    Rider user = new Rider();
                    user.setIntroduceCode(edtIntroduceCode.getText().toString());
                    user.setMyCode(phoneNumber.getText().toString());
                    user.setName(edtName.getText().toString());
                    user.setPhone("+94"+phoneNumber.getText().toString());
                    user.setAvatarUrl(avatarUrl);
                    user.setRates("0.0");

                    users.child(mAuth.getCurrentUser().getUid())
                            .setValue(user)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Toast.makeText(PhoneLogin.this, "You have Registered Successfully..", Toast.LENGTH_SHORT).show();

                                    loginUser();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    waitingDialog.dismiss();
                                    Toast.makeText(PhoneLogin.this, "Error : "+e.getMessage(), Toast.LENGTH_SHORT).show();
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
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Profile Image : "),Common.PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Common.PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null)
        {
            Uri saveUri = data.getData();
            if(saveUri != null)
            {
                CropImage.activity(saveUri)
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .setAspectRatio(1,1)
                        .start(this);
            }
        }


        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);

            if(resultCode == RESULT_OK)
            {
                Uri uri = result.getUri();
                image_upload.setImageURI(uri);

                final ProgressDialog mDialog = new ProgressDialog(this);
                mDialog.setMessage("Uploading..");
                mDialog.show();

                String imageName = FirebaseAuth.getInstance().getCurrentUser().getUid().toString()+UUID.randomUUID().toString();
                final StorageReference imageFolder = storageReference.child("riderImages/"+imageName+".jpg");
                imageFolder.putFile(uri)
                        .addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                if(task.isSuccessful())
                                {
                                    mDialog.dismiss();
                                    Toast.makeText(PhoneLogin.this, "Image Uploaded..", Toast.LENGTH_SHORT).show();


                                    imageFolder.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            avatarUrl = uri.toString();
                                        }
                                    });
                                }
                                else
                                {
                                    Toast.makeText(PhoneLogin.this, "Error Occured : "+task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }

                            }
                        })
                        .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                                double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                                mDialog.setMessage("Uploading..");
                            }
                        });
            }
        }

    }
}
