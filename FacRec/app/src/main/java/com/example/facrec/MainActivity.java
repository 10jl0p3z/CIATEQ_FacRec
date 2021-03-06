package com.example.facrec;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.Engine;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Size;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;



public class MainActivity extends AppCompatActivity {

    Button btnOpenDialog;
    Button btnUp;
    TextView txtFolder;

    String ProtectedDirectory = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ProtectedFiles";
    String tmpImgPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ProtectedFiles/tmp";
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
    ImageView img;
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
    private Uri ficheroSalidaUri;

    private org.tensorflow.demo.Classifier classifier;
    private static final int INPUT_SIZE = 224;
    private static final int IMAGE_MEAN = 128;
    private static final float IMAGE_STD = 128.0f;
    private static final String INPUT_NAME = "input";
    //private static final String OUTPUT_NAME = "MobilenetV1/Predictions/Softmax";
    private static final String OUTPUT_NAME = "final_result";

    private static final String MODEL_FILE = "file:///android_asset/graph.pb";
    private static final String LABEL_FILE = "file:///android_asset/labels.txt";

    private static int TTS_DATA_CHECK = 1;
    private static int TTS_TAKE_PHOTO = 2;
    private TextToSpeech tts = null;
    private boolean ttsIsInit = false;

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

        Init_TF();
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
                img = (ImageView) dialog.findViewById(R.id.UserImg);
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
                        if (CurrentFileName.equalsIgnoreCase(UnlockedFor.toString())){ /* Already unlocked*/
                            if (SelectedFile.isDirectory()){
                                ListDir(SelectedFile);
                            }else{
                                Toast.makeText(MainActivity.this, "Archivo seleccionado, listo para usar",
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

    // display the current directory and files
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

    // Initilize the Text to Speech component
    private void initTextToSpeech() {
        Intent intent = new Intent(Engine.ACTION_CHECK_TTS_DATA);
        startActivityForResult(intent, TTS_DATA_CHECK);
    }

    // Proces the data when activity is completed
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == TTS_DATA_CHECK) {
            // Verify Text to speech
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
        }else if (requestCode == TTS_TAKE_PHOTO){
            // execute the Tensor flow to labe the user image
            if (resultCode == Activity.RESULT_OK)
            {

                Bitmap test = BitmapFactory.decodeFile(ficheroSalidaUri.getPath());
                //Bitmap test = BitmapFactory.decodeFile(Environment.getExternalStorageDirectory().getAbsolutePath() + "/ProtectedFiles/Joel/joel.jpg");

                Bitmap otherTest = Bitmap.createScaledBitmap(test, INPUT_SIZE, INPUT_SIZE, false);

                final List<org.tensorflow.demo.Classifier.Recognition> results = classifier.recognizeImage(otherTest);

                if (results.get(0).getConfidence() > 0.5){
                    ProcesResults(results.get(0).getTitle());
                    img.setImageBitmap(otherTest);
                }
                else{
                    ProcesResults("Intruder");

                }
            }
        }
        else{}

    }


// PLay the text received
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

    // Start Camera activity to register the user
    private void StartAuthentification(){
        AlertDialog.Builder ContinueAuth = new AlertDialog.Builder(this);
        ContinueAuth.setMessage("Necesitas identificarte para acceder al contenido")
                .setCancelable(false)
                .setPositiveButton("Continuar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Intent cameraIntent =  new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        File file = new File(tmpImgPath, "tmp.jpg");
                        ficheroSalidaUri = Uri.fromFile(file);
                        cameraIntent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, ficheroSalidaUri);
                        startActivityForResult(cameraIntent,TTS_TAKE_PHOTO);
                    }
                })
                .setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Cancelar proceso
                        Toast.makeText(MainActivity.this, "Operacion cancelada",
                                Toast.LENGTH_LONG).show();
                        dismissDialog(CUSTOM_DIALOG_ID);
                    }
                });
        AlertDialog alert = ContinueAuth.create();
        alert.show();

    }

    // Return the enumeration matching the user name
    private AllowedPeople convertToAllowePeople(String myString){
        AllowedPeople myperson = null;
        for (AllowedPeople person : AllowedPeople.values()){
            if (myString.equalsIgnoreCase(person.toString())){
                myperson = person;
            }

        }
        return myperson;
    }

    // Determine if the User is allowed to access the folder
    private void ProcesResults (String autres){
        if (CurrentFileName.equalsIgnoreCase(autres)){
            speak("Bienvenido "+CurrentFileName);
            UnlockedFor = convertToAllowePeople(autres);
            img.setForeground(getResources().getDrawable(R.drawable.ic_action_ok));
            ListDir(SelectedFile);
        }else if (autres.equalsIgnoreCase(AllowedPeople.INTRUDER.toString())){
            speak("Intruso");
            speak("Intruso");
            speak("Intruso");
            UnlockedFor = AllowedPeople.ANYBODY;
            img.setForeground(getResources().getDrawable(R.drawable.ic_action_denied));

            dismissDialog(CUSTOM_DIALOG_ID);

        }else{
            speak(autres);
            speak("No tienes permisos para acceder a esta ubicacion");
            img.setForeground(getResources().getDrawable(R.drawable.ic_action_denied));
            UnlockedFor = AllowedPeople.ANYBODY;

        }
    }

// Initilize the tensor flow model
    public void Init_TF() {

        classifier =
                org.tensorflow.demo.TensorFlowImageClassifier.create(
                        getAssets(),
                        MODEL_FILE,
                        LABEL_FILE,
                        INPUT_SIZE,
                        IMAGE_MEAN,
                        IMAGE_STD,
                        INPUT_NAME,
                        OUTPUT_NAME);


    }

}

