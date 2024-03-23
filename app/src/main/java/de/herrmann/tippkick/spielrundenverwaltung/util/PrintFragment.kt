package de.herrmann.tippkick.spielrundenverwaltung.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PrintItemType
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess

class PrintFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.print, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val typeSpinner: Spinner = view.findViewById(R.id.type_spinner)
            fillTypeSpinner(typeSpinner, view)

            setCompetitionPartVisible(false, view)

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener {
                dismiss()
            }

            val printButton = view.findViewById<ImageButton>(R.id.print)
            printButton?.setOnClickListener {
                startCreatePdf(view)
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun startCreatePdf(view: View) {

        val typeSpinner: Spinner = view.findViewById(R.id.type_spinner)
        val type = typeSpinner.selectedItem.toString()
        if (PrintItemType.COMPETITION.toString().equals(type)) {
            startCreateCompetitionPdf(view)
        }
    }

    private fun startCreateCompetitionPdf(view: View) {

        val competitionSpinner = view.findViewById<Spinner>(R.id.competition_spinner)
        val competition = competitionSpinner.selectedItem as CompetitionDAO

        val pairingDBAccess = PairingDBAccess()
        val pairings = pairingDBAccess.getPairingsForCompetition(requireContext(),
            competition.id, null)

        PdfCreator().createCompetitionPdf(competition, pairings, requireContext())
    }

    private fun fillTypeSpinner(spinner: Spinner, theView: View) {

        val arraySpinner = arrayOf(
            PrintItemType.COMPETITION.toString()
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Currently, there are only competitions for printing.
                val competitionsDBAccess = CompetitionsDBAccess()
                val competitions = competitionsDBAccess.getAllCompetitions(requireContext())
                fillCompetitionSpinner(competitions, theView)
            }
        }
    }

    private fun fillCompetitionSpinner(competitions: List<CompetitionDAO>, view: View) {

        val arraySpinner = mutableListOf<CompetitionDAO>()

        competitions.forEach { competition ->
            if (Util.isCompetitionFinished(competition.id, requireContext())) {
                arraySpinner.add(competition)
            }
        }

        val adapter: ArrayAdapter<CompetitionDAO> = ArrayAdapter<CompetitionDAO>(
            requireContext(), android.R.layout.simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        val spinner = view.findViewById<Spinner>(R.id.competition_spinner)
        spinner.adapter = adapter

        setCompetitionPartVisible(true, view)
    }

    private fun setCompetitionPartVisible(visible: Boolean, view: View) {

        val spinner = view.findViewById<Spinner>(R.id.competition_spinner)
        spinner.isVisible = visible

        val spinnerText = view.findViewById<TextView>(R.id.select_competition)
        spinnerText.isVisible = visible
    }
}