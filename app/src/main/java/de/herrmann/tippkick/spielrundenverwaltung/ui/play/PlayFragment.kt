package de.herrmann.tippkick.spielrundenverwaltung.ui.play

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.core.view.allViews
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
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
    private var selectedGroup: Int = 1
    private var isTableSelected: Boolean = false

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
            binding.previousRound.isVisible = false
            binding.nextRound.isVisible = false
            binding.selectCompetitionText.isVisible = false
        }
        else {
            currentPairingsRound = 1
            loadPairingsForCurrentRound()
        }

        binding.previousRound.setOnClickListener {
            currentPairingsRound -= 1
            loadPairingsForCurrentRound()
        }

        binding.nextRound.setOnClickListener {

            if (isDrawNextRoundEnabled()) {
                if (Util.isDfbCompetition(currentCompetition)) {
                    currentPairingsRound += 1
                    DrawUtil.drawNextRoundDfb(
                        currentCompetition!!.id, currentPairings, requireContext(),
                        currentPairingsRound, getString(R.string.drawing_next_round_finished)
                    )
                    loadPairingsForCurrentRound()
                }
                else if (Util.isGroupCompetition(currentCompetition)) {
                    currentPairingsRound += 1
                    DrawUtil.drawNextRoundGroupCompetition(currentCompetition!!,
                        currentPairings, requireContext(), currentPairingsRound,
                        getString(R.string.drawing_next_round_finished))
                    loadPairingsForCurrentRound()
                }
            }
            else {
                currentPairingsRound += 1
                loadPairingsForCurrentRound()
            }
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

        if (Util.isDfbCompetition(currentCompetition)) {
            val pairingsList: ListView = binding.pairingsList
            val arrayAdapter: ArrayAdapter<PairingDAO> =
                ArrayAdapter(requireContext(), R.layout.list_view_center_text, pairings)
            pairingsList.adapter = arrayAdapter
            pairingsList.setOnItemClickListener { _, _, position, _ ->
                showEditPairingPopup(pairings[position])
            }
        }
        else if (Util.isGroupCompetitionGroupRound(currentCompetition, currentPairingsRound)) {

            loadPairingsForGroup(pairings, binding.pairingsListGroup1, 1)
            loadPairingsForGroup(pairings, binding.pairingsListGroup2, 2)
            loadPairingsForGroup(pairings, binding.pairingsListGroup3, 3)
            loadPairingsForGroup(pairings, binding.pairingsListGroup4, 4)
            loadPairingsForGroup(pairings, binding.pairingsListGroup5, 5)
            loadPairingsForGroup(pairings, binding.pairingsListGroup6,6)

            setGroupsVisibility()
            setTabsVisibility()
        }
        else if (Util.isGroupCompetitionKnockout(currentCompetition, currentPairingsRound)) {
            val sortedPairings: List<PairingDAO> = sortPairingsForKnockout(pairings)
            val pairingsList: ListView = binding.pairingsList

            pairingsList.dividerHeight = 0

            val arrayAdapter: ArrayAdapter<PairingDAO?> = object : ArrayAdapter<PairingDAO?>(
                requireContext(),
                R.layout.list_view_center_text,
                sortedPairings
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    if (position % 2 == 1) {
                        view.setPadding(0, 0, 0, 60)
                    }
                    else {
                        view.setPadding(0, 0, 0, 0)
                    }
                    return view
                }
            }

            pairingsList.adapter = arrayAdapter
            pairingsList.setOnItemClickListener { _, _, position, _ ->
                showEditPairingPopup(sortedPairings[position])
            }
        }

        currentPairings.clear()
        currentPairings.addAll(pairings)

        setCompetitionRoundName()
        setNextPreviousButtonsEnabledDisabled()

        if (CompetitionType.GROUP_STAGE == currentCompetition!!.competitionType) {
            calculateAndFillTables()
        }
    }

    private fun sortPairingsForKnockout(pairings: List<PairingDAO>) : List<PairingDAO> {

        val pairingsSorted = mutableListOf<PairingDAO>()
        pairings.forEach { pairing ->
            if (!containsPairing(pairingsSorted, pairing)) {
                pairingsSorted.add(pairing)
            }

            if (!containsPairingReverse(pairingsSorted, pairing)) {
                pairingsSorted.add(getReverseMatch(pairings, pairing))
            }
        }

        return pairingsSorted
    }

    private fun getReverseMatch(pairings: List<PairingDAO>, pairing: PairingDAO) : PairingDAO {

        pairings.forEach { p ->
            if (p.teamIdHome == pairing.teamIdAway && p.teamIdAway == pairing.teamIdHome) {
                return p
            }
        }

        throw RuntimeException("Reverse match not part of the list")
    }

    private fun containsPairing(pairings: List<PairingDAO>, pairing: PairingDAO) : Boolean {

        for (p in pairings) {
            if (p.teamIdHome == pairing.teamIdHome && p.teamIdAway == pairing.teamIdAway) {
                return true
            }
        }

        return false
    }

    private fun containsPairingReverse(pairings: List<PairingDAO>, pairing: PairingDAO) : Boolean {

        for (p in pairings) {
            if (p.teamIdAway == pairing.teamIdHome && p.teamIdHome == pairing.teamIdAway) {
                return true
            }
        }

        return false
    }

    private fun setTabsVisibility() {

        binding.tabs.getTabAt(5)!!.view.isVisible = (currentCompetition!!.numberOfGroups > 5)
        binding.tabs.getTabAt(4)!!.view.isVisible = (currentCompetition!!.numberOfGroups > 4)
        binding.tabs.getTabAt(3)!!.view.isVisible = (currentCompetition!!.numberOfGroups > 3)
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

        binding.pairingsListGroup1.isVisible = (selectedGroup == 1) && !isTableSelected
        binding.distanceHolderGroup1.isVisible = (selectedGroup == 1) && !isTableSelected
        binding.tableViewGroup1.isVisible = (selectedGroup == 1) && isTableSelected

        binding.pairingsListGroup2.isVisible = (selectedGroup == 2) && !isTableSelected
        binding.distanceHolderGroup2.isVisible = (selectedGroup == 2) && !isTableSelected
        binding.tableViewGroup2.isVisible = (selectedGroup == 2) && isTableSelected

        binding.pairingsListGroup3.isVisible = (selectedGroup == 3) && !isTableSelected
        binding.distanceHolderGroup3.isVisible = (selectedGroup == 3) && !isTableSelected
        binding.tableViewGroup3.isVisible = (selectedGroup == 3) && isTableSelected

        binding.pairingsListGroup4.isVisible = (selectedGroup == 4) && !isTableSelected
        binding.distanceHolderGroup4.isVisible = (selectedGroup == 4) && !isTableSelected
        binding.tableViewGroup4.isVisible = (selectedGroup == 4) && isTableSelected

        binding.pairingsListGroup5.isVisible = (selectedGroup == 5) && !isTableSelected
        binding.distanceHolderGroup5.isVisible = (selectedGroup == 5) && !isTableSelected
        binding.tableViewGroup5.isVisible = (selectedGroup == 5) && isTableSelected

        binding.pairingsListGroup6.isVisible = (selectedGroup == 6) && !isTableSelected
        binding.distanceHolderGroup6.isVisible = (selectedGroup == 6) && !isTableSelected
        binding.tableViewGroup6.isVisible = (selectedGroup == 6) && isTableSelected
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

        if (Util.isDfbCompetition(currentCompetition)) {
            binding.layoutKnockout.isVisible = true
            binding.layoutGroup.isVisible = false
        }
        else if (Util.isGroupCompetitionGroupRound(currentCompetition, currentPairingsRound)) {
            binding.layoutKnockout.isVisible = false
            binding.layoutGroup.isVisible = true
        }
        else if (Util.isGroupCompetitionKnockout(currentCompetition, currentPairingsRound)) {
            binding.layoutKnockout.isVisible = true
            binding.layoutGroup.isVisible = false
        }
    }

    private fun showEditPairingPopup(pairing: PairingDAO) {

        val existsNextRound = Util.existsNextRound(
            currentCompetition!!, currentPairingsRound,
            requireContext()
        )
        val pairingIsFinalAndFinished = currentPairings.size == 1 && pairing.isFinished
        val pairingDialog =
            EditPairingDialogFragment(currentCompetition!!, pairing, existsNextRound || pairingIsFinalAndFinished)

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

        setPreviousButtonEnabledDisabled()
        setNextButtonEnabledDisabled()
    }

    private fun isDrawNextRoundEnabled(): Boolean {
        val existsNextRound = Util.existsNextRound(currentCompetition!!, currentPairingsRound, requireContext())
        return roundFinished() && !existsNextRound
    }

    private fun setNextButtonEnabledDisabled() {

        val nextButton = binding.nextRound
        if (currentPairings.isEmpty() || currentCompetition == null) {
            nextButton.isEnabled = false
            nextButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
            return
        }

        val pairingDBAccess = PairingDBAccess()
        val nextRoundPairings = pairingDBAccess.getPairingsForCompetition(
            requireContext(),
            currentCompetition!!.id, currentPairings[0].round + 1
        )

        if (isDrawNextRoundEnabled()) {
            // next round button is set to draw next round
            nextButton.isEnabled = true
            nextButton.setImageResource(R.drawable.draw_white)
            nextButton.setBackgroundColor(Color.parseColor("#6200EE"))
        }
        else if (!nextRoundPairings.isEmpty()) {
            // next round button enabled
            nextButton.isEnabled = true
            nextButton.setImageResource(R.drawable.arrow_forward_white)
            nextButton.setBackgroundColor(Color.parseColor("#6200EE"))
        }
        else {
            // next round button disabled
            nextButton.isEnabled = false
            nextButton.setImageResource(R.drawable.arrow_forward_white)
            nextButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }
    }

    private fun setPreviousButtonEnabledDisabled() {

        val previousButton = binding.previousRound
        if (currentPairings.isNotEmpty() && currentPairings[0].round > 1) {
            previousButton.isEnabled = true
            previousButton.setBackgroundColor(Color.parseColor("#6200EE"))
        }
        else {
            previousButton.isEnabled = false
            previousButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
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

    private fun calculateAndFillTables() {

        calculateAndFillTable(1, binding.tableGroup1)
        calculateAndFillTable(2, binding.tableGroup2)
        calculateAndFillTable(3, binding.tableGroup3)

        if (currentCompetition!!.numberOfGroups > 3) {
            calculateAndFillTable(4, binding.tableGroup4)
        }

        if (currentCompetition!!.numberOfGroups > 4) {
            calculateAndFillTable(5, binding.tableGroup5)
        }

        if (currentCompetition!!.numberOfGroups > 5) {
            calculateAndFillTable(6, binding.tableGroup6)
        }
    }

    private fun calculateAndFillTable(group: Int, tableGroup: TableLayout) {

        // Remove all generated rows, they will added right after that.
        val toRemoveView = mutableListOf<View>()
        for (view in tableGroup.allViews) {
            if (R.id.header_row_1 == view.id || R.id.header_row_2 == view.id ||
                R.id.header_row_3 == view.id || R.id.header_row_4 == view.id ||
                R.id.header_row_5 == view.id || R.id.header_row_6 == view.id
            ) {
                continue
            }

            toRemoveView.add(view)
        }

        toRemoveView.forEach { view ->
            tableGroup.removeView(view)
        }

        // Generate new rows.
        val tableCalculator = TableCalculator(requireContext(), currentPairings, group)
        val tableEntries: List<TableEntry> = tableCalculator.calculate()

        var position = 1
        for (tableEntry in tableEntries) {

            val tableRow = createTableRowForView(tableEntry, position++)
            tableGroup.addView(tableRow)
        }
    }

    private fun createTableRowForView(tableEntry: TableEntry, position: Int): TableRow {

        val tableRow = TableRow(requireContext())

        val positionText = TextView(requireContext())
        positionText.gravity = Gravity.CENTER
        positionText.setPadding(3, 3, 3, 3)
        positionText.text = position.toString()
        positionText.textSize = 16F
        tableRow.addView(positionText)

        val teamText = TextView(requireContext())
        teamText.gravity = Gravity.START
        teamText.setPadding(3, 3, 3, 3)
        teamText.text = tableEntry.getTeamName()
        teamText.textSize = 16F
        tableRow.addView(teamText)

        val matchesText = TextView(requireContext())
        matchesText.gravity = Gravity.CENTER
        matchesText.setPadding(3, 3, 3, 3)
        matchesText.text = tableEntry.getMatchCount().toString()
        matchesText.textSize = 16F
        tableRow.addView(matchesText)

        val winsText = TextView(requireContext())
        winsText.gravity = Gravity.CENTER
        winsText.setPadding(3, 3, 3, 3)
        winsText.text = tableEntry.getWins().toString()
        winsText.textSize = 16F
        tableRow.addView(winsText)

        val drawsText = TextView(requireContext())
        drawsText.gravity = Gravity.CENTER
        drawsText.setPadding(3, 3, 3, 3)
        drawsText.text = tableEntry.getDraws().toString()
        drawsText.textSize = 16F
        tableRow.addView(drawsText)

        val lostsText = TextView(requireContext())
        lostsText.gravity = Gravity.CENTER
        lostsText.setPadding(3, 3, 3, 3)
        lostsText.text = tableEntry.getLosts().toString()
        lostsText.textSize = 16F
        tableRow.addView(lostsText)

        val goalsText = TextView(requireContext())
        goalsText.gravity = Gravity.CENTER
        goalsText.setPadding(3, 3, 3, 3)
        goalsText.text =
            tableEntry.getGoalsShot().toString() + " : " + tableEntry.getGoalsConceded().toString()
        goalsText.textSize = 16F
        tableRow.addView(goalsText)

        val diffText = TextView(requireContext())
        diffText.gravity = Gravity.CENTER
        diffText.setPadding(3, 3, 3, 3)
        diffText.text = (tableEntry.getGoalsShot() - tableEntry.getGoalsConceded()).toString()
        diffText.textSize = 16F
        tableRow.addView(diffText)

        val pointsText = TextView(requireContext())
        pointsText.gravity = Gravity.CENTER
        pointsText.setPadding(3, 3, 3, 3)
        pointsText.text = tableEntry.getPoints().toString()
        pointsText.textSize = 16F
        tableRow.addView(pointsText)
        return tableRow
    }
}