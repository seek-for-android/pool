/* 
 * Copyright (C) 2008 OpenIntents.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * Based on AndDev.org's file browser V 2.0.
 * With extensions for mobile security smartcard support.
 * 
 */

package org.openintents.filemanager;

//TODO Addons:
//- don't create a (plaintext) temp file but have a mem-mapped file?
//- block cipher for large files --> done
//- add system intent to save encrypted files to SD card --> geht nicht, da keine eigene Aktivity
//- autostart when SD card gets inserted --> geht durch connectionlistener

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openintents.distribution.AboutDialog;
import org.openintents.distribution.EulaActivity;
import org.openintents.distribution.UpdateMenu;
import org.openintents.filemanager.util.FileUtils;
import org.openintents.filemanager.util.MimeTypeParser;
import org.openintents.filemanager.util.MimeTypes;
import org.openintents.intents.FileManagerIntents;
import org.openintents.util.MenuIntentOptionsWithIcons;
import org.xmlpull.v1.XmlPullParserException;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.content.res.XmlResourceParser;
import android.giesecke.security.CardCommandBuilder;
import android.giesecke.security.CipheredFile;
import android.giesecke.security.Helper;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.smartcard.CardException;
import android.smartcard.ICardChannel;
import android.smartcard.SmartcardClient;
import android.smartcard.SmartcardClient.ISmartcardConnectionListener;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * MainActivity of the Security File Manager based on the OI FileManager
 * 
 * 
 * @author OpenIntents
 * @author stuerzem
 *
 */
public class FileManagerActivity extends ListActivity
{

	/**
	 * Tag for logging information
	 */
	private static final String TAG = "FileManagerActivity";

	private int mState;

	private static final int STATE_BROWSE = 1;
	private static final int STATE_PICK_FILE = 2;
	private static final int STATE_PICK_DIRECTORY = 3;

	protected static final int REQUEST_CODE_MOVE = 1;
	protected static final int REQUEST_CODE_COPY = 2;
	protected static final int REQUEST_DELETE_FILE = 3;

	private static final int MENU_ABOUT = Menu.FIRST + 1;
	private static final int MENU_UPDATE = Menu.FIRST + 2;
	// private static final int MENU_PREFERENCES = Menu.FIRST + 3;
	private static final int MENU_NEW_FOLDER = Menu.FIRST + 4;
	private static final int MENU_DELETE = Menu.FIRST + 5;
	private static final int MENU_RENAME = Menu.FIRST + 6;
	private static final int MENU_SEND = Menu.FIRST + 7;
	private static final int MENU_OPEN = Menu.FIRST + 8;
	private static final int MENU_MOVE = Menu.FIRST + 9;
	private static final int MENU_COPY = Menu.FIRST + 10;
	private static final int MENU_DECRYPT = Menu.FIRST + 11;
	private static final int MENU_ENCRYPT = Menu.FIRST + 12;

	private static final int DIALOG_NEW_FOLDER = 1;
	private static final int DIALOG_DELETE = 2;
	private static final int DIALOG_RENAME = 3;
	private static final int DIALOG_ABOUT = 4;
	private static final int DIALOG_DECRYPT = 5;
	private static final int DIALOG_ENCRYPT = 6;
	private static final int DIALOG_DECRYPT_ONTHEFLY = 7;
	private static final int DIALOG_PIN = 8;

	private static final int COPY_BUFFER_SIZE = 32 * 1024;

	private static final String BUNDLE_CURRENT_DIRECTORY = "current_directory";
	private static final String BUNDLE_CONTEXT_FILE = "context_file";
	private static final String BUNDLE_CONTEXT_TEXT = "context_text";
	private static final String BUNDLE_SHOW_DIRECTORY_INPUT = "show_directory_input";
	private static final String BUNDLE_STEPS_BACK = "steps_back";
	private static final String BUNDLE_PIN_CHECKED = "is_Pin_checked";
	private static final String BUNDLE_VALID_PIN = "is_Pin_valid";
	// private static final String BUNDLE_PIN_COUNTER = "pinCounter";

	/** Contains directories and files together */
	private ArrayList<IconifiedText> directoryEntries = new ArrayList<IconifiedText>();

	/** Dir separate for sorting */
	List<IconifiedText> mListDir = new ArrayList<IconifiedText>();

	/** Files separate for sorting */
	List<IconifiedText> mListFile = new ArrayList<IconifiedText>();

	/** SD card separate for sorting */
	List<IconifiedText> mListSdCard = new ArrayList<IconifiedText>();

	/**
	 * represents the current directory
	 */
	private File currentDirectory = new File("");

	private String mSdCardPath = "";
	private String passphrase = null;

	private MimeTypes mMimeTypes;

	private String mContextText;
	private File mContextFile = new File("");
	private Drawable mContextIcon;

	/** How many steps one can make back using the back key. */
	private int mStepsBack;

	private EditText mEditFilename;
	private Button mButtonPick;
	private LinearLayout mDirectoryButtons;

	private LinearLayout mDirectoryInput;
	private EditText mEditDirectory;
	private ImageButton mButtonDirectoryPick;

	private TextView mEmptyText;
	private ProgressBar mProgressBar;

	private DirectoryScanner mDirectoryScanner;
	private File mPreviousDirectory;
	private ThumbnailLoader mThumbnailLoader;

	/**
	 * Main Handler
	 */
	private Handler currentHandler;
	/**
	 * Handler to update the progressbar of the UI
	 */
	private Handler progressHandler;
	/**
	 * Handler to update the UI for events of the encrypted file
	 */
	private Handler encryptedFileHandler;

	/**
	 * Remember if the correct Pin was entered
	 */
	private boolean validPin = false;
	
	/**
	 * Remember if the Pin was checked
	 */
	private boolean pinChecked = false;

	/**
	 * List of Contents is ready<br>
	 * obj = DirectoryContents
	 */
	static final public int MESSAGE_SHOW_DIRECTORY_CONTENTS = 500; 
	
	/**
	 * Set Progressbar<br>
	 * arg1 = current value, <br>
	 * arg2 = max value
	 */
	static final public int MESSAGE_SET_PROGRESS = 501; 

	/**
	 * View needs to be redrawn, <br>
	 * obj = IconifiedText
	 */
	static final public int MESSAGE_ICON_CHANGED = 502; 

	/**
	 * A File has to be encrypted
	 */
	static final public int MESSAGE_ENCRYPT = 503; 

	/**
	 * A File has to be decrypted
	 */
	static final public int MESSAGE_DECRYPT = 504; 

	/**
	 * A File has to be decrypted on the fly
	 */
	static final public int MESSAGE_DECRYPTONTHEFLY = 505; 

	/**
	 * Refreshes the List
	 */
	static final public int MESSAGE_REFRESH_LIST = 506; 

	/**
	 * A error occured while decrypting the file
	 */
	static final public int MESSAGE_SHOW_DECRYPT_ERROR = 507;

	/**
	 * A error occured while encrypting the file
	 */
	static final public int MESSAGE_SHOW_ENCRYPT_ERROR = 508;
	
	/**
	 * The file was not encrypted
	 */
	static final public int MESSAGE_SHOW_IS_NOT_ENCRYPTED = 509;
	
	/**
	 * The file was not encrypted
	 */
	static final public int MESSAGE_CAN_NOT_OPEN_FILE = 510;

	/**
	 * The AID of the FileManager applet on the smart card.
	 */
	private static final byte[] APPLET_AID = new byte[] { (byte) 0xD2, 0x76, 0x00, 0x00, 0x05, 0x00, 0x02, (byte) 0xFF,
			0x49, 0x40, 0x25, (byte) 0x89, (byte) 0xC0, 0x01, (byte) 0x6D, 0x01 };

	/**
	 * ProgressDialog to show the ciphering progress
	 */
	private ProgressDialog progressDialog = null;

	/**
	 * Represents a Smartcard
	 */
	SmartcardClient smartcard = null;

	/**
	 * Channel to communicate with an application running on the smartcard
	 */
	ICardChannel cardChannel = null;

