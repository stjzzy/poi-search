package com.example.poisearch.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.poisearch.model.POI;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ExcelExporter {

    private static final String TAG = "ExcelExporter";
    public static final String DEFAULT_FOLDER_NAME = "POI导出";

    public interface ExportCallback {
        void onSuccess(String filePath);
        void onError(String error);
    }

    public static void exportToExcel(Context context, List<POI> poiList, String customPath, ExportCallback callback) {
        if (poiList == null || poiList.isEmpty()) {
            callback.onError("没有数据可导出");
            return;
        }

        new Thread(() -> {
            FileOutputStream outputStream = null;
            Workbook workbook = null;
            try {
                // 创建工作簿
                workbook = new XSSFWorkbook();
                Sheet sheet = workbook.createSheet("POI数据");

                // 创建标题样式
                CellStyle headerStyle = workbook.createCellStyle();
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerFont.setFontHeightInPoints((short) 12);
                headerStyle.setFont(headerFont);

                // 创建标题行
                Row headerRow = sheet.createRow(0);
                String[] headers = {"序号", "名称", "类别", "地址", "电话", "经度", "纬度", "距离(米)", "评分"};
                for (int i = 0; i < headers.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(headers[i]);
                    cell.setCellStyle(headerStyle);
                }

                // 填充数据
                for (int i = 0; i < poiList.size(); i++) {
                    POI poi = poiList.get(i);
                    Row row = sheet.createRow(i + 1);

                    row.createCell(0).setCellValue(i + 1);
                    row.createCell(1).setCellValue(poi.getName() != null ? poi.getName() : "");
                    row.createCell(2).setCellValue(poi.getCategory() != null ? poi.getCategory() : "");
                    row.createCell(3).setCellValue(poi.getAddress() != null ? poi.getAddress() : "");
                    row.createCell(4).setCellValue(poi.getPhone() != null ? poi.getPhone() : "");
                    row.createCell(5).setCellValue(poi.getLongitude());
                    row.createCell(6).setCellValue(poi.getLatitude());
                    row.createCell(7).setCellValue(poi.getDistance());
                    row.createCell(8).setCellValue(poi.getRating());
                }

                // 设置列宽（Android不支持autoSizeColumn，使用固定宽度）
                int[] columnWidths = {8, 20, 15, 30, 15, 12, 12, 12, 10};
                for (int i = 0; i < headers.length; i++) {
                    sheet.setColumnWidth(i, columnWidths[i] * 256);
                }

                // 生成文件名
                String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
                String fileName = "POI数据_" + timeStamp + ".xlsx";

                // 确定保存目录
                File directory;
                if (customPath != null && !customPath.isEmpty()) {
                    // 使用自定义路径
                    directory = new File(customPath);
                    Log.d(TAG, "使用自定义路径: " + customPath);
                } else {
                    // 使用默认路径
                    directory = getDefaultDirectory(context);
                    Log.d(TAG, "使用默认路径");
                }

                // 确保目录存在
                if (!directory.exists()) {
                    boolean created = directory.mkdirs();
                    if (!created) {
                        Log.e(TAG, "无法创建目录: " + directory.getAbsolutePath());
                        // 尝试使用应用私有目录作为备用
                        directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                        if (directory == null) {
                            directory = context.getFilesDir();
                        }
                        Log.d(TAG, "使用备用目录: " + directory.getAbsolutePath());
                    }
                }

                // 检查目录是否可写
                if (!directory.canWrite()) {
                    Log.e(TAG, "目录不可写: " + directory.getAbsolutePath());
                    // 切换到应用私有目录
                    directory = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    if (directory == null) {
                        directory = context.getFilesDir();
                    }
                    Log.d(TAG, "切换到可写目录: " + directory.getAbsolutePath());
                }

                File file = new File(directory, fileName);
                Log.d(TAG, "保存文件到: " + file.getAbsolutePath());

                outputStream = new FileOutputStream(file);
                workbook.write(outputStream);
                outputStream.flush();

                Log.d(TAG, "文件导出成功: " + file.getAbsolutePath());
                callback.onSuccess(file.getAbsolutePath());

            } catch (IOException e) {
                Log.e(TAG, "导出失败", e);
                callback.onError("导出失败: " + e.getMessage());
            } catch (Exception e) {
                Log.e(TAG, "导出异常", e);
                callback.onError("导出异常: " + e.getMessage());
            } finally {
                // 确保资源被释放
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭输出流失败", e);
                    }
                }
                if (workbook != null) {
                    try {
                        workbook.close();
                    } catch (IOException e) {
                        Log.e(TAG, "关闭工作簿失败", e);
                    }
                }
            }
        }).start();
    }

    /**
     * 获取默认导出目录
     */
    public static File getDefaultDirectory(Context context) {
        File directory;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Android 10+ 使用应用外部文件目录
            directory = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), DEFAULT_FOLDER_NAME);
        } else {
            // Android 9及以下使用外部存储根目录
            directory = new File(Environment.getExternalStorageDirectory(), DEFAULT_FOLDER_NAME);
        }
        return directory;
    }

    /**
     * 获取默认导出路径的字符串表示（友好显示）
     */
    public static String getDefaultPath(Context context) {
        return getDefaultDirectory(context).getAbsolutePath();
    }

    /**
     * 获取用户友好的路径显示
     */
    public static String getUserFriendlyPath(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            return "内部存储/Android/data/" + context.getPackageName() + "/files/Documents/" + DEFAULT_FOLDER_NAME;
        } else {
            return "内部存储/" + DEFAULT_FOLDER_NAME;
        }
    }

    public static void shareExcelFile(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            } else {
                uri = Uri.fromFile(file);
            }

            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "POI数据导出");
            shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            context.startActivity(Intent.createChooser(shareIntent, "分享Excel文件"));
        } catch (Exception e) {
            Log.e(TAG, "分享文件失败", e);
            Toast.makeText(context, "分享失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 打开文件所在目录
     */
    public static void openFileLocation(Context context, String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(context, "文件不存在", Toast.LENGTH_SHORT).show();
                return;
            }

            File parentDir = file.getParentFile();
            if (parentDir == null) {
                Toast.makeText(context, "无法打开目录", Toast.LENGTH_SHORT).show();
                return;
            }

            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri uri;
            
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                uri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", parentDir);
                intent.setDataAndType(uri, "resource/folder");
            } else {
                uri = Uri.fromFile(parentDir);
                intent.setDataAndType(uri, "resource/folder");
            }
            
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            
            // 尝试打开文件管理器
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            } else {
                // 如果无法直接打开目录，尝试使用文件管理器应用
                Intent fileManagerIntent = new Intent(Intent.ACTION_VIEW);
                fileManagerIntent.setDataAndType(uri, "*/*");
                fileManagerIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                
                if (fileManagerIntent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(fileManagerIntent);
                } else {
                    Toast.makeText(context, "未找到文件管理器应用", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "打开目录失败", e);
            Toast.makeText(context, "打开目录失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * 获取用户友好的路径显示
     */
    public static String getDisplayPath(String absolutePath) {
        if (absolutePath == null) return "";
        
        // 转换应用私有目录路径
        if (absolutePath.contains("/Android/data/")) {
            int lastSlash = absolutePath.lastIndexOf("/");
            String fileName = lastSlash >= 0 ? absolutePath.substring(lastSlash) : absolutePath;
            return "内部存储/Android/data/..." + fileName;
        }
        
        // 转换外部存储路径
        if (absolutePath.startsWith("/storage/emulated/0/")) {
            return "内部存储/" + absolutePath.substring("/storage/emulated/0/".length());
        }
        
        return absolutePath;
    }
}
