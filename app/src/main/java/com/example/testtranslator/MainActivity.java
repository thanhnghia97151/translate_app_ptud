package com.example.testtranslator;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.common.modeldownload.FirebaseModelDownloadConditions;
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslator;
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private Spinner fromSpinner, toSpinner;
    private TextInputEditText sourceEdt;
    private ImageView micTv,idRead,imgTranslatedRead,imgCamera,imgImage;
    private MaterialButton translateBtn;
    private TextView translatedTV,tvTranslated;
    private TextToSpeech textToSpeech;
    private TextToSpeech textToSpeechTranslated;
    private boolean ready;
    private boolean readyTranslated;
    private Locale languageLocale=null;
    private Locale languageLocaleTranslated=null;



    String[] fromLanguages = {"From", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan", "Czech", "Hindi", "Urdu", "Welsh", "China", "Vietnamese","Korean","Japanese","French","Italian"};
    String[] toLanguages = {"To", "English", "Afrikaans", "Arabic", "Belarusian", "Bengali", "Catalan", "Czech", "Hindi", "Urdu", "Welsh", "China", "Vietnamese","Korean","Japanese","French","Italian"};

    private static final int REQUEST_PERMISSION_CODE = 1;
    int languageCode, fromLanguageCode=0, toLanguageCode = 0;
    FirebaseTranslator translator;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        fromSpinner = findViewById(R.id.idFromSpinner);
        toSpinner = findViewById(R.id.idToSpinner);
        sourceEdt = findViewById(R.id.idEditSource);
        micTv = findViewById(R.id.idIVMic);
        translateBtn = findViewById(R.id.idBtnTranslate);
        translatedTV = findViewById(R.id.idTVTranslatedTV);
        tvTranslated = findViewById(R.id.tvTranslated);
        idRead = findViewById(R.id.idRead);
        imgTranslatedRead = findViewById(R.id.imgTranslatedRead);
        imgCamera = findViewById(R.id.idCamera);
        imgImage = findViewById(R.id.idImg);

        //
        imgImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ChoseImageActivity.class));
            }
        });
        //

        //
        Intent intent = this.getIntent();
        String resultDetectTextFromImage = intent.getStringExtra("result");
        sourceEdt.setText(resultDetectTextFromImage);
        //

        imgCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,ScannerActivity.class));
            }
        });

        imgTranslatedRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOutTranslated();
            }
        });

        idRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                speakOut();
            }
        });

        //Ghi chú: chỉ đọc được các ngôn ngữ
