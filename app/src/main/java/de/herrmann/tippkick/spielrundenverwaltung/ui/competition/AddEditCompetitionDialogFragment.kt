package de.herrmann.tippkick.spielrundenverwaltung.ui.competition

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionTeamsRelationDAO
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionType
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsTeamsRelationDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.logic.DrawUtil
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess

class AddEditCompetitionDialogFragment : DialogFragment() {

    lateinit var callback: Runnable
    var competition: CompetitionDAO? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            // New competition
            if (competition == null) {
                competition = CompetitionDAO()
            }

            val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.add_edit_competition, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val competitionTypeSpinner: Spinner = view.findViewById(R.id.competition_type_spinner)
            fillCompetitionTypeSpinner(competitionTypeSpinner)

            val nameField: EditText = view.findViewById(R.id.eingabe)
            if (!isNewCompetition()) {
                nameField.setText(competition!!.name)
            }
            nameField.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(cs: CharSequence?, arg1: Int, arg2: Int, arg3: Int) {
                    setStartDrawingButtonEnabledDisabled(view)
                }

                override fun beforeTextChanged(
                    arg0: CharSequence?,
                    arg1: Int,
                    arg2: Int,
                    arg3: Int
                ) {
                }

                override fun afterTextChanged(arg0: Editable?) {}
            })

            val teamsButton: Button = view.findViewById(R.id.teams_list)
            loadNumberOfSelectedTeams()
            fillTeamsButtonText(teamsButton)
            teamsButton.setOnClickListener { openTeamsSelection(view) }

            val numberOfTeamsSpinner: Spinner = view.findViewById(R.id.number_of_teams_spinner)
            fillNumberOfTeamsSpinner(numberOfTeamsSpinner, view)

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener {
                dismiss()
                callback.run()
            }

            val deleteButton = view.findViewById<ImageButton>(R.id.delete)
            deleteButton?.setOnClickListener {
                showDeleteQuestion()
            }

            val saveButton = view.findViewById<ImageButton>(R.id.add)
            saveButton?.setOnClickListener {
                save(nameField, competitionTypeSpinner, numberOfTeamsSpinner, view)
                Toast.makeText(context, R.string.competition_saved, Toast.LENGTH_LONG).show()
                dismiss()
                callback.run()
            }

            val startDrawingButton: Button = view.findViewById(R.id.finish_competition)
            startDrawingButton.setOnClickListener {
                showFinishQuestion(nameField, competitionTypeSpinner, numberOfTeamsSpinner, view)
            }

            setStartDrawingButtonEnabledDisabled(view)
            disableButtons(view, isStarted())

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showDeleteQuestion() {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.delete_competition)
        builder.setMessage(R.string.delete_competition_question)

        builder.setPositiveButton(R.string.delete) { _, _ -> delete() }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        builder.show()
    }

    private fun delete() {

        val pairingDBAccess = PairingDBAccess()
        pairingDBAccess.deletePairingsForCompetition(requireContext(), competition!!.id)

        val competitionDBAccess = CompetitionsDBAccess()
        competitionDBAccess.deleteCompetition(requireContext(), competition!!.id);
        dismiss()
        callback.run()
    }

    private fun loadNumberOfSelectedTeams() {

        if (isNewCompetition()) {
            return
        }

        val dbAccess = CompetitionsTeamsRelationDBAccess()
        val competitionRelationsList =
            dbAccess.getCompetitionTeamRelationForCompetition(requireContext(), competition!!.id)

        competition!!.teamRelations.clear()
        competition!!.addTeamRelations(competitionRelationsList)
    }

    private fun fillTeamsButtonText(teamsButton: Button) {

        val count = competition!!.teamRelations.size
        val text = count.toString() + " " + getString(R.string.teams_chosen)
        teamsButton.text = text
    }

    private fun openTeamsSelection(view: View) {

        val selectTeamDialog = SelectTeamsDialogFragment()
        selectTeamDialog.callback = Runnable {
            run {
                val teamsButton: Button = view.findViewById(R.id.teams_list)
                updateSelectedTeamsList(selectTeamDialog.checkedItemIds)
                fillTeamsButtonText(teamsButton)
                setStartDrawingButtonEnabledDisabled(view)
            }
        }

        val selectedTeamIds: MutableList<Int> = mutableListOf()
        competition!!.teamRelations.forEach { relation ->
            selectedTeamIds.add(relation.teamId)
        }

        selectTeamDialog.checkedItemIds = selectedTeamIds
        selectTeamDialog.show(requireActivity().supportFragmentManager, null)
    }

    private fun updateSelectedTeamsList(selectedTeamIds: MutableList<Int>) {

        competition!!.teamRelations.clear()
        selectedTeamIds.forEach { id ->
            val relation = CompetitionTeamsRelationDAO()
            relation.competitionId = competition!!.id
            relation.teamId = id
            competition!!.teamRelations.add(relation)
        }
    }

    private fun fillCompetitionTypeSpinner(spinner: Spinner) {

        val arraySpinner = arrayOf(
            CompetitionType.DFB_POKAL.toString()
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (!isNewCompetition()) {
            val spinnerPosition: Int = adapter.getPosition(competition!!.competitionType.toString())
            spinner.setSelection(spinnerPosition)
        }
    }

    private fun save(
        nameField: EditText, competitionTypeSpinner: Spinner,
        numberOfTeamsSpinner: Spinner,
        view: View
    ) {
        val competitionName = nameField.text.toString()
        if (competitionName.isEmpty()) {
            Toast.makeText(context, R.string.enter_competition_please, Toast.LENGTH_LONG)
                .show()
        }
        else {
            val selItem = competitionTypeSpinner.selectedItem
            val competitionType = CompetitionType.getEnum(selItem.toString())
            val numberOfTeams = Integer.parseInt(numberOfTeamsSpinner.selectedItem.toString())

            val competitionDbAccess = CompetitionsDBAccess()
            if (isNewCompetition()) {
                val newCompetitionId = competitionDbAccess.insertCompetition(
                    requireContext(), competitionType, competitionName, numberOfTeams, false
                )

                val competitionsTeamsRelationDBAccess = CompetitionsTeamsRelationDBAccess()
                competition!!.teamRelations.forEach { relation ->
                    competitionsTeamsRelationDBAccess.insertCompetitionTeamRelation(
                        requireContext(), relation.teamId,
                        newCompetitionId.toInt()
                    )
                }

                // Reload competition
                this.competition = competitionDbAccess.getCompetitionById(requireContext(),
                    newCompetitionId.toInt())
                fillNumberOfTeamsSpinner(numberOfTeamsSpinner, view)
            }
            else {
                competition!!.competitionType = competitionType
                competition!!.name = competitionName
                competition!!.numberOfTeams = numberOfTeams

                competitionDbAccess.updateCompetition(requireContext(), competition)

                val competitionsTeamsRelationDBAccess = CompetitionsTeamsRelationDBAccess()
                competitionsTeamsRelationDBAccess.updateCompetitionTeamRelationsForCompetition(
                    requireContext(), competition!!.id, competition!!.teamRelations
                )
            }
        }
    }

    private fun fillNumberOfTeamsSpinner(spinner: Spinner, mainView: View) {

        val arraySpinner = arrayOf(
            "2", "4", "8", "16", "32", "64"
        )
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(
            requireContext(), android.R.layout.simple_spinner_item, arraySpinner
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter

        if (!isNewCompetition()) {
            val spinnerPosition: Int = adapter.getPosition(competition!!.numberOfTeams.toString())
            spinner.setSelection(spinnerPosition)
        }

        try {
            spinner.onItemSelectedListener = object : OnItemSelectedListener {

                override fun onNothingSelected(parent: AdapterView<*>?) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    setStartDrawingButtonEnabledDisabled(mainView)
                }
            }
        }
        catch (e: Exception) {
            e.printStackTrace()
            throw e
        }
    }

    private fun isNewCompetition(): Boolean {
        return competition!!.id == null
    }

    private fun disableButtons(view: View, isStarted: Boolean) {

        val nameField: EditText = view.findViewById(R.id.eingabe)
        nameField.isEnabled = !isStarted

        val competitionTypeSpinner: Spinner = view.findViewById(R.id.competition_type_spinner)
        competitionTypeSpinner.isEnabled = !isStarted

        val numberOfTeamsSpinner: Spinner = view.findViewById(R.id.number_of_teams_spinner)
        numberOfTeamsSpinner.isEnabled = !isStarted

        val teamsButton: Button = view.findViewById(R.id.teams_list)
        teamsButton.isEnabled = !isStarted

        val deleteButton: ImageButton = view.findViewById(R.id.delete)
        deleteButton.isEnabled = !isNewCompetition()
        if (isNewCompetition()) {
            deleteButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }

        val saveButton: ImageButton = view.findViewById(R.id.add)
        saveButton.isEnabled = !isStarted
        if (isStarted) {
            saveButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }

        val startDrawingButton: Button = view.findViewById(R.id.finish_competition)
        startDrawingButton.isEnabled = !isStarted
        startDrawingButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
    }

    private fun setStartDrawingButtonEnabledDisabled(view: View) {

        val startDrawingButton: Button = view.findViewById(R.id.finish_competition)

        // Don't activate if the competition has already been started.
        if (isStarted()) {
            startDrawingButton.isEnabled = false
            startDrawingButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
            return
        }

        // Don't activate if the competition has no name.
        val nameField: EditText = view.findViewById(R.id.eingabe)
        if (nameField.text.isEmpty()) {
            startDrawingButton.isEnabled = false
            startDrawingButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
            return
        }

        // Don't activate if the number of teams is not the requested number.
        val numberOfTeamsSpinner: Spinner = view.findViewById(R.id.number_of_teams_spinner)
        val numberOfTeams: Int = Integer.parseInt(numberOfTeamsSpinner.selectedItem.toString())
        if (numberOfTeams != competition!!.teamRelations.size) {
            startDrawingButton.setBackgroundColor(Color.parseColor("#C0C0C0"))
            startDrawingButton.isEnabled = false
            return
        }

        startDrawingButton.setBackgroundColor(Color.parseColor("#6200EE"))
        startDrawingButton.isEnabled = true
    }

    private fun showFinishQuestion(
        nameField: EditText, competitionTypeSpinner: Spinner,
        numberOfTeamsSpinner: Spinner, view: View
    ) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.start_drawing)
        builder.setMessage(R.string.start_drawing_question)

        builder.setPositiveButton(R.string.yes) { _, _ ->
            save(nameField, competitionTypeSpinner, numberOfTeamsSpinner, view)
            DrawUtil.drawCompetitionsFirstRound(competition!!, requireContext(), getString(R.string.drawing_finished))
            disableButtons(view, isStarted())
        }
        builder.setNegativeButton(R.string.no) { _, _ -> }

        builder.show()
    }

    private fun isStarted(): Boolean {
        return competition!!.isStarted
    }
}