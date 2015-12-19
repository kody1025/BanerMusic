package com.banermusic.services.proxy;

import android.util.Log;

import com.banermusic.constant.BaseConstants;
import com.banermusic.logger.MyLogger;

import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.StringTokenizer;

/**
 * 代理
 * 
 * @author Kody
 * 
 */
public class MediaPlayerProxy implements Runnable {
	private static final String LOG_TAG = MediaPlayerProxy.class.getSimpleName();

	private static MyLogger logger = MyLogger.getLogger(MediaPlayerProxy.class.getName());

	private int port;

	private ServerSocket socket;

	private Thread thread;

	private boolean isRunning = true;

	protected static RequestDealThread downloadThread;

	/**
	 * 创建ServerSocket，使用自动分配端口
	 */
	public void init() {
		try {
			socket = new ServerSocket(port, 0, InetAddress.getByAddress(new byte[]{127, 0, 0, 1}));
			socket.setSoTimeout(5000);
			port = socket.getLocalPort();
			Log.d(LOG_TAG, "port " + port + " obtained");
		} catch (UnknownHostException e) {
			Log.e(LOG_TAG, "Error initializing server", e);
		} catch (IOException e) {
			Log.e(LOG_TAG, "Error initializing server", e);
		}
	}

	public void start() {
		if (socket == null) {
			throw new IllegalStateException("Cannot start proxy; it has not been initialized.");
		}
		thread = new Thread(this);
		thread.start();
	}

	public void stop() {
		isRunning = false;
		if (thread == null) {
			throw new IllegalStateException("Cannot stop proxy; it has not been started.");
		}
		thread.interrupt();
		try {
			thread.join(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Log.d(LOG_TAG, "stop");
	}

	public String getProxyURL(String url) {
		return String.format("http://127.0.0.1:%d/%s", port, url);
	}

	@Override
	public void run() {
		Log.d(LOG_TAG, "running");
		while (isRunning) {
			try {
				final Socket client = socket.accept();
				if (client == null) {
					continue;
				}
				Log.d(LOG_TAG, "client connected");
				logger.i("server socket client received");
				downloadThread = new RequestDealThread(client);
				downloadThread.start();

			} catch (SocketTimeoutException e) {
				// Do nothing
			} catch (IOException e) {
				Log.e(LOG_TAG, "Error connecting to client", e);
			}
		}
		Log.d(LOG_TAG, "Proxy interrupted. Shutting down.");
	}


}