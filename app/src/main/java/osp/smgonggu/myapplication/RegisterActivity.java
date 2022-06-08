package osp.smgonggu.myapplication;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


import javax.net.ssl.HttpsURLConnection;

public class RegisterActivity extends AppCompatActivity {

    private DatabaseReference mDatabase;// 파이어베이스 연동할 데이터베이스 리퍼런스 함수

    // 로그에 사용할 TAG 변수 선언
    final private String TAG = getClass().getSimpleName();

    // 사용할 컴포넌트 선언
    EditText title_et, content_et;
    Button reg_button;

    // 유저아이디 변수
    String userid = "";

    private final int GET_GALLERY_IMAGE = 200;
    private ImageView imageview;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

// ListActivity 에서 넘긴 userid 를 변수로 받음
        userid = getIntent().getStringExtra("userid");

// 컴포넌트 초기화
        title_et = findViewById(R.id.title_et);
        content_et = findViewById(R.id.content_et);
        reg_button = findViewById(R.id.reg_button);
        imageview = (ImageView)findViewById(R.id.imageView);
//ㅅㄷㅂㅁㄴㅇㄻㄴㅇㄹㄴㅁㅇㄹ
        imageview = findViewById(R.id.imageView);

        imageview.setOnClickListener(v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            launcher.launch(intent);
        });
// 버튼 이벤트 추가
        reg_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
// 게시물 등록 함수
                RegBoard regBoard = new RegBoard();
                regBoard.execute(userid, title_et.getText().toString(), content_et.getText().toString());
            }
        });

    }

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>()
            {
                @Override
                public void onActivityResult(ActivityResult result)
                {
                    if (result.getResultCode() == RESULT_OK)
                    {
                        Log.e(TAG, "result : " + result);
                        Intent intent = result.getData();
                        Log.e(TAG, "intent : " + intent);
                        Uri uri = intent.getData();
                        Log.e(TAG, "uri : " + uri);
//                        imageview.setImageURI(uri);
                        Glide.with(RegisterActivity.this)
                                .load(uri)
                                .into(imageview);
                    }
                }
            });


    class RegBoard extends AsyncTask<String, Void, String> {

            @Override
            protected void onPreExecute() {
                super.onPreExecute();

                Log.d(TAG, "onPreExecute");
            }


            @Override
            protected void onPostExecute(String result) {
                super.onPostExecute(result);
                Log.d(TAG, "onPostExecute, " + result);

                if (result.equals("success")) {
// 결과값이 success 이면
// 토스트 메시지를 뿌리고
// 이전 액티비티(ListActivity)로 이동,
// 이때 ListActivity 의 onResume() 함수 가 호출되며, 데이터를 새로 고침
                    Toast.makeText(RegisterActivity.this, "등록되었습니다.", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(RegisterActivity.this, result, Toast.LENGTH_SHORT).show();
                }

            }


            @Override
            protected String doInBackground(String... params) {

                String userid = params[0];
                String title = params[1];
                String content = params[2];

                    //Connect to firebase
                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                    //response after uploading to firebase
                    final String[] response = {""};

                        //map content to object
                        Map<String, Object> comment = new HashMap<>();
                        comment.put("userid", userid);
                        comment.put("content", content);
                        comment.put("title", title);
                        //add data to comments collection in firebase
                        db.collection("comments")
                                .add(comment)
                                .addOnSuccessListener(
                                        new OnSuccessListener<DocumentReference>() {
                                            @Override
                                            public void onSuccess(DocumentReference documentReference) {
                                                response[0] = "Comment added Successfully";
                                                Intent intent = new Intent(RegisterActivity.this, ListActivity.class);
                                                startActivity(intent);
                                            }
                                        }
                                )
                                .addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        response[0] = "Error";

                                    }
                                });

                    return response[0];
                }
        }
    }