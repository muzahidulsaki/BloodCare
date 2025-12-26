import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.bloodcare.ProfileViewModel
import com.example.bloodcare.R
import com.example.bloodcare.fragment.UploadImageFragment
import java.util.Calendar

class BasicInfoFragment : Fragment() {

    private lateinit var etDob: TextView
    private lateinit var tvAge: TextView
    private lateinit var ddGender: TextView
    private lateinit var ddDonate: TextView
    private lateinit var etAbout: TextView
    private lateinit var btnNext: AppCompatButton

    // ✅ Shared ViewModel
    private lateinit var viewModel: ProfileViewModel

    private var calculatedAge = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val view = inflater.inflate(R.layout.fragment_basic_info, container, false)

        // ✅ ViewModel init (Activity scope)
        viewModel = ViewModelProvider(requireActivity())[ProfileViewModel::class.java]

        // View init
        etDob = view.findViewById(R.id.etDob)
        tvAge = view.findViewById(R.id.tvAge)
        ddGender = view.findViewById(R.id.ddGender)
        ddDonate = view.findViewById(R.id.ddDonate)
        etAbout = view.findViewById<TextView>(R.id.etAbout)
        btnNext = view.findViewById(R.id.btnNextFragment)

        btnNext.isEnabled = false

        // Date Picker
        etDob.setOnClickListener {
            val calendar = Calendar.getInstance()

            val currentYear = calendar.get(Calendar.YEAR)
            val currentMonth = calendar.get(Calendar.MONTH)
            val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

            val dialog = DatePickerDialog(
                requireContext(),
                { _, year, month, day ->

                    etDob.text = "$day/${month + 1}/$year"

                    calculatedAge = currentYear - year
                    if (currentMonth < month ||
                        (currentMonth == month && currentDay < day)
                    ) {
                        calculatedAge--
                    }

                    tvAge.text = "Your age - $calculatedAge"
                    checkInputs()
                },
                currentYear, currentMonth, currentDay
            )

            dialog.datePicker.maxDate = System.currentTimeMillis()
            dialog.show()
        }

        // Gender Dropdown
        ddGender.setOnClickListener {
            val popup = PopupMenu(requireContext(), ddGender)
            popup.menu.add("Female")
            popup.menu.add("Male")

            popup.setOnMenuItemClickListener { item ->
                ddGender.text = item.title
                checkInputs()
                true
            }
            popup.show()
        }

        // Donate Dropdown
        ddDonate.setOnClickListener {
            val popup = PopupMenu(requireContext(), ddDonate)
            popup.menu.add("Yes")
            popup.menu.add("No")

            popup.setOnMenuItemClickListener { item ->
                ddDonate.text = item.title
                checkInputs()
                true
            }
            popup.show()
        }

        // ✅ NEXT BUTTON
        btnNext.setOnClickListener {

            // 🔥 STEP-3: Save Fragment data → ViewModel
            viewModel.dob = etDob.text.toString()
            viewModel.age = calculatedAge.toString()
            viewModel.gender = ddGender.text.toString()
            viewModel.donate = ddDonate.text.toString()
            viewModel.about = etAbout.text.toString()


            // Next Fragment
            parentFragmentManager.beginTransaction()
                .replace(android.R.id.content, UploadImageFragment())
                .addToBackStack(null)
                .commit()
        }

        return view
    }

    private fun checkInputs() {
        val dob = etDob.text.toString().trim()
        val gender = ddGender.text.toString().trim()
        val donate = ddDonate.text.toString().trim()

        btnNext.isEnabled =
            dob.isNotEmpty() &&
                    gender.isNotEmpty() &&
                    donate.isNotEmpty() &&
                    calculatedAge >= 18
    }
}
