package io.github.franciscosviana.stmservicos.domain.service;

import com.opencsv.CSVWriter;
import io.github.franciscosviana.stmservicos.api.assembler.OrdemServicoOutputAssembler;
import io.github.franciscosviana.stmservicos.api.model.output.OrdemServicoOutput;
import io.github.franciscosviana.stmservicos.api.model.output.SolucaoOSOutput;
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
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrdemServicoExportService {

    private final SolucaoService solucaoService;
    private final TemplateEngine templateEngine;
    private final OrdemServicoRepository repository;
    private final OrdemServicoOutputAssembler assembler;
    private final OrdemServicoService ordemServicoService;

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────
    // BUSCA COM FILTROS (reutiliza a Specification existente)
    // ─────────────────────────────────────────────────────────────────
    private List<OrdemServicoOutput> buscarComFiltros(
            String osClt, String osg, String dataAbertura, String status, String cliente,
            String credenciado, String cidade, String estado, String rastreio) {

        Specification<OrdemServico> spec = OrdemServicoSpecification
                .filtro(osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

        return repository.findAll(spec)
                .stream()
                .map(assembler::toModel)
                .toList();
    }

    // ─────────────────────────────────────────────────────────────────
    // XLSX
    // ─────────────────────────────────────────────────────────────────
    public byte[] exportarXlsx(String osClt, String osg, String dataAbertura, String status, String cliente,
                               String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

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
    public byte[] exportarCsv(String osClt, String osg, String dataAbertura, String status, String cliente,
                              String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

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
    public byte[] exportarPdf(String osClt, String osg, String dataAbertura, String status, String cliente,
                              String credenciado, String cidade, String estado, String rastreio) {

        List<OrdemServicoOutput> ordens = buscarComFiltros(
                osClt, osg, dataAbertura, status, cliente, credenciado, cidade, estado, rastreio);

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
// RELATÓRIO INDIVIDUAL – PDF
// ─────────────────────────────────────────────────────────────────
    public byte[] exportarRelatorioPdf(UUID ordemId) {

        OrdemServicoOutput os = ordemServicoService.buscarPorId(ordemId);

        SolucaoOSOutput solucao = null;
        try {
            solucao = solucaoService.buscarPorOrdem(ordemId);
        } catch (Exception ignored) {
            // OS ainda não finalizada — solução ficará em branco no relatório
        }

        Context ctx = new Context();
        ctx.setVariable("os", os);
        ctx.setVariable("solucao", solucao);   // pode ser null
        ctx.setVariable("fmt", FMT);

        String html = templateEngine.process("relatorio-os-individual", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF individual", e);
        }
    }

    // ─────────────────────────────────────────────────────────────────
// RELATÓRIO INDIVIDUAL – XLSX
// ─────────────────────────────────────────────────────────────────
    public byte[] exportarRelatorioXlsx(UUID ordemId) {

        OrdemServicoOutput os = ordemServicoService.buscarPorId(ordemId);

        SolucaoOSOutput solucao = null;
        try {
            solucao = solucaoService.buscarPorOrdem(ordemId);
        } catch (Exception ignored) {
        }

        try (XSSFWorkbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // ── Estilos ──────────────────────────────────────────────
            CellStyle titleStyle = wb.createCellStyle();
            Font titleFont = wb.createFont();
            titleFont.setBold(true);
            titleFont.setFontHeightInPoints((short) 14);
            titleFont.setColor(IndexedColors.DARK_BLUE.getIndex());
            titleStyle.setFont(titleFont);

            CellStyle sectionStyle = wb.createCellStyle();
            Font sectionFont = wb.createFont();
            sectionFont.setBold(true);
            sectionFont.setColor(IndexedColors.WHITE.getIndex());
            sectionStyle.setFont(sectionFont);
            sectionStyle.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
            sectionStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);

            CellStyle labelStyle = wb.createCellStyle();
            Font labelFont = wb.createFont();
            labelFont.setBold(true);
            labelStyle.setFont(labelFont);
            labelStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            labelStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            labelStyle.setBorderBottom(BorderStyle.THIN);
            labelStyle.setBorderRight(BorderStyle.THIN);

            // ── Aba única: OS + Solução ───────────────────────────────
            Sheet sheet = wb.createSheet("Ordem de Serviço");
            sheet.setColumnWidth(0, 7000);
            sheet.setColumnWidth(1, 14000);
            sheet.setColumnWidth(2, 7000);
            sheet.setColumnWidth(3, 14000);

            int r = 0;

            // Título principal
            Row titleRow = sheet.createRow(r++);
            Cell titleCell = titleRow.createCell(0);
            titleCell.setCellValue("Ordem de Serviço – " + nvl(os.getOsg()));
            titleCell.setCellStyle(titleStyle);
            sheet.addMergedRegion(new CellRangeAddress(r - 1, r - 1, 0, 3));
            r++; // linha em branco

            // ── Seção: Dados da OS ────────────────────────────────────
            r = addSectionHeader(sheet, r, sectionStyle, "DADOS DA ORDEM DE SERVIÇO", 4);
            r = addRow2Col(sheet, r, labelStyle, "OSG", nvl(os.getOsg()),
                    "OS Cliente", nvl(os.getOsClt()));
            r = addRow2Col(sheet, r, labelStyle, "Status", nvl(os.getStatus()),
                    "Data Abertura",
                    os.getDataHoraAbertura() != null
                            ? os.getDataHoraAbertura().format(FMT) : "");
            r = addRow2Col(sheet, r, labelStyle, "Equipamento", nvl(os.getEquipamento()),
                    "Série", nvl(os.getSerie()));
            r = addRow2Col(sheet, r, labelStyle, "PIB", nvl(os.getPib()),
                    "Rastreio", nvl(os.getRastreio()));
            r = addRowFull(sheet, r, labelStyle, "Defeito", nvl(os.getDefeito()));
            r++;

            // ── Seção: Dados do Cliente ───────────────────────────────
            r = addSectionHeader(sheet, r, sectionStyle, "DADOS DO CLIENTE / CREDENCIADO", 4);
            r = addRow2Col(sheet, r, labelStyle,
                    "Cliente", os.getCliente() != null ? os.getCliente().getNome() : "",
                    "Credenciado", os.getCredenciado() != null ? os.getCredenciado().getRag() : "");
            r = addRow2Col(sheet, r, labelStyle,
                    "Técnico", os.getTecnico() != null ? os.getTecnico().getNome() : "",
                    "Contato", nvl(os.getContato()));
            r = addRow2Col(sheet, r, labelStyle,
                    "Departamento", nvl(os.getDepartamento()),
                    "Telefone", nvl(os.getTelefone()));

            // Endereço — exibe se o output tiver esses campos
            if (os.getEndereco() != null) {
                r = addRow2Col(sheet, r, labelStyle,
                        "Cidade", nvl(os.getEndereco().getCidade()),
                        "Estado", nvl(os.getEndereco().getEstado()));
                r = addRowFull(sheet, r, labelStyle,
                        "Logradouro", nvl(os.getEndereco().getLogradouro()));
            }
            r++;

            // ── Seção: Solução / Finalização ──────────────────────────
            r = addSectionHeader(sheet, r, sectionStyle, "SOLUÇÃO / FINALIZAÇÃO", 4);

            if (solucao != null) {
                r = addRow2Col(sheet, r, labelStyle,
                        "Data Atendimento",
                        solucao.getDataAtendimento() != null ? solucao.getDataAtendimento().format(FMT) : "",
                        "Hora Inicial",
                        solucao.getHoraInicial() != null ? solucao.getHoraInicial().format(FMT) : "");
                r = addRow2Col(sheet, r, labelStyle,
                        "Hora Final",
                        solucao.getHoraFinal() != null ? solucao.getHoraFinal().format(FMT) : "",
                        "Peça Solicitada", nvl(solucao.getPecaSolicitada()));
                r = addRow2Col(sheet, r, labelStyle,
                        "KM", solucao.getKm() != null ? solucao.getKm().toString() : "",
                        "Pedágio", solucao.getPedagio() != null ? solucao.getPedagio().toString() : "");
                r = addRow2Col(sheet, r, labelStyle,
                        "Estacionamento", solucao.getEstacionamento() != null ? solucao.getEstacionamento().toString() : "",
                        "Outros", solucao.getOutros() != null ? solucao.getOutros().toString() : "");
                r = addRowFull(sheet, r, labelStyle, "Solução", nvl(solucao.getSolucao()));
                r = addRowFull(sheet, r, labelStyle, "Observação", nvl(solucao.getObservacao()));
            } else {
                Row emptyRow = sheet.createRow(r++);
                emptyRow.createCell(0).setCellValue("OS ainda não finalizada.");
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX individual", e);
        }
    }

// ── Helpers de layout ─────────────────────────────────────────────

    /**
     * Linha de cabeçalho de seção spanning N colunas
     */
    private int addSectionHeader(Sheet sheet, int r, CellStyle style, String title, int cols) {
        Row row = sheet.createRow(r);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        for (int c = 1; c < cols; c++) {
            Cell filler = row.createCell(c);
            filler.setCellStyle(style);
        }
        sheet.addMergedRegion(new CellRangeAddress(r, r, 0, cols - 1));
        return r + 1;
    }

    /**
     * Linha com 2 pares label|valor lado a lado (4 colunas)
     */
    private int addRow2Col(Sheet sheet, int r, CellStyle labelStyle,
                           String l1, String v1, String l2, String v2) {
        Row row = sheet.createRow(r);
        Cell c0 = row.createCell(0);
        c0.setCellValue(l1);
        c0.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(v1);
        Cell c2 = row.createCell(2);
        c2.setCellValue(l2);
        c2.setCellStyle(labelStyle);
        row.createCell(3).setCellValue(v2);
        return r + 1;
    }

    /**
     * Linha com label em col 0 e valor ocupando as 3 colunas restantes
     */
    private int addRowFull(Sheet sheet, int r, CellStyle labelStyle, String label, String value) {
        Row row = sheet.createRow(r);
        Cell c0 = row.createCell(0);
        c0.setCellValue(label);
        c0.setCellStyle(labelStyle);
        row.createCell(1).setCellValue(value);
        sheet.addMergedRegion(new CellRangeAddress(r, r, 1, 3));
        return r + 1;
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