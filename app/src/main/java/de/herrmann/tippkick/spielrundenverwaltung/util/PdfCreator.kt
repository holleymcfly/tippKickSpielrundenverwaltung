package de.herrmann.tippkick.spielrundenverwaltung.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import java.io.File
import java.io.FileOutputStream


class PdfCreator {

    private var verticalPosition: Float = 100F
    private lateinit var competition: CompetitionDAO
    private lateinit var pairings: List<PairingDAO>
    private lateinit var context: Context

    fun createCompetitionPdf(
        competition: CompetitionDAO, pairings: List<PairingDAO>,
        context: Context
    ) {

        this.competition = competition
        this.pairings = pairings
        this.context = context

        val document = PdfDocument()
        val pageInfo = PageInfo.Builder(595, 842, 1).create()
        val page = document.startPage(pageInfo)
        val canvas = page.canvas

        writeCompetitionName(canvas, competition.name)

        document.finishPage(page)

        openDocument(document)

        document.close()
    }

    private fun writeCompetitionName(canvas: Canvas, competitionName: String) {

        val title = Paint()
        title.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        title.textSize = 15F

        canvas.drawText(competitionName, 10F, verticalPosition, title)
    }

    private fun openDocument(document: PdfDocument) {

        try {
            val file =
                File(context.getExternalFilesDir("/"), "Competition-" + competition.id + ".pdf")
            document.writeTo(FileOutputStream(file))

            val photoURI = FileProvider.getUriForFile(
                context,
                context.applicationContext.packageName + ".provider",
                file
            )

            val intent = Intent(Intent.ACTION_VIEW, photoURI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)

            startActivity(context, intent, null)
        }
        catch (e: Exception) {
            Util.showOkButtonMessage(context, context.getString(R.string.pdf_cannot_be_opened))
        }
    }
}