	/**
	 * The smartcardConnectionListener will be informed, if a smartcard service
	 * is successfully connected, or when a existing connection is lost.
	 */
	ISmartcardConnectionListener connectionListener = new ISmartcardConnectionListener()
	{

		/**
		 * Try to Connect to the smart card
		 */
		public void serviceConnected()
		{
			Log.i(TAG, "Connect to service");
			try
			{
				// get all Readers
				for (String reader : smartcard.getReaders())
					Log.i(TAG, "reader: " + reader);

				// cancel if no smartcard is available
				if (smartcard.isCardPresent(smartcard.getReaders()[0]) == false)
					return;
				Log.i(TAG, "Smartcard is present");

				// Connect to the applet
				// Normally on the reader: "Mobile Security Card 00 00"
				cardChannel = smartcard.openLogicalChannel(smartcard.getReaders()[0], APPLET_AID);

				Log.i(TAG, "Logical channel opened to Applet " + APPLET_AID.toString());

				// Pinabfrage
//				if (pinChecked == false)
//				{
//					showDialog(DIALOG_PIN);
//				}

			} catch (CardException e)
			{
				Log.e(TAG, "Logical channel exception: " + e.getMessage());
				cardChannel = null;
				return;
			} catch (Exception e)
			{
				Log.e(TAG, e.getMessage());
				cardChannel = null;
				return;
			}
		}

		public void serviceDisconnected()
		{
			// to reconnect if the service was killed
			Log.i(TAG, "Service disconnected");
			connectToService();
		}
	};

