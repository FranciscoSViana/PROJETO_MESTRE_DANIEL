package io.github.franciscosviana.stmservicos.domain.service;

import io.github.franciscosviana.stmservicos.api.model.input.ContasReceberFilter;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberOutput;
import io.github.franciscosviana.stmservicos.api.model.output.ContasReceberTotaisOutput;
import io.github.franciscosviana.stmservicos.domain.repository.ContasReceberRepository;
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
public class ContasReceberService {

    private final TemplateEngine templateEngine;
    private final ContasReceberRepository contasReceberRepository;

    public Page<ContasReceberOutput> listar(ContasReceberFilter filtro, Pageable pageable) {
        return contasReceberRepository.buscarComFiltro(filtro, pageable);
    }

    public ContasReceberTotaisOutput buscarTotais(ContasReceberFilter filtro) {
        return contasReceberRepository.buscarTotais(filtro);
    }

    public List<String> listarLotes() {
        return contasReceberRepository.buscarLotesDistintos();
    }

    public List<String> listarLotesPorCliente(String cliente) {
        return contasReceberRepository.buscarLotesPorCliente(cliente);
    }

    public List<ContasReceberOutput> listarOsPendentes(String cliente, String lote) {
        ContasReceberFilter filtro = new ContasReceberFilter();
        filtro.setClienteSnapshot(cliente);
        filtro.setLote(lote);
        filtro.setPago(false);         // ← era recebido=false, agora pago=false
        // recebido não filtra aqui — queremos ver tanto pendentes quanto corrigidos
        return contasReceberRepository.buscarTodosComFiltro(filtro);
    }

    // ── XLSX ──────────────────────────────────────────────────────────────────
    public byte[] exportarXlsx(ContasReceberFilter filtro) {
        List<ContasReceberOutput> dados = contasReceberRepository.buscarTodosComFiltro(filtro);

        try (Workbook wb = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = wb.createSheet("Contas a Receber");

            CellStyle headerStyle = wb.createCellStyle();
            Font hFont = wb.createFont();
            hFont.setBold(true);
            headerStyle.setFont(hFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setBorderBottom(BorderStyle.THIN);

            CellStyle moedaStyle = wb.createCellStyle();
            moedaStyle.setDataFormat(wb.createDataFormat().getFormat("#,##0.00"));

            String[] cols = {
                    "OSG", "OS Cliente", "Cliente", "Status OS", "Data Abertura",
                    "Vlr. Chamado", "KM", "Vlr. KM", "Pedágio", "Estacionamento",
                    "Outros", "Vlr. Total",
                    "Recebido", "Pago", "Tipo Pgto", "Banco", "NF",
                    "Lote", "Data Prevista", "Data Pagamento"
            };

            Row header = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(cols[i]);
                cell.setCellStyle(headerStyle);
                sheet.setColumnWidth(i, 5000);
            }

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
            DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");

            int rowIdx = 1;
            for (ContasReceberOutput d : dados) {
                Row row = sheet.createRow(rowIdx++);
                int col = 0;
                row.createCell(col++).setCellValue(nvl(d.getOsg()));
                row.createCell(col++).setCellValue(nvl(d.getOsClt()));
                row.createCell(col++).setCellValue(nvl(d.getCliente()));
                row.createCell(col++).setCellValue(d.getStatusOrdem() != null ? d.getStatusOrdem().name() : "");
                row.createCell(col++).setCellValue(d.getDataHoraAbertura() != null ? d.getDataHoraAbertura().format(dtf) : "");
                setCurrency(row, col++, d.getValorChamado(), moedaStyle);
                setCurrency(row, col++, d.getKm(), moedaStyle);
                setCurrency(row, col++, d.getValorKm(), moedaStyle);
                setCurrency(row, col++, d.getPedagio(), moedaStyle);
                setCurrency(row, col++, d.getEstacionamento(), moedaStyle);
                setCurrency(row, col++, d.getValorOutros(), moedaStyle);
                setCurrency(row, col++, d.getValorTotal(), moedaStyle);
                row.createCell(col++).setCellValue(d.isRecebido() ? "SIM" : "NÃO");
                row.createCell(col++).setCellValue(d.isPago() ? "SIM" : "NÃO");
                row.createCell(col++).setCellValue(d.getTipoPagamento() != null ? d.getTipoPagamento().name() : "");
                row.createCell(col++).setCellValue(nvl(d.getBanco()));
                row.createCell(col++).setCellValue(nvl(d.getNf()));
                row.createCell(col++).setCellValue(nvl(d.getLote()));
                row.createCell(col++).setCellValue(d.getDataPrevista() != null ? d.getDataPrevista().format(df) : "");
                row.createCell(col++).setCellValue(d.getDataPagamento() != null ? d.getDataPagamento().format(df) : "");
            }

            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar XLSX de contas a receber", e);
        }
    }

    // ── PDF ───────────────────────────────────────────────────────────────────
    public byte[] exportarPdf(ContasReceberFilter filtro) {
        List<ContasReceberOutput> dados = contasReceberRepository.buscarTodosComFiltro(filtro);
        Context ctx = new Context();
        ctx.setVariable("dados", dados);
        String html = templateEngine.process("relatorio-contas-receber", ctx);
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            ITextRenderer renderer = new ITextRenderer();
            renderer.setDocumentFromString(html);
            renderer.layout();
            renderer.createPDF(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erro ao gerar PDF de contas a receber", e);
        }
    }

    private void setCurrency(Row row, int col, BigDecimal val, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(val != null ? val.doubleValue() : 0.0);
        cell.setCellStyle(style);
    }

    private String nvl(String s) {
        return s != null ? s : "";
    }
}