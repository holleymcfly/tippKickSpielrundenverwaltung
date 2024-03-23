package de.herrmann.tippkick.spielrundenverwaltung.util

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.text.Html
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.persistence.TeamsDBAccess

class VersionInfoFragment : DialogFragment() {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater = requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.version_info, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val textField = view.findViewById<TextView>(R.id.info_text)
            textField.text = Html.fromHtml(VersionInfo.getCurrentVersionInfo(), 0)

            val okButton = view.findViewById<ImageButton>(R.id.ok)
            okButton.setOnClickListener { dismiss() }

            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }
}