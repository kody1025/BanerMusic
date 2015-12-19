package com.banermusic.services.proxy;

import android.util.Log;

import com.banermusic.constant.BaseConstants;
import com.banermusic.db.CacheFileInfoDao;
import com.banermusic.logger.MyLogger;
import com.banermusic.util.FileDownloadUtils;
import com.banermusic.util.ProxyHttpUtils;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.StringTokenizer;

public class RequestDealThread extends Thread {
	private static final String LOG_TAG = RequestDealThread.class.getSimpleName();

	private MyLogger logger = MyLogger.getLogger(RequestDealThread.class.getName());

	Socket client;
	HttpUriRequest request;

	FileDownloadUtils fileUtils;
	/** MediaPlayer发出的原始请求Range Start */
	private int originRangeStart;
	/** 和本地缓存判断后，需要发出的请求Range Start */
	private int realRangeStart;

	CacheFileInfoDao cacheDao;

	public RequestDealThread(Socket client) {
		//this.request = request;
		this.client = client;
		cacheDao = CacheFileInfoDao.getInstance();
	}

	@Override
	public void run() {
		try {
			request = readRequest(client);
			if(request != null){
				fileUtils = FileDownloadUtils.getInstance(request.getURI(), true);
				processRequest(request, client);
			}
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private int getRangeStart(HttpUriRequest request) {
		Header rangeHeader = request.getFirstHeader(BaseConstants.RANGE);
		if (rangeHeader != null) {
			String value = rangeHeader.getValue();
			return Integer.valueOf(value.substring(value.indexOf("bytes=") + 6, value.indexOf("-")));
		}
		return 0;
	}

	private HttpUriRequest readRequest(Socket client) {
		// 得到Request String
		HttpUriRequest request = null;
		int bytes_read;
		byte[] local_request = new byte[1024];
		String requestStr = "";
		try {
			while ((bytes_read = client.getInputStream().read(local_request)) != -1) {
				byte[] tmpBuffer = new byte[bytes_read];
				System.arraycopy(local_request, 0, tmpBuffer, 0, bytes_read);
				String str = new String(tmpBuffer);
				Log.i(LOG_TAG + " Header-> ", str);
				logger.i(" Header-> "+str);
				requestStr = requestStr + str;
				if (requestStr.contains("GET") && requestStr.contains(BaseConstants.HTTP_END)) {
					break;
				}
			}
		} catch (IOException e) {
			Log.e(LOG_TAG, "获取Request Header异常", e);
			return request;
		}

		if (requestStr == "") {
			Log.i(LOG_TAG, "请求头为空，获取异常");
			return request;
		}

		// 将Request String组合为HttpUriRequest
		String[] requestParts = requestStr.split(BaseConstants.LINE_BREAK);
		StringTokenizer st = new StringTokenizer(requestParts[0]);
		String method = st.nextToken();
		String uri = st.nextToken();

		Log.d(LOG_TAG + " URL-> ", uri);
		request = new HttpGet(uri.substring(1));
		for (int i = 1; i < requestParts.length; i++) {
			int separatorLocation = requestParts[i].indexOf(":");
			String name = requestParts[i].substring(0, separatorLocation).trim();
			String value = requestParts[i].substring(separatorLocation + 1).trim();
			// 不添加Host Header，因为URL的Host为127.0.0.1
			if (!name.equals(BaseConstants.HOST)) {
				request.addHeader(name, value);
			}
		}
		// 如果没有Range，统一添加默认Range,方便后续处理
		if (request.getFirstHeader(BaseConstants.RANGE) == null) {
			request.addHeader(BaseConstants.RANGE, BaseConstants.RANGE_PARAMS_0);
		}
		return request;
	}

	/**
	 * 伪造Response Header，发送缓存内容
	 * 
	 * @param rangeStart
	 *            数据起始位置（如果从头开始则为0）
	 * @param rangeEnd
	 *            数据截止位置（一般为缓存长度-1）
	 * @param fileLength
	 *            缓存文件长度
	 * @param audioCache
	 *            缓存内容
	 * @throws IOException
	 */
	private void sendLocalHeaderAndCache(int rangeStart, int rangeEnd, int fileLength, byte[] audioCache)
			throws IOException {
		// 返回MediaPlayer Header信息
		String httpString = ProxyHttpUtils.genResponseHeader(rangeStart, rangeEnd, fileLength);
		byte[] httpHeader = httpString.toString().getBytes();
		client.getOutputStream().write(httpHeader, 0, httpHeader.length);
		// 返回Content
		if (audioCache != null && audioCache.length > 0) {
			client.getOutputStream().write(audioCache, 0, audioCache.length);
		}
	}

	private void processRequest(HttpUriRequest request, Socket client) throws IllegalStateException, IOException {
		if (request == null) {
			return;
		}

		try {
			byte[] audioCache = null;
			// 得到MediaPlayer原始请求Range起始
			originRangeStart = getRangeStart(request);

			// 数据库缓存的文件大小
			int cacheFileSize = cacheDao.getFileSize(fileUtils.getFileName());

			Log.i(LOG_TAG, "原始请求Range起始值：" + originRangeStart + " 本地缓存文件长度：" + fileUtils.getLength() + "数据库文件缓存长度:"+ cacheFileSize);
			logger.i("原始请求Range起始值：" + originRangeStart + " 本地缓存文件长度：" + fileUtils.getLength()+ "数据库文件缓存长度:"+ cacheFileSize);

			/*
			 * 如果缓存完成，无需发送请求，本地缓存返回MediaPlayer。
			 */
			if (fileUtils.isEnable() && fileUtils.getLength() == cacheFileSize) {
				audioCache = fileUtils.read(originRangeStart, BaseConstants.AUDIO_BUFFER_MAX_LENGTH);
				sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
				return;
			}

			// TODO 这里可能需要网络判断
			/*
			 * 请求Range起始值和本地缓存比对。如果有缓存，得到缓存内容，修改Range。 如果没有缓存，则Range不变。
			 */
			if(fileUtils.isEnable() && originRangeStart < fileUtils.getLength() && fileUtils.getLength() < cacheFileSize){
				audioCache = fileUtils.read(originRangeStart, BaseConstants.AUDIO_BUFFER_MAX_LENGTH);
				Log.i(LOG_TAG, "本地已缓存长度（跳过）:" + fileUtils.getLength());
				logger.i("本地已缓存长度（跳过）:" + fileUtils.getLength());
				realRangeStart = fileUtils.getLength();
				// 替换请求Header
				request.removeHeaders(BaseConstants.RANGE);
				request.addHeader(BaseConstants.RANGE, BaseConstants.RANGE_PARAMS + realRangeStart + "-");
			} else {
				realRangeStart = originRangeStart;
			}

			// 数据库缓存信息丢失，但缓存文件还在，则删除缓存文件，重新下载
			if (fileUtils.isEnable() && fileUtils.getLength() > 0 && cacheFileSize <= 0) {
				fileUtils.delete();
				fileUtils = FileDownloadUtils.getInstance(request.getURI(), true);
			}

			// 缓存是否已经到最大值（如果缓存已经到最大值，则只需要返回缓存）
			boolean isCacheEnough = (audioCache != null && audioCache.length == BaseConstants.AUDIO_BUFFER_MAX_LENGTH) ? true : false;

			/*
			 * 如果缓存足够，且本地有文件长度，则直接发送缓存,不发送请求。。。。。。。。。。。。。。。。。。。
			 * 如果缓存足够，本地没有文件长度，则发送请求，使用ResponseHeader，返回缓存,!不接收ResponseContent
			 * 如果缓存不足，则发送请求，使用ResponseHeader，返回缓存，!返回Response Content
			 */
			// 缓存足够&&有文件大小
			if (isCacheEnough && cacheFileSize > 0) {
				sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
			}
			// 缓存不够。或者数据库没有文件大小
			else {
				HttpResponse realResponse = null;
				/*
				 * 返回Header和Cache
				 */
				// 如果数据库没有存文件大小，则获取（处理数据库没有文件大小的情况）
				if (cacheFileSize <= 0) {
					Log.d(LOG_TAG, "数据库未包含文件大小，发送请求");
					logger.i("数据库未包含文件大小，发送请求");
					realResponse = ProxyHttpUtils.send(request);
					if (realResponse == null) {
						return;
					}
					cacheFileSize = getContentLength(realResponse);
					sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
				}

				// 如果缓存不足，返回Response Content（处理缓存不足的情况）
				if (realResponse == null) {
					Log.d(LOG_TAG, "缓存不足，发送请求");
					logger.i("缓存不足，发送请求");
					// 得到需要发送请求Range Start（本地缓存结尾位置+1=缓存长度）
					realRangeStart = fileUtils.getLength();
					// 替换请求Header
					request.removeHeaders(BaseConstants.RANGE);
					request.addHeader(BaseConstants.RANGE, BaseConstants.RANGE_PARAMS + realRangeStart + "-");

					realResponse = ProxyHttpUtils.send(request);
					if (realResponse == null) {
						return;
					}
					sendLocalHeaderAndCache(originRangeStart, cacheFileSize - 1, cacheFileSize, audioCache);
				}

				Log.d(LOG_TAG, "接收ResponseContent");
				logger.i("接收ResponseContent");
				InputStream data = realResponse.getEntity().getContent();
				if (!isCacheEnough) {
					byte[] buff = new byte[1024 * 64];
					boolean isPrint = true;
					int fileLength = 0;
					int readBytes;
					while (Thread.currentThread() == MediaPlayerProxy.downloadThread
							&& (readBytes = data.read(buff, 0, buff.length)) != -1) {
						int fileBufferLocation = fileLength + realRangeStart;
						fileLength += readBytes;
						int fileBufferEndLocation = fileLength + realRangeStart;
						// 保存文件
						if (fileUtils.getLength() == fileBufferLocation) {
							fileUtils.write(buff, readBytes);
						}
						// 打印缓存大小
						if (System.currentTimeMillis() / 500 % 1 == 0) {
							if (isPrint) {
								Log.d(LOG_TAG, "Cache Size:" + readBytes + " File Start:" + fileBufferLocation
										+ "File End:" + fileBufferEndLocation);
								logger.i("Cache Size:" + readBytes + " File Start:" + fileBufferLocation
										+ "File End:" + fileBufferEndLocation);
								isPrint = false;
							}
						} else {
							isPrint = true;
						}

						client.getOutputStream().write(buff, 0, readBytes);
					}
				}
			}
		} catch (SocketException e) {
			Log.i(LOG_TAG, "连接被终止", e);
		} catch (Exception e) {
			Log.e(LOG_TAG, e.getMessage(), e);
		} finally {
			fileUtils.close();
			client.close();
			Log.i(LOG_TAG, "代理关闭");
			logger.i("代理关闭");
		}
	}

	/**
	 * 得到Content大小
	 * 
	 * @param response
	 * @return
	 */
	private int getContentLength(HttpResponse response) {
		int contentLength = 0;
		Header header = response.getFirstHeader(BaseConstants.CONTENT_RANGE);
		if (header != null) {
			String range = header.getValue();
			contentLength = Integer.valueOf(range.substring(range.indexOf("-") + 1, range.indexOf("/"))) + 1;
		} else {
			header = response.getFirstHeader(BaseConstants.CONTENT_LENGTH);
			if (header != null) {
				contentLength = Integer.valueOf(header.getValue());
			}
		}
		if (contentLength != 0) {
			cacheDao.insertOrUpdate(fileUtils.getFileName(), contentLength);
		}
		return contentLength;
	}
}
