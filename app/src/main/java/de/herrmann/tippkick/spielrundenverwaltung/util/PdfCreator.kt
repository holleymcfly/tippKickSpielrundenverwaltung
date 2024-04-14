package de.herrmann.tippkick.spielrundenverwaltung.util

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.Page
import android.graphics.pdf.PdfDocument.PageInfo
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import java.io.File
import java.io.FileOutputStream
import java.util.Date


class PdfCreator {

    private val PAGE_HEIGHT = 842
    private val PAGE_WIDTH = 595

    private val horizontalStart: Float = 30F

    private val REGULAR_SIZE = 9F

    private lateinit var document: PdfDocument
    private lateinit var page: Page
    private var pageNumber = 0
    private lateinit var canvas: Canvas

    private var verticalPosition: Float = 50F
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

        document = PdfDocument()
        nextPage(false)

        writeCompetitionName()
        writeCompetitionDate()

        if (Util.isDfbCompetition(this.competition)) {
            writeDfbCompetitionBody()
        }
        else if (Util.isGroupCompetition(this.competition)) {
            writeGroupCompetitionBody()
        }

        document.finishPage(page)

        openDocument(document)

        document.close()
    }

    private fun writeGroupCompetitionBody() {

        writeGroupPhaseOfGroupCompetition()
    }

    private fun writeGroupPhaseOfGroupCompetition() {

        val pairingDBAccess = PairingDBAccess()
        val allPairings = pairingDBAccess.getPairingsForCompetition(this.context, this.competition.id, 1)
        allPairings.forEach { pairing -> pairing.setContext(this.context) }

        verticalPosition += REGULAR_SIZE

        val groupHeaderPaint = Paint()
        groupHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        groupHeaderPaint.textSize = 12F

        val paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        paint.textSize = REGULAR_SIZE

        val paintBold = Paint()
        paintBold.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paintBold.textSize = REGULAR_SIZE

        for (group in 1.. this.competition.numberOfGroups) {

            val pairingsInGroup = Util.getPairingsForGroup(allPairings, group)

            drawText(context.getString(R.string.group) + group, horizontalStart+10F,
                groupHeaderPaint)
            verticalPosition += 15F

            pairingsInGroup.forEach { pairing ->
                drawText(pairing.toStringShort(), horizontalStart+10F, paint)
                drawText(pairing.toResultOnly(), 400F, paintBold)
                verticalPosition += REGULAR_SIZE
                drawText(Util.toDateTimeString(pairing.playDate), 400F, paint)
                verticalPosition += REGULAR_SIZE + 2F
            }

            verticalPosition += REGULAR_SIZE
        }
    }

    private fun writeDfbCompetitionBody() {

        val pairingsDBAccess = PairingDBAccess()
        for (round in 1..getMaxRound()) {
            val pairingsInRound = pairingsDBAccess.getPairingsForCompetition(
                this.context,
                this.competition.id, round
            )
            pairingsInRound.forEach { pairing -> pairing.setContext(this.context) }
            writePairingRound(pairingsInRound)
        }
    }

    private fun nextPage(finishPage: Boolean) {

        if (finishPage) {
            document.finishPage(page)
        }

        pageNumber++
        val pageInfo = PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, this.pageNumber).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas

    }

    private fun drawText(text: String, horizontalStart: Float, paint: Paint) {

        if (verticalPosition > 800) {
            nextPage(true)
            verticalPosition = 50F
        }

        canvas.drawText(text, horizontalStart, verticalPosition, paint)
    }

    private fun writeCompetitionName() {

        val paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paint.textSize = 20F

        val title = competition.competitionType.toString() + ": " + competition.name
        drawText(title, horizontalStart, paint)
        verticalPosition += 20F
    }

    private fun writeCompetitionDate() {

        val start = competition.startedAt
        val end = getLatestPairing()

        var text = "Gespielt vom " + Util.toDateString(start);
        if (end != null) {
            text += " bis " + Util.toDateString(end)
        }

        val paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        paint.textSize = 15F

        drawText(text, horizontalStart, paint)
        verticalPosition += 15F
    }

    private fun writePairingRound(pairingsInRound: List<PairingDAO>) {

        verticalPosition += REGULAR_SIZE

        val roundHeaderPaint = Paint()
        roundHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC))
        roundHeaderPaint.textSize = 15F

        drawText(Util.getRoundTitle(context, competition, pairingsInRound), horizontalStart+10F,
            roundHeaderPaint)
        verticalPosition += 15F

        val paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        paint.textSize = REGULAR_SIZE

        val paintBold = Paint()
        paintBold.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paintBold.textSize = REGULAR_SIZE

        pairingsInRound.forEach { pairing ->
            drawText(pairing.toStringShort(), horizontalStart+10F, paint)
            drawText(pairing.toResultOnly(), 400F, paintBold)
            verticalPosition += REGULAR_SIZE
            drawText(Util.toDateTimeString(pairing.playDate), 400F, paint)
            verticalPosition += REGULAR_SIZE + 2F
        }
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

    private fun getLatestPairing(): Date? {

        var latest: Date? = null
        this.pairings.forEach { pairing ->
            if (latest == null) {
                latest = pairing.playDate
            }
            else {
                if (pairing.playDate != null && pairing.playDate.after(latest)) {
                    latest = pairing.playDate
                }
            }
        }
        return latest
    }

    private fun getMaxRound(): Int {

        var maxRound = 0
        pairings.forEach { pairing ->
            if (pairing.round > maxRound) {
                maxRound = pairing.round
            }
        }

        return maxRound
    }
}