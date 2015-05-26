package com.example.inputclient;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.widget.TabHost;
import android.widget.TabHost.TabSpec;
import android.widget.Toast;

public class MainActivity extends FragmentActivity {

	private TabHost mTabHost=null;
	public final static String TAG = "ImageTransferHelper";
	public static final int REQUEST_TO_ENABLE_BT = 100;
	private BluetoothAdapter mBluetoothAdapter;
	private UUID MY_UUID = UUID
			.fromString("D04E3068-E15B-4482-8306-4CABFA1726E7");
	private final static String CBT_SERVER_DEVICE_NAME = "IM-T100K";
	private BluetoothSocket sock;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		mTabHost=(TabHost)findViewById(R.id.tabhost);
		mTabHost.setup();
		TabHost.OnTabChangeListener tabChangeListener = new TabHost.OnTabChangeListener() {
			 
            @Override
            public void onTabChanged(String tabId) {
                FragmentManager fm =   getSupportFragmentManager();
                OneFragment one = (OneFragment) fm.findFragmentByTag("TAB1");
                TwoFragment two = (TwoFragment) fm.findFragmentByTag("TAB2");
                ThreeFragment three=(ThreeFragment) fm.findFragmentByTag("TAB3");
                
                FragmentTransaction ft = fm.beginTransaction();
 
                /** Detaches the androidfragment if exists */
                if(one!=null)
                    ft.detach(one);
                /** Detaches the applefragment if exists */
                if(two !=null)
                    ft.detach(two);
                if(three !=null)
                		ft.detach(three);
                
 
                /** If current tab is android */
                if(tabId.equalsIgnoreCase("TAB1")){
 
                    if(one==null){
                        /** Create AndroidFragment and adding to fragmenttransaction */
                        ft.add(R.id.content,new OneFragment(), "TAB1");
                    }else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                       // ft.replace(R.id.content,one);
                    	ft.attach(one);
                    }
 
                }else if(tabId.equalsIgnoreCase("TAB2")){    /** If current tab is apple */
                    if(two==null){
                        /** Create AppleFragment and adding to fragmenttransaction */
                        ft.add(R.id.content,new TwoFragment(), "TAB2");
                     }else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                        //ft.replace(R.id.content, two);
                    	 ft.attach(two);
                    }
                }else{
                	if(three==null){
                        /** Create AppleFragment and adding to fragmenttransaction */
                        ft.add(R.id.content,new ThreeFragment(), "TAB3");
                     }else{
                        /** Bring to the front, if already exists in the fragmenttransaction */
                        ft.replace(R.id.content, three);
                    }
                }
                ft.commit();
                fm.executePendingTransactions();
            }
        };
        mTabHost.setOnTabChangedListener(tabChangeListener);
		TabSpec ts1 = mTabHost.newTabSpec("TAB1");
       ts1.setIndicator("tab1");
       ts1.setContent(new DummyTabContent(getBaseContext()));
       mTabHost.addTab(ts1);
 
        TabSpec ts2 = mTabHost.newTabSpec("TAB2");
        ts2.setIndicator("tab2");
        ts2.setContent(new DummyTabContent(getBaseContext()));
        mTabHost.addTab(ts2);
 
        TabSpec ts3 = mTabHost.newTabSpec("TAB3");
        ts3.setIndicator("tab3");
        ts3.setContent(new DummyTabContent(getBaseContext()));
        mTabHost.addTab(ts3);
 
       mTabHost.setCurrentTab(0);
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		
	/*	if (mBluetoothAdapter == null) {
			Log.v(TAG, "Device does not support Bluetooth");
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				Log.v(TAG, "Bluetooth supported but not enabled");
				Toast.makeText(MainActivity.this,
						"Bluetooth supported but not enabled",
						Toast.LENGTH_LONG).show();
				Intent enableBtIntent = new Intent(
						BluetoothAdapter.ACTION_REQUEST_ENABLE);
				startActivityForResult(enableBtIntent, REQUEST_TO_ENABLE_BT);
			} else {
				Log.v(TAG, "Bluetooth supported and enabled");
				// discover new Bluetooth devices
				discoverBluetoothDevices();

				// find devices that have been paired
				getBondedDevices();
			}
		}*/
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TO_ENABLE_BT) {
			discoverBluetoothDevices();
			getBondedDevices();
			return;
		}
	}

	private void discoverBluetoothDevices() {
		// register a BroadcastReceiver for the ACTION_FOUND Intent
		// to receive info about each device discovered.
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		registerReceiver(mReceiver, filter);
		mBluetoothAdapter.startDiscovery();
	}

	// for each device discovered, the broadcast info is received
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			// When discovery finds a device
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				Log.v(TAG, "BroadcastReceiver on Receive - " + device.getName()
						+ ": " + device.getAddress());
				String name = device.getName();

				// found another Android device of mine and start communication
				if (name != null
						&& name.equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {
					new ConnectThread(device).start();
				}
			}
		}
	};
	protected void onDestroy() {
		unregisterReceiver(mReceiver);
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}
	// bonded devices are those that have already paired with the current device
	// sometime in the past (and have not been unpaired)
	private void getBondedDevices() {
		Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
				.getBondedDevices();
		if (pairedDevices.size() > 0) {
			for (BluetoothDevice device : pairedDevices) {
				Log.v(TAG, "bonded device - " + device.getName() + ": "
						+ device.getAddress());
				if (device.getName().equalsIgnoreCase(CBT_SERVER_DEVICE_NAME)) {
					Log.d(TAG, CBT_SERVER_DEVICE_NAME);
					new ConnectThread(device).start();
					break;
				}
			}
		} else {
			Toast.makeText(MainActivity.this, "No bonded devices",
					Toast.LENGTH_LONG).show();
		}
	}

	private class ConnectThread extends Thread {
		int bytesRead;
		int total;
		private final BluetoothSocket mmSocket;

		public ConnectThread(BluetoothDevice device) {
			BluetoothSocket tmp = null;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				Log.v(TAG, "before createRfcommSocketToServiceRecord");
				tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
				Log.v(TAG, "after createRfcommSocketToServiceRecord");
			} catch (IOException e) {
				Log.v(TAG,
						" createRfcommSocketToServiceRecord exception: "
								+ e.getMessage());
			}
			mmSocket = tmp;
		}

		public void run() {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();
			Log.d(TAG, "ready to connect");
			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();

			} catch (IOException e) {
				Log.v(TAG, e.getMessage());
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
				return;
			}
			Log.d(TAG, "success to connect");
			sock = mmSocket;
			manageConnectedSocket(sock);
		}

		private void manageConnectedSocket(BluetoothSocket socket) {
			byte[] buffer = new byte[1];

			OutputStream mOutStream = null;
			try {
				mOutStream = socket.getOutputStream();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				Log.d(TAG, "cant get stream");
				e1.printStackTrace();
			}
			buffer[0]='a';
			try {
				mOutStream.write(buffer);
				mOutStream.flush();
			} catch (IOException e) {
				Log.e(TAG, e.getMessage());
			}
		}
	}

}