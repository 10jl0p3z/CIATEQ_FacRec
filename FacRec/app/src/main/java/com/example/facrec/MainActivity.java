package com.example.facrec;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.content.pm.PackageManager;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.content.Intent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.lang.String;


public class MainActivity extends AppCompatActivity {

    Button btnOpenDialog;
    Button btnUp;
    TextView txtFolder;

    String KEY_TEXTPSS = "TEXTPSS";
    String ProtectedDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ProtectedFiles";
    static final int CUSTOM_DIALOG_ID = 0;
    ListView lstviewDialog;
    File root;
    File curFolder;
    private List<String> fileList = new ArrayList<String>();
    private List<String> fileListname = new ArrayList<String>();

    AllowedPeople UnlockedFor = AllowedPeople.ANYBODY;
    boolean LoginCompleted = false;

    File SelectedFile = null;
    String CurrentFileName = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
        {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }
        initTextToSpeech();

        btnOpenDialog = (Button) findViewById(R.id.opendialog);
        btnOpenDialog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(CUSTOM_DIALOG_ID);
            }
        });
        root = new File(ProtectedDirectory);
        //root = new File(ProtectedDirectory);
        curFolder = root;
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        Dialog dialog = null;

        switch (id){
            case CUSTOM_DIALOG_ID:
                dialog = new Dialog(MainActivity.this);
                dialog.setContentView(R.layout.dialoglayout);
                dialog.setTitle("Custom Dialog");
                dialog.setCancelable(true);
                dialog.setCanceledOnTouchOutside(true);

                txtFolder = (TextView) dialog.findViewById(R.id.folder);
                btnUp = (Button) dialog.findViewById(R.id.up);
                btnUp.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ListDir(curFolder.getParentFile());

                    }
                });

                lstviewDialog = (ListView) dialog.findViewById(R.id.dialoglist);
                lstviewDialog.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        SelectedFile = new File(fileList.get(position));
                        CurrentFileName = fileListname.get(position);
                        if (AllowedPeople.ANYBODY != UnlockedFor){ /* Already unlocked*/
                            if (SelectedFile.isDirectory()){
                                ListDir(SelectedFile);
                            }else{
                                Toast.makeText(MainActivity.this, SelectedFile.toString() + " selected",
                                        Toast.LENGTH_LONG).show();
                                dismissDialog(CUSTOM_DIALOG_ID);
                            }
                        }else {
                            StartAuthentification();

                        }


                    }
                });

            break;
        }
        return dialog;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        switch (id){
            case CUSTOM_DIALOG_ID:
                ListDir(curFolder);
                break;
        }
    }

    void ListDir( File f){
        if(f.equals(root)){
            btnUp.setEnabled(false);
        }else{
            btnUp.setEnabled(true);
        }

        curFolder = f;
        txtFolder.setText(f.getPath());

        File[] files = f.listFiles();
        fileList.clear();
        fileListname.clear();

        for (File file : files){
            fileList.add(file.getPath());
            fileListname.add(file.getName());
        }
        ArrayAdapter<String> directoryList = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, fileListname);

        lstviewDialog.setAdapter(directoryList);
    }
    private static int TTS_DATA_CHECK = 1;
    private TextToSpeech tts = null;
    private boolean ttsIsInit = false;
    private void initTextToSpeech() {
        Intent intent = new Intent(Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_DATA_CHECK);
    }
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TTS_DATA_CHECK) {
            if (resultCode == Engine.CHECK_VOICE_DATA_PASS) {
                tts = new TextToSpeech(this, new OnInitListener() {
                    public void onInit(int status) {
                        if (status == TextToSpeech.SUCCESS) {
                                ttsIsInit = true;
                                Locale loc = new Locale("es","","");
                                if (tts.isLanguageAvailable(loc) >= TextToSpeech.LANG_AVAILABLE)
                                    tts.setLanguage(loc);
                                tts.setPitch(0.8f);
                                tts.setSpeechRate(1.1f);

                        }
                    }
                });
            } else {
                Intent installVoice = new Intent(Engine.ACTION_INSTALL_TTS_DATA);
                startActivity(installVoice);
            }
        }

    }
    private void speak(String myText) {
        if (tts != null && ttsIsInit) {
            tts.speak(myText, TextToSpeech.QUEUE_ADD, null);
        }
    }
    @Override public void onDestroy() {
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
    }

    public enum AllowedPeople {
        ANYBODY,
        ALEJANDRO,
        ANTONIO,
        ATLANTIDA,
        JAVIER,
        CARLOS,
        CORBAL,
        CRISTIAN,
        FERNANDO,
        GENESIS,
        GUSTAVO,
        JOEL,
        JUANPABLO,
        INTRUDER

    }

    private void StartAuthentification(){
        AlertDialog.Builder ContinueAuth = new AlertDialog.Builder(this);
        ContinueAuth.setMessage("Necesitas identificarte para acceder al contenido")
                .setCancelable(false)
                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // call here the identification process
                        ProcesResults();
                        //CameraActivity.CameraInit();
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cancelar proceso
                        dismissDialog(CUSTOM_DIALOG_ID);
                    }
                });
        AlertDialog alert = ContinueAuth.create();
        alert.show();

    }
    private AllowedPeople convertToAllowePeople(String myString){
        AllowedPeople myperson = null;
        for (AllowedPeople person : AllowedPeople.values()){
            if (myString.equalsIgnoreCase(person.toString())){
                myperson = person;
            }

        }
        return myperson;
    }

    private void ProcesResults (){
        String autres = "Joel";

        if (CurrentFileName.equalsIgnoreCase(autres)){
            speak("Bienvenido "+CurrentFileName);
            UnlockedFor = convertToAllowePeople(autres);
            ListDir(SelectedFile);
        }else if (autres.equalsIgnoreCase(AllowedPeople.INTRUDER.toString())){
            speak("Intruso");
            speak("Intruso");
            speak("Intruso");
        }else{
            speak(autres);
            speak("No tienes permisos para acceder a esta ubicacion");
        }
    }
}

