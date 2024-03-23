package de.herrmann.tippkick.spielrundenverwaltung.ui.competition

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.fragment.app.Fragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.databinding.FragmentCompetitionBinding
import de.herrmann.tippkick.spielrundenverwaltung.model.CompetitionDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.CompetitionsDBAccess

class CompetitionFragment : Fragment() {

    private var _binding: FragmentCompetitionBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCompetitionBinding.inflate(inflater, container, false)

        val root: View = binding.root

        loadCompetitions()

        val addButton = binding.add
        addButton.setOnClickListener {
            showAddEditCompetitionPopup(null)
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadCompetitions() {

        val competitionsDbAccess = CompetitionsDBAccess()
        val competitions = competitionsDbAccess.getAllCompetitions(requireContext())

        val textView: TextView = binding.textCompetitions
        if (competitions.size == 0) {
            textView.setText(R.string.no_competitions_yet)
        }
        else {
            textView.text = ""
            val competitionsList = binding.competitionsList

            val arrayAdapter: ArrayAdapter<CompetitionDAO> =
                ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, competitions)
            competitionsList.adapter = arrayAdapter
            competitionsList.setOnItemClickListener { _, _, position, _ ->

                val competition: CompetitionDAO = competitions[position]
                showAddEditCompetitionPopup(competition)
            }
        }
    }

    private fun showAddEditCompetitionPopup(competition: CompetitionDAO?) {

        val competitionDialog = AddEditCompetitionDialogFragment()
        competitionDialog.callback = Runnable {
            run {
                loadCompetitions()
            }
        }

        if (competition != null) {
            competitionDialog.competition = competition
        }

        competitionDialog.isCancelable = false
        competitionDialog.show(requireActivity().supportFragmentManager, null)
    }
}