//        en_US
//        de_DE
//        fr
//        es_ES
//        de
//        en
//        it_IT
//        it
//        en_GB
//        es
//        fr_FR



        fromSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                fromLanguageCode = getLanguageCode(fromLanguages[i]);
                TextTOSpeech();

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter fromAdapter = new ArrayAdapter(this,R.layout.spinner_item,fromLanguages);
        fromAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        fromSpinner.setAdapter(fromAdapter);

        toSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                toLanguageCode = getLanguageCode(toLanguages[i]);

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ArrayAdapter toAdapter = new ArrayAdapter(this,R.layout.spinner_item,toLanguages);
        toAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        toSpinner.setAdapter(toAdapter);

        translateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                translatedTV.setText("");
                if (sourceEdt.getText().toString().isEmpty())
                    Toast.makeText(MainActivity.this,"Please enter your text to translate!",Toast.LENGTH_SHORT).show();
                else if(fromLanguageCode==0)
                    Toast.makeText(MainActivity.this,"Please select sour language!",Toast.LENGTH_SHORT).show();
                else if(toLanguageCode==0)
                    Toast.makeText(MainActivity.this,"Please select source language translation!",Toast.LENGTH_SHORT).show();
                else{

                    translateText(fromLanguageCode,toLanguageCode,sourceEdt.getText().toString());
                }


            }
        });
        micTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,Locale.getDefault());
                i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak to convert into text!");
                try{
                    startActivityForResult(i,REQUEST_PERMISSION_CODE);

                }catch (Exception e)
                {
                    e.printStackTrace();
                    Toast.makeText(MainActivity.this, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    private  void TextToSpeechTranslated(){
        textToSpeechTranslated = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e("TTS", "TextToSpeech.OnInitListener.onInit...");
                printOutSupportedLanguagesTranslated();
                setTextToSpeechLanguageTranslated();
            }
        });
    }
    private void TextTOSpeech(){
        textToSpeech = new TextToSpeech(getApplicationContext(), new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                Log.e("TTS", "TextToSpeech.OnInitListener.onInit...");
                printOutSupportedLanguages();
                setTextToSpeechLanguage();
            }
        });
    }
    private void printOutSupportedLanguagesTranslated(){
        // Supported Languages
        Set<Locale> supportedLanguages = textToSpeechTranslated.getAvailableLanguages();
        if(supportedLanguages!= null) {
            for (Locale lang : supportedLanguages) {
                Log.e("TTS", "Supported Language: " + lang);
            }
        }
    }
    private void printOutSupportedLanguages()  {
        // Supported Languages
        Set<Locale> supportedLanguages = textToSpeech.getAvailableLanguages();
        if(supportedLanguages!= null) {
            for (Locale lang : supportedLanguages) {
                Log.e("TTS", "Supported Language: " + lang);
            }
        }
    }
    @Override
    public void onPause() {
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (textToSpeechTranslated != null) {
            textToSpeechTranslated.stop();
            textToSpeechTranslated.shutdown();
        }
        super.onPause();
    }
    private void speakOutTranslated(){
        if (tvTranslated.getText().toString()!="" && languageLocaleTranslated!=null){
            if (!readyTranslated) {
                Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
                return;
            }
            // Text to Speak
            String toSpeak = tvTranslated.getText().toString();
            Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show();
            // A random String (Unique ID).
            String utteranceId = UUID.randomUUID().toString();
            textToSpeechTranslated.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }
    }
    private void speakOut() {
        if (sourceEdt.getText().toString()!="" && languageLocale!=null){
            if (!ready) {
                Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
                return;
            }
            // Text to Speak
            String toSpeak = sourceEdt.getText().toString();
            Toast.makeText(this, toSpeak, Toast.LENGTH_SHORT).show();
            // A random String (Unique ID).
            String utteranceId = UUID.randomUUID().toString();
            textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
        }

    }

    private void setTextToSpeechLanguageTranslated(){
        Locale language = languageLocaleTranslated;
        if (language == null) {
            this.readyTranslated = false;
            Toast.makeText(this, "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = textToSpeechTranslated.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.readyTranslated = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.readyTranslated = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.readyTranslated = true;
            Locale currentLanguage = textToSpeechTranslated.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }
    private void setTextToSpeechLanguage() {
        Locale language = languageLocale;
        if (language == null) {
            this.ready = false;
            Toast.makeText(this, "Not language selected", Toast.LENGTH_SHORT).show();
            return;
        }
        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.ready = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
            return;
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.ready = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
            return;
        } else {
            this.ready = true;
            Locale currentLanguage = textToSpeech.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==REQUEST_PERMISSION_CODE){
            if (resultCode==RESULT_OK && data!=null){
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                sourceEdt.setText(result.get(0));
            }
        }
    }
    private void translateText(int fromLanguageCode , int toLanguageCode, String source){
        translatedTV.setText("Downloading Model..");
        FirebaseTranslatorOptions options = new FirebaseTranslatorOptions.Builder()
                .setSourceLanguage(fromLanguageCode)
                .setTargetLanguage(toLanguageCode)
                .build();

        FirebaseApp.initializeApp(this);
        translator = FirebaseNaturalLanguage.getInstance(FirebaseApp.getInstance()).getTranslator(options);
        FirebaseModelDownloadConditions conditions = new FirebaseModelDownloadConditions.Builder().build();

        translator.downloadModelIfNeeded(conditions).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
                translatedTV.setText("Translating..");
                translator.translate(source).addOnSuccessListener(new OnSuccessListener<String>() {
                    @Override
                    public void onSuccess(String s) {
                        translatedTV.setText("Complete!");

                        tvTranslated.setText(s);
                        TextToSpeechTranslated();

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Fail to translate: "+e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(MainActivity.this, "Fail to download language Modal"+e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    public int getLanguageCode(String language){
        int languageCode = 0;
        switch (language){
            case "English":
                languageLocale=Locale.US;
                languageLocaleTranslated=Locale.US;
                languageCode = FirebaseTranslateLanguage.EN;
                break;
            case "Afrikaans":
                languageCode = FirebaseTranslateLanguage.AF;
                break;
            case "Arabic":
                languageCode = FirebaseTranslateLanguage.AR;
                break;
            case "Belarusian":
                languageCode = FirebaseTranslateLanguage.BE;
                break;
            case "Bengali":
                languageCode = FirebaseTranslateLanguage.BN;
                break;
            case "Catalan":
                languageCode = FirebaseTranslateLanguage.CA;
                break;
            case "Czech":
                languageCode = FirebaseTranslateLanguage.CS;
                break;
            case "Welsh":
                languageCode = FirebaseTranslateLanguage.CY;
                break;
            case "Hindi":
                languageCode = FirebaseTranslateLanguage.HI;
                break;
            case "Urdu":

                languageCode = FirebaseTranslateLanguage.UR;
                break;
            case "China":
                languageLocale=Locale.CHINA;
                languageLocaleTranslated=Locale.CHINA;
                languageCode = FirebaseTranslateLanguage.ZH;
                break;
            case "Vietnamese":
                languageCode = FirebaseTranslateLanguage.VI;
                break;
            case "Korean":
                languageLocale=Locale.KOREAN;
                languageLocaleTranslated=Locale.KOREAN;
                languageCode = FirebaseTranslateLanguage.KO;
                break;
            case "Japanese":
                languageLocale=Locale.JAPANESE;
                languageLocaleTranslated=Locale.JAPANESE;
                languageCode = FirebaseTranslateLanguage.JA;
                break;
            case "French":
                languageLocale=Locale.FRENCH;
                languageLocaleTranslated=Locale.FRENCH;
                languageCode = FirebaseTranslateLanguage.FR;
                break;
            case "Italian":
                languageLocale=Locale.ITALIAN;
                languageLocaleTranslated=Locale.ITALIAN;
                languageCode = FirebaseTranslateLanguage.IT;
                break;
        }
        return  languageCode;
    }
}