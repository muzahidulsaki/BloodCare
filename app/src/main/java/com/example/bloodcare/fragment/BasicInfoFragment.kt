import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.bloodcare.R

class BasicInfoFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // XML লেআউট লোড করা
        val view = inflater.inflate(R.layout.fragment_basic_info, container, false)

        // উদাহরণস্বরূপ একটি ভিউ খুঁজে বের করা

        return view
    }
}