package io.github.franciscosviana.stmservicos.domain.service;

import com.opencsv.CSVWriter;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.repository.OrdemServicoRepository;
import io.github.franciscosviana.stmservicos.domain.repository.spec.OrdemServicoSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoExportService {

    private final OrdemServicoRepository repository;
    private final OrdemServicoOutputAssembler assembler; // injete o seu assembler real
    private final TemplateEngine templateEngine;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────
    // BUSCA COM FILTROS (reutiliza a Specification existente)
    // ─────────────────────────────────────────────────────────────────
    private List<OrdemServicoOutput> buscarComFiltros(
            String osClt, String osg, String status, String cliente,
            String credenciado, String cidade, String estado, String rastreio) {

        Specification<OrdemServico> spec = OrdemServicoSpecification
                .filtro(osClt, osg, status, cliente, credenciado, cidade, estado, rastreio);

        return repository.findAll(spec)
                .stream()
                .map(assembler::toModel)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────
    // XLSX
    // ─────────────────────────────────────────────────────────────────
    public byte[] exportarXlsx(String osClt, String osg, String status, String cliente,
                               String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, status, cliente, credenciado, cidade, estado, rastreio);

        try (XSSFWorkbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Ordens de Serviço");

            // ── Estilos ──────────────────────────────────────────────
            CellStyle titleStyle = workbook.createCellStyle();
            Font titleFont = workbook.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleStyle.setFont(titleFont);
            titleStyle.setAlignment(HorizontalAlignment.CENTER);

            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setColor(IndexedColors.WHITE.getIndex());
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle dateStyle = workbook.createCellStyle();
            dateStyle.setDataFormat(workbook.createDataFormat().getFormat("dd/mm/yyyy hh:mm"));

            CellStyle altRowStyle = workbook.createCellStyle();
            altRowStyle.setFillForegroundColor(IndexedColors.LIGHT_CORNFLOWER_BLUE.getIndex());
            altRowStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            // ── Título ───────────────────────────────────────────────
            Row titleRow = sheet.createRow(0);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Relatório de Ordens de Serviço");
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 13));

            // ── Cabeçalho ────────────────────────────────────────────
            String[] headers = {
                    "OSG", "OS Cliente", "Status", "Data Abertura",
                    "Cliente", "Credenciado", "Técnico",
                    "Contato", "Departamento", "Telefone",
                    "Equipamento", "Série", "Defeito", "Rastreio"
            };

            Row headerRow = sheet.createRow(1);
            for (int i = 0; i < headers.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers[i]);
                cell.setCellStyle(headerStyle);
            }

            // ── Dados ────────────────────────────────────────────────
            int rowNum = 2;
            for (OrdemServicoOutput os : ordens) {
                Row row = sheet.createRow(rowNum);

                if (rowNum % 2 == 0) {
                    for (int c = 0; c < headers.length; c++) {
                        row.createCell(c).setCellStyle(altRowStyle);
                    }
                }

                setCellValue(row, 0, os.getOsg());
                setCellValue(row, 1, os.getOsClt());
                setCellValue(row, 2, os.getStatus());
                setCellValue(row, 3,
                        os.getDataHoraAbertura() != null ? os.getDataHoraAbertura().format(FMT) : "");
                setCellValue(row, 4,
                        os.getCliente() != null ? os.getCliente().getNome() : "");
                setCellValue(row, 5,
                        os.getCredenciado() != null ? os.getCredenciado().getRag() : "");
                setCellValue(row, 6,
                        os.getTecnico() != null ? os.getTecnico().getNome() : "");
                setCellValue(row, 7, os.getContato());
                setCellValue(row, 8, os.getDepartamento());
                setCellValue(row, 9, os.getTelefone());
                setCellValue(row, 10, os.getEquipamento());
                setCellValue(row, 11, os.getSerie());
                setCellValue(row, 12, os.getDefeito());
                setCellValue(row, 13, os.getRastreio());

                rowNum++;
            }

            // ── Autosize ─────────────────────────────────────────────
            for (int i = 0; i < headers.length; i++) {
                sheet.autoSizeColumn(i);
                sheet.setColumnWidth(i, sheet.getColumnWidth(i) + 512);
            }

            workbook.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // CSV
    // ─────────────────────────────────────────────────────────────────
    public byte[] exportarCsv(String osClt, String osg, String status, String cliente,
                              String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, status, cliente, credenciado, cidade, estado, rastreio);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             OutputStreamWriter osw = new OutputStreamWriter(out, StandardCharsets.UTF_8);
             CSVWriter writer = new CSVWriter(osw, ';',
                     CSVWriter.DEFAULT_QUOTE_CHARACTER,
                     CSVWriter.DEFAULT_ESCAPE_CHARACTER,
                     CSVWriter.DEFAULT_LINE_END)) {

            // BOM para Excel reconhecer UTF-8
            out.write(0xEF);
            out.write(0xBB);
            out.write(0xBF);

            writer.writeNext(new String[]{
                    "OSG", "OS Cliente", "Status", "Data Abertura",
                    "Cliente", "Credenciado", "Técnico",
                    "Contato", "Departamento", "Telefone",
                    "Equipamento", "Série", "Defeito", "Rastreio"
            });

            for (OrdemServicoOutput os : ordens) {
                writer.writeNext(new String[]{
                        nvl(os.getOsg()),
                        nvl(os.getOsClt()),
                        nvl(os.getStatus()),
                        os.getDataHoraAbertura() != null ? os.getDataHoraAbertura().format(FMT) : "",
                        os.getCliente() != null ? os.getCliente().getNome() : "",
                        os.getCredenciado() != null ? os.getCredenciado().getRag() : "",
                        os.getTecnico() != null ? os.getTecnico().getNome() : "",
                        nvl(os.getContato()),
                        nvl(os.getDepartamento()),
                        nvl(os.getTelefone()),
                        nvl(os.getEquipamento()),
                        nvl(os.getSerie()),
                        nvl(os.getDefeito()),
                        nvl(os.getRastreio())
                });
            }

            writer.flush();
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar CSV", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // PDF  (via Thymeleaf + Flying Saucer)
    // ─────────────────────────────────────────────────────────────────
    public byte[] exportarPdf(String osClt, String osg, String status, String cliente,
                              String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, status, cliente, credenciado, cidade, estado, rastreio);

        Context ctx = new Context();
        ctx.setVariable("ordens", ordens);
        ctx.setVariable("fmt", FMT);

        String html = templateEngine.process("relatorio-os", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
    // Helpers
    // ─────────────────────────────────────────────────────────────────
    private void setCellValue(Row row, int col, String value) {
        Cell cell = row.getCell(col);
        if (cell == null) cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
    }

    private String nvl(String v) {
        return v != null ? v : "";
    }
}