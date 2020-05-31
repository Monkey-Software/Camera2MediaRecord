package com.brianhoang.recordvideo.utils;

import android.graphics.Bitmap;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Calendar;

public class RecordFileUtil {

    public static final String TAG = "RecordFileUtil";
    private static final Object mLock = new Object();

    public static String DEFAULT_DIR;
    protected static String mTmpFileSubFix = "";  //后缀,
    protected static String mTmpFilePreFix = "";  //前缀;

    public static String getCreateFileDir(String name) {
        File file = new File(DEFAULT_DIR + name);
        if (!file.exists()) {
            file.mkdirs();
        }
        return DEFAULT_DIR + name;
    }

    public static void setFileDir(String dir) {
        DEFAULT_DIR = dir;
        getCreateFileDir("");
    }

    /**
     * 在指定的文件夹里创建一个文件名字, 名字是当前时间,指定后缀.
     *
     * @return
     */
    public static String createFile(String dir, String suffix) {
        synchronized (mLock) {
            Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH) + 1;
            int day = c.get(Calendar.DAY_OF_MONTH);
            int second = c.get(Calendar.SECOND);
            int millisecond = c.get(Calendar.MILLISECOND);
            year = year - 2000;

            String dirPath = dir;
            File d = new File(dirPath);
            if (!d.exists())
                d.mkdirs();

            if (dirPath.endsWith("/") == false) {
                dirPath += "/";
            }

            String name = mTmpFilePreFix;
            name += String.valueOf(year);
            name += String.valueOf(month);
            name += String.valueOf(day);
            name += String.valueOf(hour);
            name += String.valueOf(minute);
            name += String.valueOf(second);
            name += String.valueOf(millisecond);
            name += mTmpFileSubFix;
            if (suffix.startsWith(".") == false) {
                name += ".";
            }
            name += suffix;


            try {
                Thread.sleep(1); // 保持文件名的唯一性.
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            String retPath = dirPath + name;
            File file = new File(retPath);
            if (file.exists() == false) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            return retPath;
        }
    }

    /**
     * 在box目录下生成一个mp4的文件,并返回名字的路径.
     *
     * @return
     */
    public static String createMp4FileInBox() {
        return createFile(DEFAULT_DIR, ".mp4");
    }

    /**
     * 在box目录下生成一个指定后缀名的文件,并返回名字的路径.这里仅仅创建一个名字.
     *
     * @param suffix 指定的后缀名.
     * @return
     */
    public static String createFileInBox(String suffix) {
        return createFile(DEFAULT_DIR, suffix);
    }

    /**
     * 删除指定的文件.
     *
     * @param path
     */
    public static void deleteFile(String path) {
        if (path != null) {
            File file = new File(path);
            if (file.exists()) {
                file.delete();
            }
        }
    }

    /**
     * 递归删除目录下的所有文件及子目录下所有文件
     *
     * @param dir 将要删除的文件目录
     * @return boolean Returns "true" if all deletions were successful.
     * If a deletion fails, the method stops attempting to
     * delete and returns "false".
     */
    public static boolean deleteDir(File dir) {
        if (dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return dir.delete();
    }

    /**
     * LSNEW
     *
     * @param bmp
     */
    public static String saveBitmap(Bitmap bmp) {
        if (bmp != null) {
            try {
                BufferedOutputStream bos;
                String name = createFileInBox("png");
                bos = new BufferedOutputStream(new FileOutputStream(name));
                bmp.compress(Bitmap.CompressFormat.PNG, 90, bos);
                bos.close();
                return name;
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            Log.i("saveBitmap", "error  bmp  is null");
        }
        return "save Bitmap ERROR";
    }

    /**
     * LSNEW
     *
     * @return
     */
    public static boolean deleteDefaultDir() {
        File file = new File(DEFAULT_DIR);
        if (file.isDirectory()) {
            String[] children = file.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(file, children[i]));
                if (!success) {
                    return false;
                }
            }
        }
        // 目录此时为空，可以删除
        return file.delete();
    }
}
