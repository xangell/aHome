package com.example.modernhome;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

public class Controller implements Observer {

	private SpeechRecognizer _sr;
	private ObservableRecognitionListener _speechListener;
	private Intent _speechRecognitionIntent;
	public MainActivity _mainView;
    public AudioManager _audioManager;
	public boolean _buzzWordRecognized;
    public SoundPool _soundPool;
    public int _sound;

	public Controller(MainActivity View) {
		_mainView = View;
		init();
	}
	
	private void say(String text)
	{
		Intent tts = new Intent(_mainView, TTS.class);
		tts.putExtra(Intent.EXTRA_TEXT, text);
		_mainView.startActivity(tts);		
	}
	
	private void init()
	{
		_soundPool = new SoundPool(1, AudioManager.STREAM_ALARM, 0);
		_sound = _soundPool.load(_mainView, R.raw.ding, 1);
		_buzzWordRecognized = false;
		_speechListener = new ObservableRecognitionListener();
		_speechListener.addObserver(this);
		_audioManager = (AudioManager) _mainView
				.getSystemService(Context.AUDIO_SERVICE);		
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
			// turn off beep sound
			_audioManager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
		}
		_mainView.getWindow().setFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
	}

	private void matchStrings(ArrayList<String> matches) {
		AsyncHttpCommunication communication = new AsyncHttpCommunication();

		if (matches.contains("okay Zuhause")) {
			_mainView.buzzWordRecognized();

		}
		else if (_buzzWordRecognized) {
			if (matches.contains("Licht aus")) {
                _mainView.executeText.setText("Schalte Licht aus");
				communication.execute("Lampe", "aus");
                _mainView.commandRecognized();
			} else if (matches.contains("Licht an")) {
                _mainView.executeText.setText("Schalte Licht ein");
                communication.execute("Lampe", "an");
                _mainView.commandRecognized();

			} else if (matches.contains("Kaffee an")) {
                _mainView.executeText.setText("Schalte Kaffemaschine an");
				communication.execute("Kaffee", "an");
                _mainView.commandRecognized();

			} else if (matches.contains("Kaffee aus")) {
                _mainView.executeText.setText("Schalte Kaffemaschine aus");
                communication.execute("Kaffee", "aus");
                _mainView.commandRecognized();

			} else if (matches.contains("schalosien hoch")) {
                _mainView.executeText.setText("Fahre Schalosien hoch");
                communication.execute("Schalosien", "hoch");
                _mainView.commandRecognized();

			} else if (matches.contains("schalosien runter")) {
                _mainView.executeText.setText("Fahre Schalosien runter");
                communication.execute("Schalosien", "runter");
                _mainView.commandRecognized();

			}
			_buzzWordRecognized = false;
		}
	}

	public void onPause() {
		_sr.destroy();
	}

	public void onResume() {
		if (SpeechRecognizer.isRecognitionAvailable(_mainView
				.getApplicationContext())) {
			_sr = SpeechRecognizer.createSpeechRecognizer(_mainView
					.getApplicationContext());
			_sr.setRecognitionListener(_speechListener);
			_speechRecognitionIntent = new Intent(
					RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
			_speechRecognitionIntent.putExtra(
					RecognizerIntent.EXTRA_LANGUAGE_MODEL,
					RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
			_speechRecognitionIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 10);
			_speechRecognitionIntent.putExtra(
					RecognizerIntent.EXTRA_CALLING_PACKAGE,
					"com.example.modernhome");
		}
		_sr.startListening(_speechRecognitionIntent);
	}

	@Override
	public void update(Observable observable, Object data) {
		if (data != null) {
			if (data instanceof ArrayList<?>) {
				@SuppressWarnings("unchecked")
				ArrayList<String> temp = (ArrayList<String>) data;
				matchStrings(temp);
			} else if (data instanceof String) {
				String errorMessage = (String) data;
				Log.d("ERROR", errorMessage);
				_buzzWordRecognized = false;
			}
		}
		if (_speechListener.hasSpeechEnded())
			_sr.startListening(_speechRecognitionIntent);

	}

}
