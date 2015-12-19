package com.banermusic.util;

import android.os.StatFs;
import android.util.Log;

import com.banermusic.constant.BaseConstants;
import com.banermusic.db.CacheFileInfoDao;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.URI;

public class FileDownloadUtils {
	private static final String LOG_TAG = FileDownloadUtils.class.getSimpleName();

	private static FileDownloadUtils fileUtilsByMediaPlayer;
	private static FileDownloadUtils fileUtilsByPreLoader;

	private String downloadPath = BaseConstants.PATH_TEMP + File.separator + "download";

	public static FileDownloadUtils getInstance(URI uri, boolean isUseByMediaPlayer) {
		// 如果文件名有错误，返回空Utils
		String name = getValidFileName(uri);
		if (name == null || name.length() <= 0) {
			FileDownloadUtils utils = new FileDownloadUtils(null);
			utils.setEnableFalse();
			return utils;
		}
		// 如果是预加载，如果MediaPlayer正在使用文件，则不预加载，返回空Utils
		if (!isUseByMediaPlayer) {
			if (fileUtilsByMediaPlayer != null && fileUtilsByMediaPlayer.getFileName().equals(name)) {
				FileDownloadUtils utils = new FileDownloadUtils(null);
				utils.setEnableFalse();
				return utils;
			}
		}
		// 创建Utils，关闭之前Util。如果是MediaPlayer使用文件Preloader也在使用，则关闭Preloader的Utils
		if (isUseByMediaPlayer) {
			//if (fileUtilsByPreLoader != null && fileUtilsByPreLoader.getFileName().equals(name)) {
			//	close(fileUtilsByPreLoader, false);
			//}
			//close(fileUtilsByMediaPlayer, true);
			//fileUtilsByMediaPlayer = new FileDownloadUtils(name);
			//return fileUtilsByMediaPlayer;
			return new FileDownloadUtils(name);
		} else {
			close(fileUtilsByPreLoader, false);
			fileUtilsByPreLoader = new FileDownloadUtils(name);
			return fileUtilsByPreLoader;
		}
	}

	private boolean isEnable;

	private FileOutputStream outputStream;
	private RandomAccessFile randomAccessFile;
	private File file;

	/**
	 * 判断存储是否可用
	 *
	 * @return
	 */
	private boolean isSdAvaliable() {
		// 判断外部存储器是否可用
		File dir = new File(downloadPath);
		if (!dir.exists()) {
			dir.mkdirs();
			if(!dir.exists()){
				return false;
			}
		}

		// 可用空间大小是否大于SD卡预留最小值
		long freeSize = this.getAvailaleSize(downloadPath);
		if (freeSize > BaseConstants.SD_REMAIN_SIZE) {
			return true;
		} else {
			return false;
		}
	}

	private FileDownloadUtils(String name) {
		if (name == null || name.length() <= 0) {
			isEnable = false;
			return;
		}

		if (!isSdAvaliable()) {
			isEnable = false;
			return;
		}

		try {
			file = new File(downloadPath + File.separator + name);
			if (!file.exists()) {
				File dir = new File(downloadPath);
				dir.mkdirs();
				file.createNewFile();
			}
			randomAccessFile = new RandomAccessFile(file, "r");
			outputStream = new FileOutputStream(file, true);
			isEnable = true;
		} catch (IOException e) {
			isEnable = false;
			Log.e(LOG_TAG, "文件操作失败", e);
		}
	}

	public String getFileName() {
		return file.getName();
	}

	public boolean isEnable() {
		return isEnable;
	}

	public void setEnableFalse() {
		isEnable = false;
	}

	public int getLength() {
		if (isEnable) {
			return (int) file.length();
		} else {
			return -1;
		}
	}

	public boolean delete() {
		// 关闭占用文件
		this.close();
		return file.delete();
	}

	public byte[] read(int startPos) {
		if (isEnable) {
			int byteCount = (int) (getLength() - startPos);
			byte[] tmp = new byte[byteCount];
			try {
				randomAccessFile.seek(startPos);
				randomAccessFile.read(tmp);
				return tmp;
			} catch (IOException e) {
				Log.e(LOG_TAG, "缓存读取失败", e);
				return null;
			}
		} else {
			return null;
		}
	}

	public byte[] read(int startPos, int length) {
		if (isEnable) {
			int byteCount = (int) (getLength() - startPos);
			if (byteCount > length) {
				byteCount = length;
			}
			byte[] tmp = new byte[byteCount];
			try {
				randomAccessFile.seek(startPos);
				randomAccessFile.read(tmp);
				return tmp;
			} catch (IOException e) {
				Log.e(LOG_TAG, "缓存读取失败", e);
				return null;
			}
		} else {
			return null;
		}
	}

	public void write(byte[] buffer, int byteCount) {
		if (isEnable) {
			try {
				outputStream.write(buffer, 0, byteCount);
				outputStream.flush();
			} catch (IOException e) {
				Log.e(LOG_TAG, "缓存写入失败", e);
			}
		}
	}

	public static void close(FileDownloadUtils fileUtils, boolean isUseByMediaPlayer) {
		if (isUseByMediaPlayer) {
			if (fileUtilsByMediaPlayer != null && fileUtilsByMediaPlayer == fileUtils) {
				fileUtilsByMediaPlayer.setEnableFalse();
				fileUtilsByMediaPlayer = null;
			}
		} else {
			if (fileUtilsByPreLoader != null && fileUtilsByPreLoader == fileUtils) {
				fileUtilsByPreLoader.setEnableFalse();
				fileUtilsByPreLoader = null;
			}
		}
	}

	public static void deleteFile(String url) {
		URI uri = URI.create(url);
		String name = getValidFileName(uri);
		boolean isSuccess = new File(name).delete();
		if (isSuccess) {
			CacheFileInfoDao.getInstance().delete(name);
		}
	}

	public static boolean isFinishCache(String url) {
		URI uri = URI.create(url);
		String name = getValidFileName(uri);
		File f = new File(name);

		if (!f.exists()) {
			return false;
		}
		if (f.length() != CacheFileInfoDao.getInstance().getFileSize(name)) {
			return false;
		}
		return true;
	}

	/**
	 * 获取有效的文件名
	 * 
	 * @param url
	 * @return
	 */
	protected static String getValidFileName(URI uri) {
		String path = uri.getRawPath();
		String name = path.substring(path.lastIndexOf("/"));
		name = name.replace("\\", "");
		name = name.replace("/", "");
		name = name.replace(":", "");
		name = name.replace("*", "");
		name = name.replace("?", "");
		name = name.replace("\"", "");
		name = name.replace("<", "");
		name = name.replace(">", "");
		name = name.replace("|", "");
		name = name.replace(" ", "_"); // 前面的替换会产生空格,最后将其一并替换掉
		return name;
	}

	/**
	 * 获取外部存储器可用的空间
	 *
	 * @return
	 */
	private long getAvailaleSize(String dir) {
		StatFs stat = new StatFs(dir);
		long blockSize = stat.getBlockSize();
		long availableBlocks = stat.getAvailableBlocks();
		return availableBlocks * blockSize; // 获取可用大小
	}

	public void close(){
		if(randomAccessFile != null){
			try {
				randomAccessFile.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(outputStream != null){
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
