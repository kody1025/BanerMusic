package com.banermusic.util;

import android.os.Environment;

import java.io.File;

/**
 * Created by kodywu on 7/10/15.
 */
public class DirectoryUtil {

    private static String APPDIR = "/BanerMusic";
    private static String DOWNLOADDIR = "/download";
    private static File downloadDir;

    public static File getDownloadDirectory() throws Exception {

        if(downloadDir == null || !downloadDir.exists()){
            if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                File file = Environment.getExternalStorageDirectory();
                if(file.exists()){
                    String dir = file.getPath()+APPDIR+DOWNLOADDIR;
                    File _downloadDir = new File(dir);
                    _downloadDir.mkdirs();
                    if(_downloadDir.exists() || _downloadDir.isDirectory()){
                        return downloadDir = _downloadDir;
                    }else{
                        throw new Exception("无法生成下载目录！");
                    }
                }else{
                    throw new Exception("检测到SD卡，但无法读取，请重新插入！");
                }
            }else{
                File _downloadDir = new File(Environment.getDataDirectory().getPath()+ APPDIR + DOWNLOADDIR);
                _downloadDir.mkdirs();
                if(_downloadDir.exists() || _downloadDir.isDirectory()){
                    return downloadDir = _downloadDir;
                }else{
                    throw new Exception("无法生成下载目录！");
                }
            }
        }
        return downloadDir;
    }
}
