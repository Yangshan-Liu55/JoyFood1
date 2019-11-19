package com.liu55.shan.joyfood;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Created by shan on 2018-03-06. //SettingFragment
 */

public class LoginFragment extends Fragment {

    private EditText oldEmail, newEmail, password, newPassword;
    private Button btnChangeEmail, btnChangePassword, btnSendResetEmail, btnRemoveUser,
            changeEmail, changePassword, sendEmail, toLogin, signOut;
    private ProgressBar progressBar;
    private LinearLayout setting_layout;
    private ImageView img_logo_view;
    private FirebaseAuth.AuthStateListener authStateListener;
    private FirebaseAuth mAuth;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        final View view = inflater.inflate(com.liu55.shan.joyfood.R.layout.setting, container, false);

        //Get Firebase auth instance
        mAuth = FirebaseAuth.getInstance();//一定先实例化，否则onStart时add authStateListener报错

        //declare current user
        final FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        authStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser(); //cannot set final currentUser null
                if (user == null) {//user auth state changed - user is null, launch login activity
                    setting_layout.setVisibility(View.GONE);
                    toLogin.setVisibility(View.VISIBLE);
                    img_logo_view.setVisibility(View.VISIBLE);
                }
                else {
                    setting_layout.setVisibility(View.VISIBLE);
                    toLogin.setVisibility(View.GONE);
                    img_logo_view.setVisibility(View.GONE);
                }

            }
        };

        //Buttons, link buttons
        btnChangeEmail = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.change_email_button);
        btnChangePassword = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.change_password_button);
        btnSendResetEmail = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.sending_pass_reset_button);
        btnRemoveUser = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.remove_user_button);
        changeEmail = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.changeEmail);
        changePassword = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.changePass);
        sendEmail = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.send);
        toLogin = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.btn_setting_to_login);
        signOut = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.sign_out);
        Button btnFinishAllAct = (Button) view.findViewById(com.liu55.shan.joyfood.R.id.btn_finish_all_acts);
//        Button btnForceOffline = (Button) view.findViewById(R.id.btn_force_off_line);

        setting_layout = (LinearLayout)view.findViewById(com.liu55.shan.joyfood.R.id.setting_layout);
        img_logo_view = (ImageView)view.findViewById(com.liu55.shan.joyfood.R.id.img_logo_view);

        //TextViews
        oldEmail = (EditText) view.findViewById(com.liu55.shan.joyfood.R.id.old_email);
        newEmail = (EditText) view.findViewById(com.liu55.shan.joyfood.R.id.new_email);
        password = (EditText) view.findViewById(com.liu55.shan.joyfood.R.id.password);
        newPassword = (EditText) view.findViewById(com.liu55.shan.joyfood.R.id.newPassword);

        //Initialize the view, set the TextViews and buttons not visible
        oldEmail.setVisibility(View.GONE);
        newEmail.setVisibility(View.GONE);
        password.setVisibility(View.GONE);
        newPassword.setVisibility(View.GONE);
        changeEmail.setVisibility(View.GONE);
        changePassword.setVisibility(View.GONE);
        sendEmail.setVisibility(View.GONE);

        progressBar = (ProgressBar) view.findViewById(com.liu55.shan.joyfood.R.id.progressBar);

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        //When user wants to change password
        btnChangePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.VISIBLE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.VISIBLE);
                sendEmail.setVisibility(View.GONE);
            }
        });

        changePassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                if (currentUser != null) {
                    if (newPassword.getText().toString().trim().length() < 6) {
                        newPassword.setError("Password too short, enter minimum 6 characters");
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        currentUser.updatePassword(newPassword.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to update password!", Toast.LENGTH_SHORT).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }

            }
        });

        //When user wants to change email adress
        btnChangeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.GONE);
                newEmail.setVisibility(View.VISIBLE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.VISIBLE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.GONE);
            }
        });
        changeEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                if (currentUser != null) {
                    if (newEmail.getText().toString().trim().equals("")) {
                        newEmail.setError("Enter new email");
                        progressBar.setVisibility(View.GONE);
                    }
                    else {
                        currentUser.updateEmail(newEmail.getText().toString().trim())
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(getActivity(), "Email address is updated. Please sign in with new email id!", Toast.LENGTH_LONG).show();
                                            signOut();
                                            progressBar.setVisibility(View.GONE);
                                        } else {
                                            Toast.makeText(getActivity(), "Failed to update email!", Toast.LENGTH_LONG).show();
                                            progressBar.setVisibility(View.GONE);
                                        }
                                    }
                                });
                    }
                }
            }
        });

        //Send email for resetting password
        btnSendResetEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldEmail.setVisibility(View.VISIBLE);
                oldEmail.setText(currentUser.getEmail().toString());
                newEmail.setVisibility(View.GONE);
                password.setVisibility(View.GONE);
                newPassword.setVisibility(View.GONE);
                changeEmail.setVisibility(View.GONE);
                changePassword.setVisibility(View.GONE);
                sendEmail.setVisibility(View.VISIBLE);
            }
        });

        sendEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progressBar.setVisibility(View.VISIBLE);

                if (oldEmail.getText().toString().trim().equals("")) {
                    oldEmail.setError("Enter your email");
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    mAuth.sendPasswordResetEmail(oldEmail.getText().toString().trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(getActivity(), "Reset password email is sent!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    } else {
                                        Toast.makeText(getActivity(), "Failed to send reset email!", Toast.LENGTH_SHORT).show();
                                        progressBar.setVisibility(View.GONE);
                                    }
                                }
                            });
                }
            }
        });

        //Delete account
        btnRemoveUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (currentUser != null) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    builder.setTitle("Delete your account");
                    builder.setMessage("Are you sure to delete?");

                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(getActivity(),"Canceled", Toast.LENGTH_SHORT).show();
                        }
                    });

                    builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            progressBar.setVisibility(View.VISIBLE);

                            currentUser.delete()
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            if (task.isSuccessful()) {
                                                progressBar.setVisibility(View.GONE);
                                                startActivity(new Intent(getActivity(), SignUpActivity.class));
                                                getActivity().finish();
                                            } else {
                                                Toast.makeText(getActivity(), "Failed to delete your account!", Toast.LENGTH_SHORT).show();
                                                progressBar.setVisibility(View.GONE);
                                            }
                                        }
                                    });
                        }
                    });
                    builder.create().show();
                }
                else {
                    Toast.makeText(getActivity(),"Not logged in yet!", Toast.LENGTH_SHORT).show();
                }
            }
        });

        toLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentUser != null) {
                    Toast.makeText(getActivity(), "Already logged in", Toast.LENGTH_SHORT).show();
                }
                else {
                    startActivity(new Intent(getActivity(), LoginActivity.class));
                    getActivity().finish();
                }
            }
        });

        //Log out
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signOut();
            }
        });

        btnFinishAllAct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ActivityCollector.finishAll();
            }
        });

//        btnForceOffline.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent("com.liu55.shan.joyfood.FORCE_OFFLINE");
//                getActivity().sendBroadcast(i);
//            }
//        });

        return view;
    }

    //sign out method //repeats in several methods above
    public void signOut() {
        mAuth.signOut();
        getActivity().finish(); //finish the activity for update all data
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authStateListener);
    }

    @Override
    public void onResume() {
        super.onResume();
        progressBar.setVisibility(View.GONE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authStateListener != null) {
            mAuth.removeAuthStateListener(authStateListener);
        }
    }
}
