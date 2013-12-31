package net.onpu_tamago.libs.wifi;

import java.util.List;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class Wifi {

	private static final String TAG = "[Wifi]libs";
	public static final int ERROR_UNKNOWN = -1;
	public static final int ERROR_NOPASSWORD = 1;
	public static final int SUCCESS = 0;
	private Context mContext;
	private WifiManager mManager;

	/**
	 * Wi-Fiスキャン中にコールバックされるインターフェースです
	 * 
	 * @author 高見知英
	 * 
	 */
	public interface ScanWifiCallback {
		public void foundSSID(ScanResult result);
	}

	public Wifi(Context context) {
		this.mContext = context;
		this.mManager = (WifiManager) context
				.getSystemService(Context.WIFI_SERVICE);
	}

	/**
	 * コンテキストを取得します。
	 * 
	 * @return コンテキスト
	 */
	public Context getContext() {
		return mContext;
	}

	/**
	 * Wi-Fiマネージャオブジェクトを取得します。
	 * 
	 * @return Wi-Fiマネージャオブジェクト
	 */
	public WifiManager getWifiManager() {
		return mManager;
	}

	/**
	 * 無線LANをONにし、準備が完了するまで待機します。 なお、このメソッドはサブスレッドで呼び出されることを前提に設計されています。
	 */
	public void goWifiEnabled() {
		if (!mManager.isWifiEnabled()) {
			mManager.setWifiEnabled(true);
			while (!mManager.isWifiEnabled()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 無線LANをOFFにし、切断が完了するまで待機します。 なお、このメソッドはサブスレッドで呼び出されることを前提に設計されています。
	 */
	public void goWifiDisabled() {
		if (mManager.isWifiEnabled()) {
			mManager.setWifiEnabled(false);
			while (mManager.isWifiEnabled()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Wi-Fi SSIDをスキャンし、全ての値を読み込みます。 なお、このメソッドはサブスレッドで呼び出されることを前提に設計されています。
	 * 
	 * @param callback
	 *            SSIDが見つかったときに処理を行うコールバック
	 */
	public void scanWifi(final ScanWifiCallback callback) {
		if (!mManager.isWifiEnabled())
			throw new IllegalStateException("Wifi Diabled");
		mManager.startScan();
		List<ScanResult> aplist = null;
		while (aplist == null) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			aplist = mManager.getScanResults();
		}
		for (int i = 0; i < aplist.size(); i++) {
			ScanResult r = aplist.get(i);
			if (callback != null) {
				callback.foundSSID(r);
			}
		}
	}

	/**
	 * 現在接続しているWi-Fi SSIDに関する情報を取得します。
	 * 
	 * @return Wi-Fi接続情報
	 */
	public WifiInfo getCurrentWifiInfo() {
		return mManager.getConnectionInfo();
	}

	/**
	 * 任意のSSIDに接続を試行します。このメソッドでは、過去に接続したことがある(接続履歴にある)SSIDへの接続のみが行えます。
	 * 過去に接続したことのないSSIDを指定すると、処理が失敗します。
	 * 
	 * @param newSSID
	 *            接続するSSID
	 * @return 接続できたかどうか。
	 */
	public boolean switchConnection(String newSSID) {
		boolean result = false;
		if (!mManager.isWifiEnabled())
			throw new IllegalStateException("Wifi Diabled");
		if (newSSID == null)
			throw new IllegalArgumentException("ssid is null");

		// 接続したことあるリストにあるか？
		int id = findConfiguratedNetworks(newSSID);
		if (id == -1) {
			result = false;
		} else {
			connectNetworkId(id);
			Log.i(TAG, "SSID " + newSSID + " Connected");
			result = true;
		}
		return result;
	}

	/**
	 * Wi-Fi設定を新規に追加します。 もし、同名の設定が存在した場合は、それを削除します。
	 * 
	 * @param configuration
	 *            Wi-Fi設定
	 * @param connect
	 *            追加したWi-Fi設定に接続を行う場合、trueを指定します。
	 * @return 設定に成功した場合、true
	 */
	public boolean addSSIDConnection(WifiConfiguration configuration,
			boolean connect) {
		boolean result = false;
		if (!mManager.isWifiEnabled())
			throw new IllegalStateException("Wifi Diabled");
		if (configuration == null)
			throw new IllegalArgumentException("Configuration is null");
		List<WifiConfiguration> confs = mManager.getConfiguredNetworks();
		for (WifiConfiguration conf : confs) {
			if (conf.SSID.equals(configuration.SSID)
					|| conf.SSID.equals("\"" + configuration.SSID + "\"")) {
				mManager.removeNetwork(conf.networkId);
				break;
			}
		}
		int id = mManager.addNetwork(configuration);
		mManager.updateNetwork(configuration);
		if(connect){
			if(id > 0){
				connectNetworkId(id);
				result = true;
			}
		}else{
			result = true;
		}
		return result;

	}

	// ユーティリティ

	/**
	 * 任意のネットワークIDに接続を行う
	 * @param id ネットワークID
	 */
	private void connectNetworkId(int id) {
		WifiInfo current = mManager.getConnectionInfo();
		if (current != null) {
			mManager.disableNetwork(current.getNetworkId());
		}
		mManager.enableNetwork(id, true);
		mManager.reconnect();
		while (mManager.getWifiState() != WifiManager.WIFI_STATE_ENABLED) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 設定済みネットワークリストから、任意のSSIDを検索する
	 * 
	 * @param SSID
	 *            検索するSSID
	 * @return 検索されたSSIDのネットワークID。見つからなかった場合、-1
	 */
	private int findConfiguratedNetworks(String SSID) {
		int result = -1;
		List<WifiConfiguration> configlist = mManager.getConfiguredNetworks();

		for (WifiConfiguration config : configlist) {
			Log.i(TAG, "Configuration:" + config.SSID);
			if (config.SSID.equals(SSID)
					|| config.SSID.equals("\"" + SSID + "\"")) {
				Log.i(TAG, "SSID Found");
				result = config.networkId;
				break;
			}
		}
		return result;
	}

}
