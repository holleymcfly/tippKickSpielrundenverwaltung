package de.herrmann.tippkick.spielrundenverwaltung.ui.competition

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ListView
import android.widget.TextView
import androidx.core.util.forEach
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.TeamDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess

class SelectTeamsDialogFragment : DialogFragment() {

    lateinit var callback: Runnable
    var checkedItemIds: MutableList<Int> = mutableListOf()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.select_teams, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            loadTeams(view)

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener { dismiss() }

            val saveButton = view.findViewById<ImageButton>(R.id.save)
            saveButton?.setOnClickListener {
                save(view)
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun loadTeams(view: View) {

        val teamsDbAccess = TeamsDBAccess()
        val teams = teamsDbAccess.getAllTeams(requireContext())

        if (teams.size == 0) {
            val textView: TextView = view.findViewById(R.id.text_no_teams)
            textView.setText(R.string.no_teams_yet)
        }
        else {
            val teamsList: ListView = view.findViewById(R.id.team_selection_list)

            val arrayAdapter: ArrayAdapter<TeamDAO> =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_multiple_choice, teams)
            teamsList.adapter = arrayAdapter

            for ((i, team) in teams.withIndex()) {
                if (checkedItemIds.contains(team.id)) {
                    teamsList.setItemChecked(i, true)
                }
            }
        }
    }

    private fun save(view: View) {
        val teamsList: ListView = view.findViewById(R.id.team_selection_list)

        this.checkedItemIds.clear()

        val checkedPositions = teamsList.checkedItemPositions
        checkedPositions.forEach { position, checked ->
            val team: TeamDAO = teamsList.getItemAtPosition(position) as TeamDAO
            if (checked) {
                this.checkedItemIds.add(team.id)
            }
        }

        dismiss()
        callback.run()
    }
}