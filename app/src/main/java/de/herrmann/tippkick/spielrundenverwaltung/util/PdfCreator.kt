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
import de.herrmann.tippkick.spielrundenverwaltung.ui.play.TableCalculator
import de.herrmann.tippkick.spielrundenverwaltung.ui.play.TableEntry
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
    private lateinit var paint: Paint
    private lateinit var paintBold: Paint

    fun createCompetitionPdf(
        competition: CompetitionDAO, pairings: List<PairingDAO>,
        context: Context
    ) {

        this.competition = competition
        this.pairings = pairings
        this.context = context

        paint = Paint()
        paint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        paint.textSize = REGULAR_SIZE

        paintBold = Paint()
        paintBold.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paintBold.textSize = REGULAR_SIZE

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
        writeKnockoutPhaseOfGroupCompetition()
    }

    private fun writeKnockoutPhaseOfGroupCompetition() {

        val pairingDBAccess = PairingDBAccess()

        val groupHeaderPaint = Paint()
        groupHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        groupHeaderPaint.textSize = 12F

        for (round in 2..getMaxRound()) {

            val pairingsInRound = pairingDBAccess.getPairingsForCompetition(
                this.context,
                this.competition.id, round
            )
            pairingsInRound.forEach { pairing -> pairing.setContext(this.context) }


            writeKnockoutHeaderOfGroupCompetition(pairingsInRound, groupHeaderPaint)
            writePairingsOfKnockoutRoundInGroupCompetition(pairingsInRound)
            verticalPosition += REGULAR_SIZE + 2F
        }
    }

    private fun writePairingsOfKnockoutRoundInGroupCompetition(pairingsInRound: MutableList<PairingDAO>) {

        pairingsInRound.forEach { pairing ->
            drawText(pairing.toStringShort(), horizontalStart + 10F, paint)
            drawText(pairing.toResultOnly(), 400F, paintBold)
            verticalPosition += REGULAR_SIZE
            drawText(Util.toDateTimeString(pairing.playDate), 400F, paint)
            verticalPosition += REGULAR_SIZE + 2F
        }
    }

    private fun writeKnockoutHeaderOfGroupCompetition(
        pairingsInRound: MutableList<PairingDAO>,
        groupHeaderPaint: Paint
    ) {

        drawText(
            Util.getRoundTitle(context, competition, pairingsInRound),
            horizontalStart + 10F, groupHeaderPaint
        )
        verticalPosition += 15F
    }

    private fun writeGroupPhaseOfGroupCompetition() {

        val pairingDBAccess = PairingDBAccess()
        val `allPairingsIn Round` =
            pairingDBAccess.getPairingsForCompetition(this.context, this.competition.id, 1)
        `allPairingsIn Round`.forEach { pairing -> pairing.setContext(this.context) }

        verticalPosition += REGULAR_SIZE

        for (group in 1..this.competition.numberOfGroups) {

            val pairingsInGroup = Util.getPairingsForGroup(`allPairingsIn Round`, group)

            writeGroupHeaderOfGroupCompetition(group)
            writeGroupPairingsOfGroupCompetition(pairingsInGroup, paint, paintBold)
            writeTableOfGroupCompetition(pairingsInGroup, group, paint, paintBold)
            verticalPosition += REGULAR_SIZE + 20F
        }
    }

    private fun writeTableOfGroupCompetition(
        pairingsInGroup: MutableList<PairingDAO>,
        group: Int, paint: Paint, paintBold: Paint
    ) {

        drawText(context.getString(R.string.ranking), horizontalStart + 10F, paintBold)
        drawText(context.getString(R.string.team), horizontalStart + 40F, paintBold)
        drawText(context.getString(R.string.matches_abbr), horizontalStart + 200F, paintBold)
        drawText(context.getString(R.string.wins_abbr), horizontalStart + 230F, paintBold)
        drawText(context.getString(R.string.undecided_abbr), horizontalStart + 260F, paintBold)
        drawText(context.getString(R.string.lost_abbr), horizontalStart + 290F, paintBold)
        drawText(context.getString(R.string.goals), horizontalStart + 320F, paintBold)
        drawText(context.getString(R.string.difference_abbr), horizontalStart + 350F, paintBold)
        drawText(context.getString(R.string.points), horizontalStart + 380F, paintBold)
        verticalPosition += REGULAR_SIZE

        val tableCalculator = TableCalculator(this.context, pairingsInGroup, group)
        val tableEntries: List<TableEntry> = tableCalculator.calculate()

        var i = 0
        for (entry in tableEntries) {
            i++
            drawText(i.toString(), horizontalStart + 10F, paint)
            drawText(entry.getTeamName(), horizontalStart + 40F, paint)
            drawText(entry.getMatchCount().toString(), horizontalStart + 200F, paint)
            drawText(entry.getWins().toString(), horizontalStart + 230F, paint)
            drawText(entry.getDraws().toString(), horizontalStart + 260F, paint)
            drawText(entry.getLosts().toString(), horizontalStart + 290F, paint)
            drawText(
                entry.getGoalsShot().toString() + " : " + entry.getGoalsConceded().toString(),
                horizontalStart + 320F, paint
            )
            drawText(
                (entry.getGoalsShot() - entry.getGoalsConceded()).toString(),
                horizontalStart + 350F, paint
            )
            drawText(entry.getPoints().toString(), horizontalStart + 380F, paint)
            verticalPosition += REGULAR_SIZE
        }
    }

    private fun writeGroupHeaderOfGroupCompetition(group: Int) {

        val groupHeaderPaint = Paint()
        groupHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        groupHeaderPaint.textSize = 12F

        drawText(
            context.getString(R.string.group) + " " + group, horizontalStart + 10F,
            groupHeaderPaint
        )
        verticalPosition += 15F
    }

    private fun writeGroupPairingsOfGroupCompetition(
        pairingsInGroup: MutableList<PairingDAO>,
        paint: Paint,
        paintBold: Paint
    ) {
        pairingsInGroup.forEach { pairing ->
            drawText(pairing.toStringShort(), horizontalStart + 10F, paint)
            drawText(pairing.toResultOnly(), 400F, paintBold)
            verticalPosition += REGULAR_SIZE
            drawText(Util.toDateTimeString(pairing.playDate), 400F, paint)
            verticalPosition += REGULAR_SIZE + 2F
        }

        verticalPosition += REGULAR_SIZE
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

        val paintCompetitionName = Paint()
        paintCompetitionName.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD))
        paintCompetitionName.textSize = 20F

        val title = competition.competitionType.toString() + ": " + competition.name
        drawText(title, horizontalStart, paintCompetitionName)
        verticalPosition += 20F
    }

    private fun writeCompetitionDate() {

        val start = competition.startedAt
        val end = getLatestPairing()

        var text = "Gespielt vom " + Util.toDateString(start);
        if (end != null) {
            text += " bis " + Util.toDateString(end)
        }

        val paintDate = Paint()
        paintDate.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.NORMAL))
        paintDate.textSize = 15F

        drawText(text, horizontalStart, paintDate)
        verticalPosition += 15F
    }

    private fun writePairingRound(pairingsInRound: List<PairingDAO>) {

        verticalPosition += REGULAR_SIZE

        val roundHeaderPaint = Paint()
        roundHeaderPaint.setTypeface(Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD_ITALIC))
        roundHeaderPaint.textSize = 15F

        drawText(
            Util.getRoundTitle(context, competition, pairingsInRound), horizontalStart + 10F,
            roundHeaderPaint
        )
        verticalPosition += 15F

        pairingsInRound.forEach { pairing ->
            drawText(pairing.toStringShort(), horizontalStart + 10F, paint)
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