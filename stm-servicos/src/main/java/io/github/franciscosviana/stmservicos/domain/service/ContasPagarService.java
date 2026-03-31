package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.ContasPagarFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasPagarOutput;
import io.github.franciscosviana.stmservicos.domain.model.OrdemServico;
import io.github.franciscosviana.stmservicos.domain.model.PagamentoOS;
import io.github.franciscosviana.stmservicos.domain.repository.ContasPagarRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.xhtmlrenderer.pdf.ITextRenderer;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ContasPagarService {

    private final TemplateEngine templateEngine;
    private final ContasPagarRepository contasPagarRepository;

    public Page<ContasPagarOutput> listar(ContasPagarFilter filtro, Pageable pageable) {
        return contasPagarRepository.buscarComFiltro(filtro, pageable);
    }

    public List<ContasPagarOutput> listarTodos(ContasPagarFilter filtro) {
        return contasPagarRepository.buscarTodosComFiltro(filtro);
    }

    // ── XLSX ────────────────────────────────────────────────────────────────
    public byte[] exportarXlsx(ContasPagarFilter filtro) {
        List<ContasPagarOutput> dados = listarTodos(filtro);

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Contas a Pagar");

            // Estilos
            CellStyle headerStyle = wb.createCellStyle();
            Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moedaStyle = wb.createCellStyle();
            DataFormat fmt = wb.createDataFormat();
            moedaStyle.setDataFormat(fmt.getFormat("#,##0.00"));

            // Cabeçalho
            String[] cols = {
                    "OSG", "OS Cliente", "Cliente", "Credenciado", "Fluxo Pagamento",
                    "Status OS", "Data Abertura",
                    "Vlr. Chamado", "KM", "Vlr. KM", "Pedágio", "Estacionamento",
                    "Outros", "Vlr. Total",
                    "Pago", "Tipo Pagamento", "Banco / Pix", "CPF/NF",
                    "Lote", "Data Pagamento"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            // Dados
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter df  = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowIdx = 1;
            for (ContasPagarOutput d : dados) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;

                row.createCell(col++).setCellValue(nvl(d.getOsg()));
                row.createCell(col++).setCellValue(nvl(d.getOsClt()));
                row.createCell(col++).setCellValue(nvl(d.getCliente()));
                row.createCell(col++).setCellValue(nvl(d.getCredenciadoRag()));
                row.createCell(col++).setCellValue(d.getTipoFluxoPagamento() != null
                        ? d.getTipoFluxoPagamento().name() : "");
                row.createCell(col++).setCellValue(d.getStatusOrdem() != null
                        ? d.getStatusOrdem().name() : "");
                row.createCell(col++).setCellValue(d.getDataHoraAbertura() != null
                        ? d.getDataHoraAbertura().format(dtf) : "");

                setCurrency(row, col++, d.getValorChamado(), moedaStyle);
                setCurrency(row, col++, d.getKm(), moedaStyle);
                setCurrency(row, col++, d.getValorKm(), moedaStyle);
                setCurrency(row, col++, d.getPedagio(), moedaStyle);
                setCurrency(row, col++, d.getEstacionamento(), moedaStyle);
                setCurrency(row, col++, d.getValorOutros(), moedaStyle);
                setCurrency(row, col++, d.getValorTotal(), moedaStyle);

                row.createCell(col++).setCellValue(d.isPago() ? "SIM" : "NÃO");
                row.createCell(col++).setCellValue(d.getTipoPagamento() != null
                        ? d.getTipoPagamento().name() : "");
                row.createCell(col++).setCellValue(
                        d.getChavePix() != null ? d.getChavePix() : nvl(d.getBanco()));
                row.createCell(col++).setCellValue(nvl(d.getCpfNf()));
                row.createCell(col++).setCellValue(nvl(d.getLote()));
                row.createCell(col++).setCellValue(d.getDataPagamento() != null
                        ? d.getDataPagamento().format(df) : "");
            }

            wb.write(out);
            return out.toByteArray();

        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX de contas a pagar", e);
        }
    }

    // ── PDF ─────────────────────────────────────────────────────────────────
    public byte[] exportarPdf(ContasPagarFilter filtro) {
        List<ContasPagarOutput> dados = listarTodos(filtro);

        Context ctx = new Context();
        ctx.setVariable("dados", dados);

        String html = templateEngine.process("relatorio-contas-pagar", ctx);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de contas a pagar", e);
        }
    }

    // ── Helpers ─────────────────────────────────────────────────────────────
    private void setCurrency(Row row, int col, BigDecimal val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }

    public List<String> listarLotes() {
        return contasPagarRepository.buscarLotesDistintos();
    }
}