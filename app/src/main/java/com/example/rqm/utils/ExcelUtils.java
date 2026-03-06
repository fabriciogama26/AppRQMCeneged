package com.example.rqm.utils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.rqm.models.Material;
import com.example.rqm.models.Requisicao;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
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

public class ExcelUtils {

    private static final String EXPORT_DIR = "exports";
    private static final String FILE_EXTENSION = ".xlsx";
    private static final String TAG = "ExcelUtils";

    public static void compartilharRequisicao(Context context, Requisicao requisicao) {
        try {
            File arquivo = exportarParaExcel(context, requisicao);
            compartilharArquivo(context, arquivo);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar Excel: ", e);
            Toast.makeText(context, "Erro ao gerar arquivo Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportarECompartilharHistorico(Context context, List<Requisicao> requisicoes) {
        if (requisicoes == null || requisicoes.isEmpty()) {
            Toast.makeText(context, "Nenhum dado para exportar", Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            File arquivo = exportarHistoricoParaExcel(context, requisicoes);
            compartilharArquivo(context, arquivo);
        } catch (Exception e) {
            Log.e(TAG, "Erro ao exportar Excel: ", e);
            Toast.makeText(context, "Erro ao gerar arquivo Excel: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static File exportarParaExcel(Context context, Requisicao requisicao) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet(OperacaoTipo.toLabel(requisicao.tipoOperacao));

        CellStyle headerStyle = criarEstiloCabecalho(workbook);
        CellStyle dataStyle = criarEstiloDados(workbook);

        criarCabecalho(sheet, headerStyle, requisicao);
        double total = preencherDados(sheet, requisicao.getMateriaisSelecionados(), dataStyle, requisicao);
        adicionarTotal(sheet, total, dataStyle);

        sheet.setColumnWidth(0, 20 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 40 * 256);
        sheet.setColumnWidth(4, 40 * 256);
        sheet.setColumnWidth(5, 20 * 256);
        sheet.setColumnWidth(6, 20 * 256);
        sheet.setColumnWidth(7, 15 * 256);
        sheet.setColumnWidth(8, 20 * 256);
        sheet.setColumnWidth(9, 20 * 256);
        sheet.setColumnWidth(10, 15 * 256);
        sheet.setColumnWidth(11, 20 * 256);
        sheet.setColumnWidth(12, 20 * 256);

        String prefixoArquivo = OperacaoTipo.isDevolucao(requisicao.tipoOperacao) ? "DEV" : "REQ";

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
        String prefixoCabecario = OperacaoTipo.isDevolucao(requisicao.tipoOperacao) ? "Devolutor" : "Requisitor";
        Row row = sheet.createRow(0);
        String[] headers = {"Projeto", "Código", "Descrição", "Observação", "Data", prefixoCabecario, "Usuario", "LP", "Serial", "Quantidade", "Valor Unitário", "Total"};
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
            row.createCell(4).setCellValue(DateUtils.toDisplayDate(requisicao.data));
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

    private static File exportarHistoricoParaExcel(Context context, List<Requisicao> requisicoes) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Historico");

        CellStyle headerStyle = criarEstiloCabecalho(workbook);
        CellStyle dataStyle = criarEstiloDados(workbook);

        String[] headers = {
                "Data", "Projeto", "Requisitor", "Usuario", "Operacao",
                "Codigo", "Descricao", "UMD", "Tipo", "LP", "Serial",
                "Quantidade", "Valor Unitario", "Total"
        };

        Row header = sheet.createRow(0);
        for (int i = 0; i < headers.length; i++) {
            Cell cell = header.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        int rowNum = 1;
        for (Requisicao req : requisicoes) {
            List<Material> materiais = req.getMateriaisSelecionados();
            if (materiais == null) {
                continue;
            }
            for (Material mat : materiais) {
                Row row = sheet.createRow(rowNum++);

                double qtde = safeToDouble(mat.quantidade);
                double valorUnit = safeToDouble(mat.valor_unitario);
                double total = valorUnit > 0 ? qtde * valorUnit : qtde;

                row.createCell(0).setCellValue(DateUtils.toDisplayDate(req.data));
                row.createCell(1).setCellValue(safeString(req.projeto));
                row.createCell(2).setCellValue(safeString(req.requisitor));
                row.createCell(3).setCellValue(safeString(req.usuario));
                row.createCell(4).setCellValue(OperacaoTipo.toLabel(req.tipoOperacao));
                row.createCell(5).setCellValue(safeString(mat.codigo));
                row.createCell(6).setCellValue(safeString(mat.descricao));
                row.createCell(7).setCellValue(safeString(mat.umb));
                row.createCell(8).setCellValue(safeString(mat.tipo));
                row.createCell(9).setCellValue(safeString(mat.lp));
                row.createCell(10).setCellValue(safeString(mat.serial));
                row.createCell(11).setCellValue(qtde);
                row.createCell(12).setCellValue(valorUnit);
                row.createCell(13).setCellValue(total);

                for (int i = 0; i <= 13; i++) {
                    row.getCell(i).setCellStyle(dataStyle);
                }
            }
        }

        sheet.setColumnWidth(0, 18 * 256);
        sheet.setColumnWidth(1, 20 * 256);
        sheet.setColumnWidth(2, 22 * 256);
        sheet.setColumnWidth(3, 18 * 256);
        sheet.setColumnWidth(4, 16 * 256);
        sheet.setColumnWidth(5, 16 * 256);
        sheet.setColumnWidth(6, 40 * 256);
        sheet.setColumnWidth(7, 10 * 256);
        sheet.setColumnWidth(8, 14 * 256);
        sheet.setColumnWidth(9, 12 * 256);
        sheet.setColumnWidth(10, 12 * 256);
        sheet.setColumnWidth(11, 14 * 256);
        sheet.setColumnWidth(12, 16 * 256);
        sheet.setColumnWidth(13, 16 * 256);

        return salvarArquivo(context, workbook, "HIST_", "HISTORICO");
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
            context.startActivity(Intent.createChooser(intent, "Compartilhar requisicao"));
        } else {
            Toast.makeText(context, "Nenhum app disponivel para compartilhar", Toast.LENGTH_SHORT).show();
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

    private static String safeString(String valor) {
        return valor != null ? valor : "";
    }
}