	/**
	 * Internal helper method to do the Smartcard Service binding
	 */
	private void connectToService()
	{
		try
		{
			if (smartcard == null)
			{
				smartcard = new SmartcardClient(this, connectionListener);
			}
		} catch (SecurityException e)
		{
			Log.e(TAG, "Binding not allowed, uses-permission SMARTCARD?");
		} catch (Exception e)
		{
			Log.e(TAG, "Exception: " + e.getMessage());
		}
	}

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle icicle)
	{
		super.onCreate(icicle);

		// Eula check
		if (!EulaActivity.checkEula(this))
		{
			return;
		}

		// Load data
		try
		{
			validPin = icicle.getBoolean(BUNDLE_VALID_PIN);
			pinChecked = icicle.getBoolean(BUNDLE_PIN_CHECKED);
			// pinCounter = icicle.getInt(BUNDLE_PIN_COUNTER);
		} catch (Exception e)
		{
			validPin = false;
			pinChecked = false;
		}

		// dismiss ProgressDialog
		if (progressDialog != null)
		{
			progressDialog.dismiss();
			progressDialog = null;
		}

		// connect to Service, initialize smartcard
		connectToService();

		currentHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				FileManagerActivity.this.handleMessage(msg);
			}
		};

		progressHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				FileManagerActivity.this.handleProgress(msg);
			}
		};

		encryptedFileHandler = new Handler()
		{
			public void handleMessage(Message msg)
			{
				FileManagerActivity.this.handleEncryptedFile(msg);
			}
		};

		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setContentView(R.layout.filelist);

		mEmptyText = (TextView) findViewById(R.id.empty_text);
		mProgressBar = (ProgressBar) findViewById(R.id.scan_progress);

		getListView().setOnCreateContextMenuListener(this);
		getListView().setEmptyView(findViewById(R.id.empty));
		getListView().setTextFilterEnabled(true);
		getListView().requestFocus();
		getListView().requestFocusFromTouch();

		mDirectoryButtons = (LinearLayout) findViewById(R.id.directory_buttons);
		mEditFilename = (EditText) findViewById(R.id.filename);

		mButtonPick = (Button) findViewById(R.id.button_pick);

		mButtonPick.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View arg0)
			{
				pickFileOrDirectory();
			}
		});

		// Initialize only when necessary:
		mDirectoryInput = null;

		// Create map of extensions:
		getMimeTypes();

		getSdCardPath();

		mState = STATE_BROWSE;

		Intent intent = getIntent();
		String action = intent.getAction();

		File browseto = new File("/");

		if (!TextUtils.isEmpty(mSdCardPath))
		{
			browseto = new File(mSdCardPath);
		}

		// Default state
		mState = STATE_BROWSE;

		if (action != null)
		{
			if (action.equals(FileManagerIntents.ACTION_PICK_FILE))
			{
				mState = STATE_PICK_FILE;
			} else if (action.equals(FileManagerIntents.ACTION_PICK_DIRECTORY))
			{
				mState = STATE_PICK_DIRECTORY;

				// Remove edit text and make button fill whole line
				mEditFilename.setVisibility(View.GONE);
				mButtonPick.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
						LinearLayout.LayoutParams.WRAP_CONTENT));
			}
		}

		if (mState == STATE_BROWSE)
		{
			// Remove edit text and button.
			mEditFilename.setVisibility(View.GONE);
			mButtonPick.setVisibility(View.GONE);
		}

		// Set current directory and file based on intent data.
		File file = FileUtils.getFile(intent.getData());
		if (file != null)
		{
			File dir = FileUtils.getPathWithoutFilename(file);
			if (dir.isDirectory())
			{
				browseto = dir;
			}
			if (!file.isDirectory())
			{
				mEditFilename.setText(file.getName());
			}
		}

		String title = intent.getStringExtra(FileManagerIntents.EXTRA_TITLE);
		if (title != null)
		{
			setTitle(title);
		}

		String buttontext = intent.getStringExtra(FileManagerIntents.EXTRA_BUTTON_TEXT);
		if (buttontext != null)
		{
			mButtonPick.setText(buttontext);
		}

		mStepsBack = 0;

		if (icicle != null)
		{
			browseto = new File(icicle.getString(BUNDLE_CURRENT_DIRECTORY));
			mContextFile = new File(icicle.getString(BUNDLE_CONTEXT_FILE));
			mContextText = icicle.getString(BUNDLE_CONTEXT_TEXT);

			boolean show = icicle.getBoolean(BUNDLE_SHOW_DIRECTORY_INPUT);
			showDirectoryInput(show);

			mStepsBack = icicle.getInt(BUNDLE_STEPS_BACK);
		}

		browseTo(browseto);
	}

	public void onDestroy()
	{
		if (cardChannel != null)
			try
			{
				cardChannel.close();
				Log.i(TAG, "Closing smartcard channel");
			} catch (CardException e)
			{
				e.printStackTrace();
			}

		if (smartcard != null)
			smartcard.shutdown();

		// must be done, after the cardchannel is closed and smartcard shutdown
		super.onDestroy();

		// Stop the scanner.
		DirectoryScanner scanner = mDirectoryScanner;

		if (scanner != null)
		{
			scanner.cancel = true;
		}

		mDirectoryScanner = null;

		ThumbnailLoader loader = mThumbnailLoader;

		if (loader != null)
		{
			loader.cancel = true;
			mThumbnailLoader = null;
		}
	}

	/**
	 * Handles the Message for the {@link #encryptedFileHandler}<br>
	 * If the Progressbar {@link #progressDialog} doesn't exist, a new one will be created. <br>
	 * It will be deleted if the progress bigger than 100.
	 * 
	 * @param message Filename of the encrypted file, or the occured error
	 */
	private void handleEncryptedFile(Message message)
	{
		if(message.getData().containsKey("error")){
			String error = message.getData().getString("error");
			Log.i(TAG, "Error: " + error);
			
		} else	if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
			if (message.getData().containsKey("file"))
			{
				String filename = message.getData().getString("file");
				Log.i(TAG, filename);
				progressDialog.setMessage(this.getString(R.string.ciphering) + " " + filename + "! " + this.getString(R.string.wait));
			} else
			{
				progressDialog.setMessage(this.getString(R.string.ciphering) + " " + this.getString(R.string.wait));
			}
			
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(false);
			progressDialog.setProgress(0);
			progressDialog.show();
		}

	}
	
	/**
	 * Handles the Message for the {@link #progressHandler}<br>
	 * If the Progressbar {@link #progressDialog} doesn't exist, a new one will be created. <br>
	 * It will be deleted if the progress bigger 100.
	 * 
	 * @param message Progress of the current file, that gets ciphered
	 */
	private void handleProgress(Message message)
	{
		if (progressDialog == null)
		{
			progressDialog = new ProgressDialog(this);
			progressDialog.setMessage(this.getString(R.string.ciphering) + " " + this.getString(R.string.wait));
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setCancelable(false);
			progressDialog.setProgress(0);
			progressDialog.show();
		}

		int progress = message.what;

		if (progress == 0)
		{
			progressDialog.setProgress(0);
		} else if (progress > 100)
		{
			if (progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
		} else
		{
			progressDialog.setProgress(progress);
		}
		message.getData().clear();
	}

	/**
	 * Handles the Message for the {@link #currentHandler}<br>
	 * 
	 * @param message Message what should be done. Choose one of the following opportunities:<br>
	 * {@link #MESSAGE_DECRYPT}<br>{@link #MESSAGE_DECRYPTONTHEFLY}<br>{@link #MESSAGE_ENCRYPT}<br>{@link #MESSAGE_ICON_CHANGED}<br>
	 * {@link #MESSAGE_REFRESH_LIST}<br>{@link #MESSAGE_SET_PROGRESS}<br>{@link #MESSAGE_SHOW_DECRYPT_ERROR}<br>
	 * {@link #MESSAGE_SHOW_DIRECTORY_CONTENTS}<br>{@link #MESSAGE_SHOW_ENCRYPT_ERROR}<br>{@link #MESSAGE_SHOW_IS_NOT_ENCRYPTED}
	 */
	private void handleMessage(Message message)
	{
		Log.i(TAG, "Received message " + message.what);

		switch (message.what)
			{
			case MESSAGE_SHOW_DIRECTORY_CONTENTS:
				showDirectoryContents((DirectoryContents) message.obj);
				break;

			case MESSAGE_SET_PROGRESS:
				setProgress(message.arg1, message.arg2);
				break;

			case MESSAGE_REFRESH_LIST:
				refreshList();
				break;

			case MESSAGE_ICON_CHANGED:
				notifyIconChanged((IconifiedText) message.obj);
				break;

			case MESSAGE_ENCRYPT:
				lockScreenRotation();
				progressDialog = null;
				//encrypt in a new Thread
				//because of this the Alertdialog is closed
				Thread t = new Thread()
				{
					public void run()
					{
						encrypt();
						unlockScreenRotation();
					}
				};
				t.start();

				break;

			case MESSAGE_DECRYPT:
				lockScreenRotation();
				progressDialog = null;
				//decrypt in a new Thread
				//because of this the Alertdialog is closed
				Thread th = new Thread()
				{
					public void run()
					{
						decrypt();
						unlockScreenRotation();
					}
				};
				th.start();

				break;

			case MESSAGE_DECRYPTONTHEFLY:

				lockScreenRotation();
				progressDialog = null;
				//decryptOnTheFly in a new Thread
				//because of this the Alertdialog is closed
				Thread thread = new Thread()
				{
					public void run()
					{
						decryptOnTheFly();
						unlockScreenRotation();
					}
				};
				thread.start();
				
				break;

			case MESSAGE_SHOW_DECRYPT_ERROR:

				Toast.makeText(FileManagerActivity.this, R.string.failed_decrypt, Toast.LENGTH_SHORT).show();
				break;

			case MESSAGE_SHOW_ENCRYPT_ERROR:

				Toast.makeText(FileManagerActivity.this, R.string.failed_encrypt, Toast.LENGTH_SHORT).show();
				break;
				
			case MESSAGE_SHOW_IS_NOT_ENCRYPTED:
			
			Toast.makeText(FileManagerActivity.this, R.string.not_encrypted, Toast.LENGTH_SHORT).show();
			break;
			
			case MESSAGE_CAN_NOT_OPEN_FILE:
			Toast.makeText(this, R.string.can_not_open_file, Toast.LENGTH_SHORT).show();
			break;
			}
	}

	/**
	 * Call the method to encrypt the {@link #mContextFile}
	 */
	private void encrypt()
	{
		Log.i(TAG, "Encrypt Data");
		//create new CipheredFile to make ciphering operations on it
		CipheredFile encFile = new CipheredFile(mContextFile.getAbsolutePath(), cardChannel, passphrase,
				progressHandler, encryptedFileHandler);
		try
		{
			//encrypt File
			encFile.encrypt();
			Log.i(TAG, "Successfully encrypted");
		} catch (CardException e)
		{
			currentHandler.sendEmptyMessage(MESSAGE_SHOW_ENCRYPT_ERROR);
		}finally{
			if (progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
			unlockScreenRotation();
			currentHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
		}
	}

	private void decrypt()
	{
		Log.i(TAG, "Decrypt Data");
		//create new CipheredFile to make ciphering operations on it
		CipheredFile decFile = new CipheredFile(mContextFile.getAbsolutePath(), cardChannel, passphrase,
				progressHandler, encryptedFileHandler);
		try
		{
			//decrypt File
			decFile.decrypt();
			Log.i(TAG, "Successfully decrypted");
		} catch (Exception e)
		{
			if(e.getMessage().equals("File is not encrypted")){
				currentHandler.sendEmptyMessage(MESSAGE_SHOW_IS_NOT_ENCRYPTED);
			} else
			{
				currentHandler.sendEmptyMessage(MESSAGE_SHOW_DECRYPT_ERROR);
			}
		}finally{
			if (progressDialog != null)
			{
				progressDialog.dismiss();
				progressDialog = null;
			}
			unlockScreenRotation();
			currentHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
		}
	}

	private void decryptOnTheFly()
	{
		Log.i(TAG, "Decrypt Data on the fly");
		lockScreenRotation();
		//create new CipheredFile to make ciphering operations on it
		CipheredFile decFile = new CipheredFile(mContextFile.getAbsolutePath(), cardChannel, passphrase,
				progressHandler, encryptedFileHandler);
		
		try
		{
			//decrypt File on the Fly
			decFile.decryptOnTheFly();
			Log.i(TAG, "Successfully decrypted");
			
			//Open File
			Log.i(TAG, "Open decrypted File");
			// updated path of the encrypted file
			String filePath = mContextFile.getAbsolutePath();
			int index = filePath.lastIndexOf("/") + 1;
			mContextFile = new File(filePath.substring(0, index) + "tmp_" + filePath.substring(index, filePath.length() - 4));
			
			Log.i(TAG, "End decrypt on the fly");
			
		} catch (Exception e)
		{
			currentHandler.sendEmptyMessage(MESSAGE_SHOW_DECRYPT_ERROR);
		}finally{
			currentHandler.sendEmptyMessage(MESSAGE_REFRESH_LIST);
			unlockScreenRotation();
		}
		
		//try to open the file
		try
		{
			openFile(mContextFile, true); // rekursiv
		} catch (Exception e)
		{
			currentHandler.sendEmptyMessage(MESSAGE_CAN_NOT_OPEN_FILE);
		}
		
	}

	/**
	 * Lock Screenrotation while performing an action
	 */
	private void lockScreenRotation()
	{
		// Stop the screen orientation changing during an event
		switch (this.getResources().getConfiguration().orientation)
			{
			case Configuration.ORIENTATION_PORTRAIT:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
				break;
			case Configuration.ORIENTATION_LANDSCAPE:
				this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
				break;
			}
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig)
	{
		super.onConfigurationChanged(newConfig);
	}

	/**
	 * Unlock Screenrotation
	 */
	private void unlockScreenRotation()
	{
		// allow screen rotations again
		this.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
	}

	private void notifyIconChanged(IconifiedText text)
	{
		if (getListAdapter() != null)
		{
			((BaseAdapter) getListAdapter()).notifyDataSetChanged();
		}
	}

	private void setProgress(int progress, int maxProgress)
	{
		mProgressBar.setMax(maxProgress);
		mProgressBar.setProgress(progress);
		mProgressBar.setVisibility(View.VISIBLE);
	}

	private void showDirectoryContents(DirectoryContents contents)
	{
		mDirectoryScanner = null;

		mListSdCard = contents.listSdCard;
		mListDir = contents.listDir;
		mListFile = contents.listFile;

		directoryEntries.ensureCapacity(mListSdCard.size() + mListDir.size() + mListFile.size());

		addAllElements(directoryEntries, mListSdCard);
		addAllElements(directoryEntries, mListDir);
		addAllElements(directoryEntries, mListFile);

		IconifiedTextListAdapter itla = new IconifiedTextListAdapter(this);
		itla.setListItems(directoryEntries, getListView().hasTextFilter());
		setListAdapter(itla);
		getListView().setTextFilterEnabled(true);

		selectInList(mPreviousDirectory);
		refreshDirectoryPanel();
		setProgressBarIndeterminateVisibility(false);

		mProgressBar.setVisibility(View.GONE);
		mEmptyText.setVisibility(View.VISIBLE);

		mThumbnailLoader = new ThumbnailLoader(currentDirectory, mListFile, currentHandler, this);
		mThumbnailLoader.start();
	}

	private void onCreateDirectoryInput()
	{
		mDirectoryInput = (LinearLayout) findViewById(R.id.directory_input);
		mEditDirectory = (EditText) findViewById(R.id.directory_text);

		mButtonDirectoryPick = (ImageButton) findViewById(R.id.button_directory_pick);

		mButtonDirectoryPick.setOnClickListener(new View.OnClickListener()
		{

			public void onClick(View arg0)
			{
				goToDirectoryInEditText();
			}
		});
	}

	// private boolean mHaveShownErrorMessage;
	private File mHaveShownErrorMessageForFile = null;

	private void goToDirectoryInEditText()
	{
		File browseto = new File(mEditDirectory.getText().toString());

		if (browseto.equals(currentDirectory))
		{
			showDirectoryInput(false);
		} else
		{
			if (mHaveShownErrorMessageForFile != null && mHaveShownErrorMessageForFile.equals(browseto))
			{
				// Don't let user get stuck in wrong directory.
				mHaveShownErrorMessageForFile = null;
				showDirectoryInput(false);
			} else
			{
				if (!browseto.exists())
				{
					// browseTo() below will show an error message,
					// because file does not exist.
					// It is ok to show this the first time.
					mHaveShownErrorMessageForFile = browseto;
				}
				browseTo(browseto);
			}
		}
	}

	/**
	 * Show the directory line as input box instead of button row. If Directory
	 * input does not exist yet, it is created. Since the default is show ==
	 * false, nothing is created if it is not necessary (like after icicle).
	 * 
	 * @param show
	 */
	private void showDirectoryInput(boolean show)
	{
		if (show)
		{
			if (mDirectoryInput == null)
			{
				onCreateDirectoryInput();
			}
		}
		if (mDirectoryInput != null)
		{
			mDirectoryInput.setVisibility(show ? View.VISIBLE : View.GONE);
			mDirectoryButtons.setVisibility(show ? View.GONE : View.VISIBLE);
		}

		refreshDirectoryPanel();
	}

	/**
	 * Refreshes the DirectoryPane, if its visible
	 */
	private void refreshDirectoryPanel()
	{
		if (isDirectoryInputVisible())
		{
			// Set directory path
			String path = currentDirectory.getAbsolutePath();
			mEditDirectory.setText(path);

			// Set selection to last position so user can continue to type:
			mEditDirectory.setSelection(path.length());
		} else
		{
			setDirectoryButtons();
		}
	}

	/*
	 * @Override protected void onResume() { super.onResume(); }
	 */

	@Override
	protected void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		// remember file name
		outState.putString(BUNDLE_CURRENT_DIRECTORY, currentDirectory.getAbsolutePath());
		outState.putString(BUNDLE_CONTEXT_FILE, mContextFile.getAbsolutePath());
		outState.putString(BUNDLE_CONTEXT_TEXT, mContextText);
		boolean show = isDirectoryInputVisible();
		outState.putBoolean(BUNDLE_SHOW_DIRECTORY_INPUT, show);
		outState.putInt(BUNDLE_STEPS_BACK, mStepsBack);

		// remember pwd
		outState.putBoolean(BUNDLE_VALID_PIN, validPin);

		// remember CheckedPin
		outState.putBoolean(BUNDLE_PIN_CHECKED, pinChecked);

		// remember pinCounter
		// outState.putInt(BUNDLE_PIN_COUNTER, pinCounter);
	}

	/**
	 * @return
	 */
	private boolean isDirectoryInputVisible()
	{
		return ((mDirectoryInput != null) && (mDirectoryInput.getVisibility() == View.VISIBLE));
	}

	private void pickFileOrDirectory()
	{
		File file = null;
		if (mState == STATE_PICK_FILE)
		{
			String filename = mEditFilename.getText().toString();
			file = FileUtils.getFile(currentDirectory.getAbsolutePath(), filename);
		} else if (mState == STATE_PICK_DIRECTORY)
		{
			file = currentDirectory;
		}

		Intent intent = getIntent();
		intent.setData(FileUtils.getUri(file));
		setResult(RESULT_OK, intent);
		finish();
	}

	/**
	 * 
	 */
	private void getMimeTypes()
	{
		MimeTypeParser mtp = new MimeTypeParser();

		XmlResourceParser in = getResources().getXml(R.xml.mimetypes);

		try
		{
			mMimeTypes = mtp.fromXmlResource(in);
		} catch (XmlPullParserException e)
		{
			Log.e(TAG, "PreselectedChannelsActivity: XmlPullParserException", e);
			throw new RuntimeException("PreselectedChannelsActivity: XmlPullParserException");
		} catch (IOException e)
		{
			Log.e(TAG, "PreselectedChannelsActivity: IOException", e);
			throw new RuntimeException("PreselectedChannelsActivity: IOException");
		}
	}

	/**
	 * This function browses up one level according to the field:
	 * currentDirectory
	 */
	private void upOneLevel()
	{
		if (mStepsBack > 0)
		{
			mStepsBack--;
		}
		if (currentDirectory.getParent() != null)
			browseTo(currentDirectory.getParentFile());
	}

	/**
	 * Jump to some location by clicking on a directory button.
	 * 
	 * This resets the counter for "back" actions.
	 * 
	 * @param aDirectory
	 */
	private void jumpTo(final File aDirectory)
	{
		mStepsBack = 0;
		browseTo(aDirectory);
	}

	/**
	 * Browse to some location by clicking on a list item.
	 * 
	 * @param aDirectory
	 */
	private void browseTo(final File aDirectory)
	{
		// setTitle(aDirectory.getAbsolutePath());

		if (aDirectory.isDirectory())
		{
			if (aDirectory.equals(currentDirectory))
			{
				// Switch from button to directory input
				showDirectoryInput(true);
			} else
			{
				mPreviousDirectory = currentDirectory;
				currentDirectory = aDirectory;
				refreshList();
				// selectInList(previousDirectory);
				// refreshDirectoryPanel();
			}
		} else
		{
			if (mState == STATE_BROWSE || mState == STATE_PICK_DIRECTORY)
			{
				// Lets start an intent to View the file, that was clicked...
				openFile(aDirectory, false);
			} else if (mState == STATE_PICK_FILE)
			{
				// Pick the file
				mEditFilename.setText(aDirectory.getName());
			}
		}
	}

	/**
	 * Open a File with an intent
	 * @param aFile
	 * @param decryptOnTheFly true, if a further encrypted file has to be shown, and the tmp file should get deleted
	 */
	private void openFile(File aFile, boolean decryptOnTheFly)
	{
		if (!aFile.exists())
		{
			Toast.makeText(this, R.string.error_file_does_not_exists, Toast.LENGTH_SHORT).show();
			return;
		}

		//decrypt on the fly and call the open Method recursive
		if (aFile.getAbsolutePath().endsWith(".enc") && validPin)
		{
			if (Helper.isFileEncrypted(aFile))
			{
				mContextFile = aFile;
				// Dialog anzeigen
				passphrase = null;
				showDialog(DIALOG_DECRYPT_ONTHEFLY);
				return;
			} 
		}

		Intent intent = new Intent(android.content.Intent.ACTION_VIEW);

		Uri data = FileUtils.getUri(aFile);
		String type = mMimeTypes.getMimeType(aFile.getName());
		intent.setDataAndType(data, type);

		// Were we in GET_CONTENT mode?
		Intent originalIntent = getIntent();

		if (originalIntent != null && originalIntent.getAction() != null
				&& originalIntent.getAction().equals(Intent.ACTION_GET_CONTENT))
		{
			// In that case, we should probably just return the requested data.
			setResult(RESULT_OK, intent);
			finish();
			return;
		}

		try
		{
			if (decryptOnTheFly)
			{
				startActivityForResult(intent, REQUEST_DELETE_FILE);
				try
				{
					mContextFile.deleteOnExit();
				} catch (Exception e)
				{
				}
			} else
			{
				startActivity(intent);
			}
		} catch (ActivityNotFoundException e)
		{
			Toast.makeText(this, R.string.application_not_available, Toast.LENGTH_SHORT).show();
		}
		;
	}

	/**
	 * this method refreshes the List
	 */
	private void refreshList()
	{

		// Cancel an existing scanner, if applicable.
		DirectoryScanner scanner = mDirectoryScanner;

		if (scanner != null)
		{
			scanner.cancel = true;
		}

		ThumbnailLoader loader = mThumbnailLoader;

		if (loader != null)
		{
			loader.cancel = true;
			mThumbnailLoader = null;
		}

		directoryEntries.clear();
		mListDir.clear();
		mListFile.clear();
		mListSdCard.clear();

		setProgressBarIndeterminateVisibility(true);

		// Don't show the "folder empty" text since we're scanning.
		mEmptyText.setVisibility(View.GONE);

		// Also DON'T show the progress bar - it's kind of lame to show that
		// for less than a second.
		mProgressBar.setVisibility(View.GONE);
		setListAdapter(null);

		mDirectoryScanner = new DirectoryScanner(currentDirectory, this, currentHandler, mMimeTypes, mSdCardPath);
		mDirectoryScanner.start();

		// Add the "." == "current directory"
		/*
		 * directoryEntries.add(new IconifiedText(
		 * getString(R.string.current_dir),
		 * getResources().getDrawable(R.drawable.ic_launcher_folder)));
		 */
		// and the ".." == 'Up one level'
		/*
		 * if(currentDirectory.getParent() != null) directoryEntries.add(new
		 * IconifiedText( getString(R.string.up_one_level),
		 * getResources().getDrawable(R.drawable.ic_launcher_folder_open)));
		 */
	}

	private void selectInList(File selectFile)
	{
		String filename = selectFile.getName();
		IconifiedTextListAdapter la = (IconifiedTextListAdapter) getListAdapter();
		int count = la.getCount();
		for (int i = 0; i < count; i++)
		{
			IconifiedText it = (IconifiedText) la.getItem(i);
			if (it.getText().equals(filename))
			{
				getListView().setSelection(i);
				break;
			}
		}
	}

	private void addAllElements(List<IconifiedText> addTo, List<IconifiedText> addFrom)
	{
		int size = addFrom.size();
		for (int i = 0; i < size; i++)
		{
			addTo.add(addFrom.get(i));
		}
	}

	private void setDirectoryButtons()
	{
		String[] parts = currentDirectory.getAbsolutePath().split("/");

		mDirectoryButtons.removeAllViews();

		int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;

		// Add home button separately
		ImageButton ib = new ImageButton(this);
		ib.setImageResource(R.drawable.ic_launcher_home_small);
		ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
		ib.setOnClickListener(new View.OnClickListener()
		{
			public void onClick(View view)
			{
				jumpTo(new File("/"));
			}
		});
		mDirectoryButtons.addView(ib);

		// Add other buttons

		String dir = "";

		for (int i = 1; i < parts.length; i++)
		{
			dir += "/" + parts[i];
			if (dir.equals(mSdCardPath))
			{
				// Add SD card button
				ib = new ImageButton(this);
				ib.setImageResource(R.drawable.icon_sdcard_small);
				ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
				ib.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View view)
					{
						jumpTo(new File(mSdCardPath));
					}
				});
				mDirectoryButtons.addView(ib);
			} else
			{
				Button b = new Button(this);
				b.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
				b.setText(parts[i]);
				b.setTag(dir);
				b.setOnClickListener(new View.OnClickListener()
				{
					public void onClick(View view)
					{
						String dir = (String) view.getTag();
						jumpTo(new File(dir));
					}
				});
				mDirectoryButtons.addView(b);
			}
		}

		// This code is to add the Imagebutton for the lock and unlock option
		if (cardChannel != null && !cardChannel.isClosed())
		{
			ImageButton crypt = new ImageButton(this);
			crypt.setImageResource(validPin ? R.drawable.ic_lockopen : R.drawable.ic_lock);
			crypt.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
			crypt.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					// Pinabfrage
					if (validPin == false)
					{
						showDialog(DIALOG_PIN);
					} else
					{
						Toast.makeText(FileManagerActivity.this, R.string.ciphering_disabled, Toast.LENGTH_LONG)
								.show();
						validPin = false;
						setDirectoryButtons();
					}
				}
			});
			mDirectoryButtons.addView(crypt);
		}

		checkButtonLayout();
	}

	private void checkButtonLayout()
	{
		// Let's measure how much space we need:
		int spec = View.MeasureSpec.UNSPECIFIED;
		mDirectoryButtons.measure(spec, spec);
		// int count = mDirectoryButtons.getChildCount();

		int requiredwidth = mDirectoryButtons.getMeasuredWidth();
		int width = getWindowManager().getDefaultDisplay().getWidth();

		if (requiredwidth > width)
		{
			int WRAP_CONTENT = LinearLayout.LayoutParams.WRAP_CONTENT;

			// Create a new button that shows that there is more to the left:
			ImageButton ib = new ImageButton(this);
			ib.setImageResource(R.drawable.ic_menu_back_small);
			ib.setLayoutParams(new LinearLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT));
			// 
			ib.setOnClickListener(new View.OnClickListener()
			{
				public void onClick(View view)
				{
					// Up one directory.
					upOneLevel();
				}
			});
			mDirectoryButtons.addView(ib, 0);

			// New button needs even more space
			ib.measure(spec, spec);
			requiredwidth += ib.getMeasuredWidth();

			// Need to take away some buttons
			// but leave at least "back" button and one directory button.
			while (requiredwidth > width && mDirectoryButtons.getChildCount() > 2)
			{
				View view = mDirectoryButtons.getChildAt(1);
				requiredwidth -= view.getMeasuredWidth();

				mDirectoryButtons.removeViewAt(1);
			}
		}
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);

		IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();

		if (adapter == null)
		{
			return;
		}

		IconifiedText text = (IconifiedText) adapter.getItem(position);

		String file = text.getText();
		/*
		 * if (selectedFileString.equals(getString(R.string.up_one_level))) {
		 * upOneLevel(); } else {
		 */
		String curdir = currentDirectory.getAbsolutePath();
		File clickedFile = FileUtils.getFile(curdir, file);
		if (clickedFile != null)
		{
			if (clickedFile.isDirectory())
			{
				// If we click on folders, we can return later by the "back"
				// key.
				mStepsBack++;
			}
			browseTo(clickedFile);
		}
		/*
		 * }
		 */
	}

	private void getSdCardPath()
	{
		mSdCardPath = android.os.Environment.getExternalStorageDirectory().getAbsolutePath();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		super.onCreateOptionsMenu(menu);

		menu.add(0, MENU_NEW_FOLDER, 0, R.string.menu_new_folder).setIcon(android.R.drawable.ic_menu_add).setShortcut(
				'0', 'f');

		UpdateMenu.addUpdateMenu(this, menu, 0, MENU_UPDATE, 0, R.string.update);
		menu.add(0, MENU_ABOUT, 0, R.string.about).setIcon(android.R.drawable.ic_menu_info_details).setShortcut('0',
				'a');

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu)
	{
		super.onPrepareOptionsMenu(menu);

		// Generate any additional actions that can be performed on the
		// overall list. This allows other applications to extend
		// our menu with their own actions.
		Intent intent = new Intent(null, getIntent().getData());
		intent.addCategory(Intent.CATEGORY_ALTERNATIVE);
		// menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0,
		// new ComponentName(this, NoteEditor.class), null, intent, 0, null);

		// Workaround to add icons:
		MenuIntentOptionsWithIcons menu2 = new MenuIntentOptionsWithIcons(this, menu);
		menu2.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, FileManagerActivity.class),
				null, intent, 0, null);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		// Intent intent;
		switch (item.getItemId())
			{
			case MENU_NEW_FOLDER:
				showDialog(DIALOG_NEW_FOLDER);
				return true;

			case MENU_UPDATE:
				UpdateMenu.showUpdateBox(this);
				return true;

			case MENU_ABOUT:
				showAboutBox();
				return true;
				/*
				 * case MENU_PREFERENCES: intent = new Intent(this,
				 * PreferenceActivity.class); startActivity(intent); return
				 * true;
				 */
			}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo)
	{
		AdapterView.AdapterContextMenuInfo info;
		try
		{
			info = (AdapterView.AdapterContextMenuInfo) menuInfo;
		} catch (ClassCastException e)
		{
			Log.e(TAG, "bad menuInfo", e);
			return;
		}
		/*
		 * Cursor cursor = (Cursor) getListAdapter().getItem(info.position); if
		 * (cursor == null) { // For some reason the requested item isn't
		 * available, do nothing return; }
		 */
		IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();

		if (adapter == null)
		{
			return;
		}

		IconifiedText it = (IconifiedText) adapter.getItem(info.position);
		menu.setHeaderTitle(it.getText());
		menu.setHeaderIcon(it.getIcon());
		File file = FileUtils.getFile(currentDirectory, it.getText());

		//add Menueitems
//		boolean encrypted = file.toString().endsWith(".enc");
		boolean encrypted = Helper.isFileEncrypted(file);
		if (validPin && !encrypted)
		{
			menu.add(0, MENU_ENCRYPT, 0, R.string.menu_encrypt);
		}
		if ((validPin && encrypted) || (validPin && file.isDirectory()))
		{
			menu.add(0, MENU_DECRYPT, 0, R.string.menu_decrypt);
		}

		if (!file.isDirectory())
		{
			if (mState == STATE_PICK_FILE)
			{
				// Show "open" menu
				menu.add(0, MENU_OPEN, 0, R.string.menu_open);
			}
			menu.add(0, MENU_SEND, 0, R.string.menu_send);
		}
		menu.add(0, MENU_MOVE, 0, R.string.menu_move);

		if (!file.isDirectory())
		{
			menu.add(0, MENU_COPY, 0, R.string.menu_copy);
		}

		menu.add(0, MENU_RENAME, 0, R.string.menu_rename);
		menu.add(0, MENU_DELETE, 0, R.string.menu_delete);

		// if (!file.isDirectory()) {
		Uri data = Uri.fromFile(file);
		Intent intent = new Intent(null, data);
		String type = mMimeTypes.getMimeType(file.getName());

		intent.setDataAndType(data, type);
		// intent.addCategory(Intent.CATEGORY_SELECTED_ALTERNATIVE);

		Log.i(TAG, "Data=" + data);
		Log.i(TAG, "Type=" + type);

		if (type != null)
		{
			// Add additional options for the MIME type of the selected file.
			menu.addIntentOptions(Menu.CATEGORY_ALTERNATIVE, 0, 0, new ComponentName(this, FileManagerActivity.class),
					null, intent, 0, null);
		}
		// }
	}

	@Override
	public boolean onContextItemSelected(MenuItem item)
	{
		super.onContextItemSelected(item);
		AdapterContextMenuInfo menuInfo = (AdapterContextMenuInfo) item.getMenuInfo();

		// Remember current selection
		IconifiedTextListAdapter adapter = (IconifiedTextListAdapter) getListAdapter();

		if (adapter == null)
		{
			return false;
		}

		IconifiedText ic = (IconifiedText) adapter.getItem(menuInfo.position);
		mContextText = ic.getText();
		mContextIcon = ic.getIcon();
		mContextFile = FileUtils.getFile(currentDirectory, ic.getText());

		//if a menuitem is clicked
		switch (item.getItemId())
			{
			case MENU_OPEN:
				openFile(mContextFile, false);
				return true;

			case MENU_MOVE:
				promptDestinationAndMoveFile();
				return true;

			case MENU_COPY:
				promptDestinationAndCopyFile();
				return true;

			case MENU_DELETE:
				showDialog(DIALOG_DELETE);
				return true;

			case MENU_RENAME:
				showDialog(DIALOG_RENAME);
				return true;

			case MENU_SEND:
				sendFile(mContextFile);
				return true;

			case MENU_DECRYPT:
				// Menu to encrpyt the file
				Log.i(TAG, "Start Decrypt");
				if (!Helper.isFileEncrypted(mContextFile) && !mContextFile.isDirectory())
				{
					Toast.makeText(this, R.string.not_encrypted, Toast.LENGTH_SHORT).show();
					Log.i(TAG, "Not Encrypted! abort!");
					return true;
				}
				showDialog(DIALOG_DECRYPT);

				return true;

				// Menu to decrypt the file
			case MENU_ENCRYPT:
				Log.i(TAG, "Start Encrypt");

				if (Helper.isFileEncrypted(mContextFile))
				{
					Toast.makeText(this, R.string.already_encrypted, Toast.LENGTH_SHORT).show();
					Log.i(TAG, "Already Encrypted! abort!");
					return true;
				}
				showDialog(DIALOG_ENCRYPT);

				return true;
			}

		return false;
	}

	@Override
	protected Dialog onCreateDialog(int id)
	{
		switch (id)
			{
			case DIALOG_NEW_FOLDER:
				LayoutInflater inflater = LayoutInflater.from(this);
				View view = inflater.inflate(R.layout.dialog_new_folder, null);
				final EditText etext1 = (EditText) view.findViewById(R.id.foldername);
				etext1.setText("");
				return new AlertDialog.Builder(this).setIcon(android.R.drawable.ic_dialog_alert).setTitle(
						R.string.create_new_folder).setView(view).setPositiveButton(android.R.string.ok,
						new OnClickListener()
						{

							public void onClick(DialogInterface dialog, int which)
							{
								createNewFolder(etext1.getText().toString());
							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						// Cancel should'nt do anything.
					}

				}).create();

			case DIALOG_DELETE:
				return new AlertDialog.Builder(this).setTitle(getString(R.string.really_delete, mContextText)).setIcon(
						android.R.drawable.ic_dialog_alert).setPositiveButton(android.R.string.ok,
						new OnClickListener()
						{

							public void onClick(DialogInterface dialog, int which)
							{
								deleteFileOrFolder(mContextFile);
							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						// Cancel should not do anything.
					}

				}).create();

			case DIALOG_RENAME:
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.dialog_new_folder, null);
				final EditText etext2 = (EditText) view.findViewById(R.id.foldername);
				return new AlertDialog.Builder(this).setTitle(R.string.menu_rename).setView(view).setPositiveButton(
						android.R.string.ok, new OnClickListener()
						{

							public void onClick(DialogInterface dialog, int which)
							{

								renameFileOrFolder(mContextFile, etext2.getText().toString());
							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						// Cancel should not do anything.
					}

				}).create();
			case DIALOG_PIN:
				Log.i(TAG, "Pin Dialog");
				// Handle Pin Dialog
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.pin_request_dialog, null);
				final TextView textview_pin = (TextView) view.findViewById(R.id.pin_input);
				textview_pin.setText(new String(""));
				return new AlertDialog.Builder(this).setTitle("Pin Dialog").setIcon(android.R.drawable.ic_dialog_alert)
						.setPositiveButton(android.R.string.ok, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								String pin = textview_pin.getText().toString();
								byte[] asciiHex = new byte[pin.length()];

								// convert input into ascii-hex bytes
								for (int i = 0; i < pin.length(); i++)
									asciiHex[i] = (byte) (pin.charAt(i));
								byte[] rsp = null;

								validPin = false;
								pinChecked = true;

								// Check the correct length of the Pin
//								if (asciiHex.length != 4)
//								{
//									Log.e(TAG, "PIN has not the correct length");
//									Toast.makeText(FileManagerActivity.this, "PIN is not correct!", Toast.LENGTH_LONG)
//											.show();
//								} 
								
									CardCommandBuilder verifyPin = new CardCommandBuilder(
											CardCommandBuilder.CardCommand.VerifyPin, asciiHex);
									byte[] verifyAPDU = verifyPin.getCommandBytes();
									try
									{
										Log.i(TAG, "-> " + Helper.bytesToString(verifyAPDU));
										rsp = cardChannel.transmit(verifyAPDU);

									if ((rsp[rsp.length - 2] & 0xFF) != 0x90 || (rsp[rsp.length - 1] & 0xFF) != 0x00)
									{
										Toast.makeText(FileManagerActivity.this,
												R.string.wrong_pin,
												Toast.LENGTH_LONG).show();
									} else
									{
										validPin = true;
										// refesh this here to make sure
										// that key button will be shown in
										// correct manner
											refreshDirectoryPanel();
											Log.i(TAG, "PIN is correct");
											Toast.makeText(FileManagerActivity.this, R.string.correct_pin,
													Toast.LENGTH_LONG).show();
										}
										Log.i(TAG, "<- " + Helper.bytesToString(rsp));

									} catch (CardException e)
									{
										e.printStackTrace();
									}
//								}
								textview_pin.setText("");
							}
						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
						{

							public void onClick(DialogInterface dialog, int which)
							{
								textview_pin.setText("");
							}
						}).setView(view).create();

			case DIALOG_ENCRYPT:
				passphrase = null; // global handled passphrase
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.dialog_encrypt, null);
				final EditText etextPwd1 = (EditText) view.findViewById(R.id.foldername);
				etextPwd1.setText(new String(""));
				return new AlertDialog.Builder(this).setTitle(R.string.menu_encrypt).setView(view).setPositiveButton(
						android.R.string.ok, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								//read password
								passphrase = filterUnusualCharacters(etextPwd1.getText().toString());
								Log.i(TAG, "Try to encrypt with " + passphrase);

								Log.i(TAG, "Encrypt Data");
								
								//encrypt file
								currentHandler.sendMessage(currentHandler.obtainMessage(MESSAGE_ENCRYPT));

								etextPwd1.setText("");
							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{
					public void onClick(DialogInterface dialog, int which)
					{
						Log.i(TAG, "Encrypt abort");
						etextPwd1.setText("");
					}
				}).create();

			case DIALOG_DECRYPT_ONTHEFLY:
				passphrase = null; // global handled passphrase
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.dialog_decrypt, null);
				final EditText etextpwd2 = (EditText) view.findViewById(R.id.foldername);
				etextpwd2.setText(new String(""));
				// passwd
				return new AlertDialog.Builder(this).setTitle(R.string.menu_decrypt).setView(view).setPositiveButton(
						android.R.string.ok, new OnClickListener()
						{

							public void onClick(DialogInterface dialog, int which)
							{
								Log.i(TAG, "Start decrypting on the fly");

								//read password
								passphrase = filterUnusualCharacters(etextpwd2.getText().toString());

								// encrypt on the fly
								Log.i(TAG, "Decrpt into temp File");
								currentHandler.sendMessage(currentHandler.obtainMessage(MESSAGE_DECRYPTONTHEFLY));

							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						etextpwd2.setText("");
						Log.i(TAG, "Abort decrypt on the fly");
					}

				}).create();

			case DIALOG_DECRYPT:
				passphrase = null; // global handled passphrase
				inflater = LayoutInflater.from(this);
				view = inflater.inflate(R.layout.dialog_decrypt, null);
				final EditText etextpwd3 = (EditText) view.findViewById(R.id.foldername);
				etextpwd3.setText(new String(""));
				// passwd
				return new AlertDialog.Builder(this).setTitle(R.string.menu_decrypt).setView(view).setPositiveButton(
						android.R.string.ok, new OnClickListener()
						{
							public void onClick(DialogInterface dialog, int which)
							{
								//read password
								passphrase = filterUnusualCharacters(etextpwd3.getText().toString());
								Log.i(TAG, "Try to decrypt with " + passphrase);

								// decrypt
								Log.i(TAG, "Decrypt Data");
								currentHandler.handleMessage(currentHandler.obtainMessage(MESSAGE_DECRYPT));

								etextpwd3.setText("");
							}

						}).setNegativeButton(android.R.string.cancel, new OnClickListener()
				{

					public void onClick(DialogInterface dialog, int which)
					{
						Log.i(TAG, "Decrypt aborted");
						etextpwd3.setText("");
					}

				}).create();

			case DIALOG_ABOUT:
				return new AboutDialog(this);
			}

		return null;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog)
	{
		super.onPrepareDialog(id, dialog);

		switch (id)
			{
			case DIALOG_NEW_FOLDER:
				EditText et = (EditText) dialog.findViewById(R.id.foldername);
				et.setText("");
				break;

			case DIALOG_DELETE:
				((AlertDialog) dialog).setTitle(getString(R.string.really_delete, mContextText));
				break;

			case DIALOG_RENAME:
				et = (EditText) dialog.findViewById(R.id.foldername);
				et.setText(mContextText);
				TextView tv = (TextView) dialog.findViewById(R.id.foldernametext);
				if (mContextFile.isDirectory())
				{
					tv.setText(R.string.file_name);
				} else
				{
					tv.setText(R.string.file_name);
				}
				((AlertDialog) dialog).setIcon(mContextIcon);
				break;

			case DIALOG_ABOUT:
				break;
			}
	}

	private void showAboutBox()
	{
		// startActivity(new Intent(this, AboutDialog.class));
		AboutDialog.showDialogOrStartActivity(this, DIALOG_ABOUT);
	}

	private void promptDestinationAndMoveFile()
	{

		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

		intent.setData(FileUtils.getUri(currentDirectory));

		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.move_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.move_button));

		startActivityForResult(intent, REQUEST_CODE_MOVE);
	}

	/**
	 * Copy File
	 */
	private void promptDestinationAndCopyFile()
	{

		Intent intent = new Intent(FileManagerIntents.ACTION_PICK_DIRECTORY);

		intent.setData(FileUtils.getUri(currentDirectory));

		intent.putExtra(FileManagerIntents.EXTRA_TITLE, getString(R.string.copy_title));
		intent.putExtra(FileManagerIntents.EXTRA_BUTTON_TEXT, getString(R.string.copy_button));

		startActivityForResult(intent, REQUEST_CODE_COPY);
	}

	/**
	 * Create a new Folder in the {@link #currentDirectory}
	 * @param foldername
	 */
	private void createNewFolder(String foldername)
	{
		if (!TextUtils.isEmpty(foldername))
		{
			File file = FileUtils.getFile(currentDirectory, foldername);
			if (file.mkdirs())
			{

				// Change into new directory:
				browseTo(file);
			} else
			{
				Toast.makeText(this, R.string.error_creating_new_folder, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Recursively delete a directory and all of its children.
	 * 
	 * @param toastOnError If set to true, this function will toast if an error occurs.
	 * @param file
	 * 
	 * @returns true if successful, false otherwise.
	 */
	private boolean recursiveDelete(File file, boolean toastOnError)
	{
		// Recursively delete all contents.
		File[] files = file.listFiles();

		for (int x = 0; x < files.length; x++)
		{
			File childFile = files[x];
			if (childFile.isDirectory())
			{
				if (!recursiveDelete(childFile, toastOnError))
				{
					return false;
				}
			} else
			{
				if (!childFile.delete())
				{
					Toast.makeText(this, getString(R.string.error_deleting_child_file, childFile.getAbsolutePath()),
							Toast.LENGTH_LONG);
					return false;
				}
			}
		}

		if (!file.delete())
		{
			Toast.makeText(this, getString(R.string.error_deleting_folder, file.getAbsolutePath()), Toast.LENGTH_LONG);
			return false;
		}

		return true;
	}

	/**
	 * Delete a File or a Folder
	 * @param file File or Folder
	 */
	private void deleteFileOrFolder(File file)
	{

		if (file.isDirectory())
		{
			if (recursiveDelete(file, true))
			{
				refreshList();
				Toast.makeText(this, R.string.folder_deleted, Toast.LENGTH_SHORT).show();
			}
		} else
		{
			if (file.delete())
			{
				// Delete was successful.
				refreshList();
				Toast.makeText(this, R.string.file_deleted, Toast.LENGTH_SHORT).show();
			} else
			{
				Toast.makeText(this, R.string.error_deleting_file, Toast.LENGTH_SHORT).show();
			}
		}
	}

	/**
	 * Rename a Folder or a Folder
	 * @param file File or Folder
	 * @param newFileName
	 */
	private void renameFileOrFolder(File file, String newFileName)
	{

		File newFile = FileUtils.getFile(currentDirectory, newFileName);

		rename(file, newFile);
	}

	/**
	 * Rename a File
	 * @param oldFile
	 * @param newFile
	 */
	private void rename(File oldFile, File newFile)
	{
		int toast = 0;
		if (oldFile.renameTo(newFile))
		{
			// Rename was successful.
			refreshList();
			if (newFile.isDirectory())
			{
				toast = R.string.folder_renamed;
			} else
			{
				toast = R.string.file_renamed;
			}
		} else
		{
			if (newFile.isDirectory())
			{
				toast = R.string.error_renaming_folder;
			} else
			{
				toast = R.string.error_renaming_file;
			}
		}
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Move a File or a Folder
	 * @param oldFile
	 * @param newFile
	 */
	private void move(File oldFile, File newFile)
	{
		int toast = 0;
		if (oldFile.renameTo(newFile))
		{
			// Rename was successful.
			refreshList();
			if (newFile.isDirectory())
			{
				toast = R.string.folder_moved;
			} else
			{
				toast = R.string.file_moved;
			}
		} else
		{
			if (newFile.isDirectory())
			{
				toast = R.string.error_moving_folder;
			} else
			{
				toast = R.string.error_moving_file;
			}
		}
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	}

	/**
	 * @ RETURNS: A file name that is guaranteed to not exist yet.
	 * 
	 * PARAMS: context - Application context. path - The path that the file is
	 * supposed to be in. fileName - Desired file name. This name will be
	 * modified to create a unique file if necessary.
	 */
	private File createUniqueCopyName(Context context, File path, String fileName)
	{
		// Does that file exist?
		File file = FileUtils.getFile(path, fileName);

		if (!file.exists())
		{
			// Nope - we can take that.
			return file;
		}

		// Try a simple "copy of".
		file = FileUtils.getFile(path, context.getString(R.string.copied_file_name, fileName));

		if (!file.exists())
		{
			// Nope - we can take that.
			return file;
		}

		int copyIndex = 2;

		// Well, we gotta find a unique name at some point.
		while (copyIndex < 500)
		{
			file = FileUtils.getFile(path, context.getString(R.string.copied_file_name_2, copyIndex, fileName));

			if (!file.exists())
			{
				// Nope - we can take that.
				return file;
			}

			copyIndex++;
		}

		// I GIVE UP.
		return null;
	}

	/**
	 * Copy File
	 * @param oldFile
	 * @param newFile
	 */
	private void copy(File oldFile, File newFile)
	{
		int toast = 0;

		try
		{
			FileInputStream input = new FileInputStream(oldFile);
			FileOutputStream output = new FileOutputStream(newFile);

			byte[] buffer = new byte[COPY_BUFFER_SIZE];

			while (true)
			{
				int bytes = input.read(buffer);

				if (bytes <= 0)
				{
					break;
				}

				output.write(buffer, 0, bytes);
			}

			output.close();
			input.close();

			toast = R.string.file_copied;
			refreshList();

		} catch (Exception e)
		{
			toast = R.string.error_copying_file;
		}
		Toast.makeText(this, toast, Toast.LENGTH_SHORT).show();
	}

	/**
	 * Send a file with the Action_Send intent
	 * @param file
	 */
	private void sendFile(File file)
	{

		String filename = file.getName();
		String content = "hh";

		Log.i(TAG, "Title to send: " + filename);
		Log.i(TAG, "Content to send: " + content);

		Intent i = new Intent();
		i.setAction(Intent.ACTION_SEND);
		i.setType(mMimeTypes.getMimeType(file.getName()));
		i.putExtra(Intent.EXTRA_SUBJECT, filename);
		// i.putExtra(Intent.EXTRA_STREAM, FileUtils.getUri(file));
		i.putExtra(Intent.EXTRA_STREAM, Uri.parse("content://" + FileManagerProvider.AUTHORITY + "/mimetype/"
				+ file.getAbsolutePath()));

		i = Intent.createChooser(i, getString(R.string.menu_send));

		try
		{
			startActivity(i);
		} catch (ActivityNotFoundException e)
		{
			Toast.makeText(this, R.string.send_not_available, Toast.LENGTH_SHORT).show();
			Log.e(TAG, "Email client not installed");
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{

		if (keyCode == KeyEvent.KEYCODE_BACK)
		{
			if (mStepsBack > 0)
			{
				upOneLevel();
				return true;
			}
		}

		return super.onKeyDown(keyCode, event);
	}

	// Helper methods

	/**
	 * Filter "\n\r\t" am Ende des Strings weg
	 */
	protected String filterUnusualCharacters(String in)
	{
		while (in.endsWith("\n") || in.endsWith("\t") || in.endsWith("\r"))
			in = in.substring(0, in.length() - 2);
		String result = in;
		return result;
	}

	// For targetSdkVersion="5" or higher, one needs to use the following code
	// instead of the one above:
	// (See
	// http://android-developers.blogspot.com/2009/12/back-and-other-hard-keys-three-stories.html
	// )

	/*
	 * //@Override public boolean onKeyDown(int keyCode, KeyEvent event) { if
	 * (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.ECLAIR
	 * && keyCode == KeyEvent.KEYCODE_BACK && event.getRepeatCount() == 0) { //
	 * Take care of calling this method on earlier versions of // the platform
	 * where it doesn't exist. onBackPressed(); }
	 * 
	 * return super.onKeyDown(keyCode, event); }
	 * 
	 * //@Override public void onBackPressed() { // This will be called either
	 * automatically for you on 2.0 // or later, or by the code above on earlier
	 * versions of the // platform. if (mStepsBack > 0) { upOneLevel(); } else {
	 * finish(); } }
	 */

	/**
	 * This is called after the file manager finished.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		switch (requestCode)
			{
			case REQUEST_CODE_MOVE:
				if (resultCode == RESULT_OK && data != null)
				{
					// obtain the filename
					File movefrom = mContextFile;
					File moveto = FileUtils.getFile(data.getData());
					if (moveto != null)
					{
						moveto = FileUtils.getFile(moveto, movefrom.getName());
						move(movefrom, moveto);
					}

				}
				break;

			case REQUEST_CODE_COPY:
				if (resultCode == RESULT_OK && data != null)
				{
					// obtain the filename
					File copyfrom = mContextFile;
					File copyto = FileUtils.getFile(data.getData());
					if (copyto != null)
					{
						copyto = createUniqueCopyName(this, copyto, copyfrom.getName());

						if (copyto != null)
						{
							copy(copyfrom, copyto);
						}
					}
				}
				break;
				
			// result to delete the "DecryptOnTheFly File"
			case REQUEST_DELETE_FILE:
				File toDelete = mContextFile;
				if(toDelete.exists()){
					try
					{
						toDelete.delete();
					} catch (Exception e)
					{
					}
				}
				refreshList();
				break;
			}
	}

}