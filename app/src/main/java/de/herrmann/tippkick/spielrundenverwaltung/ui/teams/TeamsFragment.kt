package de.herrmann.tippkick.spielrundenverwaltung.ui.teams

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.databinding.FragmentTeamsBinding
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.model.TeamDAO
import de.herrmann.tippkick.spielrundenverwaltung.util.Util


class TeamsFragment : Fragment() {

    private var _binding: FragmentTeamsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentTeamsBinding.inflate(inflater, container, false)

        val root: View = binding.root

        loadTeams()

        val addButton = binding.add
        addButton.setOnClickListener { showAddTeamPopup() }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadTeams() {

        val teamsDbAccess = TeamsDBAccess()
        val teams = teamsDbAccess.getAllTeams(requireContext())

        if (teams.size == 0) {
            val textView: TextView = binding.textTeams
            textView.setText(R.string.no_teams_yet)

            importTeams()

            textView.setText("")
        }
        else {
            val teamsList = binding.teamsList

            val arrayAdapter: ArrayAdapter<TeamDAO> =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, teams)
            teamsList.adapter = arrayAdapter
            teamsList.setOnItemClickListener { _, _, position, _ ->

                val team: TeamDAO = teams[position]
                showEditTeamPopup(team)
            }
        }
    }

    private fun importTeams() {

        Util.showOkButtonMessage(requireContext(), getString(R.string.import_teams))
        val allTeams = Util.getAllTeamsList()
        val dbAccessTeam = TeamsDBAccess()
        allTeams.forEach { team ->
            dbAccessTeam.insertTeam(requireContext(), team)
        }
        loadTeams()
    }

    private fun showEditTeamPopup(team: TeamDAO) {

        val teamDialog = EditTeamDialogFragment()
        teamDialog.callback = Runnable {
            run {
                loadTeams()
            }
        }

        teamDialog.team = team
        teamDialog.isCancelable = false
        teamDialog.show(requireActivity().supportFragmentManager, null)
    }

    private fun showAddTeamPopup() {

        val teamDialog = AddTeamDialogFragment()
        teamDialog.callback = Runnable {
            run {
                loadTeams()
            }
        }

        teamDialog.isCancelable = false
        teamDialog.show(requireActivity().supportFragmentManager, null)
    }
}