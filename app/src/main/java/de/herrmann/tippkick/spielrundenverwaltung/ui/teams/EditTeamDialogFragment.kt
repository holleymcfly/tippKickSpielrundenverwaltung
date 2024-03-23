package de.herrmann.tippkick.spielrundenverwaltung.ui.teams

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.model.TeamDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsTeamsRelationDBAccess


class EditTeamDialogFragment : DialogFragment() {

    lateinit var callback: Runnable
    lateinit var team: TeamDAO

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.edit_team, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener { dismiss() }

            val nameField = view.findViewById<EditText>(R.id.eingabe)
            nameField.setText(team.name)

            val saveButton = view.findViewById<ImageButton>(R.id.save)
            saveButton?.setOnClickListener {
                save(nameField)
            }

            val deleteButton = view.findViewById<ImageButton>(R.id.delete)
            deleteButton?.setOnClickListener {
                showDeleteQuestion()
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun showDeleteQuestion() {

        val builder = AlertDialog.Builder(context)
        builder.setTitle(R.string.delete_team)
        builder.setMessage(R.string.delete_team_question)

        builder.setPositiveButton(R.string.delete) { _, _ -> delete() }
        builder.setNegativeButton(R.string.cancel) { _, _ -> }

        builder.show()
    }

    private fun delete() {

        val competitionTeamRelationDbAccess = CompetitionsTeamsRelationDBAccess()
        val relationsForTeam = competitionTeamRelationDbAccess.getCompetitionTeamRelationForTeam(
            requireContext(), team.id)

        if (relationsForTeam.isNotEmpty()) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle(R.string.hint)
            builder.setMessage(R.string.cannot_delete_team)
            builder.setPositiveButton(R.string.ok) { _, _ -> }
            builder.show()
        }
        else {
            val teamsDbAccess = TeamsDBAccess()
            teamsDbAccess.deleteTeam(context, team.id)
            dismiss()
            callback.run()
        }
    }

    private fun save(nameField: EditText) {
        val teamName = nameField.text.toString()
        if (teamName.isEmpty()) {
            Toast.makeText(context, R.string.enter_team_please, Toast.LENGTH_LONG).show()
        }
        else {
            team.name = teamName
            val teamsDbAccess = TeamsDBAccess()
            teamsDbAccess.updateTeam(requireContext(), team)
            dismiss()
            callback.run()
        }
    }
}