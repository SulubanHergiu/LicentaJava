package com.example.licentajava;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.licentajava.Model.Attendee;
import com.example.licentajava.Model.JavaMailAPI;
import com.example.licentajava.Utils.FirebaseDatabaseHelper;
import com.itextpdf.text.Document;
import com.itextpdf.text.Font;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class AttendeeListActivity extends AppCompatActivity {
    private RecyclerView mRecylclerView;
    private FirebaseDatabaseHelper dataHelper;
    private String mName="";
    private String mId="";
    private TextView eventName;
    Button  mMailButton;
    Button  mPdfButton;
    private String mMail="";
    List<Attendee> attendeesList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        dataHelper=new FirebaseDatabaseHelper();
        super.onCreate(savedInstanceState);
        Intent intent=getIntent();
        mName=intent.getStringExtra("name");
        mId=intent.getStringExtra("id");
        mMail=intent.getStringExtra("mail");

        setContentView(R.layout.activity_attendee_list);
        eventName=(TextView) findViewById(R.id.event_name);
        eventName.setText(mName);
        mMailButton=(Button) findViewById(R.id.mail);
        mPdfButton=(Button) findViewById(R.id.pdf);
        mMailButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                if (mMail.equals("-")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(AttendeeListActivity.this);
                    builder.setMessage("You need to set an e-mail address to your Facebook account in order to receive mails.")
                            .setCancelable(false)
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int id) {

                                }
                            });
                    AlertDialog alert = builder.create();
                    alert.show();
                } else {
                    sendMail();

                }
            }
        });

            mPdfButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(Build.VERSION.SDK_INT> Build.VERSION_CODES.M)
                    {
                        if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED)
                        {
                            String[] parmission={Manifest.permission.WRITE_EXTERNAL_STORAGE};
                            requestPermissions(parmission,1000);
                        }
                        else savePdf();
                    }
                    else savePdf();
                }



            });
        mRecylclerView=(RecyclerView) findViewById(R.id.attendeesRecyclerView) ;
        dataHelper.readAttendees(mId, new FirebaseDatabaseHelper.DataStatus() {
            @Override
            public void DataIsLoaded(List<Attendee> attendees, List<String> keys) {
                new AteendeeView_Config().setConfig(mRecylclerView,AttendeeListActivity.this,attendees,keys);
                attendeesList=attendees;
                mMailButton.setEnabled(true);
                mPdfButton.setEnabled(true);
            }

        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        //JavaMailAPI javaMailAPI=new JavaMailAPI(this,"huluban.sergiu@yahoo.com","iiiii","uuuuu");


    }
    private void sendMail(){
        String AttList="";
        for(Attendee atte : attendeesList){
            AttList+=atte.getName()+" "+atte.getEmail()+" "+atte.getStartTime()+"-"+atte.getEndTime()+"\n";

        }
        JavaMailAPI javaMailAPI=new JavaMailAPI(this,mMail,mName+" Attendance",AttList);
        javaMailAPI.execute();
    }
    private  void savePdf()
    {
        Document doc=new Document();
        String mFile=mName;
        String mFilePath= getExternalFilesDir(null)+"/"+mFile+".pdf";
        Font smallBold=new Font(Font.FontFamily.TIMES_ROMAN,12,Font.NORMAL);
        try{
            PdfWriter.getInstance(doc,new FileOutputStream(mFilePath));
            doc.open();

            doc.addAuthor("MyApp");
            doc.add(new Paragraph(mName+" Attendance",smallBold));
            for(Attendee atte : attendeesList){
                String AttList=atte.getName()+" "+atte.getEmail()+" "+atte.getStartTime()+"-"+atte.getEndTime()+"\n";
                doc.add(new Paragraph(AttList,smallBold));

            }

            doc.close();
            Toast.makeText(this, ""+mFile+".pdf"+" is saved to "+mFilePath, Toast.LENGTH_SHORT).show();
        }
        catch (Exception e)
        {
            Toast.makeText(this,"This is Error msg : " +e.getMessage(), Toast.LENGTH_SHORT).show();
        }

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode)
        {
            case  1000:
                if(grantResults.length>0 && grantResults[0]== PackageManager.PERMISSION_GRANTED)
                {
                    savePdf();
                }
                else Toast.makeText(this, "parmission denied..", Toast.LENGTH_SHORT).show();
        }
    }
}

