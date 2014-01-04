package net.onpu_tamago.libs.wifi;

import android.net.wifi.WifiInfo;

public final class WifiUtil {
	private WifiUtil() {
	}

	/**
	 * Wi-Fi SSIDの名称を取得します。
	 * Android4.2以降のOSでは、SSIDの名称の前後にダブルクオーテーションが入るため、これを取り除きます。
	 * ダブルクオーテーションがない場合は、そのまま文字列を返します。
	 * 
	 * @param ssid
	 *            SSID名
	 * @return ダブルクオーテーションを取り除いたSSID名
	 */
	public static String getSSIDName(String ssid) {
		String result;
		if (ssid.startsWith("\"") && ssid.endsWith("\"")) {
			result = ssid.split("\"")[1];
		} else {
			result = ssid;
		}
		return result;
	}

	/**
	 * {@link WifiInfo#getIpAddress()}で得られるIPアドレス数値を可読性のある文字列に変更します。
	 * 
	 * @param ipAddress
	 *            {@link WifiInfo#getIpAddress()}で得られるIPアドレス
	 * @return 可読性のある文字列
	 */
	public static String ipAddrToReadable(int ipAddress) {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < 32; i+=8) {
			sb.append((ipAddress >> i) & 0xFF);
			sb.append(".");
		}
		return sb.substring(0, sb.length() - 1);
	}
}
