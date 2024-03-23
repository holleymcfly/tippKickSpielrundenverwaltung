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

class AddTeamDialogFragment : DialogFragment() {

    lateinit var callback: Runnable

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.add_team, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener { dismiss() }

            val nameField = view.findViewById<EditText>(R.id.eingabe)
            val saveButton = view.findViewById<ImageButton>(R.id.add)
            saveButton?.setOnClickListener {

                val teamName = nameField.text.toString()
                if (teamName.isEmpty()) {
                    Toast.makeText(context, R.string.enter_team_please, Toast.LENGTH_LONG).show()
                }
                else {
                    val teamsDbAccess =
                        TeamsDBAccess()
                    teamsDbAccess.insertTeam(requireContext(), teamName)
                    Toast.makeText(context, R.string.team_saved,
                        Toast.LENGTH_LONG).show()
                    dismiss()
                    callback.run()
                }
            }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}