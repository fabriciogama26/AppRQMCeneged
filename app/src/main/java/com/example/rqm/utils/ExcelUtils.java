package com.example.rqm.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Classe utilitária para exportar e compartilhar arquivos Excel (.xlsx)
 * contendo os dados de uma requisição ou devolução de materiais.
 */
public class ExcelUtils {

    private static final String EXPORT_DIR = "exports";
    private static final String FILE_EXTENSION = ".xlsx";
    private static final String TAG = "ExcelUtils";

    /**
     * Gera e compartilha um arquivo Excel baseado na requisição recebida.
     * @param context Contexto da aplicação.
     * @param requisicao Objeto contendo os dados a serem exportados.
     */
    public static void compartilharRequisicao(Context context, Requisicao requisicao) {
        try {
            File arquivo = exportarParaExcel(context, requisicao);
            compartilharArquivo(context, arquivo);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar Excel: ", e);
            Toast.makeText(context, "Erro ao gerar arquivo Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Gera o arquivo Excel com base nos dados da requisição.
     */
    public static File exportarParaExcel(Context context, Requisicao requisicao) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(requisicao.tipoOperacao);

        CellStyle headerStyle = criarEstiloCabecalho(workbook);
        CellStyle dataStyle = criarEstiloDados(workbook);

        criarCabecalho(sheet, headerStyle, requisicao);
        double total = preencherDados(sheet, requisicao.getMateriaisSelecionados(), dataStyle, requisicao);
        adicionarTotal(sheet, total, dataStyle);

        // Define larguras fixas, pois autoSizeColumn usa java.awt
        sheet.setColumnWidth(0, 20 * 256); // Projeto
        sheet.setColumnWidth(1, 20 * 256); // Código
        sheet.setColumnWidth(2, 40 * 256); // Descrição
        sheet.setColumnWidth(4, 40 * 256); // Observação
        sheet.setColumnWidth(5, 20 * 256); // Data
        sheet.setColumnWidth(6, 20 * 256); // Requitor(Devolutor)
        sheet.setColumnWidth(7, 15 * 256); // Usuario
        sheet.setColumnWidth(8, 20 * 256); // LP
        sheet.setColumnWidth(9, 20 * 256); // Serial
        sheet.setColumnWidth(10, 15 * 256); // Quantidade
        sheet.setColumnWidth(11, 20 * 256); // Valor Unitário
        sheet.setColumnWidth(12, 20 * 256); // Total

        String prefixoArquivo = requisicao.tipoOperacao.equalsIgnoreCase("Devolução") ? "DEV" : "REQ";

        return salvarArquivo(context, workbook, prefixoArquivo + "_", requisicao.projeto);
    }

    private static CellStyle criarEstiloCabecalho(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        style.setFont(font);
        style.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static CellStyle criarEstiloDados(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private static void criarCabecalho(Sheet sheet, CellStyle style, Requisicao requisicao) {
        String prefixoCabecario = requisicao.tipoOperacao.equalsIgnoreCase("Devolução") ? "Devolutor" : "Requisitor";
        Row row = sheet.createRow(0);
        String[] headers = {"Projeto", "Código", "Descrição","Observação","Data", prefixoCabecario, "Usuario", "LP", "Serial", "Quantidade", "Valor Unitário", "Total"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(style);
        }
    }



    private static double preencherDados(Sheet sheet, List<Material> materiais, CellStyle style, Requisicao requisicao) {
        double totalGeral = 0;
        int rowNum = 1;
        for (Material mat : materiais) {
            Row row = sheet.createRow(rowNum++);
            double qtde = safeToDouble(mat.quantidade);
            double valorUnit = safeToDouble(mat.valor_unitario);
            double total = qtde * valorUnit;


            row.createCell(0).setCellValue(requisicao.projeto != null ? requisicao.projeto : "");
            row.createCell(1).setCellValue(mat.codigo != null ? mat.codigo : "");
            row.createCell(2).setCellValue(mat.descricao != null ? mat.descricao : "");
            row.createCell(3).setCellValue(requisicao.observacao != null ? requisicao.observacao : "");
            row.createCell(4).setCellValue(requisicao.data != null ? requisicao.data : "");
            row.createCell(5).setCellValue(requisicao.requisitor != null ? requisicao.requisitor : "");
            row.createCell(6).setCellValue(requisicao.usuario != null ? requisicao.usuario : "");
            row.createCell(7).setCellValue(mat.lp != null ? mat.lp : "");
            row.createCell(8).setCellValue(mat.serial != null ? mat.serial : "");
            row.createCell(9).setCellValue(qtde);
            row.createCell(10).setCellValue(valorUnit);
            row.createCell(11).setCellValue(total);

            for (int i = 0; i <= 11; i++) {
                row.getCell(i).setCellStyle(style);
            }

            totalGeral += total;
        }
        return totalGeral;
    }

    private static void adicionarTotal(Sheet sheet, double total, CellStyle style) {
        int rowNum = sheet.getLastRowNum() + 1;
        Row row = sheet.createRow(rowNum);
        row.createCell(10).setCellValue("TOTAL GERAL:");
        row.createCell(11).setCellValue(total);
        row.getCell(10).setCellStyle(style);
        row.getCell(11).setCellStyle(style);
    }

    private static File salvarArquivo(Context context, Workbook workbook, String prefixo, String projeto) throws IOException {
        File dir = new File(context.getExternalFilesDir(null), EXPORT_DIR);
        if (!dir.exists()) dir.mkdirs();

        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        String nomeArquivo = prefixo + projeto + "_" + timestamp + FILE_EXTENSION;
        File arquivo = new File(dir, nomeArquivo);

        try (FileOutputStream out = new FileOutputStream(arquivo)) {
            workbook.write(out);
            workbook.close();
        }

        Log.d(TAG, "Arquivo salvo: " + arquivo.getAbsolutePath());
        return arquivo;
    }

    private static void compartilharArquivo(Context context, File arquivo) {
        Uri uri = FileProvider.getUriForFile(
                context,
                context.getPackageName() + ".provider",
                arquivo
        );

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

        if (intent.resolveActivity(context.getPackageManager()) != null) {
            context.startActivity(Intent.createChooser(intent, "Compartilhar requisição"));
        } else {
            Toast.makeText(context, "Nenhum app disponível para compartilhar", Toast.LENGTH_SHORT).show();
        }
    }

    private static double safeToDouble(Object valor) {
        try {
            if (valor instanceof Number) {
                return ((Number) valor).doubleValue();
            }
            return Double.parseDouble(valor.toString());
        } catch (Exception e) {
            return 0;
        }
    }
}
