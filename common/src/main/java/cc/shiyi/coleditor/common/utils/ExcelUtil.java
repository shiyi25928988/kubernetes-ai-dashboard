package cc.shiyi.coleditor.common.utils;


import cc.shiyi.coleditor.common.annotation.excel.ExcelColumnTitle;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class ExcelUtil {

    /**
     * 生成Excel文件
     * <p>
     * 该方法根据提供的数据列表生成一个Excel文件，并保存到指定路径
     *
     * @param fileName       文件名（不包含扩展名）
     * @param sheetName      Excel工作表名称
     * @param dataObjectList 要写入Excel的数据对象列表
     * @return 生成的Excel文件对象
     * @throws IOException 当文件操作发生错误时抛出
     */
    public static <T> File genExcelFile(String fileName, String sheetName, List dataObjectList) throws IOException {
        XSSFWorkbook wb = genExcel(sheetName, dataObjectList);
        File file = new File(System.getProperty("user.dir") + File.separator + fileName + ".xlsx");
        FileOutputStream fos = new FileOutputStream(file);
        wb.write(fos);
        fos.close();
        return file;
    }

    /**
     * 生成Excel工作簿对象
     * @param sheetName Excel工作表名称
     * @param dataObjectList 数据对象列表，用于填充Excel数据
     * @param <T> 数据对象泛型类型
     * @return XSSFWorkbook Excel工作簿对象，如果数据为空则返回null
     * @throws IOException IO异常
     */
    public static <T> XSSFWorkbook genExcel(String sheetName, List<T> dataObjectList) throws IOException {
        if(Objects.isNull(dataObjectList) || dataObjectList.isEmpty()){return null;}

        XSSFWorkbook wb = new XSSFWorkbook();
        XSSFSheet sheet = wb.createSheet();
        wb.setSheetName(0, sheetName);
        {
            //通过反射 do类的JSONField字段设置title row
            XSSFRow titleRow = sheet.createRow(0);
            Field[] fields = dataObjectList.get(0).getClass().getDeclaredFields();
            if (fields.length > 0) {
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    Cell cell = titleRow.createCell(i, CellType.STRING);
                    if (Objects.nonNull(fields[i].getAnnotation(ExcelColumnTitle.class)))
                        cell.setCellValue(fields[i].getAnnotation(ExcelColumnTitle.class).name());
                    cell.setCellStyle(genTitleRowStyle(wb));
                }
            }
        }

        AtomicInteger dataRowNum = new AtomicInteger(1);
        {
            for (int j = 0; j < dataObjectList.size(); j++) {
                XSSFRow row = sheet.createRow(dataRowNum.getAndIncrement());
                Field[] fields = dataObjectList.get(j).getClass().getDeclaredFields();
                if (fields.length > 0) {
                    for (int i = 0; i < fields.length; i++) {
                        fields[i].setAccessible(true);
                        try {
                            Cell cell = row.createCell(i, CellType.STRING);
                            cell.setCellValue(String.valueOf(fields[i].get(dataObjectList.get(j))));
                            cell.setCellStyle(genRowStyle(wb, j));
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        return wb;
    }

    /**
     * 根据行号生成对应的行样式
     * 奇数行使用奇数行样式，偶数行使用偶数行样式
     *
     * @param workbook 工作簿对象，用于创建样式
     * @param rowNum 行号，从0开始计数
     * @return 返回对应行号的单元格样式
     */
    private static CellStyle genRowStyle(Workbook workbook, int rowNum){
        if(rowNum % 2 == 1){
            return genOddRowStyle(workbook);
        }else{
            return genEvenRowStyle(workbook);
        }
    }

    /**
     * 生成标题行单元格样式
     * 该函数创建一个用于Excel标题行的单元格样式，包含白色粗体字体、居中对齐和绿色背景
     *
     * @param workbook Excel工作簿对象，用于创建样式和字体
     * @return CellStyle 返回配置好的单元格样式对象
     */
    private static CellStyle genTitleRowStyle(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.WHITE.index);
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        cellStyle.setFillBackgroundColor(IndexedColors.GREEN.getIndex());
        cellStyle.setFillForegroundColor(IndexedColors.BLACK.getIndex());
        cellStyle.setFillPattern(FillPatternType.THIN_BACKWARD_DIAG);
        return cellStyle;
    }

    //生成奇数行style
    private static CellStyle genOddRowStyle(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.index);
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setFillBackgroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
        cellStyle.setFillPattern(FillPatternType.THIN_BACKWARD_DIAG);
        return cellStyle;
    }

    //生成偶数行style
    private static CellStyle genEvenRowStyle(Workbook workbook){
        CellStyle cellStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setColor(IndexedColors.BLACK.index);
        font.setBold(true);
        cellStyle.setFont(font);
        cellStyle.setAlignment(HorizontalAlignment.LEFT);
        cellStyle.setFillBackgroundColor(IndexedColors.WHITE.getIndex());
        cellStyle.setFillForegroundColor(IndexedColors.WHITE.getIndex());
        cellStyle.setFillPattern(FillPatternType.THIN_BACKWARD_DIAG);
        return cellStyle;
    }

    public static InputStream getInputStreamFromWorkBook(Workbook workbook, String fileName) throws IOException {
        OutputStream outputStream = new FileOutputStream(System.getProperty("user.dir") + File.separator + fileName);
        workbook.write(outputStream);
        InputStream inputStream = new BufferedInputStream(new FileInputStream(System.getProperty("user.dir") + File.separator + fileName));
        return inputStream;
    }

    public static <T> List<T> processExcelData(MultipartFile file, Class<T> clazz, boolean isSkipTitleRow) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        Workbook workbook = new XSSFWorkbook(file.getInputStream());
        return processExcelData(workbook, clazz, isSkipTitleRow);
    }

    public static <T> List<T> processExcelData(InputStream inputStream, Class<T> clazz, boolean isSkipTitleRow) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IOException {
        Workbook workbook = new XSSFWorkbook(inputStream);
        return processExcelData(workbook, clazz, isSkipTitleRow);
    }
    public static <T> List<T> processExcelData(Workbook workbook, Class<T> clazz, boolean isSkipTitleRow) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<T> list  = new ArrayList<>();
        Sheet sheet = workbook.getSheetAt(0);
        for(Row row : sheet){
            if(isSkipTitleRow) {
                if (row.getRowNum() == 0) {
                    continue;
                }
            }
            T t = clazz.getConstructor().newInstance();
            Field[] fields = clazz.getDeclaredFields();
            for(int i = 0; i < row.getLastCellNum(); i++){
                if(i <= fields.length) {
                    if (Objects.nonNull(row.getCell(i))) {
                        fields[i].setAccessible(true);
                        if(fields[i].getType().equals(Float.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, Float.parseFloat(row.getCell(i).getStringCellValue()));
                        }
                        else if(fields[i].getType().equals(Double.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, Double.parseDouble(row.getCell(i).getStringCellValue()));
                        }
                        else if(fields[i].getType().equals(Integer.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, Integer.parseInt(row.getCell(i).getStringCellValue()));
                        }
                        else if(fields[i].getType().equals(Long.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, Long.parseLong(row.getCell(i).getStringCellValue()));
                        }
                        else if(fields[i].getType().equals(Boolean.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, Boolean.parseBoolean(row.getCell(i).getStringCellValue()));
                        }
                        else if(fields[i].getType().equals(String.class)){
                            row.getCell(i).setCellType(CellType.STRING);
                            fields[i].set(t, row.getCell(i).getStringCellValue().trim());
                        }
                    }
                }
            }
            list.add(t);
        }
        return list;
    }
}
