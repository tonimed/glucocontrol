package com.glucocontrol.data.export

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import com.glucocontrol.domain.model.GlucoseReading
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

// PdfDocument trabaja en puntos a 72 dpi — A4 son 595 × 842 pts.
@Singleton
class PdfExporter @Inject constructor(@ApplicationContext private val context: Context) {
    fun export(readings: List<GlucoseReading>, stream: OutputStream, title: String = "Informe de glucosa") {
        val doc = PdfDocument()
        try {
            var pageNum = 1
            var currentPage = newPage(doc, pageNum++)
            var canvas = currentPage.canvas

            drawTitle(canvas, title)
            drawColumnHeaders(canvas)

            val rowPaint = buildPaint(textSize = 11f)
            var y = DATA_START_Y

            readings.forEach { reading ->
                // Nueva página si no hay espacio para otra fila
                if (y + ROW_H > PAGE_H - MARGIN) {
                    doc.finishPage(currentPage)
                    currentPage = newPage(doc, pageNum++)
                    canvas = currentPage.canvas
                    drawColumnHeaders(canvas)
                    y = DATA_START_Y
                }
                drawRow(canvas, rowPaint, y.toFloat(), reading)
                y += ROW_H
            }

            doc.finishPage(currentPage)
            doc.writeTo(stream)
        } finally {
            doc.close()
        }
    }

    private fun newPage(doc: PdfDocument, num: Int): PdfDocument.Page {
        val info = PdfDocument.PageInfo.Builder(PAGE_W, PAGE_H, num).create()
        return doc.startPage(info)
    }

    private fun buildPaint(textSize: Float, bold: Boolean = false): Paint = Paint().apply {
        this.textSize = textSize
        typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD) else Typeface.DEFAULT
        isAntiAlias = true
    }

    private fun drawTitle(canvas: Canvas, title: String) {
        canvas.drawText(title, MARGIN.toFloat(), TITLE_Y.toFloat(), buildPaint(18f, bold = true))
    }

    private fun drawColumnHeaders(canvas: Canvas) {
        val paint = buildPaint(11f, bold = true)
        val y = COL_HEADER_Y.toFloat()
        HEADERS.forEachIndexed { i, label -> canvas.drawText(label, colX(i), y, paint) }
        // línea separadora debajo de los encabezados
        val divider = Paint().apply { strokeWidth = 0.8f }
        canvas.drawLine(
            MARGIN.toFloat(),
            (COL_HEADER_Y + 5).toFloat(),
            (PAGE_W - MARGIN).toFloat(),
            (COL_HEADER_Y + 5).toFloat(),
            divider,
        )
    }

    private fun drawRow(canvas: Canvas, paint: Paint, y: Float, r: GlucoseReading) {
        canvas.drawText(r.date.toString(), colX(0), y, paint)
        canvas.drawText(r.time?.toString() ?: "-", colX(1), y, paint)
        canvas.drawText("${r.valueMgDl}", colX(2), y, paint)
        canvas.drawText(r.tag.label, colX(3), y, paint)
        // Truncar notas para que no desborden la columna
        canvas.drawText((r.notes ?: "-").take(MAX_NOTES_CHARS), colX(4), y, paint)
    }

    private fun colX(index: Int): Float = COL_STARTS[index].toFloat()

    companion object {
        private const val PAGE_W = 595
        private const val PAGE_H = 842
        private const val MARGIN = 40
        private const val TITLE_Y = 55
        private const val COL_HEADER_Y = 78
        private const val DATA_START_Y = 100
        private const val ROW_H = 20
        private const val MAX_NOTES_CHARS = 38

        private val HEADERS = arrayOf("Fecha", "Hora", "mg/dL", "Etiqueta", "Notas")

        // posiciones x absolutas de cada columna desde el borde izquierdo de la página
        private val COL_STARTS = intArrayOf(40, 130, 190, 245, 345)
    }
}
