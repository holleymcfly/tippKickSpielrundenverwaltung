package de.herrmann.tippkick.spielrundenverwaltung.ui.play

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import de.herrmann.tippkick.spielrundenverwaltung.R
import de.herrmann.tippkick.spielrundenverwaltung.model.PairingDAO
import de.herrmann.tippkick.spielrundenverwaltung.persistence.PairingDBAccess
import de.herrmann.tippkick.spielrundenverwaltung.util.Util
import android.view.inputmethod.EditorInfo


class EditPairingDialogFragment(private val currentPairing: PairingDAO, private val isDisabled: Boolean) : DialogFragment() {

    lateinit var callback: Runnable

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {

            val layoutInflater =
                requireContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            val view = layoutInflater.inflate(R.layout.edit_pairing, null)
            val dialog = AlertDialog.Builder(requireContext()).setView(view).create()

            val cancelButton = view.findViewById<ImageButton>(R.id.cancel)
            cancelButton?.setOnClickListener { dismiss() }

            val goalsHome = view.findViewById<EditText>(R.id.goals_home)
            if (currentPairing.hasHomeGoalsSet()) {
                goalsHome.setText(currentPairing.goalsHome.toString())
            }

            val goalsAway = view.findViewById<EditText>(R.id.goals_away)
            if (currentPairing.hasAwayGoalsSet()) {
                goalsAway.setText(currentPairing.goalsAway.toString())
            }

            val extraTime: CheckBox = view.findViewById(R.id.extra_time)
            if (currentPairing.extraTime) {
                extraTime.isChecked = true
            }

            val penalty: CheckBox = view.findViewById(R.id.penalty)
            if (currentPairing.penalty) {
                penalty.isChecked = true
            }
            penalty.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    extraTime.isChecked = true
                }
            }

            val saveButton = view.findViewById<ImageButton>(R.id.save)
            saveButton?.setOnClickListener {
                save(goalsHome, goalsAway, extraTime, penalty)
            }

            goalsAway.setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    save(goalsHome, goalsAway, extraTime, penalty)
                    return@setOnEditorActionListener true
                }
                false
            }

            val pairing = view.findViewById<TextView>(R.id.pairing)
            pairing.text = currentPairing.toStringShort()

            setViewEnabledDisabled(view)
            return dialog
        } ?: throw IllegalStateException("Activity cannot be null")
    }

    private fun setViewEnabledDisabled(view: View) {

        val goalsHome = view.findViewById<EditText>(R.id.goals_home)
        val goalsAway = view.findViewById<EditText>(R.id.goals_away)
        val extraTime = view.findViewById<CheckBox>(R.id.extra_time)
        val penalty = view.findViewById<CheckBox>(R.id.penalty)
        val save = view.findViewById<ImageButton>(R.id.save)

        goalsHome.isEnabled = !isDisabled
        goalsAway.isEnabled = !isDisabled
        extraTime.isEnabled = !isDisabled
        penalty.isEnabled = !isDisabled
        save.isEnabled = !isDisabled

        if (isDisabled) {
            save.setBackgroundColor(Color.parseColor("#C0C0C0"))
        }
    }

    private fun save(
        goalsHomeTextField: EditText, goalsAwayTextField: EditText, extraTime: CheckBox,
        penalty: CheckBox
    ) {

        val goalsHome: Int = getIntOrMinusOne(goalsHomeTextField)
        val goalsAway: Int = getIntOrMinusOne(goalsAwayTextField)

        if ( (goalsHome == -1 && goalsAway != -1) || (goalsHome != -1 && goalsAway == -1)) {
            Util.showOkButtonMessage(requireContext(), getString(R.string.enter_valid_result))
            return
        }

        if (goalsHome != -1 && goalsHome == goalsAway) {
            Util.showOkButtonMessage(requireContext(), getString(R.string.no_undecided_match))
            return
        }

        val pairingDBAccess = PairingDBAccess()
        pairingDBAccess.updatePairing(
            requireContext(), this.currentPairing.id,
            goalsHome, goalsAway,
            extraTime.isChecked, penalty.isChecked
        )
        dismiss()
        callback.run()
    }

    private fun getIntOrMinusOne(input: EditText): Int {

        return try {
            input.text.toString().toInt()
        }
        catch (e: Exception) {
            -1
        }
    }
}