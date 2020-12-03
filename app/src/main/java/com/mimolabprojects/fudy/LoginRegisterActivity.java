package com.mimolabprojects.fudy;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.IdpResponse;
import com.firebase.ui.auth.data.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;
import com.mimolabprojects.fudy.Common.Common;
import com.mimolabprojects.fudy.Model.TokenModel;
import com.mimolabprojects.fudy.Model.UserModel;
import com.mimolabprojects.fudy.Retrofit.ICloudFunctions;
import com.mimolabprojects.fudy.Retrofit.IFCMService;
import com.mimolabprojects.fudy.Retrofit.RetrofitCloudClient;
import com.mimolabprojects.fudy.Retrofit.RetrofitFCMClient;

import java.util.Arrays;
import java.util.List;

import dmax.dialog.SpotsDialog;
import io.reactivex.disposables.CompositeDisposable;

public class LoginRegisterActivity extends AppCompatActivity {

    private static int APP_REQUEST_CODE = 7070;
    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener listener;
    private AlertDialog dialog;
    private CompositeDisposable compositeDisposable = new CompositeDisposable();
    private ICloudFunctions cloudFunctions;

    private DatabaseReference userRef;
    private List<AuthUI.IdpConfig> providers;






    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(listener);
    }

    @Override
    protected void onStop() {
        if (listener !=null)
            firebaseAuth.removeAuthStateListener(listener);
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_register);

        init();


    }

    private void init() {
        providers = Arrays.asList(new AuthUI.IdpConfig.PhoneBuilder().build());

        userRef = FirebaseDatabase.getInstance().getReference(Common.USER_REFERENCE);
        firebaseAuth = FirebaseAuth.getInstance();
        dialog = new SpotsDialog.Builder().setCancelable(false).setContext(this).build();
        listener = firebaseAuth -> {

            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {

                            FirebaseUser user = firebaseAuth.getCurrentUser();
                            if (user != null)
                            {
                                //USer alreadyLogged in
                                checkUserFromFirebase(user);

                            }
                            else {
                                phoneLogin();
                            }

                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {

                            Toast.makeText(LoginRegisterActivity.this, "You must enable this permission to use this app", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                        }
                    }).check();
        };

    }



    private void checkUserFromFirebase(FirebaseUser user) {
        dialog.show();
        userRef.child(user.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        if (dataSnapshot.exists()){
                            UserModel userModel = dataSnapshot.getValue(UserModel.class);
                            goToHomeActivity(userModel);
                        }else{
                            showRegisterDialog(user);
                        }
                        dialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        Toast.makeText(LoginRegisterActivity.this, ""+databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });



        
    }


    private void showRegisterDialog(FirebaseUser user) {

        androidx.appcompat.app.AlertDialog.Builder builder = new androidx.appcompat.app.AlertDialog.Builder(this);
        builder.setTitle("Registering");
        builder.setMessage("Please Fill in The Information");

        View itemView  = LayoutInflater.from(this).inflate(R.layout.layout_register, null);
        EditText edt_name = (EditText) itemView.findViewById(R.id.edt_name);
        EditText edt_phone = (EditText) itemView.findViewById(R.id.edt_phone);
        EditText edt_address = (EditText) itemView.findViewById(R.id.edt_address);

        //SET USER DATA
        edt_phone.setText(user.getPhoneNumber());

        builder.setView(itemView);
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });

        builder.setPositiveButton("Register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if (TextUtils.isEmpty(edt_name.getText().toString())){
                    Toast.makeText(LoginRegisterActivity.this, "PLease Enter Your Name", Toast.LENGTH_SHORT).show();
                    return;
                }

                else if (TextUtils.isEmpty(edt_address.getText().toString())){
                    Toast.makeText(LoginRegisterActivity.this, "PLease Enter Your Address", Toast.LENGTH_SHORT).show();
                    return;
                }

                UserModel userModel = new UserModel();
                TokenModel token = new TokenModel();
                userModel.setUid(user.getUid());
                userModel.setName( edt_name.getText().toString());
                userModel.setAddress( edt_address.getText().toString());
                userModel.setPhone( edt_phone.getText().toString());

                userRef.child(user.getUid())
                        .setValue(userModel)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                dialog.dismiss();
                                Toast.makeText(LoginRegisterActivity.this, "Congratulations ! Registered Successfully", Toast.LENGTH_SHORT).show();
                                goToHomeActivity(userModel);

                            }
                        });////////////////////////////////////////////////////////////////////////////////////////////////////////////

            }
        });

        androidx.appcompat.app.AlertDialog dialog = builder.create();
        dialog.show();

    }

    private void goToHomeActivity(UserModel userModel) {
///Update Token On App Load
        FirebaseInstanceId.getInstance()
                .getInstanceId()
                .addOnFailureListener(e -> {
                    Toast.makeText(this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();

                    Common.currentUser = userModel;
                    startActivity(new Intent(LoginRegisterActivity.this, Homectivity.class));
                    finish();

                })
                .addOnCompleteListener(task -> {
                    Common.currentUser = userModel;
                    Common.updateToken(LoginRegisterActivity.this, task.getResult().getToken());
                    startActivity(new Intent(LoginRegisterActivity.this, Homectivity.class));
                    finish();

                });


    }
    private void phoneLogin() {
        startActivityForResult(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build(), APP_REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE)
        {
            IdpResponse response = IdpResponse.fromResultIntent(data);
            if (resultCode == RESULT_OK)
            {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            }
            else {
                Toast.makeText(this, "Failed To Login", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
