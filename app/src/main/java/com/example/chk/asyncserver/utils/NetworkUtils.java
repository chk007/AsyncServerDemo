package com.example.chk.asyncserver.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

/**
 * 网络工具类
 */
public final class NetworkUtils {
	/** 私有构造函数 */
	private NetworkUtils() {
	}

	/**
	 * Check network status.
	 *
	 * @param aContext
	 *            Context
	 * @return true, device is connected to network
	 */
	public static boolean isNetworkAvailable(Context aContext) {
		NetworkInfo networkInfo = ((ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (networkInfo != null) {
			return networkInfo.isAvailable();
		}
		return false;
	}

	/**
	 * Check network status.
	 *
	 * @param aContext
	 *            Context
	 * @return true, device is connected with WiFi hotspot
	 */
	public static boolean isWifiAvailable(Context aContext, String tiop) {
		NetworkInfo ni = ((ConnectivityManager) aContext.getSystemService(Context.CONNECTIVITY_SERVICE))
				.getActiveNetworkInfo();
		if (ni == null || !ni.isAvailable()) {
			Toast.makeText(aContext, "没有网络", Toast.LENGTH_SHORT).show();
			return false;
		}
		return ni.getType() == ConnectivityManager.TYPE_WIFI;
	}

	/**
	 * 获取当前IP地址
	 *
	 * @return ip地址
	 */
	public static String getLocalIpAddress() {
		String ip = null;
		try {
			// 遍历网络接口
			for (Enumeration<NetworkInterface> net = NetworkInterface.getNetworkInterfaces(); net.hasMoreElements();) {
				NetworkInterface netInterface = net.nextElement();
				// 遍历IP地址
				for (Enumeration<InetAddress> enumIpAddr = netInterface.getInetAddresses();
					 enumIpAddr.hasMoreElements();) {
					InetAddress inetAddress = enumIpAddr.nextElement();
					if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
						ip = inetAddress.getHostAddress().toString();
					}
				}
			}
		} catch (SocketException e) {
			e.printStackTrace();
		}
		return ip;
	}
}
