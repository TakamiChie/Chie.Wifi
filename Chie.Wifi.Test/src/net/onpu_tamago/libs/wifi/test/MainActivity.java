package net.onpu_tamago.libs.wifi.test;

import net.onpu_tamago.libs.wifi.Wifi;
import net.onpu_tamago.libs.wifi.WifiUtil;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;

public class MainActivity extends Activity {

	private Wifi mController;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		this.mController = new Wifi(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@SuppressLint("NewApi")
	@Override
	public boolean onOptionsItemSelected(final MenuItem item) {
		final EditText out_result = (EditText) findViewById(R.id.out_result);
		out_result.setText("");
		final Editable d = out_result.getText();
		d.append(item.getTitle() + ":Start\n");
		Thread t = new Thread(new Runnable() {
			@Override
			public void run() {
				EditText in_ssid = (EditText) findViewById(R.id.in_ssid);
				EditText in_pass = (EditText) findViewById(R.id.in_pass);
				String ssid = in_ssid.getText().length() == 0 ? null : in_ssid
						.getText().toString();
				String pass = in_pass.getText().length() == 0 ? null : in_pass
						.getText().toString();
				try {
					switch (item.getItemId()) {
					case R.id.op_goWifiEnabled:
						mController.goWifiEnabled();
						break;
					case R.id.op_goWifiDisabled:
						mController.goWifiDisabled();
						break;
					case R.id.op_currentWifiInfo:
						runOnUiThread(new Runnable() {
							public void run() {
								WifiInfo i = mController.getCurrentWifiInfo();
								d.append("BSSID:" + i.getBSSID() + "\n");
								d.append("SSID:" + i.getSSID() + "\n");
								d.append("IPAddr:" + i.getIpAddress() + "\n");
								d.append("LinkSpeed:" + i.getLinkSpeed() + "\n");
								d.append("MaxAddr:" + i.getMacAddress() + "\n");
								d.append("NetId:" + i.getNetworkId() + "\n");
								d.append("RSSI:" + i.getRssi() + "\n");
							}
						});
						break;
					case R.id.op_currentWifiSSID:
						runOnUiThread(new Runnable() {
							public void run() {
								String s = mController.getCurrentWifiSSID();
								if (s == null) {
									d.append("SSID is null\n	");
								} else {
									d.append("SSID:" + s + "\n");

								}
							}
						});
						break;
					case R.id.op_addSSIDConnection:
						WifiConfiguration conf = new WifiConfiguration();
						conf.SSID = "\"" + ssid + "\"";
						conf.preSharedKey = "\"" + pass + "\"";
						if (mController.addSSIDConnection(conf, true)) {
							runOnUiThread(new Runnable() {
								public void run() {
									d.append("Success\n");
								}
							});
						} else {
							runOnUiThread(new Runnable() {
								public void run() {
									d.append("Failed\n");
								}
							});
						}
						break;
					case R.id.op_changeConnection:
						if (mController.switchConnection(ssid)) {
							runOnUiThread(new Runnable() {
								public void run() {
									d.append("Success\n");
								}
							});
						} else {
							runOnUiThread(new Runnable() {
								public void run() {
									d.append("Failed\n");
								}
							});
						}
						break;
					case R.id.op_scanwifi:
						mController.scanWifi(new Wifi.ScanWifiCallback() {
							@Override
							public void foundSSID(final ScanResult result) {
								runOnUiThread(new Runnable() {
									public void run() {
										d.append("---------------------\n");
										d.append("BSSID:" + result.BSSID + "\n");
										d.append("SSID:" + result.SSID + "\n");
										d.append("Capability:"
												+ result.capabilities + "\n");
										d.append("Frequency:"
												+ result.frequency + "\n");
										d.append("Level:" + result.level + "\n");
										if (Build.VERSION.SDK_INT >= 17) {
											d.append("TimeStamp:"
													+ result.timestamp + "\n");
										}
									}
								});
							}
						});
						break;

					case R.id.op_getSSIDName:
						runOnUiThread(new Runnable() {
							public void run() {
								d.append("SSIDName:"
										+ WifiUtil.getSSIDName("SSIDName")
										+ "\n");
								d.append("\"QuotedSSIDName\":"
										+ WifiUtil
												.getSSIDName("\"QuotedSSIDName\"")
										+ "\n	");
							}
						});
						break;
					case R.id.op_ipAddrToReadable:
						runOnUiThread(new Runnable() {
							public void run() {
								d.append("Native IPAddr:"
										+ mController.getCurrentWifiInfo()
												.getIpAddress() + "\n");
								d.append("Readable IPAddr:"
										+ WifiUtil.ipAddrToReadable(mController
												.getCurrentWifiInfo()
												.getIpAddress()) + "\n");
							}
						});
					default:
						break;
					}
				} catch (final Exception e) {
					runOnUiThread(new Runnable() {

						@Override
						public void run() {
							d.append(e.toString() + "\n");
						}
					});
				}
				runOnUiThread(new Runnable() {

					@Override
					public void run() {
						d.append(item.getTitle() + ":Finished\n");
					}
				});
			}
		});
		t.start();
		return super.onOptionsItemSelected(item);
	}

	public void waitFor(final Runnable subThread, final Runnable uiThread) {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				subThread.run();
				runOnUiThread(uiThread);
			}
		});
		t.start();
	}
}
