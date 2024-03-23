package de.herrmann.tippkick.spielrundenverwaltung.ui.play

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.databinding.FragmentPlayBinding
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.logic.DrawUtil
import de.herrmann.tippkick.spielrundenverwaltung.util.Util

class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    private var currentPairings: MutableList<PairingDAO> = mutableListOf()
    private var currentCompetition: CompetitionDAO? = null
    private var currentPairingsRound: Int = 0;

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPlayBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val noCompetitionText: TextView = binding.noCompetitionText
        val competitionSpinner: Spinner = binding.selectCompetition

        initCompetitionSpinner(competitionSpinner)

        if (!loadCompetitions()) {
            noCompetitionText.text = getText(R.string.no_finished_competitions_yet)
            competitionSpinner.isVisible = false
            setDrawNextRoundVisibility()
            binding.previousRound.isVisible = false
            binding.nextRound.isVisible = false
            binding.selectCompetitionText.isVisible = false
        }
        else {
            currentPairingsRound = 1
            loadPairingsForCurrentRound()
        }

        binding.drawNextRound.setOnClickListener {
            currentPairingsRound += 1
            DrawUtil.drawNextRound(currentCompetition!!.id, currentPairings, requireContext(),
                currentPairingsRound, getString(R.string.drawing_next_round_finished));
            loadPairingsForCurrentRound()
        }

        binding.previousRound.setOnClickListener {
            currentPairingsRound -= 1
            loadPairingsForCurrentRound()
        }

        binding.nextRound.setOnClickListener {
            currentPairingsRound += 1
            loadPairingsForCurrentRound()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initCompetitionSpinner(competitionSpinner: Spinner) {

        competitionSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {

            override fun onNothingSelected(parent: AdapterView<*>?) {}

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentPairingsRound = 1
                loadPairingsForCurrentRound()
            }
        }
    }

    private fun loadCompetitions(): Boolean {

        val competitionsDBAccess = CompetitionsDBAccess()
        val allCompetitions: List<CompetitionDAO> = competitionsDBAccess.getAllCompetitions(requireContext())

        val arraySpinner = mutableListOf<CompetitionDAO>()

        allCompetitions.forEach {competition ->
            if (competition.isStarted) {
                arraySpinner.add(competition)
            }
        }

        val adapter: ArrayAdapter<CompetitionDAO> = ArrayAdapter<CompetitionDAO>(
            requireContext(), android.R.layout.simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        val spinner: Spinner = binding.selectCompetition
        spinner.adapter = adapter

        return allCompetitions.isNotEmpty()
    }

    private fun loadPairingsForCurrentRound() {

        val competitionSpinner = binding.selectCompetition
        currentCompetition = competitionSpinner.selectedItem as CompetitionDAO

        val pairingsDBAccess = PairingDBAccess()
        val pairings: List<PairingDAO> = pairingsDBAccess.getPairingsForCompetition(requireContext(),
            currentCompetition!!.id, currentPairingsRound)
        for (pairing in pairings) {
            pairing.setContext(requireContext())
        }

        val pairingsList: ListView = binding.pairingsList
        val arrayAdapter: ArrayAdapter<PairingDAO> =
            ArrayAdapter(requireContext(), R.layout.list_view_center_text, pairings)
        pairingsList.adapter = arrayAdapter
        pairingsList.setOnItemClickListener { _, _, position, _ ->

            val pairing: PairingDAO = pairings[position]
            showEditPairingPopup(pairing)
        }

        currentPairings.clear()
        currentPairings.addAll(pairings)

        setCompetitionRoundName()
        setNextPreviousButtonsEnabledDisabled()
        setDrawNextRoundVisibility()
    }

    private fun showEditPairingPopup(pairing: PairingDAO) {

        val existsNextRound = Util.existsNextRound(currentCompetition!!, currentPairingsRound,
            requireContext());
        val pairingIsFinalAndFinished = currentPairings.size == 1 && pairing.isFinished
        val pairingDialog = EditPairingDialogFragment(pairing, existsNextRound || pairingIsFinalAndFinished)
        pairingDialog.callback = Runnable {
            run {
                loadPairingsForCurrentRound()
            }
        }

        pairingDialog.isCancelable = false
        pairingDialog.show(requireActivity().supportFragmentManager, null)
    }

    private fun setCompetitionRoundName() {

        val roundText: TextView = binding.roundText
        if (currentPairings.size == 1) {
            roundText.setText(R.string.final_)
        }
        else if (currentPairings.size == 2) {
            roundText.setText(R.string.semi_final)
        }
        else if (currentPairings.size == 4) {
            roundText.setText(R.string.quarter_final)
        }
        else if (currentPairings.size == 8) {
            roundText.setText(R.string.round_of_16)
        }
        else {
            val text: String = getText(R.string.round).toString() + " " +
                    currentPairings.get(0).round
            roundText.setText(text)
        }
    }

    private fun setNextPreviousButtonsEnabledDisabled() {

        val previousButton = binding.previousRound
        if (currentPairings.isNotEmpty() && currentPairings.get(0).round > 1) {
            previousButton.isEnabled = true
            previousButton.setBackgroundColor(Color.parseColor("#6200EE"))
        }
        else {
            previousButton.isEnabled = false
            previousButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }

        val nextButton = binding.nextRound
        if (currentPairings.isEmpty() || currentCompetition == null) {
            nextButton.isEnabled = false
            nextButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }
        else {
            val pairingDBAccess = PairingDBAccess()
            val nextRoundPairings = pairingDBAccess.getPairingsForCompetition(requireContext(),
                currentCompetition!!.id, currentPairings.get(0).round+1)
            nextButton.isEnabled = nextRoundPairings.isNotEmpty()
            if (nextRoundPairings.isEmpty()) {
                nextButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
            }
            else {
                nextButton.setBackgroundColor(Color.parseColor("#6200EE"))
            }
        }
    }

    private fun roundFinished(): Boolean {

        this.currentPairings.forEach {
            if (!it.isFinished) {
                return false;
            }
        }

        return true;
    }

    private fun setDrawNextRoundVisibility() {

        val drawNextRound: Button = binding.drawNextRound
        val existsNextRound = currentCompetition != null &&
                Util.existsNextRound(currentCompetition!!, currentPairingsRound, requireContext());
        if (roundFinished() && this.currentPairings.size > 1 && !existsNextRound) {
            drawNextRound.isVisible = true
        }
        else {
            drawNextRound.isVisible = false
        }
    }
}