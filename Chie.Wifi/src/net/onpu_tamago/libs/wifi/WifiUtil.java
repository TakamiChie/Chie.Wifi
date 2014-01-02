package net.onpu_tamago.libs.wifi;

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
}
