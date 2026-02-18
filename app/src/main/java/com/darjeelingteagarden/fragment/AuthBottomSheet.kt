package com.darjeelingteagarden.fragment

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.RadioButton
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.coroutineScope
import com.darjeelingteagarden.activity.AboutActivity
import com.darjeelingteagarden.databinding.DialogAuthBinding
import com.darjeelingteagarden.model.AuthState
import com.darjeelingteagarden.viewModel.AuthViewModel
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AuthBottomSheet : BottomSheetDialogFragment() {

    private var _binding: DialogAuthBinding? = null
    private val binding get() = _binding!!

    private lateinit var smsConsentLauncher: ActivityResultLauncher<Intent>

    // Using activityViewModels ensures the data survives even if the dialog is dismissed
    private val viewModel: AuthViewModel by activityViewModels()

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            val rawPhoneNumber = Identity.getSignInClient(requireActivity())
                .getPhoneNumberFromIntent(data)

            // Strip everything but digits, then grab the last 10
            val cleanNumber = rawPhoneNumber.filter { it.isDigit() }.takeLast(10)
            binding.textInputEditTextPhoneNumber.setText(cleanNumber)
        }
        else{
            showKeyboardWithDelay()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = DialogAuthBinding.inflate(inflater, container, false)

        val bottomSheet = dialog?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
        bottomSheet?.let {
            val behavior = BottomSheetBehavior.from(it)
            behavior.isHideable = false // Prevents swiping down to hide
            behavior.state = BottomSheetBehavior.STATE_EXPANDED // Keep it open
        }

        smsConsentLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            if (it.resultCode == Activity.RESULT_OK && it.data != null){
                val message = it.data!!.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                extractOtpFromMessage(message)
            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupAnimations()
        setupClickListeners()
        observeViewModel()

        lifecycle.coroutineScope.launch {
            delay(3000)
            requestPhoneHint()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
//        return super.onCreateDialog(savedInstanceState)

        val dialog = super.onCreateDialog(savedInstanceState)

        dialog.setCanceledOnTouchOutside(false)

        isCancelable = false

        return dialog
    }

    private fun setupAnimations() {
        binding.authViewFlipper.setInAnimation(context, android.R.anim.slide_in_left)
        binding.authViewFlipper.setOutAnimation(context, android.R.anim.slide_out_right)
    }

    private fun setupClickListeners() {
        // Step 1: Check if phone exists
        binding.btnCheckPhone.setOnClickListener {
            val phone = binding.textInputEditTextPhoneNumber.text.toString()
            if (phone.isNotEmpty() && phone.length == 10) {
                viewModel.discoverAuth(requireContext().applicationContext, phone)
                binding.tvMsgLoginWithOtp.text = getString(com.darjeelingteagarden.R.string.enter_4_digit_code_sent_to_phone, phone)
                binding.tvMsgVerifyOtp.text = getString(com.darjeelingteagarden.R.string.enter_4_digit_code_sent_to_phone, phone)
            } else {
                binding.textInputLayoutPhoneNumber.error = "Invalid phone number"
            }
        }

        // Step 2: Login with OTP
        binding.btnLogin.setOnClickListener {
            val phone = binding.textInputEditTextPhoneNumber.text.toString()
            val otp = binding.textInputEditTextOTP.text.toString()
            viewModel.verifyOtpAndLogin(requireContext().applicationContext, phone, otp)
        }

        // Step 3: Register new user
        binding.btnVerifyOTP.setOnClickListener {
            val phone = binding.textInputEditTextPhoneNumber.text.toString()
            val otp = binding.textInputEditTextOtpRegister.text.toString()
            viewModel.verifyOtp(requireContext().applicationContext, phone, otp)
        }

        binding.btnCompleteRegistration.setOnClickListener {
            val name = binding.textInputEditTextName.text.toString().trim()
            val phone = binding.textInputEditTextPhoneNumber.text.toString().trim()
            val otp = binding.textInputEditTextOtpRegister.text.toString().trim()
            val selectedRadioId = binding.radioGroupBusinessRevenue.checkedRadioButtonId

            if (selectedRadioId == -1){
                Toast.makeText(context, "Please select your business revenue", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            val selectedRadio = binding.root.findViewById<RadioButton>(selectedRadioId)

            val businessRevenue = selectedRadio.tag.toString()

            Log.i("business revenue", businessRevenue)

            if (name.isBlank() || name.length < 2){
                binding.textInputLayoutName.error = "Please enter your name"
                return@setOnClickListener
            }
            else{
                binding.textInputLayoutName.error = null
            }

            viewModel.registerUserWithOtp(requireContext().applicationContext, name, businessRevenue, phone, otp)
        }

        binding.btnChangePhoneNumber.setOnClickListener {
            binding.tvTitle.text = getString(com.darjeelingteagarden.R.string.welcome)
            binding.authViewFlipper.displayedChild = 0
            binding.btnChangePhoneNumber.visibility = View.GONE
        }

        binding.txtPrivacyPolicyInfo.setOnClickListener {
            binding.txtPrivacyPolicyInfo.setOnClickListener {
                val intent = Intent(activity as Context, AboutActivity::class.java)
                startActivity(intent)
            }
        }
    }

    private fun observeViewModel() {
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Idle -> {
                    binding.tvTitle.text = getString(com.darjeelingteagarden.R.string.welcome)
                    binding.authViewFlipper.displayedChild = 0
                    binding.btnChangePhoneNumber.visibility = View.GONE
                }
                is AuthState.LoginWithOtp -> {
                    startSmsUserConsent()
                    binding.tvTitle.text =
                        getString(com.darjeelingteagarden.R.string.enter_otp_to_login)
                    binding.authViewFlipper.displayedChild = 1
                    binding.btnChangePhoneNumber.visibility = View.VISIBLE
                    binding.textInputEditTextOTP.requestFocus()
                }
                is AuthState.VerifyOtp -> {
                    startSmsUserConsent()
                    binding.tvTitle.text =
                        getString(com.darjeelingteagarden.R.string.enter_otp_to_sign_up)
                    binding.authViewFlipper.displayedChild = 2
                    binding.btnChangePhoneNumber.visibility = View.VISIBLE
                    binding.textInputEditTextOtpRegister.requestFocus()
                }
                is AuthState.NewUser -> {
                    binding.tvTitle.text = getString(com.darjeelingteagarden.R.string.last_step)
                    binding.authViewFlipper.displayedChild = 3
                    binding.btnChangePhoneNumber.visibility = View.GONE
                    binding.textInputEditTextName.requestFocus()
                }
                is AuthState.Success -> {
                    // Notify the activity and close
                    parentFragmentManager.setFragmentResult("auth_key", bundleOf("isLoggedIn" to true))
                    binding.authViewFlipper.displayedChild = 5
                    binding.tvTitle.text = getString(com.darjeelingteagarden.R.string.success)
                    //close keyboard
                    val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
                    lifecycle.coroutineScope.launch {
                        delay(3000)
                        dismiss()
                    }
                }
                is AuthState.Loading -> {
                    binding.authViewFlipper.displayedChild = 4
                }
                is AuthState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                }
                else -> {}
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val TAG = "AuthBottomSheet"
        fun newInstance() = AuthBottomSheet()
    }

    private fun requestPhoneHint() {
        val request = GetPhoneNumberHintIntentRequest.builder().build()

        Identity.getSignInClient(requireActivity())
            .getPhoneNumberHintIntent(request)
            .addOnSuccessListener { result ->
                try {
                    // This launches the Google overlay
                    startIntentSenderForResult(result.intentSender, 1001, null, 0, 0, 0, null)
                } catch (e: Exception) {
                    Log.e("Auth", "Launching hint failed", e)
                }
            }
    }

    private fun showKeyboardWithDelay() {
        binding.textInputEditTextPhoneNumber.requestFocus()
        // A delay of 100ms is usually the "sweet spot" for UI transitions
        binding.textInputEditTextPhoneNumber.postDelayed({
            val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(binding.textInputEditTextPhoneNumber, InputMethodManager.SHOW_IMPLICIT)
        }, 100)
    }

    private fun extractOtpFromMessage(message: String?) {
        message?.let {
            // This regex will find any sequence of 6 digits.
            val otpPattern = "\\d{6}".toRegex()
            val otp = otpPattern.find(it)?.value
            otp?.let {
                // Assuming you have separate OTP fields for login and registration
                if (binding.authViewFlipper.displayedChild == 1) { // Login OTP
                    binding.textInputEditTextOTP.setText(otp)
                } else if (binding.authViewFlipper.displayedChild == 2) { // Register OTP
                    binding.textInputEditTextOtpRegister.setText(otp)
                }
            }
        }
    }

    private fun startSmsUserConsent() {
        SmsRetriever.getClient(requireActivity()).startSmsUserConsent(null)
            .addOnSuccessListener {
                Log.d(TAG, "SMS User Consent API started.")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error starting SMS User Consent API.", e)
            }
    }
}