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
import com.google.android.material.tabs.TabLayout
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.databinding.FragmentPlayBinding
import de.herrmann.tippkick.spielrundenverwaltung.logic.DrawUtil
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionType
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.util.Util

class PlayFragment : Fragment() {

    private var _binding: FragmentPlayBinding? = null
    private val binding get() = _binding!!

    private var currentPairings: MutableList<PairingDAO> = mutableListOf()
    private var currentCompetition: CompetitionDAO? = null
    private var currentPairingsRound: Int = 0
    private var selectedGroup: Int = 1;
    private var isTableSelected: Boolean = false;

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

        binding.drawNextRoundDfb.setOnClickListener {
            currentPairingsRound += 1
            DrawUtil.drawNextRound(
                currentCompetition!!.id, currentPairings, requireContext(),
                currentPairingsRound, getString(R.string.drawing_next_round_finished)
            )
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

        val tab1 = binding.tabs.getTabAt(0)
        tab1?.view?.setOnClickListener {
            selectedGroup = 1
            setGroupsVisibility()
        }

        val tab2 = binding.tabs.getTabAt(1)
        tab2?.view?.setOnClickListener {
            selectedGroup = 2
            setGroupsVisibility()
        }

        val tab3 = binding.tabs.getTabAt(2)
        tab3?.view?.setOnClickListener {
            selectedGroup = 3
            setGroupsVisibility()
        }

        val tab4 = binding.tabs.getTabAt(3)
        tab4?.view?.setOnClickListener {
            selectedGroup = 4
            setGroupsVisibility()
        }

        val tab5 = binding.tabs.getTabAt(4)
        tab5?.view?.setOnClickListener {
            selectedGroup = 5
            setGroupsVisibility()
        }

        val tab6 = binding.tabs.getTabAt(5)
        tab6?.view?.setOnClickListener {
            selectedGroup = 6
            setGroupsVisibility()
        }

        val tabGroup1Matchday = binding.tabsGroup1.getTabAt(0)
        tabGroup1Matchday?.view?.setOnClickListener {
            isTableSelected = false
            setGroupsVisibility()
        }

        val tabGroup1Table = binding.tabsGroup1.getTabAt(1)
        tabGroup1Table?.view?.setOnClickListener {
            isTableSelected = true
            setGroupsVisibility()
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
        val allCompetitions: List<CompetitionDAO> =
            competitionsDBAccess.getAllCompetitions(requireContext())

        val arraySpinner = mutableListOf<CompetitionDAO>()

        allCompetitions.forEach { competition ->
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

        setCompetitionTypeView()

        val pairingsDBAccess = PairingDBAccess()
        val pairings: List<PairingDAO> = pairingsDBAccess.getPairingsForCompetition(
            requireContext(),
            currentCompetition!!.id, currentPairingsRound
        )
        for (pairing in pairings) {
            pairing.setContext(requireContext())
        }

        if (CompetitionType.DFB_POKAL.equals(currentCompetition!!.competitionType)) {
            val pairingsList: ListView = binding.pairingsList
            val arrayAdapter: ArrayAdapter<PairingDAO> =
                ArrayAdapter(requireContext(), R.layout.list_view_center_text, pairings)
            pairingsList.adapter = arrayAdapter
            pairingsList.setOnItemClickListener { _, _, position, _ ->
                showEditPairingPopup(pairings[position])
            }
        }
        else if (CompetitionType.GROUP_STAGE.equals(currentCompetition!!.competitionType)) {

            selectedGroup = 1;

            loadPairingsForGroup(pairings, binding.pairingsListGroup1, 1)
            loadPairingsForGroup(pairings, binding.pairingsListGroup2, 2)
            loadPairingsForGroup(pairings, binding.pairingsListGroup3, 3)
            loadPairingsForGroup(pairings, binding.pairingsListGroup4, 4)
            loadPairingsForGroup(pairings, binding.pairingsListGroup5, 5)
            loadPairingsForGroup(pairings, binding.pairingsListGroup6,6)

            setGroupsVisibility()
            setTabsVisibility()
        }

        currentPairings.clear()
        currentPairings.addAll(pairings)

        setCompetitionRoundName()
        setNextPreviousButtonsEnabledDisabled()
        setDrawNextRoundVisibility()
    }

    private fun setTabsVisibility() {

        if (currentCompetition!!.numberOfGroups < 6) {
            binding.tabs.getTabAt(5)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED)
        }
        else {
            binding.tabs.getTabAt(5)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED)
        }

        if (currentCompetition!!.numberOfGroups < 5) {
            binding.tabs.getTabAt(4)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED)
        }
        else {
            binding.tabs.getTabAt(4)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED)
        }

        if (currentCompetition!!.numberOfGroups < 4) {
            binding.tabs.getTabAt(3)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_UNLABELED)
        }
        else {
            binding.tabs.getTabAt(3)!!.setTabLabelVisibility(TabLayout.TAB_LABEL_VISIBILITY_LABELED)
        }
    }

    private fun loadPairingsForGroup(pairings: List<PairingDAO>, groupPairingsList: ListView,
                                     group: Int) {

        val groupPairings = getPairingsOfGroup(pairings, group)
        val arrayAdapter: ArrayAdapter<PairingDAO> =
            ArrayAdapter(requireContext(), R.layout.list_view_center_text, groupPairings)
        groupPairingsList.adapter = arrayAdapter
        groupPairingsList.setOnItemClickListener { _, _, position, _ ->
            showEditPairingPopup(groupPairings[position])
        }
    }

    private fun setGroupsVisibility() {

        binding.group1.isVisible = (selectedGroup == 1)
        binding.group2.isVisible = (selectedGroup == 2)
        binding.group3.isVisible = (selectedGroup == 3)
        binding.group4.isVisible = (selectedGroup == 4)
        binding.group5.isVisible = (selectedGroup == 5)
        binding.group6.isVisible = (selectedGroup == 6)

        binding.tableGroup1.isVisible = isTableSelected
        binding.pairingsListGroup1.isVisible = !isTableSelected
    }

    private fun getPairingsOfGroup(pairings: List<PairingDAO>, group: Int): List<PairingDAO> {

        val pairingsOfGroup = mutableListOf<PairingDAO>()
        pairings.forEach { pairing ->
            if (pairing.group == group) {
                pairingsOfGroup.add(pairing)
            }
        }

        return pairingsOfGroup
    }

    private fun setCompetitionTypeView() {

        if (CompetitionType.DFB_POKAL.equals(currentCompetition!!.competitionType)) {
            binding.layoutDfb.isVisible = true
            binding.layoutGroup.isVisible = false
        }
        else {
            binding.layoutDfb.isVisible = false
            binding.layoutGroup.isVisible = true
        }
    }

    private fun showEditPairingPopup(pairing: PairingDAO) {

        val existsNextRound = Util.existsNextRound(
            currentCompetition!!, currentPairingsRound,
            requireContext()
        )
        val pairingIsFinalAndFinished = currentPairings.size == 1 && pairing.isFinished
        val pairingDialog =
            EditPairingDialogFragment(pairing, existsNextRound || pairingIsFinalAndFinished)

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
        roundText.text = Util.getRoundTitle(requireContext(), currentPairings)
    }

    private fun setNextPreviousButtonsEnabledDisabled() {

        val previousButton = binding.previousRound
        if (currentPairings.isNotEmpty() && currentPairings[0].round > 1) {
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
            val nextRoundPairings = pairingDBAccess.getPairingsForCompetition(
                requireContext(),
                currentCompetition!!.id, currentPairings[0].round + 1
            )
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
                return false
            }
        }

        return true
    }

    private fun setDrawNextRoundVisibility() {

        val drawNextRound: Button = binding.drawNextRoundDfb
        val existsNextRound = currentCompetition != null &&
                Util.existsNextRound(currentCompetition!!, currentPairingsRound, requireContext())
        drawNextRound.isVisible =
            roundFinished() && this.currentPairings.size > 1 && !existsNextRound
    }
}